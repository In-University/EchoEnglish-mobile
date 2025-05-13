package com.example.echoenglish_mobile.view.activity.grammar;

import com.example.echoenglish_mobile.view.activity.grammar.model.Content;

public class ContentItem extends GrammarItem {
    private final Content content;

    public ContentItem(Content content) {
        this.content = content;
    }

    public Content getContent() {
        return content;
    }

    @Override
    public int getViewType() {
        return mapContentViewType(content.getContentType());
    }
}