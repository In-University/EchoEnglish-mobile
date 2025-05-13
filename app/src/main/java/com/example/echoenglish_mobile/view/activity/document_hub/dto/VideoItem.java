package com.example.echoenglish_mobile.view.activity.document_hub.dto;

public class VideoItem {
    private String title;
    private String videoId;
    private String thumbnailUrl;
    private String badgeText;
    private String duration;
    private String lessonInfo;
    private String levelInfo;

    public VideoItem(String title, String videoId, String badgeText, String duration, String lessonInfo, String levelInfo) {
        this.title = title;
        this.videoId = videoId;
        this.thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
        this.badgeText = badgeText;
        this.duration = duration;
        this.lessonInfo = lessonInfo;
        this.levelInfo = levelInfo;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getVideoId() {
        return videoId;
    }

    public void setVideoId(String videoId) {
        this.videoId = videoId;
        this.thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/mqdefault.jpg";
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getBadgeText() {
        return badgeText;
    }

    public void setBadgeText(String badgeText) {
        this.badgeText = badgeText;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getLessonInfo() {
        return lessonInfo;
    }

    public void setLessonInfo(String lessonInfo) {
        this.lessonInfo = lessonInfo;
    }

    public String getLevelInfo() {
        return levelInfo;
    }

    public void setLevelInfo(String levelInfo) {
        this.levelInfo = levelInfo;
    }
}