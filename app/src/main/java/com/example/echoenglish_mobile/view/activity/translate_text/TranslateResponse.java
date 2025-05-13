package com.example.echoenglish_mobile.view.activity.translate_text;

import com.google.gson.annotations.SerializedName;

public class TranslateResponse {
    @SerializedName("text") // Đảm bảo khớp key JSON
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}