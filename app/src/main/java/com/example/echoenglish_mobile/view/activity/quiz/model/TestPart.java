package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Set;

public class TestPart {

    @SerializedName("partId")
    private Integer partId;

    @SerializedName("partNumber")
    private Integer partNumber;

    @SerializedName("test")
    private Test test;

    @SerializedName("groups")
    private Set<TestQuestionGroup> groups;

    public Integer getPartId() {
        return partId;
    }

    public void setPartId(Integer partId) {
        this.partId = partId;
    }

    public Integer getPartNumber() {
        return partNumber;
    }

    public void setPartNumber(Integer partNumber) {
        this.partNumber = partNumber;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public Set<TestQuestionGroup> getGroups() {
        return groups;
    }

    public void setGroups(Set<TestQuestionGroup> groups) {
        this.groups = groups;
    }
}