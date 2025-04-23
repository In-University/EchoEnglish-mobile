package com.example.echoenglish_mobile.model.request;

import com.google.gson.annotations.SerializedName;

public class StartConversationRequest {
    @SerializedName("context")
    private String context;

    @SerializedName("initialUserInput")
    private String initialUserInput;

    public StartConversationRequest(String context, String initialUserInput) {
        this.context = context;
        this.initialUserInput = initialUserInput;
    }

    public String getContext() { return context; }
    public String getInitialUserInput() { return initialUserInput; }
}
