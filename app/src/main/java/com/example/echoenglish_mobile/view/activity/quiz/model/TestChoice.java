package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

public class TestChoice {

    @SerializedName("choiceId")
    private Integer choiceId;

    @SerializedName("choiceLabel")
    private String choiceLabel;

    @SerializedName("choiceText")
    private String choiceText;

    @SerializedName("choiceExplanation")
    private String choiceExplanation;

    // We don't need the parent 'question' object back in the Choice details
    // @SerializedName("question")
    // private TestQuestion question;

    // Getters and Setters
    public Integer getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Integer choiceId) {
        this.choiceId = choiceId;
    }

    public String getChoiceLabel() {
        return choiceLabel;
    }

    public void setChoiceLabel(String choiceLabel) {
        this.choiceLabel = choiceLabel;
    }

    public String getChoiceText() {
        return choiceText;
    }

    public void setChoiceText(String choiceText) {
        this.choiceText = choiceText;
    }

    public String getChoiceExplanation() {
        return choiceExplanation;
    }

    public void setChoiceExplanation(String choiceExplanation) {
        this.choiceExplanation = choiceExplanation;
    }

    // Getter/Setter for 'question' if included
    // public TestQuestion getQuestion() { return question; }
    // public void setQuestion(TestQuestion question) { this.question = question; }
}