package com.example.echoenglish_mobile.model;

import java.io.Serializable;

public class SentenceSummary implements Serializable {
    private double total_duration;
    private int word_count;
    private double speaking_rate_wpm;
    private int filter_word_count;
    public String total_duration_feedback;
    public String word_count_feedback;
    public String speaking_rate_wpm_feedback;
    public String filter_word_count_feedback;

    public SentenceSummary(double total_duration, int word_count, double speaking_rate_wpm, int filter_word_count, String total_duration_feedback, String word_count_feedback, String speaking_rate_wpm_feedback, String filter_word_count_feedback) {
        this.total_duration = total_duration;
        this.word_count = word_count;
        this.speaking_rate_wpm = speaking_rate_wpm;
        this.filter_word_count = filter_word_count;
        this.total_duration_feedback = total_duration_feedback;
        this.word_count_feedback = word_count_feedback;
        this.speaking_rate_wpm_feedback = speaking_rate_wpm_feedback;
        this.filter_word_count_feedback = filter_word_count_feedback;
    }

    public double getTotal_duration() {
        return total_duration;
    }

    public void setTotal_duration(double total_duration) {
        this.total_duration = total_duration;
    }

    public int getWord_count() {
        return word_count;
    }

    public void setWord_count(int word_count) {
        this.word_count = word_count;
    }

    public double getSpeaking_rate_wpm() {
        return speaking_rate_wpm;
    }

    public void setSpeaking_rate_wpm(double speaking_rate_wpm) {
        this.speaking_rate_wpm = speaking_rate_wpm;
    }

    public int getFilter_word_count() {
        return filter_word_count;
    }

    public void setFilter_word_count(int filter_word_count) {
        this.filter_word_count = filter_word_count;
    }

    public String getTotal_duration_feedback() {
        return total_duration_feedback;
    }

    public void setTotal_duration_feedback(String total_duration_feedback) {
        this.total_duration_feedback = total_duration_feedback;
    }

    public String getWord_count_feedback() {
        return word_count_feedback;
    }

    public void setWord_count_feedback(String word_count_feedback) {
        this.word_count_feedback = word_count_feedback;
    }

    public String getSpeaking_rate_wpm_feedback() {
        return speaking_rate_wpm_feedback;
    }

    public void setSpeaking_rate_wpm_feedback(String speaking_rate_wpm_feedback) {
        this.speaking_rate_wpm_feedback = speaking_rate_wpm_feedback;
    }

    public String getFilter_word_count_feedback() {
        return filter_word_count_feedback;
    }

    public void setFilter_word_count_feedback(String filter_word_count_feedback) {
        this.filter_word_count_feedback = filter_word_count_feedback;
    }
}
