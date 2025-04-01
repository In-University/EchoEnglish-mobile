package com.example.echoenglish_mobile.data.model;

import java.io.Serializable;

public class WordDetail implements Serializable {
    private String text;
    private double start_time;
    private double end_time;
    private String error;
    private Pronunciation pronunciation;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public double getStart_time() {
        return start_time;
    }

    public void setStart_time(double start_time) {
        this.start_time = start_time;
    }

    public double getEnd_time() {
        return end_time;
    }

    public void setEnd_time(double end_time) {
        this.end_time = end_time;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Pronunciation getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(Pronunciation pronunciation) {
        this.pronunciation = pronunciation;
    }
}
