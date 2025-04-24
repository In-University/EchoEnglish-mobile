package com.example.echoenglish_mobile.view.activity.flashcard.dto.response;

public class CategoryResponse {

    // @SerializedName("id") // Không cần nếu tên key JSON là "id"
    private Long id;

    // @SerializedName("name") // Không cần nếu tên key JSON là "name"
    private String name;

    // Thêm các trường khác nếu API backend trả về (ví dụ: iconUrl, description)
    // private String iconUrl;
    // private String description;

    // Constructor mặc định (cần thiết cho Gson)
    public CategoryResponse() {
    }

    // Getters (cần thiết để truy cập dữ liệu)
    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    // public String getIconUrl() { return iconUrl; }
    // public String getDescription() { return description; }

    // Setters (Tùy chọn)
    // public void setId(Long id) { this.id = id; }
    // public void setName(String name) { this.name = name; }
    // public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    // public void setDescription(String description) { this.description = description; }
}