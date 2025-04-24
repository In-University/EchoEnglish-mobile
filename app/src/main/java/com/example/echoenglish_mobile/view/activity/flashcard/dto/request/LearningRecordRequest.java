package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LearningRecordRequest {
    private Long userId; // Sẽ được kiểm tra khớp với user đang login

    private Long vocabularyId; // ID của từ vựng đã học
}
