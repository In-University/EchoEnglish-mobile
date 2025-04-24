package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FlashcardDetailResponse {
    private Long id;
    private String name;
    private String imageUrl;
    private Long categoryId;
    private String categoryName;
    private Long creatorId;
    private String creatorName;
    private List<VocabularyResponse> vocabularies;
}