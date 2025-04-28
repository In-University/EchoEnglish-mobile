package com.example.echoenglish_mobile.view.activity.analyze_result;

public class SpeakingResult {
    private String title;
    private String date;
    private String category;
    private int overallScore;
    private int pronunciationScore;
    private int rhythmScore;
    private int accuracyScore;

    public SpeakingResult(String title, String date, String category, int overallScore, int pronunciationScore, int rhythmScore, int accuracyScore) {
        this.title = title;
        this.date = date;
        this.category = category;
        this.overallScore = overallScore;
        this.pronunciationScore = pronunciationScore;
        this.rhythmScore = rhythmScore;
        this.accuracyScore = accuracyScore;
    }

    // --- Getters for all fields ---
    public String getTitle() { return title; }
    public String getDate() { return date; }
    public String getCategory() { return category; }
    public int getOverallScore() { return overallScore; }
    public int getPronunciationScore() { return pronunciationScore; }
    public int getRhythmScore() { return rhythmScore; }
    public int getAccuracyScore() { return accuracyScore; }
}
