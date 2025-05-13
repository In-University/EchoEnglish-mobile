package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;


import java.util.List;

public class FlashcardDetailResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long creatorId;
    private String creatorName;
    private List<VocabularyResponse> vocabularies;

    public FlashcardDetailResponse(){

    }

    public FlashcardDetailResponse(Long id, String name, String imageUrl, Long categoryId, String categoryName, Long creatorId, String creatorName, List<VocabularyResponse> vocabularies) {
        this.id = id;
        this.name = name;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.creatorId = creatorId;
        this.creatorName = creatorName;
        this.vocabularies = vocabularies;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public Long getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(Long creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorName() {
        return creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }

    public List<VocabularyResponse> getVocabularies() {
        return vocabularies;
    }

    public void setVocabularies(List<VocabularyResponse> vocabularies) {
        this.vocabularies = vocabularies;
    }
}