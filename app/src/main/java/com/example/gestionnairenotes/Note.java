package com.example.gestionnairenotes;

// Représente une note avec toutes ses informations
public class Note {

    // Identifiant unique de la note en base de données
    private long id;

    // Titre de la note
    private String titre;

    // Contenu texte de la note
    private String contenu;

    // Couleur de la note au format hexadécimal, ex "#219653"
    private String couleur;

    // Indique si la note est marquée comme favorite
    private boolean favori;

    // Date de création de la note
    private String date;

    public Note(long id, String titre, String contenu, String couleur, boolean favori, String date) {
        this.id = id;
        this.titre = titre;
        this.contenu = contenu;
        this.couleur = couleur;
        this.favori = favori;
        this.date = date;
    }

    public long getId() { return id; }
    public String getTitre() { return titre; }
    public String getContenu() { return contenu; }
    public String getCouleur() { return couleur; }
    public boolean isFavori() { return favori; }
    public String getDate() { return date; }

    public void setTitre(String titre) { this.titre = titre; }
    public void setContenu(String contenu) { this.contenu = contenu; }
    public void setCouleur(String couleur) { this.couleur = couleur; }
    public void setFavori(boolean favori) { this.favori = favori; }
}