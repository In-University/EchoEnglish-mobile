package com.example.echoenglish_mobile.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Meaning;


import java.util.List;

public class MeaningAdapter extends RecyclerView.Adapter<MeaningAdapter.MeaningViewHolder> {

    private List<Meaning> meaningList;

    // Constructor to receive the data
    public MeaningAdapter(List<Meaning> meaningList) {
        this.meaningList = meaningList;
    }

    // Method to update the data list and refresh the RecyclerView
    public void updateData(List<Meaning> newMeaningList) {
        this.meaningList = newMeaningList;
        notifyDataSetChanged(); // Notify the adapter that the data has changed
    }

    @NonNull
    @Override
    public MeaningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout using the context from the parent ViewGroup
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary_meaning, parent, false);
        return new MeaningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeaningViewHolder holder, int position) {
        // Get the data item for the current position
        Meaning meaning = meaningList.get(position);
        // Bind the data to the ViewHolder
        holder.bind(meaning);
    }

    @Override
    public int getItemCount() {
        // Return the total number of items in the list
        return meaningList == null ? 0 : meaningList.size();
    }

    // --- ViewHolder Inner Class ---
    // It can remain static as it doesn't need access to non-static members of MeaningAdapter
    // Or you can make it non-static if preferred.
    static class MeaningViewHolder extends RecyclerView.ViewHolder {
        TextView tvPartOfSpeech;
        TextView tvLevel;
        TextView tvDefinition;
        TextView tvExample;
        LinearLayout layoutExample; // Container for example text

        MeaningViewHolder(@NonNull View itemView) {
            super(itemView);
            // Find views within the item layout
            tvPartOfSpeech = itemView.findViewById(R.id.tvPartOfSpeech);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            tvExample = itemView.findViewById(R.id.tvExample);
            layoutExample = itemView.findViewById(R.id.layoutExample);
        }

        // Method to bind Meaning data to the views in the ViewHolder
        void bind(Meaning meaning) {
            tvPartOfSpeech.setText(meaning.getPartOfSpeech());
            tvDefinition.setText(meaning.getDefinition());

            // Handle optional Level visibility
            if (!TextUtils.isEmpty(meaning.getLevel())) {
                tvLevel.setText(meaning.getLevel());
                tvLevel.setVisibility(View.VISIBLE);
            } else {
                tvLevel.setVisibility(View.GONE);
            }

            // Handle optional Example visibility
            if (!TextUtils.isEmpty(meaning.getExample())) {
                tvExample.setText(meaning.getExample());
                layoutExample.setVisibility(View.VISIBLE);
            } else {
                layoutExample.setVisibility(View.GONE);
            }
        }
    }
}
