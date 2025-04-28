package com.example.echoenglish_mobile.view.activity.analyze_result;

public class WritingResult {
    private String title;
    private String date;
    private String type;
    private int wordCount;

    public WritingResult(String title, String date, String type, int wordCount) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.wordCount = wordCount;
    }

    // --- Getters for all fields ---
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getType() { return type; }
    public int getWordCount() { return wordCount; }
}
