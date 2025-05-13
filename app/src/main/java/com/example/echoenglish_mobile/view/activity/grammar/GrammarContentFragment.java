package com.example.echoenglish_mobile.view.activity.grammar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.grammar.model.Content;
import com.example.echoenglish_mobile.view.activity.grammar.model.Grammar;
import com.example.echoenglish_mobile.view.activity.grammar.model.Subsection;
import com.example.echoenglish_mobile.view.activity.grammar.model.Topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GrammarContentFragment extends Fragment {

    private static final String ARG_GRAMMAR = "grammar";
    private Grammar grammar;
    private RecyclerView recyclerViewGrammarContent;

    public static GrammarContentFragment newInstance(Grammar grammar) {
        GrammarContentFragment fragment = new GrammarContentFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_GRAMMAR, grammar);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            grammar = (Grammar) getArguments().getSerializable(ARG_GRAMMAR);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_grammar_content, container, false);
        return rootView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerViewGrammarContent = view.findViewById(R.id.recyclerViewGrammarContent);

        if (grammar != null) {
            List<GrammarItem> displayItems = buildDisplayItems(grammar);

            GrammarContentAdapter adapter = new GrammarContentAdapter(displayItems);
            recyclerViewGrammarContent.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerViewGrammarContent.setAdapter(adapter);
        }
    }

    private List<GrammarItem> buildDisplayItems(Grammar grammar) {
        List<GrammarItem> items = new ArrayList<>();

        if (grammar.getContents() != null) {
            List<Content> sortedContents = new ArrayList<>(grammar.getContents());
            Collections.sort(sortedContents, Comparator.comparingInt(Content::getOrderIndex));
            for (Content content : sortedContents) {
                if ("paragraph_with_bold".equals(content.getContentType())) {
                    items.add(new HeaderItem(content.getTextContent(), GrammarItem.VIEW_TYPE_GRAMMAR_INTRO));
                } else {
                    // If other types appear at top level, add them as ContentItem
                    addContentItem(items, content);
                }
            }
        }

        if (grammar.getSubsections() != null) {
            for (Subsection subsection : grammar.getSubsections()) {
                if (subsection.getName() != null && !subsection.getName().isEmpty()) {
                    items.add(new HeaderItem(subsection.getName(), GrammarItem.VIEW_TYPE_SUBSECTION_HEADER));
                }

                if (subsection.getContents() != null) {
                    List<Content> sortedSubsectionContents = new ArrayList<>(subsection.getContents());
                    Collections.sort(sortedSubsectionContents, Comparator.comparingInt(Content::getOrderIndex));
                    for (Content content : sortedSubsectionContents) {
                        items.add(new ContentItem(content));
                    }
                }

                if (subsection.getTopics() != null) {
                    for (Topic topic : subsection.getTopics()) {
                        if (topic.getName() != null && !topic.getName().isEmpty()) {
                            items.add(new HeaderItem(topic.getName(), GrammarItem.VIEW_TYPE_TOPIC_HEADER));
                        }

                        if (topic.getContents() != null) {
                            List<Content> sortedTopicContents = new ArrayList<>(topic.getContents());
                            Collections.sort(sortedTopicContents, Comparator.comparingInt(Content::getOrderIndex));
                            for (Content content : sortedTopicContents) {
                                addContentItem(items, content);
                            }
                        }
                    }
                }
            }
        }

        return items;
    }

    private void addContentItem(List<GrammarItem> items, Content content) {
        if (content == null) return;

        String contentType = content.getContentType();
        switch (contentType) {
            case "paragraph":
            case "paragraph_with_bold":
            case "list":
            case "image":
            case "youtube_embed":
                items.add(new ContentItem(content));
                break;
            default:
                break;
        }
    }
}