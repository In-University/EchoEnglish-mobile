package com.example.echoenglish_mobile.view.activity.chatbot;
import java.util.List;

public class ConversationCategory {
    private String title;
    private String colorHex;
    private List<ConversationScenario> scenarios;

    public ConversationCategory(String title, String colorHex, List<ConversationScenario> scenarios) {
        this.title = title;
        this.colorHex = colorHex;
        this.scenarios = scenarios;
    }

    public String getTitle() {
        return title;
    }

    public String getColorHex() {
        return colorHex;
    }

    public List<ConversationScenario> getScenarios() {
        return scenarios;
    }
}