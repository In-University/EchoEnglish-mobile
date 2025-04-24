package com.example.echoenglish_mobile.view.activity.flashcard.dto.request;

public class FlashcardUpdateRequest {

    private String name;
    private String imageUrl;

    // Constructor mặc định (cần thiết cho Gson/Retrofit)
    public FlashcardUpdateRequest() {
    }

    // Constructor để tạo nhanh đối tượng (Tùy chọn)
    public FlashcardUpdateRequest(String name, String imageUrl) {
        this.name = name;
        this.imageUrl = imageUrl;
    }

    // Getters (Có thể cần nếu bạn muốn đọc lại giá trị trước khi gửi)
    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    // Setters (Rất cần thiết để đặt giá trị trước khi gửi đi)
    public void setName(String name) {
        this.name = name;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}