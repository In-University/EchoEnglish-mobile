package com.example.echoenglish_mobile.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;


import com.example.echoenglish_mobile.model.FolderModal;
import com.example.echoenglish_mobile.model.WordModel;

import java.util.ArrayList;

public class DBHandler extends SQLiteOpenHelper {

    private static final String DB_NAME = "flashcard";
    private static final int DB_VERSION = 1;

    private static final String FOLDERS_TABLE_NAME = "folders";
    private static final String WORDS_TABLE_NAME = "words";
    private static final String ID_COL = "id";
    private static final String FOLDER_ID_COL = "folder_id";
    private static final String FOLDER_NAME_COL = "folder_name";
    private static final String WORD_COL = "word";
    private static final String DESCRIPTION_COL = "description";

    public DBHandler(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createFoldersTable(db);
        createWordsTable(db);
    }

    private void createFoldersTable(SQLiteDatabase db) {
        String foldersQuery = "CREATE TABLE " + FOLDERS_TABLE_NAME + "(" +
                FOLDER_ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                FOLDER_NAME_COL + " TEXT)";
        db.execSQL(foldersQuery);
    }

    private void createWordsTable(SQLiteDatabase db) {
        String wordsQuery = "CREATE TABLE " + WORDS_TABLE_NAME + "(" +
                ID_COL + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WORD_COL + " TEXT," +
                DESCRIPTION_COL + " TEXT," +
                FOLDER_ID_COL + " INTEGER," +
                "FOREIGN KEY(" + FOLDER_ID_COL + ") REFERENCES " + FOLDERS_TABLE_NAME + "(" + FOLDER_ID_COL + "))";
        db.execSQL(wordsQuery);
    }

    public void addFolder(String folderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOLDER_NAME_COL, folderName);
        db.insert(FOLDERS_TABLE_NAME, null, cv);
        db.close();
    }

    public ArrayList<FolderModal> getFolderNames() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(" SELECT * FROM " + FOLDERS_TABLE_NAME, null);

        ArrayList<FolderModal> folderArrayList = new ArrayList<>();
        if (cursor.moveToFirst()) {
            do {
                int folderId = cursor.getInt(cursor.getColumnIndexOrThrow(FOLDER_ID_COL));
                String folderName = cursor.getString(cursor.getColumnIndexOrThrow(FOLDER_NAME_COL));
                folderArrayList.add(new FolderModal(folderId, folderName));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return folderArrayList;
    }

    public ArrayList<WordModel> getWordsByFolderId(int folderId) {
        ArrayList<WordModel> wordList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String[] projection = {ID_COL, WORD_COL, DESCRIPTION_COL};
        String selection = FOLDER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};

        Cursor cursor = db.query(WORDS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int wordId = cursor.getInt(cursor.getColumnIndexOrThrow(ID_COL));
                String word = cursor.getString(cursor.getColumnIndexOrThrow(WORD_COL));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(DESCRIPTION_COL));
                wordList.add(new WordModel(folderId, wordId, word, description));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();

        return wordList;
    }

    public void addWord(int folderId, String word, String description) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FOLDER_ID_COL, folderId);
        cv.put(WORD_COL, word);
        cv.put(DESCRIPTION_COL, description);
        db.insert(WORDS_TABLE_NAME, null, cv);
        db.close();
    }

    public int getTotalWordsInAFolder(int folderId) {
        SQLiteDatabase db = this.getReadableDatabase();
        int totalWords = 0;
        String[] projection = {ID_COL};
        String selection = FOLDER_ID_COL + " = ?";
        String[] selectionArgs = {String.valueOf(folderId)};

        Cursor cursor = db.query(WORDS_TABLE_NAME, projection, selection, selectionArgs, null, null, null);
        if (cursor != null) {
            totalWords = cursor.getCount();
            cursor.close();
        }
        db.close();
        return totalWords;
    }

    public void updateFolderName(int folderId, String newFolderName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(FOLDER_NAME_COL, newFolderName);
        db.update(FOLDERS_TABLE_NAME, values, FOLDER_ID_COL + " = ?", new String[]{String.valueOf(folderId)});
        db.close();
    }

    public void deleteFolder(int folderId) {
        SQLiteDatabase db = this.getWritableDatabase();
        deleteWordsInFolder(db, folderId);
        db.delete(FOLDERS_TABLE_NAME, FOLDER_ID_COL + "=?", new String[]{String.valueOf(folderId)});
        db.close();
    }
    private void deleteWordsInFolder(SQLiteDatabase db, int folderId) {
        db.delete(WORDS_TABLE_NAME, FOLDER_ID_COL + "=?", new String[]{String.valueOf(folderId)});
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + WORDS_TABLE_NAME);
        onCreate(db);
    }
}
