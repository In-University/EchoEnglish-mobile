package com.example.echoenglish_mobile.view.activity.chatbot;

public class ConversationScenario {
    private String id;
    private String title;
    private String description;
    private String iconName;
    private String difficulty;
    private String duration;

    public ConversationScenario(String id, String title, String description, String iconName, String difficulty, String duration) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.iconName = iconName;
        this.difficulty = difficulty;
        this.duration = duration;
    }

    public String getId() { return id; }
    public String getTitle() {
        return title;
    }
    public String getDescription() {
        return description;
    }
    public String getIconName() { return iconName; }
    public String getDifficulty() {
        return difficulty;
    }
    public String getDuration() {
        return duration;
    }
}