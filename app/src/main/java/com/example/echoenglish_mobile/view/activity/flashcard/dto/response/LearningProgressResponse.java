package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

public class LearningProgressResponse {

    // @SerializedName("flashcardId") // Ví dụ nếu key JSON khác
    private long flashcardId;

    // @SerializedName("userId")
    private long userId;

    // @SerializedName("totalVocabularies")
    private int totalVocabularies;

    // @SerializedName("learnedVocabularies")
    private int learnedVocabularies;

    // @SerializedName("completionPercentage")
    private double completionPercentage;

    // Constructor mặc định (cần thiết cho Gson)
    public LearningProgressResponse() {
    }

    // Getters (cần thiết để truy cập dữ liệu)
    public long getFlashcardId() {
        return flashcardId;
    }

    public long getUserId() {
        return userId;
    }

    public int getTotalVocabularies() {
        return totalVocabularies;
    }

    public int getLearnedVocabularies() {
        return learnedVocabularies;
    }

    public double getCompletionPercentage() {
        return completionPercentage;
    }

    // Setters (Tùy chọn, thường không cần cho Response DTO)
    // public void setFlashcardId(long flashcardId) { this.flashcardId = flashcardId; }
    // ...
}