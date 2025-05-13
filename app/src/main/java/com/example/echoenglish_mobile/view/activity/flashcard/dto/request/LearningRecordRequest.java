package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LearningRecordRequest {
    private Long userId;

    private Long vocabularyId; // ID của từ vựng đã học

    private Boolean isRemembered;
}
