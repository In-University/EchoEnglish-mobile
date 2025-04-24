package com.example.echoenglish_mobile.view.activity.flashcard.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PexelsResponse {

    @SerializedName("page")
    private int page;
    @SerializedName("per_page")
    private int perPage;
    @SerializedName("photos") // Danh sách ảnh
    private List<PexelsPhoto> photos;
    @SerializedName("total_results")
    private int totalResults;
    @SerializedName("next_page")
    private String nextPage;
    @SerializedName("prev_page")
    private String prevPage;

    public PexelsResponse() {}

    // Getters
    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    public List<PexelsPhoto> getPhotos() { return photos; }
    public int getTotalResults() { return totalResults; }
    public String getNextPage() { return nextPage; }
    public String getPrevPage() { return prevPage; }
}