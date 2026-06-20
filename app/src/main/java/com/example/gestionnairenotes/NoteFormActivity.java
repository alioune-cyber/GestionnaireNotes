package com.example.gestionnairenotes;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;


// Gère l'écran de création et de modification d'une note
public class NoteFormActivity extends AppCompatActivity {

    // Accès à la base de données pour enregistrer ou modifier la note
    private DatabaseHelper dbHelper;

    // Champs de saisie du titre et du contenu
    private EditText edtTitre, edtContenu;

    // Zone visuelle représentant la note, dont le fond change selon la couleur choisie
    private LinearLayout zoneNote;

    // Bouton qui déclenche l'enregistrement, affiché comme Créer ou Modifier selon le mode
    private Button btnEnregistrer;

    // Identifiant de la note en cours de modification, -1 si on crée une nouvelle note
    private long noteId = -1;

    // Couleur actuellement sélectionnée pour la note
    private String couleurChoisie = "#219653";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note_form);
        hideSystemBars();

        dbHelper = new DatabaseHelper(this);

        edtTitre = findViewById(R.id.edtTitre);
        edtContenu = findViewById(R.id.edtContenu);
        zoneNote = findViewById(R.id.zoneNote);
        btnEnregistrer = findViewById(R.id.btnEnregistrer);

        // Détermine si l'écran est ouvert pour modifier une note existante
        Bundle extras = getIntent().getExtras();
        boolean modeModification = extras != null && extras.containsKey("id");

        if (extras != null) {
            couleurChoisie = extras.getString("couleur", "#219653");
        }

        // Pré-remplit le formulaire si on modifie une note déjà existante
        if (modeModification) {
            noteId = extras.getLong("id");
            edtTitre.setText(extras.getString("titre"));
            edtContenu.setText(extras.getString("contenu"));
            btnEnregistrer.setText("Modifier");
        } else {
            btnEnregistrer.setText("Créer");
        }

        appliquerCouleur(couleurChoisie);

        findViewById(R.id.fColVert).setOnClickListener(v -> appliquerCouleur("#219653"));
        findViewById(R.id.fColRouge).setOnClickListener(v -> appliquerCouleur("#EB5757"));
        findViewById(R.id.fColBleu).setOnClickListener(v -> appliquerCouleur("#2F80ED"));
        findViewById(R.id.fColJaune).setOnClickListener(v -> appliquerCouleur("#F2C94C"));
        findViewById(R.id.fColOrange).setOnClickListener(v -> appliquerCouleur("#F2994A"));
        findViewById(R.id.fColGris).setOnClickListener(v -> appliquerCouleur("#828282"));

        btnEnregistrer.setOnClickListener(v -> enregistrer(modeModification));
    }

    // Change la couleur de fond de la zone de note et mémorise la couleur choisie
    private void appliquerCouleur(String couleur) {
        couleurChoisie = couleur;
        zoneNote.setBackgroundColor(Color.parseColor(couleur));
    }

    // Vérifie la validité des champs puis crée ou met à jour la note en base de données
    private void enregistrer(boolean modeModification) {
        String titre = edtTitre.getText().toString().trim();
        String contenu = edtContenu.getText().toString().trim();

        if (titre.isEmpty() && contenu.isEmpty()) {
            Toast.makeText(this, "Une note vide ne peut pas être enregistrée", Toast.LENGTH_SHORT).show();
            return;
        }

        if (modeModification) {
            dbHelper.modifierNote(noteId, titre, contenu, couleurChoisie);
        } else {
            dbHelper.ajouterNote(titre, contenu, couleurChoisie);
        }
        finish();
    }

    private void hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);

        WindowInsetsControllerCompat controller =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());

        if (controller != null) {
            controller.hide(WindowInsetsCompat.Type.systemBars());
            controller.setSystemBarsBehavior(
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            );
        }
    }
}