package com.example.echoenglish_mobile.ui.chatbot;

public class Message {
    private String text;
    private boolean isUserMessage;

    // Constructor
    public Message(String text, boolean isUserMessage) {
        this.text = text;
        this.isUserMessage = isUserMessage;
    }

    // Getter và Setter
    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public boolean isUserMessage() {
        return isUserMessage;
    }

    public void setUserMessage(boolean userMessage) {
        isUserMessage = userMessage;
    }
}
