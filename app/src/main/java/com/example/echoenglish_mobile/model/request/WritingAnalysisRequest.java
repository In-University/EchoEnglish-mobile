package com.example.echoenglish_mobile.model.request;

public class WritingAnalysisRequest {
    private String inputText;
    private String inputContext;

    public void setInputText(String inputText) {
        this.inputText = inputText;
    }

    public void setInputContext(String inputContext) {
        this.inputContext = inputContext;
    }

    public WritingAnalysisRequest(String inputText, String inputContext) {
        this.inputText = inputText;
        this.inputContext = inputContext;
    }

    public String getInputText() {
        return inputText;
    }

    public String getInputContext() {
        return inputContext;
    }
}
