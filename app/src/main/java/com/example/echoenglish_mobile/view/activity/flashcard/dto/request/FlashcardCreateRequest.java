package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlashcardCreateRequest {
    private String name;
    private String imageUrl;
}