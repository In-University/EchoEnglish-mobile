package com.example.echoenglish_mobile.view.activity.quiz.model.request;

public class StartTestRequest {
    // Đổi kiểu dữ liệu từ String thành Long
    private Long userId;
    private Integer testId;
    private Integer partId; // Giữ lại partId nếu bạn vẫn cần nó ở client

    // Cập nhật Constructor
    public StartTestRequest(Long userId, Integer testId, Integer partId) {
        this.userId = userId;
        this.testId = testId;
        this.partId = partId;
    }

    // Getters (Setters optional)
    public Long getUserId() {
        return userId;
    }

    public Integer getTestId() {
        return testId;
    }

    public Integer getPartId() {
        return partId;
    }
}