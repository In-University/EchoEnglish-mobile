package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

import java.io.Serializable; // Nếu bạn cần truyền DTO này qua Intent/Bundle

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

// Đảm bảo đã thêm Lombok dependency

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
// implements Serializable // Thêm nếu cần truyền qua Intent
public class DueReviewCountResponse {
    private int count;
}