package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VocabularyCreateRequest {
    @NotBlank(message = "Word cannot be blank")
    private String word;

    @NotBlank(message = "Definition cannot be blank")
    private String definition;

    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
}
