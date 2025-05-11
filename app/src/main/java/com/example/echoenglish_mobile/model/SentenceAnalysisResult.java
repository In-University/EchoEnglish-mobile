package com.example.echoenglish_mobile.model;

import com.example.echoenglish_mobile.model.response.SentenceAnalysisMetadata;
import com.example.echoenglish_mobile.view.activity.pronunciation_assessment.dto.SpeakingFeedback;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

public class SentenceAnalysisResult implements Serializable {
    private String text;
    private SentenceAnalysisMetadata metadata;

    private List<WordDetail> chunks;
    private SentenceSummary summary;
    private List<PhonemeStats> phoneme_statistics;
    @SerializedName("word_level_count")
    private Map<String, Integer> wordLevelCount;
    private SpeakingFeedback feedback;

    public SpeakingFeedback getFeedback() {
        return feedback;
    }

    public void setFeedback(SpeakingFeedback feedback) {
        this.feedback = feedback;
    }

    public Map<String, Integer> getWordLevelCount() {
        return wordLevelCount;
    }

    public void setWordLevelCount(Map<String, Integer> wordLevelCount) {
        this.wordLevelCount = wordLevelCount;
    }

    public List<PhonemeStats> getPhonemeStatsList() {
        return phoneme_statistics;
    }

    public void setPhonemeStatsList(List<PhonemeStats> phonemeStatsList) {
        this.phoneme_statistics = phonemeStatsList;
    }

    //    private List<> wordTranscriptions;
    public SentenceAnalysisResult(String text, List<WordDetail> chunks, SentenceSummary summary) {
        this.text = text;
        this.chunks = chunks;
        this.summary = summary;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<WordDetail> getChunks() {
        return chunks;
    }

    public void setChunks(List<WordDetail> chunks) {
        this.chunks = chunks;
    }

    public SentenceSummary getSummary() {
        return summary;
    }

    public void setSummary(SentenceSummary summary) {
        this.summary = summary;
    }

    public SentenceAnalysisMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(SentenceAnalysisMetadata metadata) {
        this.metadata = metadata;
    }

    public List<PhonemeStats> getPhoneme_statistics() {
        return phoneme_statistics;
    }

    public void setPhoneme_statistics(List<PhonemeStats> phoneme_statistics) {
        this.phoneme_statistics = phoneme_statistics;
    }
}
