package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
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

    public WordSuggestionAdapter(Context context, List<Word> suggestions) {
        this.context = context;
        this.suggestions = suggestions != null ? suggestions : new ArrayList<>();
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
        // Ensure you are using the correct layout file name here
        View view = LayoutInflater.from(context).inflate(R.layout.item_dictionary_word_suggestion, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Word word = suggestions.get(position);

        // --- Bind Data ---
        holder.tvSuggestedWord.setText(word.getWord());

        // Bind UK Pronunciation
        boolean hasPronunciation = !TextUtils.isEmpty(word.getUkPronunciation());
        if (hasPronunciation) {
            // Thêm dấu gạch chéo nếu muốn
            holder.tvSuggestedUkPronunciation.setText("/" + word.getUkPronunciation() + "/");
            holder.tvSuggestedUkPronunciation.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestedUkPronunciation.setVisibility(View.GONE);
        }

        // Bind First Part of Speech
        String firstPos = getFirstPartOfSpeech(word);
        boolean hasPos = !TextUtils.isEmpty(firstPos);
        if (hasPos) {
            // Để dạng viết hoa cho đẹp hơn
            holder.tvSuggestedPos.setText(firstPos);
            holder.tvSuggestedPos.setVisibility(View.VISIBLE);
        } else {
            holder.tvSuggestedPos.setVisibility(View.GONE);
        }

        // Không cần đặt visibility cho LinearLayout cha nữa,
        // vì nếu cả hai con là GONE, LinearLayout sẽ tự co lại.

        // --- Handle Click ---
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, DictionaryWordDetailActivity.class);
            intent.putExtra("word_data", word);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return suggestions.size();
    }

    private String getFirstPartOfSpeech(Word word) {
        if (word.getMeanings() != null && !word.getMeanings().isEmpty()) {
            Meaning firstMeaning = word.getMeanings().get(0);
            if (firstMeaning != null && !TextUtils.isEmpty(firstMeaning.getPartOfSpeech())) {
                // Trả về loại từ, bỏ qua các ký tự như 'N/A' nếu API có thể trả về
                String pos = firstMeaning.getPartOfSpeech();
                return "N/A".equalsIgnoreCase(pos) ? null : pos;
            }
        }
        return null;
    }

    // --- ViewHolder ---
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvSuggestedWord;
        TextView tvSuggestedUkPronunciation;
        TextView tvSuggestedPos;
        ImageView ivArrow;
        // Không cần tham chiếu đến LinearLayout cha nếu không thao tác trực tiếp

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSuggestedWord = itemView.findViewById(R.id.tvSuggestedWord);
            tvSuggestedUkPronunciation = itemView.findViewById(R.id.tvSuggestedUkPronunciation);
            tvSuggestedPos = itemView.findViewById(R.id.tvSuggestedPos);
            ivArrow = itemView.findViewById(R.id.ivArrow);
        }
    }
}