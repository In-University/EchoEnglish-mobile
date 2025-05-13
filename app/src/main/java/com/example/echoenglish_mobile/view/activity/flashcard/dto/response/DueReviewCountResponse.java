package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

public class DueReviewCountResponse {
    private int count;

    public DueReviewCountResponse() {
    }

    public DueReviewCountResponse(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}