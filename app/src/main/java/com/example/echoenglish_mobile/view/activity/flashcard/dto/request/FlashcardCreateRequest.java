package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

public class FlashcardCreateRequest {
    private String name;
    private String imageUrl;

    public FlashcardCreateRequest(){

    }

    public FlashcardCreateRequest(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
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
}