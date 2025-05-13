package com.example.echoenglish_mobile.view.activity.translate_text;

import com.google.gson.annotations.SerializedName;

public class TranslateRequest {
    @SerializedName("message") // Đảm bảo khớp key JSON
    private String message;

    public TranslateRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}