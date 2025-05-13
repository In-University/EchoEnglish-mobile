package com.example.echoenglish_mobile.view.activity.quiz.model.request;

public class SubmitAnswerRequest {
    private Long testHistoryId;
    private Integer questionId;
    private Integer choiceId;

    public SubmitAnswerRequest(Long testHistoryId, Integer questionId, Integer choiceId) {
        this.testHistoryId = testHistoryId;
        this.questionId = questionId;
        this.choiceId = choiceId;
    }

    public Long getTestHistoryId() {
        return testHistoryId;
    }

    public void setTestHistoryId(Long testHistoryId) {
        this.testHistoryId = testHistoryId;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getChoiceId() {
        return choiceId;
    }

    public void setChoiceId(Integer choiceId) {
        this.choiceId = choiceId;
    }
}
