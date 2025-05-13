package com.example.echoenglish_mobile.view.activity.quiz.model.request;

public class StartTestRequest {
    private Long userId; // Adjust type if necessary (e.g., Long)
    private Integer testId;
    private Integer partId;

    public StartTestRequest(Long userId, Integer testId, Integer partId) {
        this.userId = userId;
        this.testId = testId;
        this.partId = partId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getTestId() {
        return testId;
    }

    public void setTestId(Integer testId) {
        this.testId = testId;
    }

    public Integer getPartId() {
        return partId;
    }

    public void setPartId(Integer partId) {
        this.partId = partId;
    }
}