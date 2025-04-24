package com.example.echoenglish_mobile.model.response;
public class TranscriptItem {
    private String text;
    private double startTime;
    private double duration;

    public TranscriptItem(String text, double startTime, double duration) {
        this.text = text;
        this.startTime = startTime;
        this.duration = duration;
    }

    public String getText() {
        return text;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getDuration() {
        return duration;
    }

    public String getFormattedTime() {
        int minutes = (int) (startTime / 60);
        int seconds = (int) (startTime % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }
}
