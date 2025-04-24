package com.example.echoenglish_mobile.view.activity.flashcard.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PexelsPhotoSource implements Serializable {
    private static final long serialVersionUID = 3L;

    @SerializedName("original")
    private String original;
    @SerializedName("large2x")
    private String large2x;
    @SerializedName("large")
    private String large;
    @SerializedName("medium") // URL phù hợp để hiển thị/lưu trữ
    private String medium;
    @SerializedName("small") // URL nhỏ hơn
    private String small;
    @SerializedName("portrait")
    private String portrait;
    @SerializedName("landscape")
    private String landscape;
    @SerializedName("tiny") // URL rất nhỏ cho preview
    private String tiny;

    public PexelsPhotoSource() {}

    // Getters
    public String getOriginal() { return original; }
    public String getLarge2x() { return large2x; }
    public String getLarge() { return large; }
    public String getMedium() { return medium; }
    public String getSmall() { return small; }
    public String getPortrait() { return portrait; }
    public String getLandscape() { return landscape; }
    public String getTiny() { return tiny; }
}