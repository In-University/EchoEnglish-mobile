package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

public class TestQuestionContent {
    @SerializedName("contentId")
    private Integer contentId;

    @SerializedName("contentType")
    private String contentType; // e.g., "IMAGE", "AUDIO"

    @SerializedName("contentData")
    private String contentData; // e.g., URL

    @SerializedName("contentIndex")
    private Integer contentIndex;

    // We don't need the parent 'group' object back
    // @SerializedName("group")
    // private TestQuestionGroup group;


    // Getters and Setters
    public Integer getContentId() {
        return contentId;
    }

    public void setContentId(Integer contentId) {
        this.contentId = contentId;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentData() {
        return contentData;
    }

    public void setContentData(String contentData) {
        this.contentData = contentData;
    }

    public Integer getContentIndex() {
        return contentIndex;
    }

    public void setContentIndex(Integer contentIndex) {
        this.contentIndex = contentIndex;
    }

    // Getter/Setter for 'group' if included
    // public TestQuestionGroup getGroup() { return group; }
    // public void setGroup(TestQuestionGroup group) { this.group = group; }
}