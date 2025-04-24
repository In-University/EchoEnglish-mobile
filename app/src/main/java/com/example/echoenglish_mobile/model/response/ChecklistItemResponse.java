package com.example.echoenglish_mobile.model.response;
import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class ChecklistItemResponse implements Serializable {

    @SerializedName("id")
    private String id;

    @SerializedName("description")
    private String description;

    @SerializedName("completed")
    private boolean completed;

    // No-args constructor (needed for Gson/Serialization)
    public ChecklistItemResponse() {}

    public ChecklistItemResponse(String id, String description, boolean completed) {
        this.id = id;
        this.description = description;
        this.completed = completed;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }

    @Override
    public String toString() { // Useful for debugging
        return "ChecklistItemResponse{" +
                "id='" + id + '\'' +
                ", description='" + description + '\'' +
                ", completed=" + completed +
                '}';
    }
}