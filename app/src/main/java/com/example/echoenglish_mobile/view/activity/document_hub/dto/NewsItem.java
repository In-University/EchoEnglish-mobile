package com.example.echoenglish_mobile.view.activity.document_hub.dto;

import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.concurrent.TimeUnit;

public class NewsItem {
    @SerializedName("id")
    private Long id;
    @SerializedName("url")
    private String url;
    @SerializedName("title")
    private String title;
    @SerializedName("snippet")
    private String snippet;
    @SerializedName("source")
    private String source;
    @SerializedName("publishedDate")
    private String publishedDate;

    public Long getId() { return id; }
    public String getUrl() { return url; }
    public String getTitle() { return title; }
    public String getSnippet() { return snippet; }
    public String getSource() { return source; }
    public String getPublishedDate() { return publishedDate; }

    public String getImageUrl() {
        return null;
    }

    public String getFormattedTimeAgo() {
        if (publishedDate == null || publishedDate.isEmpty()) {
            return "";
        }

        try {
            DateTimeFormatter formatter;
            if (publishedDate.contains(".")) {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS");
            } else {
                formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            }
            LocalDateTime past = LocalDateTime.parse(publishedDate, formatter);
            LocalDateTime now = LocalDateTime.now();
            Duration duration = Duration.between(past, now);

            long seconds = duration.getSeconds();
            long minutes = TimeUnit.SECONDS.toMinutes(seconds);
            long hours = TimeUnit.SECONDS.toHours(seconds);
            long days = TimeUnit.SECONDS.toDays(seconds);

            if (seconds < 0) {
                return "in the future";
            } else if (seconds < 60) {
                return "just now";
            } else if (minutes < 60) {
                return minutes + "m ago";
            } else if (hours < 24) {
                return hours + "h ago";
            } else if (days == 1) {
                return "yesterday";
            } else if (days < 7) {
                return days + "d ago";
            } else if (days < 30) {
                return (days / 7) + "w ago";
            } else if (days < 365) {
                return (days / 30) + "mo ago";
            } else {
                return (days / 365) + "y ago";
            }

        } catch (DateTimeParseException e) {
            Log.e("NewsItem", "Error parsing date: '" + publishedDate + "'", e);
            return publishedDate;
        } catch (Exception e) {
            Log.e("NewsItem", "Error formatting time ago for date: " + publishedDate, e);
            return "";
        }
    }
}
