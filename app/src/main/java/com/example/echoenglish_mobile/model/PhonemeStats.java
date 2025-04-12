package com.example.echoenglish_mobile.model;

public class PhonemeStats {
    private String phoneme;
    private int totalCount;
    private int correctCount;

    public PhonemeStats(String phoneme, int correctCount, int totalCount) {
        this.phoneme = phoneme;
        this.totalCount = totalCount;
        this.correctCount = correctCount;
    }

    public String getPhoneme() {
        return phoneme;
    }

    public void setPhoneme(String phoneme) {
        this.phoneme = phoneme;
    }

    public int getTotalCount() {
        return totalCount;
    }

    public void setTotalCount(int totalCount) {
        this.totalCount = totalCount;
    }

    public int getCorrectCount() {
        return correctCount;
    }

    public void setCorrectCount(int correctCount) {
        this.correctCount = correctCount;
    }
    public int getPercentage() {
        if (totalCount <= 0) {
            return 0;
        }
        return (int) (((float) correctCount / totalCount) * 100);
    }
}
