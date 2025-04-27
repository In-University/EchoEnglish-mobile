package com.example.echoenglish_mobile.view.activity.translate_text;

import com.google.gson.annotations.SerializedName;

public class TranslateResponse {
    @SerializedName("text") // Đảm bảo khớp key JSON
    private String text;

    // Getter (cần thiết để Retrofit/Gson đọc giá trị)
    public String getText() {
        return text;
    }

    // Setter (không bắt buộc trừ khi bạn cần set giá trị này)
    public void setText(String text) {
        this.text = text;
    }
}