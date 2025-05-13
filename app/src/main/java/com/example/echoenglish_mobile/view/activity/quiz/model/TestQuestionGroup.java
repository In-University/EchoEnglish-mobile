package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestQuestionGroup {
    @SerializedName("groupId")
    private Integer groupId;

    @SerializedName("groupIndex")
    private Integer groupIndex;

    @SerializedName("questions")
    private List<TestQuestion> questions;

    @SerializedName("contents")
    private List<TestQuestionContent> contents;

    public Integer getGroupId() {
        return groupId;
    }

    public void setGroupId(Integer groupId) {
        this.groupId = groupId;
    }

    public Integer getGroupIndex() {
        return groupIndex;
    }

    public void setGroupIndex(Integer groupIndex) {
        this.groupIndex = groupIndex;
    }

    public List<TestQuestion> getQuestions() {
        return questions;
    }

    public void setQuestions(List<TestQuestion> questions) {
        this.questions = questions;
    }

    public List<TestQuestionContent> getContents() {
        return contents;
    }

    public void setContents(List<TestQuestionContent> contents) {
        this.contents = contents;
    }

}
