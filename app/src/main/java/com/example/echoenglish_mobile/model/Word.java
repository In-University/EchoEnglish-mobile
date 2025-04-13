package com.example.echoenglish_mobile.model;

import java.io.Serializable;
import java.util.List;

public class Word implements Serializable {
    private String word;
    private String imageUrl;
    private String ukPronunciation;
    private String usPronunciation;
    private String ukAudio;
    private String usAudio;
    private List<Meaning> meanings;
    private List<Synonym> synonyms;

    public Word(String word, String imageUrl, String ukPronunciation, String usPronunciation, String ukAudio, String usAudio, List<Meaning> meanings, List<Synonym> synonyms) {
        this.word = word;
        this.imageUrl = imageUrl;
        this.ukPronunciation = ukPronunciation;
        this.usPronunciation = usPronunciation;
        this.ukAudio = ukAudio;
        this.usAudio = usAudio;
        this.meanings = meanings;
        this.synonyms = synonyms;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUkPronunciation() {
        return ukPronunciation;
    }

    public void setUkPronunciation(String ukPronunciation) {
        this.ukPronunciation = ukPronunciation;
    }

    public String getUsPronunciation() {
        return usPronunciation;
    }

    public void setUsPronunciation(String usPronunciation) {
        this.usPronunciation = usPronunciation;
    }

    public String getUkAudio() {
        return ukAudio;
    }

    public void setUkAudio(String ukAudio) {
        this.ukAudio = ukAudio;
    }

    public String getUsAudio() {
        return usAudio;
    }

    public void setUsAudio(String usAudio) {
        this.usAudio = usAudio;
    }

    public List<Meaning> getMeanings() {
        return meanings;
    }

    public void setMeanings(List<Meaning> meanings) {
        this.meanings = meanings;
    }

    public List<Synonym> getSynonyms() {
        return synonyms;
    }

    public void setSynonyms(List<Synonym> synonyms) {
        this.synonyms = synonyms;
    }
}

