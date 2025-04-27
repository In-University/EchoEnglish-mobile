package com.example.echoenglish_mobile.view.activity.grammar;


import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class GrammarContentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<GrammarItem> grammarItems;
    private Context context; // Keep context for Glide

    public GrammarContentAdapter(List<GrammarItem> grammarItems) {
        this.grammarItems = grammarItems;
    }

    @Override
    public int getItemViewType(int position) {
        return grammarItems.get(position).getViewType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Get context here
        LayoutInflater inflater = LayoutInflater.from(context);
        View view;

        switch (viewType) {
            case GrammarItem.VIEW_TYPE_GRAMMAR_INTRO:
                view = inflater.inflate(R.layout.item_grammar_intro, parent, false);
                return new GrammarIntroViewHolder(view);
            case GrammarItem.VIEW_TYPE_SUBSECTION_HEADER:
                view = inflater.inflate(R.layout.item_subsection_header, parent, false);
                return new SubsectionHeaderViewHolder(view);
            case GrammarItem.VIEW_TYPE_SUBSECTION_INTRO:
                view = inflater.inflate(R.layout.item_subsection_intro, parent, false);
                return new SubsectionIntroViewHolder(view);
            case GrammarItem.VIEW_TYPE_TOPIC_HEADER:
                view = inflater.inflate(R.layout.item_topic_header, parent, false);
                return new TopicHeaderViewHolder(view);
            case GrammarItem.VIEW_TYPE_PARAGRAPH:
                view = inflater.inflate(R.layout.item_paragraph, parent, false);
                return new ParagraphViewHolder(view);
            case GrammarItem.VIEW_TYPE_BOLD_PARAGRAPH:
                view = inflater.inflate(R.layout.item_bold_paragraph, parent, false);
                return new BoldParagraphViewHolder(view);
            case GrammarItem.VIEW_TYPE_LIST:
                view = inflater.inflate(R.layout.item_list_item, parent, false);
                return new ListViewHolder(view);
            case GrammarItem.VIEW_TYPE_IMAGE:
                view = inflater.inflate(R.layout.item_image, parent, false);
                return new ImageViewHolder(view);
            default:
                return new EmptyViewHolder(new View(context));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        GrammarItem item = grammarItems.get(position);

        if (item instanceof HeaderItem) {
            HeaderItem headerItem = (HeaderItem) item;
            switch (headerItem.getViewType()) {
                case GrammarItem.VIEW_TYPE_GRAMMAR_INTRO:
                    ((GrammarIntroViewHolder) holder).bind(headerItem);
                    break;
                case GrammarItem.VIEW_TYPE_SUBSECTION_HEADER:
                    ((SubsectionHeaderViewHolder) holder).bind(headerItem);
                    break;
                case GrammarItem.VIEW_TYPE_TOPIC_HEADER:
                    ((TopicHeaderViewHolder) holder).bind(headerItem);
                    break;
            }
        } else if (item instanceof ContentItem) {
            ContentItem contentItem = (ContentItem) item;
            switch (contentItem.getViewType()) {
                case GrammarItem.VIEW_TYPE_SUBSECTION_INTRO: // Re-using this layout
                    ((SubsectionIntroViewHolder) holder).bind(contentItem);
                    break;
                case GrammarItem.VIEW_TYPE_PARAGRAPH:
                    ((ParagraphViewHolder) holder).bind(contentItem);
                    break;
                case GrammarItem.VIEW_TYPE_BOLD_PARAGRAPH:
                    ((BoldParagraphViewHolder) holder).bind(contentItem);
                    break;
                case GrammarItem.VIEW_TYPE_LIST:
                    ((ListViewHolder) holder).bind(contentItem);
                    break;
                case GrammarItem.VIEW_TYPE_IMAGE:
                    ((ImageViewHolder) holder).bind(contentItem, context);
                    break;
            }
        }
        // No binding needed for EmptyViewHolder
    }

    @Override
    public int getItemCount() {
        return grammarItems.size();
    }

    // --- ViewHolders using findViewById ---

    static class GrammarIntroViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewGrammarIntro;

        public GrammarIntroViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewGrammarIntro = itemView.findViewById(R.id.textViewGrammarIntro);
        }

        public void bind(HeaderItem item) {
            textViewGrammarIntro.setText(item.getText());
        }
    }

    static class SubsectionHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewSubsectionHeader;

        public SubsectionHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubsectionHeader = itemView.findViewById(R.id.textViewSubsectionHeader);
        }

        public void bind(HeaderItem item) {
            textViewSubsectionHeader.setText(item.getText());
        }
    }

    static class SubsectionIntroViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewSubsectionIntro;

        public SubsectionIntroViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewSubsectionIntro = itemView.findViewById(R.id.textViewSubsectionIntro);
        }

        public void bind(ContentItem item) {
            textViewSubsectionIntro.setText(item.getContent().getTextContent());
        }
    }


    static class TopicHeaderViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewTopicHeader;

        public TopicHeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTopicHeader = itemView.findViewById(R.id.textViewTopicHeader);
        }

        public void bind(HeaderItem item) {
            textViewTopicHeader.setText(item.getText());
        }
    }

    static class ParagraphViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewParagraph;

        public ParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewParagraph = itemView.findViewById(R.id.textViewParagraph);
        }

        public void bind(ContentItem item) {
            textViewParagraph.setText(item.getContent().getTextContent());
        }
    }

    static class BoldParagraphViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewBoldParagraph;

        public BoldParagraphViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewBoldParagraph = itemView.findViewById(R.id.textViewBoldParagraph);
        }

        public void bind(ContentItem item) {
            textViewBoldParagraph.setText(item.getContent().getTextContent());
            textViewBoldParagraph.setTypeface(null, Typeface.BOLD);
        }
    }

    static class ListViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout linearLayoutListItems;

        public ListViewHolder(@NonNull View itemView) {
            super(itemView);
            linearLayoutListItems = itemView.findViewById(R.id.linearLayoutListItems);
        }

        public void bind(ContentItem item) {
            linearLayoutListItems.removeAllViews();

            String listItemsJson = item.getContent().getListItemsJson();
            if (listItemsJson != null) {
                Gson gson = new Gson();
                Type listType = new TypeToken<List<String>>(){}.getType();
                List<String> items = gson.fromJson(listItemsJson, listType);

                if (items != null) {
                    for (String listItemText : items) {
                        TextView textView = new TextView(itemView.getContext());
                        textView.setText("\u2022 " + listItemText);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                LinearLayout.LayoutParams.WRAP_CONTENT
                        );
                        int padding = (int) (itemView.getContext().getResources().getDisplayMetrics().density * 4);
                        textView.setPadding(padding * 2, padding, padding, padding);

                        linearLayoutListItems.addView(textView);
                    }
                }
            }
        }
    }


    static class ImageViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imageViewContent;
        private final TextView textViewImageAlt;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageViewContent = itemView.findViewById(R.id.imageViewContent);
            textViewImageAlt = itemView.findViewById(R.id.textViewImageAlt);
        }

        public void bind(ContentItem item, Context context) {
            String imageUrl = item.getContent().getImageSrc();
            String imageAlt = item.getContent().getImageAlt();

            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageViewContent.setVisibility(View.VISIBLE);
                Glide.with(context)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_xml_launcher_background)
                        .into(imageViewContent);
                textViewImageAlt.setText(imageAlt);
                textViewImageAlt.setVisibility(imageAlt != null && !imageAlt.isEmpty() ? View.VISIBLE : View.GONE);
            } else {
                imageViewContent.setVisibility(View.GONE);
                textViewImageAlt.setVisibility(View.GONE);
            }
        }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        public EmptyViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }
}