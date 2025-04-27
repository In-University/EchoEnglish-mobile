package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

public class VocabularyReviewResponse {
    // Vocabulary Details (similar to VocabularyResponse)
    private Long id;
    private String word;
    private String definition;
    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;

    // Learning History Details (relevant for review)
    private Long learningHistoryId; // The ID of the history record itself
    private int rememberCount;     // How many times remembered
    private LocalDateTime learnedAt;   // When it was last learned/forgotten

    public VocabularyReviewResponse() {
    }

    public VocabularyReviewResponse(Long id, String word, String definition, String phonetic, String example, String type, String imageUrl, Long learningHistoryId, int rememberCount, LocalDateTime learnedAt) {
        this.id = id;
        this.word = word;
        this.definition = definition;
        this.phonetic = phonetic;
        this.example = example;
        this.type = type;
        this.imageUrl = imageUrl;
        this.learningHistoryId = learningHistoryId;
        this.rememberCount = rememberCount;
        this.learnedAt = learnedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getDefinition() {
        return definition;
    }

    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public String getPhonetic() {
        return phonetic;
    }

    public void setPhonetic(String phonetic) {
        this.phonetic = phonetic;
    }

    public String getExample() {
        return example;
    }

    public void setExample(String example) {
        this.example = example;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getLearningHistoryId() {
        return learningHistoryId;
    }

    public void setLearningHistoryId(Long learningHistoryId) {
        this.learningHistoryId = learningHistoryId;
    }

    public int getRememberCount() {
        return rememberCount;
    }

    public void setRememberCount(int rememberCount) {
        this.rememberCount = rememberCount;
    }

    public LocalDateTime getLearnedAt() {
        return learnedAt;
    }

    public void setLearnedAt(LocalDateTime learnedAt) {
        this.learnedAt = learnedAt;
    }
}