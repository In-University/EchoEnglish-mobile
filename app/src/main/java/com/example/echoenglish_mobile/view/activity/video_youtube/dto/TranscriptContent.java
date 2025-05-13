package com.example.echoenglish_mobile.view.activity.video_youtube.dto;
import com.google.gson.annotations.SerializedName;
import java.util.List;
import com.example.echoenglish_mobile.model.response.TranscriptItem;


public class TranscriptContent {

    @SerializedName("content")
    private List<TranscriptItem> content;

    public List<TranscriptItem> getContent() {
        return content;
    }

    public void setContent(List<TranscriptItem> content) {
        this.content = content;
    }
}