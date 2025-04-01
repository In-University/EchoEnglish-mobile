package com.example.echoenglish_mobile.ui.flashcard.wordlist;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.WordModel;

import java.util.ArrayList;

public class WordListAdapter extends RecyclerView.Adapter<WordListAdapter.ItemViewHolder> {

    private ArrayList<WordModel> wordModelArrayList;
    private Context context;

    public WordListAdapter(ArrayList<WordModel> wordModelArrayList, Context context) {
        this.wordModelArrayList = wordModelArrayList;
        this.context = context;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_word_card, parent, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        WordModel item = wordModelArrayList.get(position);
        holder.bind(item);
    }

    @Override
    public int getItemCount() {
        return wordModelArrayList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitle = itemView.findViewById(R.id.textTitle);
        }

        public void bind(WordModel item) {
            textTitle.setText(item.getWord());
        }
    }
}
