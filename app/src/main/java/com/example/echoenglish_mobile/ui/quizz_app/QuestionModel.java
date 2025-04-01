package com.example.echoenglish_mobile.ui.quizz_app;
import java.util.List;

public class QuestionModel {
    private String question;
    private List<String> options;
    private String correct;

    // Constructor mặc định
    public QuestionModel() {
        this.question = "";
        this.options = List.of(); // Java 9+ (hoặc có thể dùng new ArrayList<>())
        this.correct = "";
    }

    // Constructor đầy đủ
    public QuestionModel(String question, List<String> options, String correct) {
        this.question = question;
        this.options = options;
        this.correct = correct;
    }

    // Getter và Setter
    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrect() {
        return correct;
    }

    public void setCorrect(String correct) {
        this.correct = correct;
    }
}