package com.example.echoenglish_mobile.model;
import java.util.concurrent.TimeUnit;

public class ChatMessage {

    public enum SenderType {
        ME, OTHER
    }

    private String message;
    private SenderType senderType;
    private long timestamp;

    public ChatMessage(String message, SenderType senderType, long timestamp) {
        this.message = message;
        this.senderType = senderType;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public SenderType getSenderType() {
        return senderType;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getFormattedTime() {
        long currentTime = System.currentTimeMillis();
        long diff = currentTime - timestamp;

        long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
        long hours = TimeUnit.MILLISECONDS.toHours(diff);
        long days = TimeUnit.MILLISECONDS.toDays(diff);

        if (days > 0) {
            return days + "d ago";
        } else if (hours > 0) {
            return hours + "h ago";
        } else if (minutes > 0) {
            return minutes + "m ago";
        } else {
            return "Just now";
        }
    }
}
