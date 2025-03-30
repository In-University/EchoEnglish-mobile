package com.example.echoenglish_mobile.data.model;

import java.io.Serializable;

public class PhonemeComparisonDTO implements Serializable {

    private String correctPhoneme;
    private String actualPhoneme;
    private int startIndex;
    private int endIndex;
    private String result;

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getCorrectPhoneme() {
        return correctPhoneme;
    }

    public void setCorrectPhoneme(String correctPhoneme) {
        this.correctPhoneme = correctPhoneme;
    }

    public String getActualPhoneme() {
        return actualPhoneme;
    }

    public void setActualPhoneme(String actualPhoneme) {
        this.actualPhoneme = actualPhoneme;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setStartIndex(int startIndex) {
        this.startIndex = startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    public void setEndIndex(int endIndex) {
        this.endIndex = endIndex;
    }


    public PhonemeComparisonDTO(String result, String correctPhoneme, String actualPhoneme, int startIndex, int endIndex) {
        this.result = result;
        this.correctPhoneme = correctPhoneme;
        this.actualPhoneme = actualPhoneme;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
    }

}
