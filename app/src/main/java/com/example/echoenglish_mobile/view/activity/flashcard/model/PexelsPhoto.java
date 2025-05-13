package com.example.echoenglish_mobile.view.activity.flashcard.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class PexelsPhoto implements Serializable {
    private static final long serialVersionUID = 4L;

    @SerializedName("id")
    private int id;
    @SerializedName("width")
    private int width;
    @SerializedName("height")
    private int height;
    @SerializedName("url") // URL trang Pexels của ảnh (dùng để ghi công)
    private String pexelsUrl;
    @SerializedName("photographer") // Tên tác giả (dùng để ghi công)
    private String photographer;
    @SerializedName("photographer_url") // URL trang Pexels của tác giả
    private String photographerUrl;
    @SerializedName("photographer_id")
    private int photographerId;
    @SerializedName("avg_color")
    private String avgColor;
    @SerializedName("src") // Object chứa các URL ảnh kích thước khác nhau
    private PexelsPhotoSource src;
    @SerializedName("liked")
    private boolean liked;
    @SerializedName("alt") // Mô tả ảnh
    private String alt;

    public PexelsPhoto() {}

    public int getId() { return id; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public String getPexelsUrl() { return pexelsUrl; }
    public String getPhotographer() { return photographer; }
    public String getPhotographerUrl() { return photographerUrl; }
    public int getPhotographerId() { return photographerId; }
    public String getAvgColor() { return avgColor; }
    public PexelsPhotoSource getSrc() { return src; }
    public boolean isLiked() { return liked; }
    public String getAlt() { return alt; }
}