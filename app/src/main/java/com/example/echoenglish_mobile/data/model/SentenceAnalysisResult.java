package com.example.echoenglish_mobile.data.model;

import java.io.Serializable;
import java.util.List;

public class SentenceAnalysisResult implements Serializable {
    private String text;
    private List<WordDetail> chunks;
    private SentenceSummary summary;
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
}
