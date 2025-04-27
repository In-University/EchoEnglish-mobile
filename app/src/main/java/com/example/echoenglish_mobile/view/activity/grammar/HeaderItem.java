package com.example.echoenglish_mobile.view.activity.grammar;

public class HeaderItem extends GrammarItem {
    private final String text;
    private final int headerType;

    public HeaderItem(String text, int headerType) {
        this.text = text;
        this.headerType = headerType;
    }

    public String getText() {
        return text;
    }

    @Override
    public int getViewType() {
        return headerType;
    }
}