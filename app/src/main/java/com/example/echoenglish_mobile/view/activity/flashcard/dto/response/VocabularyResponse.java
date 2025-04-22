package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VocabularyResponse implements Serializable {
    private static final long serialVersionUID = 1L; // Nên thêm cái này

    private Long id;
    private String word;
    private String definition;
    private String phonetic;
    private String example;
    private String type;
    private String imageUrl;
}