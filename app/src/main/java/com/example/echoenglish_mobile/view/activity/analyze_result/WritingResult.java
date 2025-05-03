package com.example.echoenglish_mobile.view.activity.analyze_result;

public class WritingResult {
    private String title;
    private String date;
    private String type;
    private int wordCount;
    private String feedbackJson;

    public WritingResult(String title, String date, String type, int wordCount, String feedbackJson) {
        this.title = title;
        this.date = date;
        this.type = type;
        this.wordCount = wordCount;
        this.feedbackJson = feedbackJson;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getWordCount() {
        return wordCount;
    }

    public void setWordCount(int wordCount) {
        this.wordCount = wordCount;
    }

    public String getFeedbackJson() {
        return feedbackJson;
    }

    public void setFeedbackJson(String feedbackJson) {
        this.feedbackJson = feedbackJson;
    }
}
