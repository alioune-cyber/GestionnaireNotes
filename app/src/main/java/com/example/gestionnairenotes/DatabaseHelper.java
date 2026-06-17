package com.example.gestionnairenotes;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// Gère la création de la base de données et toutes les opérations de lecture et d'écriture sur les notes
public class DatabaseHelper extends SQLiteOpenHelper {

    // Nom du fichier de base de données stocké sur l'appareil
    private static final String DB_NAME = "notes.db";

    // Numéro de version de la base de données
    private static final int DB_VERSION = 1;

    // Nom de la table contenant les notes
    public static final String TABLE_NOTES = "notes";

    // Nom des colonnes de la table notes
    public static final String COL_ID = "id";
    public static final String COL_TITRE = "titre";
    public static final String COL_CONTENU = "contenu";
    public static final String COL_COULEUR = "couleur";
    public static final String COL_FAVORI = "favori";
    public static final String COL_DATE = "date_creation";

    public DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    // Crée la table notes lors de la première installation de l'application
    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABLE_NOTES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TITRE + " TEXT, " +
                COL_CONTENU + " TEXT, " +
                COL_COULEUR + " TEXT, " +
                COL_FAVORI + " INTEGER, " +
                COL_DATE + " TEXT)";
        db.execSQL(sql);
    }

    // Recrée la table en cas de changement de version de la base de données
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTES);
        onCreate(db);
    }

    // Insère une nouvelle note dans la base de données et retourne son identifiant
    public long ajouterNote(String titre, String contenu, String couleur) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITRE, titre);
        values.put(COL_CONTENU, contenu);
        values.put(COL_COULEUR, couleur);
        values.put(COL_FAVORI, 0);
        values.put(COL_DATE, dateActuelle());
        long id = db.insert(TABLE_NOTES, null, values);
        db.close();
        return id;
    }

    // Met à jour le titre, le contenu et la couleur d'une note existante
    public void modifierNote(long id, String titre, String contenu, String couleur) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TITRE, titre);
        values.put(COL_CONTENU, contenu);
        values.put(COL_COULEUR, couleur);
        db.update(TABLE_NOTES, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Met à jour le statut favori d'une note
    public void basculerFavori(long id, boolean favori) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_FAVORI, favori ? 1 : 0);
        db.update(TABLE_NOTES, values, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Supprime définitivement une note de la base de données
    public void supprimerNote(long id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_NOTES, COL_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Retourne la date du jour formatée pour l'affichage
    private String dateActuelle() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy", Locale.FRENCH);
        return sdf.format(new Date());
    }

    // Récupère la liste des notes selon une recherche, un filtre favori et un ordre de tri
    public List<Note> recupererNotes(String recherche, boolean favorisSeulement, String tri) {
        List<Note> liste = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();

        // Construction dynamique de la condition de filtrage
        StringBuilder where = new StringBuilder();
        List<String> args = new ArrayList<>();

        if (recherche != null && !recherche.trim().isEmpty()) {
            where.append(COL_TITRE).append(" LIKE ?");
            args.add("%" + recherche.trim() + "%");
        }
        if (favorisSeulement) {
            if (where.length() > 0) where.append(" AND ");
            where.append(COL_FAVORI).append(" = 1");
        }

        // Choix de la colonne et de l'ordre de tri selon le paramètre reçu
        String orderBy;
        switch (tri) {
            case "titre_az":
                orderBy = COL_TITRE + " ASC";
                break;
            case "titre_za":
                orderBy = COL_TITRE + " DESC";
                break;
            case "date_anciennes":
                orderBy = COL_ID + " ASC";
                break;
            default:
                orderBy = COL_ID + " DESC";
                break;
        }

        Cursor cursor = db.query(TABLE_NOTES, null,
                where.length() > 0 ? where.toString() : null,
                args.isEmpty() ? null : args.toArray(new String[0]),
                null, null, orderBy);

        // Parcours du résultat de la requête pour construire la liste de notes
        if (cursor.moveToFirst()) {
            do {
                liste.add(new Note(
                        cursor.getLong(cursor.getColumnIndexOrThrow(COL_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_TITRE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_CONTENU)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_COULEUR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COL_FAVORI)) == 1,
                        cursor.getString(cursor.getColumnIndexOrThrow(COL_DATE))
                ));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return liste;
    }
}