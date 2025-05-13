package com.example.echoenglish_mobile.model.response;
import com.google.gson.annotations.SerializedName;
import java.util.Locale;

public class TranscriptItem {
    @SerializedName("text")
    private String text;

    @SerializedName("start")
    private double startTime;

    @SerializedName("dur")
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

    private String formatSingleTime(double timeInSeconds) {
        int totalSeconds = (int) Math.floor(timeInSeconds);
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
    }

    public String getFormattedTime() {
        String formattedStartTime = formatSingleTime(this.startTime);
        double endTime = this.startTime + this.duration;
        String formattedEndTime = formatSingleTime(endTime);

        return formattedStartTime + " - " + formattedEndTime;
    }
}