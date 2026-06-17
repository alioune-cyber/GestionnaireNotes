package com.example.gestionnairenotes;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import java.util.List;

// Gère l'écran principal affichant la liste des notes et toutes les actions associées
public class MainActivity extends AppCompatActivity {

    // Accès à la base de données pour récupérer, modifier et supprimer les notes
    private DatabaseHelper dbHelper;

    // Liste affichant les notes à l'écran
    private ListView listNotes;

    // Champ de saisie permettant de rechercher une note par son titre
    private EditText edtRecherche;

    // Boutons d'action de l'écran principal
    private Button btnFavoris, btnAjouter, btnTri, btnModeSombre;

    // Palette de couleurs affichée lors de la création d'une nouvelle note
    private LinearLayout palette;

    // Texte affiché quand aucune note ne correspond, et texte affichant le nombre de notes
    private TextView txtVide, txtCompteur;

    // Indique si seules les notes favorites doivent être affichées
    private boolean filtreFavoris = false;

    // Mode de tri actuellement appliqué à la liste des notes
    private String triActuel = "date_recentes";

    // Adaptateur reliant la liste de notes à l'affichage
    private NoteAdapter adapter;

    // Préférences utilisateur stockant notamment le choix du mode sombre
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Applique le mode sombre ou clair avant la construction de l'écran
        prefs = getSharedPreferences("prefs_notes", MODE_PRIVATE);
        boolean modeSombre = prefs.getBoolean("mode_sombre", false);
        AppCompatDelegate.setDefaultNightMode(
                modeSombre ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbHelper = new DatabaseHelper(this);

        listNotes = findViewById(R.id.listNotes);
        edtRecherche = findViewById(R.id.edtRecherche);
        btnFavoris = findViewById(R.id.btnFavoris);
        btnAjouter = findViewById(R.id.btnAjouter);
        btnTri = findViewById(R.id.btnTri);
        btnModeSombre = findViewById(R.id.btnModeSombre);
        palette = findViewById(R.id.palette);
        txtVide = findViewById(R.id.txtVide);
        txtCompteur = findViewById(R.id.txtCompteur);

        btnModeSombre.setText(modeSombre ? "☀️" : "🌙");

        // Affiche ou cache la palette de couleurs au clic sur le bouton d'ajout
        btnAjouter.setOnClickListener(v -> {
            palette.setVisibility(palette.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
        });

        // Ouvre l'écran de création avec la couleur sélectionnée dans la palette
        findViewById(R.id.colVert).setOnClickListener(v -> ouvrirCreation("#219653"));
        findViewById(R.id.colRouge).setOnClickListener(v -> ouvrirCreation("#EB5757"));
        findViewById(R.id.colBleu).setOnClickListener(v -> ouvrirCreation("#2F80ED"));
        findViewById(R.id.colJaune).setOnClickListener(v -> ouvrirCreation("#F2C94C"));
        findViewById(R.id.colOrange).setOnClickListener(v -> ouvrirCreation("#F2994A"));
        findViewById(R.id.colGris).setOnClickListener(v -> ouvrirCreation("#828282"));

        // Active ou désactive le filtre des notes favorites
        btnFavoris.setOnClickListener(v -> {
            filtreFavoris = !filtreFavoris;
            btnFavoris.setText(filtreFavoris ? "Tous" : "Favoris");
            rafraichirListe();
        });

        // Bascule entre le mode sombre et le mode clair, et sauvegarde le choix
        btnModeSombre.setOnClickListener(v -> {
            boolean actuel = prefs.getBoolean("mode_sombre", false);
            boolean nouveau = !actuel;
            prefs.edit().putBoolean("mode_sombre", nouveau).apply();
            AppCompatDelegate.setDefaultNightMode(
                    nouveau ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            recreate();
        });

        btnTri.setOnClickListener(v -> afficherMenuTri());

        // Filtre la liste des notes à chaque modification du texte recherché
        edtRecherche.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) { rafraichirListe(); }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    // Affiche une fenêtre proposant les différents modes de tri disponibles
    private void afficherMenuTri() {
        String[] options = {"Date (récentes)", "Date (anciennes)", "Titre (A-Z)", "Titre (Z-A)"};
        String[] valeurs = {"date_recentes", "date_anciennes", "titre_az", "titre_za"};

        new AlertDialog.Builder(this)
                .setTitle("Trier les notes")
                .setItems(options, (dialog, index) -> {
                    triActuel = valeurs[index];
                    rafraichirListe();
                })
                .show();
    }

    // Ouvre l'écran de création de note avec la couleur choisie en paramètre
    private void ouvrirCreation(String couleur) {
        palette.setVisibility(View.GONE);
        Intent intent = new Intent(this, NoteFormActivity.class);
        intent.putExtra("couleur", couleur);
        startActivity(intent);
    }

    // Recharge la liste des notes chaque fois que l'écran principal redevient visible
    @Override
    protected void onResume() {
        super.onResume();
        rafraichirListe();
    }

    // Récupère les notes selon la recherche, le filtre et le tri actuels, puis met à jour l'affichage
    private void rafraichirListe() {
        String recherche = edtRecherche.getText().toString();
        List<Note> notes = dbHelper.recupererNotes(recherche, filtreFavoris, triActuel);

        txtVide.setVisibility(notes.isEmpty() ? View.VISIBLE : View.GONE);
        txtCompteur.setText(notes.size() + " note(s)");

        adapter = new NoteAdapter(this, notes);
        listNotes.setAdapter(adapter);

        // Variables mémorisant le dernier clic pour distinguer un clic simple d'un double clic
        final long[] dernierClic = {0};
        final int[] dernierePosition = {-1};

        // Un clic simple ouvre la note, deux clics rapprochés basculent son statut favori
        listNotes.setOnItemClickListener((parent, view, position, id) -> {
            long maintenant = System.currentTimeMillis();
            if (dernierePosition[0] == position && maintenant - dernierClic[0] < 300) {
                Note note = notes.get(position);
                dbHelper.basculerFavori(note.getId(), !note.isFavori());
                rafraichirListe();
            } else {
                dernierClic[0] = maintenant;
                dernierePosition[0] = position;
                view.postDelayed(() -> {
                    if (System.currentTimeMillis() - dernierClic[0] >= 300) {
                        Note note = notes.get(position);
                        Intent intent = new Intent(this, NoteFormActivity.class);
                        intent.putExtra("id", note.getId());
                        intent.putExtra("titre", note.getTitre());
                        intent.putExtra("contenu", note.getContenu());
                        intent.putExtra("couleur", note.getCouleur());
                        startActivity(intent);
                    }
                }, 300);
            }
        });

        // Un appui long demande confirmation avant de supprimer la note
        listNotes.setOnItemLongClickListener((parent, view, position, id) -> {
            Note note = notes.get(position);
            new AlertDialog.Builder(this)
                    .setTitle("Supprimer la note")
                    .setMessage("Voulez-vous vraiment supprimer \"" + note.getTitre() + "\" ?")
                    .setPositiveButton("Supprimer", (dialog, which) -> {
                        dbHelper.supprimerNote(note.getId());
                        rafraichirListe();
                    })
                    .setNegativeButton("Annuler", null)
                    .show();
            return true;
        });
    }
}