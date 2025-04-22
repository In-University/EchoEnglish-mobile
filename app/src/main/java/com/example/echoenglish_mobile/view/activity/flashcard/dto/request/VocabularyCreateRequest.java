package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class VocabularyCreateRequest {
    private String word;
    private String definition;

    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
}
