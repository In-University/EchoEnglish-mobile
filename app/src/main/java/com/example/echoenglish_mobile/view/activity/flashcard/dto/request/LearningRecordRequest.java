package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class LearningRecordRequest {
    @NotNull(message = "User ID cannot be null")
    private Long userId; // Sẽ được kiểm tra khớp với user đang login

    // Thay bằng vocabularyId
    @NotNull(message = "Vocabulary ID cannot be null")
    private Long vocabularyId; // ID của từ vựng đã học
}
