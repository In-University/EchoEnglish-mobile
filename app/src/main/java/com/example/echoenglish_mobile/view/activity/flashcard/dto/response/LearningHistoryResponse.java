package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import java.time.LocalDateTime;

public class LearningHistoryResponse {
    private Long id;
    private Long userId;
    private String userName;

    private Long flashcardId;
    private String flashcardName;
    private Long vocabularyId;
    private String vocabularyWord;

    private LocalDateTime learnedAt;
    private int rememberCount;

    public LearningHistoryResponse() {
    }

    public LearningHistoryResponse(Long id, Long userId, String userName, Long flashcardId, String flashcardName, Long vocabularyId, String vocabularyWord, LocalDateTime learnedAt, int rememberCount) {
        this.id = id;
        this.userId = userId;
        this.userName = userName;
        this.flashcardId = flashcardId;
        this.flashcardName = flashcardName;
        this.vocabularyId = vocabularyId;
        this.vocabularyWord = vocabularyWord;
        this.learnedAt = learnedAt;
        this.rememberCount = rememberCount;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Long getFlashcardId() {
        return flashcardId;
    }

    public void setFlashcardId(Long flashcardId) {
        this.flashcardId = flashcardId;
    }

    public String getFlashcardName() {
        return flashcardName;
    }

    public void setFlashcardName(String flashcardName) {
        this.flashcardName = flashcardName;
    }

    public Long getVocabularyId() {
        return vocabularyId;
    }

    public void setVocabularyId(Long vocabularyId) {
        this.vocabularyId = vocabularyId;
    }

    public String getVocabularyWord() {
        return vocabularyWord;
    }

    public void setVocabularyWord(String vocabularyWord) {
        this.vocabularyWord = vocabularyWord;
    }

    public LocalDateTime getLearnedAt() {
        return learnedAt;
    }

    public void setLearnedAt(LocalDateTime learnedAt) {
        this.learnedAt = learnedAt;
    }

    public int getRememberCount() {
        return rememberCount;
    }

    public void setRememberCount(int rememberCount) {
        this.rememberCount = rememberCount;
    }
}