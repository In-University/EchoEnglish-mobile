package com.example.echoenglish_mobile.model.response;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class MessageHistoryItem implements Serializable {

    @SerializedName("role")
    private String role;

    @SerializedName("content")
    private String content;

    public MessageHistoryItem() {}

    public MessageHistoryItem(String role, String content) {
        this.role = role;
        this.content = content;
    }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
