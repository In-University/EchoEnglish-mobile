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
public class MemoryLevelsResponse {
    private long level0;
    private long level1;
    private long level2;
    private long level3;
    private long level4;
    private long mastered;
}