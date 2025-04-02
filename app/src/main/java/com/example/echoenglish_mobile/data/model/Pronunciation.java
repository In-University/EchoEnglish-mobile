package com.example.echoenglish_mobile.data.model;

import java.io.Serializable;
import java.util.List;

public class Pronunciation implements Serializable {
    private String target_word;
    private String target_ipa;
    private List<String> target_ipa_no_stress;
    private String transcription_ipa;
    private List<String> transcription_no_stress;
    private double similarity;
    private List<PhonemeComparison> mapping;

    public Pronunciation(String target_word, String target_ipa, List<String> target_ipa_no_stress, String transcription_ipa, List<String> transcription_no_stress, double similarity, List<PhonemeComparison> mapping) {
        this.target_word = target_word;
        this.target_ipa = target_ipa;
        this.target_ipa_no_stress = target_ipa_no_stress;
        this.transcription_ipa = transcription_ipa;
        this.transcription_no_stress = transcription_no_stress;
        this.similarity = similarity;
        this.mapping = mapping;
    }

    public String getTarget_word() {
        return target_word;
    }

    public void setTarget_word(String target_word) {
        this.target_word = target_word;
    }

    public String getTarget_ipa() {
        return target_ipa;
    }

    public void setTarget_ipa(String target_ipa) {
        this.target_ipa = target_ipa;
    }

    public List<String> getTarget_ipa_no_stress() {
        return target_ipa_no_stress;
    }

    public void setTarget_ipa_no_stress(List<String> target_ipa_no_stress) {
        this.target_ipa_no_stress = target_ipa_no_stress;
    }

    public String getTranscription_ipa() {
        return transcription_ipa;
    }

    public void setTranscription_ipa(String transcription_ipa) {
        this.transcription_ipa = transcription_ipa;
    }

    public List<String> getTranscription_no_stress() {
        return transcription_no_stress;
    }

    public void setTranscription_no_stress(List<String> transcription_no_stress) {
        this.transcription_no_stress = transcription_no_stress;
    }

    public double getSimilarity() {
        return similarity;
    }

    public void setSimilarity(double similarity) {
        this.similarity = similarity;
    }

    public List<PhonemeComparison> getMapping() {
        return mapping;
    }

    public void setMapping(List<PhonemeComparison> mapping) {
        this.mapping = mapping;
    }
}
