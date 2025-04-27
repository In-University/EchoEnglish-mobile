package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor // Thường cần nếu dùng AllArgsConstructor ở backend DTO
@Builder // Thường cần nếu dùng Builder ở backend DTO
// implements Serializable // Thêm nếu cần truyền qua Intent
public class LearningHistoryResponse {
    private Long id; // ID của bản ghi lịch sử
    private Long userId;
    private String userName;

    // --- Thông tin Flashcard (Từ Vocabulary) ---
    private Long flashcardId; // Có thể cần để biết từ thuộc flashcard nào
    private String flashcardName;

    // --- DỮ LIỆU TỪ VỰNG ---
    private Long vocabularyId; // ID của từ vựng đã học
    private String vocabularyWord; // Từ thực tế để hiển thị

    private LocalDateTime learnedAt; // Thời điểm học/ghi nhận cuối

    // --- DỮ LIỆU SỐ LẦN HỌC ---
    private int rememberCount; // Số lần đã học/nhớ
}