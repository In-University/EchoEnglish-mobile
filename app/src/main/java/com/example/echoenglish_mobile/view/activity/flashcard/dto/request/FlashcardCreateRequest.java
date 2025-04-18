package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FlashcardCreateRequest {
    @NotBlank(message = "Flashcard name cannot be blank")
    private String name;
    private String imageUrl;
}