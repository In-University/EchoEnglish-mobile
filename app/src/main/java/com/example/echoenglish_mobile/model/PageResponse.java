package com.example.echoenglish_mobile.model;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PageResponse<T> {
    @SerializedName("content")
    private List<T> content;

    @SerializedName("totalPages")
    private int totalPages;

    @SerializedName("totalElements")
    private long totalElements;

    @SerializedName("number")
    private int number;

    @SerializedName("size")
    private int size;

    @SerializedName("first")
    private boolean first;

    @SerializedName("last")
    private boolean last;

    public List<T> getContent() { return content; }
    public int getTotalPages() { return totalPages; }
    public long getTotalElements() { return totalElements; }
    public int getNumber() { return number; }
    public int getSize() { return size; }
    public boolean isFirst() { return first; }
    public boolean isLast() { return last; }
}
