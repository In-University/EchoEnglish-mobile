package com.example.echoenglish_mobile.model.request;

import com.example.echoenglish_mobile.model.response.ChecklistItemResponse;
import com.example.echoenglish_mobile.model.response.MessageHistoryItem;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ConverseRequest {
    @SerializedName("context")
    private String context;

    @SerializedName("currentChecklist")
    private List<ChecklistItemResponse> currentChecklist;

    @SerializedName("currentUserInput")
    private String currentUserInput;

    @SerializedName("history")
    private List<MessageHistoryItem> history;

    public ConverseRequest(String context, List<ChecklistItemResponse> currentChecklist, String currentUserInput, List<MessageHistoryItem> history) {
        this.context = context;
        this.currentChecklist = currentChecklist;
        this.currentUserInput = currentUserInput;
        this.history = history;
    }

    public String getContext() { return context; }
    public List<ChecklistItemResponse> getCurrentChecklist() { return currentChecklist; }
    public String getCurrentUserInput() { return currentUserInput; }
    public List<MessageHistoryItem> getHistory() { return history; }
}