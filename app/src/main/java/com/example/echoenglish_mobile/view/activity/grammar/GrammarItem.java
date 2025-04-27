package com.example.echoenglish_mobile.view.activity.grammar;

// Base item for RecyclerView adapter
public abstract class GrammarItem {
    public static final int VIEW_TYPE_GRAMMAR_INTRO = 0;
    public static final int VIEW_TYPE_SUBSECTION_HEADER = 1;
    public static final int VIEW_TYPE_SUBSECTION_INTRO = 2; // Re-using for Subsection content
    public static final int VIEW_TYPE_TOPIC_HEADER = 3;

    // Content types map to view types
    public static final int VIEW_TYPE_PARAGRAPH = 4;
    public static final int VIEW_TYPE_BOLD_PARAGRAPH = 5;
    public static final int VIEW_TYPE_LIST = 6;
    public static final int VIEW_TYPE_IMAGE = 7;
    public static final int VIEW_TYPE_YOUTUBE_EMBED = 8;


    public abstract int getViewType();

    // Helper to map content type string to integer view type
    protected int mapContentViewType(String contentType) {
        if (contentType == null) return -1;

        switch (contentType) {
            case "paragraph":
                return VIEW_TYPE_PARAGRAPH;
            case "paragraph_with_bold":
                return VIEW_TYPE_BOLD_PARAGRAPH;
            case "list":
                return VIEW_TYPE_LIST;
            case "image":
                return VIEW_TYPE_IMAGE;
            default:
                return -1; // Unknown type
        }
    }
}