package com.example.echoenglish_mobile.view.activity.grammar;

import com.example.echoenglish_mobile.view.activity.grammar.model.Content;

// Content item (wraps the original Content model)
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
        // Use the helper method from the base class to map contentType
        return mapContentViewType(content.getContentType());
    }
}