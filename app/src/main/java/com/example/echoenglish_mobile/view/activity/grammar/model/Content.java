package com.example.echoenglish_mobile.view.activity.grammar.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

public class Content implements Serializable {
    private int id;
    @SerializedName("orderIndex")
    private int orderIndex;
    @SerializedName("contentType")
    private String contentType;
    @SerializedName("textContent")
    private String textContent;
    @SerializedName("listItemsJson")
    private String listItemsJson;
    @SerializedName("imageSrc")
    private String imageSrc;
    @SerializedName("imageAlt")
    private String imageAlt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getOrderIndex() { return orderIndex; }
    public void setOrderIndex(int orderIndex) { this.orderIndex = orderIndex; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public String getTextContent() { return textContent; }
    public void setTextContent(String textContent) { this.textContent = textContent; }
    public String getListItemsJson() { return listItemsJson; }
    public void setListItemsJson(String listItemsJson) { this.listItemsJson = listItemsJson; }
    public String getImageSrc() { return imageSrc; }
    public void setImageSrc(String imageSrc) { this.imageSrc = imageSrc; }
    public String getImageAlt() { return imageAlt; }
    public void setImageAlt(String imageAlt) { this.imageAlt = imageAlt; }
}
