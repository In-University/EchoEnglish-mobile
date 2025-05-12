package com.example.echoenglish_mobile.view.activity.pronunciation_assessment.dto;
import java.io.Serializable;
import java.util.List;

public class SpeakingFeedback implements Serializable {
    private String overview;
    private List<GrammarError> errors;
    private String suggestion;

    public SpeakingFeedback(String overview, List<GrammarError> errors, String suggestion) {
        this.overview = overview;
        this.errors = errors;
        this.suggestion = suggestion;
    }

    public String getOverview() {
        return overview;
    }

    public List<GrammarError> getErrors() {
        return errors;
    }

    public String getSuggestion() {
        return suggestion;
    }
}
