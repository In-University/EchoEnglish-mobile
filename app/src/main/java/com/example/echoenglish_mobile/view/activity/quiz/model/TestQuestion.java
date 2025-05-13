package com.example.echoenglish_mobile.view.activity.quiz.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TestQuestion {

    @SerializedName("questionId")
    private Integer questionId;

    @SerializedName("questionNumber")
    private Integer questionNumber;

    @SerializedName("questionText")
    private String questionText;

    @SerializedName("correctAnswerLabel")
    private String correctAnswerLabel;

    @SerializedName("explanation")
    private String explanation;


    @SerializedName("choices")
    private List<TestChoice> choices;

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getQuestionNumber() {
        return questionNumber;
    }

    public void setQuestionNumber(Integer questionNumber) {
        this.questionNumber = questionNumber;
    }

    public String getQuestionText() {
        return questionText;
    }

    public void setQuestionText(String questionText) {
        this.questionText = questionText;
    }

    public String getCorrectAnswerLabel() {
        return correctAnswerLabel;
    }

    public void setCorrectAnswerLabel(String correctAnswerLabel) {
        this.correctAnswerLabel = correctAnswerLabel;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public List<TestChoice> getChoices() {
        return choices;
    }

    public void setChoices(List<TestChoice> choices) {
        this.choices = choices;
    }

}
