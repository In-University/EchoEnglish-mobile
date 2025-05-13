package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

public class LearningProgressResponse {

    private long flashcardId;
    private long userId;
    private int totalVocabularies;
    private int learnedVocabularies;
    private double completionPercentage;
    public LearningProgressResponse() {
    }

    public LearningProgressResponse(long flashcardId, long userId, int totalVocabularies, int learnedVocabularies, double completionPercentage) {
        this.flashcardId = flashcardId;
        this.userId = userId;
        this.totalVocabularies = totalVocabularies;
        this.learnedVocabularies = learnedVocabularies;
        this.completionPercentage = completionPercentage;
    }

    public long getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(long flashcardId) {
        this.flashcardId = flashcardId;
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public int getTotalVocabularies() {
        return totalVocabularies;
    }

    public void setTotalVocabularies(int totalVocabularies) {
        this.totalVocabularies = totalVocabularies;
    }

    public int getLearnedVocabularies() {
        return learnedVocabularies;
    }

    public void setLearnedVocabularies(int learnedVocabularies) {
        this.learnedVocabularies = learnedVocabularies;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    public void setCompletionPercentage(double completionPercentage) {
        this.completionPercentage = completionPercentage;
    }
}