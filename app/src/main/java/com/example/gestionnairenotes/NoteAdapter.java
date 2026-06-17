package com.example.gestionnairenotes;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

// Adapte la liste de notes pour qu'elle s'affiche correctement dans la ListView
public class NoteAdapter extends ArrayAdapter<Note> {

    public NoteAdapter(Context context, List<Note> notes) {
        super(context, 0, notes);
    }

    // Construit ou met à jour l'affichage d'une ligne de la liste pour une note donnée
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_note, parent, false);
        }
        Note note = getItem(position);

        View card = convertView.findViewById(R.id.cardNote);
        TextView txtTitre = convertView.findViewById(R.id.txtTitre);
        TextView txtDate = convertView.findViewById(R.id.txtDate);
        TextView iconFavori = convertView.findViewById(R.id.iconFavori);

        txtTitre.setText(note.getTitre());
        txtDate.setText(note.getDate());

        // Applique la couleur de fond de la note avec des coins arrondis
        try {
            String couleur = note.getCouleur();
            if (!couleur.startsWith("#")) {
                couleur = "#" + couleur;
            }

            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(20f);
            drawable.setColor(Color.parseColor(couleur));
            drawable.setStroke(1, Color.parseColor("#000000"));

            card.setBackground(drawable);

        } catch (Exception e) {
            // Couleur de secours si la couleur enregistrée est invalide
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(16f);
            drawable.setColor(Color.parseColor("#219653"));
            drawable.setStroke(1, Color.parseColor("#000000"));
            card.setBackground(drawable);
        }

        // Adapte la couleur du texte selon que le fond de la note soit clair ou sombre
        try {
            String couleur = note.getCouleur();
            if (!couleur.startsWith("#")) {
                couleur = "#" + couleur;
            }
            int bgColor = Color.parseColor(couleur);
            boolean fondSombre = estFondSombre(bgColor);

            txtTitre.setTextColor(fondSombre ? Color.WHITE : Color.BLACK);
            txtDate.setTextColor(fondSombre ? 0xCCFFFFFF : 0xCC000000);
        } catch (Exception e) {
            txtTitre.setTextColor(Color.WHITE);
            txtDate.setTextColor(0xCCFFFFFF);
        }

        // Affiche ou cache l'icône favori selon le statut de la note
        if (note.isFavori()) {
            iconFavori.setVisibility(View.VISIBLE);
            iconFavori.setTextColor(Color.parseColor("#FFD700"));
        } else {
            iconFavori.setVisibility(View.GONE);
        }

        return convertView;
    }

    // Calcule la luminosité d'une couleur pour déterminer si elle est considérée comme sombre
    private boolean estFondSombre(int color) {
        double luminance = (0.299 * Color.red(color) +
                0.587 * Color.green(color) +
                0.114 * Color.blue(color)) / 255;
        return luminance < 0.5;
    }
}