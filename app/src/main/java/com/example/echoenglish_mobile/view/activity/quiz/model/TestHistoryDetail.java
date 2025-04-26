package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

public class TestHistoryDetail {
    @SerializedName("id")
    private Long id;

    // Don't usually need parent TestHistory back
    // @SerializedName("testHistory")
    // private TestHistory testHistory;

    @SerializedName("question")
    private TestQuestion question;

    @SerializedName("choice")
    private TestChoice choice; // The choice the user selected

    @SerializedName("isCorrect")
    private Boolean isCorrect;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TestQuestion getQuestion() {
        return question;
    }

    public void setQuestion(TestQuestion question) {
        this.question = question;
    }

    public TestChoice getChoice() {
        return choice;
    }

    public void setChoice(TestChoice choice) {
        this.choice = choice;
    }

    public Boolean getCorrect() {
        return isCorrect;
    }

    public void setCorrect(Boolean correct) {
        isCorrect = correct;
    }
}
