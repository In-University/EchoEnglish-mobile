package com.example.echoenglish_mobile.view.activity.quiz.model.request;

public class SubmitAnswerRequest {
    private Long testHistoryId;
    private Integer questionId;
    private Integer choiceId;
    // You might add isCorrect here if calculated client-side, but usually done server-side on completion

    public SubmitAnswerRequest(Long testHistoryId, Integer questionId, Integer choiceId) {
        this.testHistoryId = testHistoryId;
        this.questionId = questionId;
        this.choiceId = choiceId;
    }
    // Getters (Setters optional)
}
