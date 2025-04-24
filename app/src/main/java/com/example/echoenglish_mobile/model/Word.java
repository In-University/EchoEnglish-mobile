package com.example.echoenglish_mobile.model;

import java.io.Serializable;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Word implements Serializable {
    private String word;
    private String imageUrl;
    private String ukPronunciation;
    private String usPronunciation;
    private String ukAudio;
    private String usAudio;
    private List<Meaning> meanings;
    private List<Synonym> synonyms;

    private transient boolean isFromHistory = false; // transient để Gson bỏ qua khi serialize/deserialize nếu lưu JSON (không cần thiết nếu chỉ lưu String)
}

