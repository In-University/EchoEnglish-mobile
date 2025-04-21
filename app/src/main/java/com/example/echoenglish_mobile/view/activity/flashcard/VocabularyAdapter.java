package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;

public class VocabularyAdapter extends RecyclerView.Adapter<VocabularyAdapter.VocabularyViewHolder> {

    private Context context;
    private List<VocabularyResponse> vocabularyList;
    private OnVocabularyDeleteClickListener deleteListener;

    public interface OnVocabularyDeleteClickListener {
        void onVocabularyDeleteClick(VocabularyResponse vocabulary, int position);
    }

    public VocabularyAdapter(Context context, List<VocabularyResponse> vocabularyList, OnVocabularyDeleteClickListener deleteListener) {
        this.context = context;
        this.vocabularyList = new ArrayList<>(vocabularyList); // đảm bảo danh sách có thể thay đổi
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public VocabularyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_vocabulary, parent, false);
        return new VocabularyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VocabularyViewHolder holder, int position) {
        VocabularyResponse vocab = vocabularyList.get(position);

        holder.textViewWord.setText(vocab.getWord());
        holder.textViewDefinition.setText(vocab.getDefinition());

        holder.textViewPhonetic.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
        holder.textViewPhonetic.setVisibility(vocab.getPhonetic() != null ? View.VISIBLE : View.GONE);

        holder.textViewExample.setText(vocab.getExample() != null ? "Ví dụ: " + vocab.getExample() : "");
        holder.textViewExample.setVisibility(vocab.getExample() != null ? View.VISIBLE : View.GONE);

        // Xử lý sự kiện xoá
        holder.buttonDelete.setOnClickListener(v -> {
            if (deleteListener != null) {
                deleteListener.onVocabularyDeleteClick(vocab, holder.getAdapterPosition());
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList == null ? 0 : vocabularyList.size();
    }

    public void updateData(List<VocabularyResponse> newVocabularies) {
        this.vocabularyList.clear();
        if (newVocabularies != null) {
            this.vocabularyList.addAll(newVocabularies);
        }
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < vocabularyList.size()) {
            vocabularyList.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, vocabularyList.size());
        }
    }

    static class VocabularyViewHolder extends RecyclerView.ViewHolder {
        TextView textViewWord, textViewPhonetic, textViewDefinition, textViewExample;
        ImageButton buttonDelete;

        VocabularyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewWord = itemView.findViewById(R.id.textViewWord);
            textViewPhonetic = itemView.findViewById(R.id.textViewPhonetic);
            textViewDefinition = itemView.findViewById(R.id.textViewDefinition);
            textViewExample = itemView.findViewById(R.id.textViewExample);
            buttonDelete = itemView.findViewById(R.id.buttonDeleteVocabulary);
        }
    }
}
