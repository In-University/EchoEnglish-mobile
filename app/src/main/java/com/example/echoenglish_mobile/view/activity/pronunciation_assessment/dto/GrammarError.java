package com.example.echoenglish_mobile.view.activity.pronunciation_assessment.dto;

import java.io.Serializable;

public class GrammarError implements Serializable {
    private String type;
    private String originalText;
    private String correctionText;
    private String explanation;
    private String severityColor;

    public GrammarError(String type, String originalText, String correctionText, String explanation, String severityColor) {
        this.type = type;
        this.originalText = originalText;
        this.correctionText = correctionText;
        this.explanation = explanation;
        this.severityColor = severityColor;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getCorrectionText() {
        return correctionText;
    }

    public void setCorrectionText(String correctionText) {
        this.correctionText = correctionText;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getSeverityColor() {
        return severityColor;
    }

    public void setSeverityColor(String severityColor) {
        this.severityColor = severityColor;
    }
}
