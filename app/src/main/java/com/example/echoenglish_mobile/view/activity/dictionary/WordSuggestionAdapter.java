package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Meaning;
import com.example.echoenglish_mobile.model.Word;
import java.util.ArrayList;
import java.util.List;

public class WordSuggestionAdapter extends RecyclerView.Adapter<WordSuggestionAdapter.ViewHolder> {

    private List<Word> suggestions;
    private Context context;
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Word word);
        void onDeleteHistoryClick(String wordToDelete);
        void onArrowClick(String wordText);
    }

    public WordSuggestionAdapter(Context context, List<Word> suggestions, OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
        this.listener = listener;
    }

    public void updateData(List<Word> newSuggestions) {
        this.suggestions.clear();
        if (newSuggestions != null) {
            this.suggestions.addAll(newSuggestions);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_dictionary_word_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        int currentPosition = holder.getAdapterPosition();
        if (currentPosition == RecyclerView.NO_POSITION) {
            return;
        }
        Word word = suggestions.get(currentPosition);

        holder.tvSuggestedWord.setText(word.getWord());

        String ukPron = word.getUkPronunciation();
        if (!TextUtils.isEmpty(ukPron)) {
            holder.tvSuggestedUkPronunciation.setText("/" + ukPron + "/");
            holder.tvSuggestedUkPronunciation.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestedUkPronunciation.setVisibility(View.GONE);
        }

        String firstPos = getFirstPartOfSpeech(word);
        if (!TextUtils.isEmpty(firstPos)) {
            holder.tvSuggestedPos.setText("(" + firstPos + ")");
            holder.tvSuggestedPos.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestedPos.setVisibility(View.GONE);
        }

        if (word.isFromHistory()) {
            holder.ivDeleteHistory.setVisibility(View.VISIBLE);
            holder.ivArrow.setVisibility(View.VISIBLE);
            holder.ivDeleteHistory.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteHistoryClick(word.getWord());
                }
            });
        } else {
            holder.ivDeleteHistory.setVisibility(View.GONE);
            holder.ivArrow.setVisibility(View.GONE);
        }

        holder.ivArrow.setOnClickListener(v -> {
            if (listener != null) {
                listener.onArrowClick(word.getWord());
            }
        });

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onSuggestionClick(word);
            }
        });

        if (!word.isFromHistory()) {
            holder.ivDeleteHistory.setOnClickListener(null);
        }
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    private String getFirstPartOfSpeech(Word word) {
        if (word == null) return null;
        if (word.getMeanings() != null && !word.getMeanings().isEmpty()) {
            Meaning firstMeaning = word.getMeanings().get(0);
            if (firstMeaning != null && !TextUtils.isEmpty(firstMeaning.getPartOfSpeech())) {
                String pos = firstMeaning.getPartOfSpeech();
                if ("N/A".equalsIgnoreCase(pos) || TextUtils.isEmpty(pos.trim())) {
                    return null;
                }
                return pos;
            }
        }
        return null;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestedWord;
        TextView tvSuggestedUkPronunciation;
        TextView tvSuggestedPos;
        ImageView ivDeleteHistory;
        ImageView ivArrow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestedWord = itemView.findViewById(R.id.tvSuggestedWord);
            tvSuggestedUkPronunciation = itemView.findViewById(R.id.tvSuggestedUkPronunciation);
            tvSuggestedPos = itemView.findViewById(R.id.tvSuggestedPos);
            ivDeleteHistory = itemView.findViewById(R.id.ivDeleteHistory);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}