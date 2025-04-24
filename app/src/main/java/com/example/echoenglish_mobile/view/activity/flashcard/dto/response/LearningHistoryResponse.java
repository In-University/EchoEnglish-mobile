package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LearningHistoryResponse {
    private Long id; // ID của bản ghi lịch sử
    private Long userId;
    private String userName;

    // --- ĐÃ LOẠI BỎ ---
    // private Long flashcardId;
    // private String flashcardName;

    // --- DỮ LIỆU TỪ VỰNG ---
    private Long vocabularyId; // ID của từ vựng đã học
    private String vocabularyWord; // Từ thực tế để hiển thị

    private LocalDateTime learnedAt; // Thời điểm học/ghi nhận cuối

    // --- DỮ LIỆU SỐ LẦN HỌC ---
    private int rememberCount; // Số lần đã học/nhớ
}