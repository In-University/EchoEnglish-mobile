package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.example.echoenglish_mobile.model.User;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestHistory {

    @SerializedName("id")
    private Long id;

    @SerializedName("user")
    private User user;

    @SerializedName("test")
    private Test test;

    @SerializedName("startedAt")
    private String startedAt;

    @SerializedName("completedAt")
    private String completedAt;

    @SerializedName("score")
    private Double score;

    @SerializedName("totalQuestions")
    private Integer totalQuestions;

    @SerializedName("correctAnswers")
    private Integer correctAnswers;

    @SerializedName("details")
    private List<TestHistoryDetail> details;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Test getTest() {
        return test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

    public String getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(String startedAt) {
        this.startedAt = startedAt;
    }

    public String getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(String completedAt) {
        this.completedAt = completedAt;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public Integer getTotalQuestions() {
        return totalQuestions;
    }

    public void setTotalQuestions(Integer totalQuestions) {
        this.totalQuestions = totalQuestions;
    }

    public Integer getCorrectAnswers() {
        return correctAnswers;
    }

    public void setCorrectAnswers(Integer correctAnswers) {
        this.correctAnswers = correctAnswers;
    }

    public List<TestHistoryDetail> getDetails() {
        return details;
    }

    public void setDetails(List<TestHistoryDetail> details) {
        this.details = details;
    }
}
