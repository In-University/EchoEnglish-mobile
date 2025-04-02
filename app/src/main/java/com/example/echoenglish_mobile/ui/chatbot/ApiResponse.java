package com.example.echoenglish_mobile.ui.chatbot;

class ApiResponse {
    private String text;  // Đúng với key trong JSON của API

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}