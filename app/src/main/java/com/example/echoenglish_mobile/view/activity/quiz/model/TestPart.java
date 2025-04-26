package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Set;

public class TestPart {

    @SerializedName("partId")
    private Integer partId;

    @SerializedName("partNumber")
    private Integer partNumber;

    // THÊM TRƯỜNG NÀY - Backend phải trả về thông tin này
    @SerializedName("test") // Hoặc tên key JSON tương ứng từ backend
    private Test test; // Bao gồm cả testId, name...

    // Giữ lại Set nếu bạn đã đổi ở backend để tránh MultipleBagFetch
    @SerializedName("groups")
    private List<TestQuestionGroup> groups; // Hoặc List nếu backend trả về List

    // Getters and Setters
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

    // Getter/Setter cho test
    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public List<TestQuestionGroup> getGroups() { // Hoặc List
        return groups;
    }

    public void setGroups(List<TestQuestionGroup> groups) { // Hoặc List
        this.groups = groups;
    }
}