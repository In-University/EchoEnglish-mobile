package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

public class FlashcardUpdateRequest {

    private String name;
    private String imageUrl;

    public FlashcardUpdateRequest() {
    }

    public FlashcardUpdateRequest(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}