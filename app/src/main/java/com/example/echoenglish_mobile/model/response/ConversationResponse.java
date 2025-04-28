package com.example.echoenglish_mobile.model.response;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConversationResponse {
    @SerializedName("aiResponse")
    private String aiResponse;

    @SerializedName("updatedChecklist")
    private List<ChecklistItemResponse> updatedChecklist;

    @SerializedName("allTasksCompleted")
    private boolean allCompleted;

    public String getAiResponse() { return aiResponse; }
    public List<ChecklistItemResponse> getUpdatedChecklist() { return updatedChecklist; }
    public boolean isAllCompleted() { return allCompleted; }

    public void setAiResponse(String aiResponse) { this.aiResponse = aiResponse; }
    public void setUpdatedChecklist(List<ChecklistItemResponse> updatedChecklist) { this.updatedChecklist = updatedChecklist; }
    public void setAllCompleted(boolean allCompleted) { this.allCompleted = allCompleted; }
}