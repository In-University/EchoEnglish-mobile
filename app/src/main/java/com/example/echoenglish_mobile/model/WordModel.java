package com.example.echoenglish_mobile.model;

public class WordModel {

    private int folderId;
    private int wordId;
    private String word;
    private String meaning;



    public WordModel(int folderId, int wordId, String word, String meaning) {
        this.folderId = folderId;
        this.wordId = wordId;
        this.word = word;
        this.meaning = meaning;
    }

    public String getWord() {
        return word;
    }

    public String getMeaning() {
        return meaning;
    }

}