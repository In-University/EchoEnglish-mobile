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

}