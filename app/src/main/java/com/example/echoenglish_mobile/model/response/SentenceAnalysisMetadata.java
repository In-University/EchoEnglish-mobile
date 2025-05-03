package com.example.echoenglish_mobile.model.response;

import java.io.Serializable;
import java.time.LocalDateTime;
public class SentenceAnalysisMetadata implements Serializable {
    private String userId;
    private String taskId;
    private String targetWord;
    private LocalDateTime createdAt;

    private String audioName;
    private Long audioSize;
    private String audioContentType;
    private Double audioDuration;

    public SentenceAnalysisMetadata(String userId, String taskId, String targetWord, LocalDateTime createdAt, String audioName, Long audioSize, String audioContentType, Double audioDuration) {
        this.userId = userId;
        this.taskId = taskId;
        this.targetWord = targetWord;
        this.createdAt = createdAt;
        this.audioName = audioName;
        this.audioSize = audioSize;
        this.audioContentType = audioContentType;
        this.audioDuration = audioDuration;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getTargetWord() {
        return targetWord;
    }

    public void setTargetWord(String targetWord) {
        this.targetWord = targetWord;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getAudioName() {
        return audioName;
    }

    public void setAudioName(String audioName) {
        this.audioName = audioName;
    }

    public Long getAudioSize() {
        return audioSize;
    }

    public void setAudioSize(Long audioSize) {
        this.audioSize = audioSize;
    }

    public String getAudioContentType() {
        return audioContentType;
    }

    public void setAudioContentType(String audioContentType) {
        this.audioContentType = audioContentType;
    }

    public Double getAudioDuration() {
        return audioDuration;
    }

    public void setAudioDuration(Double audioDuration) {
        this.audioDuration = audioDuration;
    }
}
