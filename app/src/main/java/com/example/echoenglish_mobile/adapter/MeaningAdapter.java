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

    public MeaningAdapter(List<Meaning> meaningList) {
        this.meaningList = meaningList;
    }

    public void updateData(List<Meaning> newMeaningList) {
        this.meaningList = newMeaningList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MeaningViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dictionary_meaning, parent, false);
        return new MeaningViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MeaningViewHolder holder, int position) {
        Meaning meaning = meaningList.get(position);
        holder.bind(meaning);
    }

    @Override
    public int getItemCount() {
        return meaningList == null ? 0 : meaningList.size();
    }

    static class MeaningViewHolder extends RecyclerView.ViewHolder {
        TextView tvPartOfSpeech;
        TextView tvLevel;
        TextView tvDefinition;
        TextView tvExample;
        LinearLayout layoutExample;

        MeaningViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPartOfSpeech = itemView.findViewById(R.id.tvPartOfSpeech);
            tvLevel = itemView.findViewById(R.id.tvLevel);
            tvDefinition = itemView.findViewById(R.id.tvDefinition);
            tvExample = itemView.findViewById(R.id.tvExample);
            layoutExample = itemView.findViewById(R.id.layoutExample);
        }

        void bind(Meaning meaning) {
            tvPartOfSpeech.setText(meaning.getPartOfSpeech());
            tvDefinition.setText(meaning.getDefinition());

            if (!TextUtils.isEmpty(meaning.getLevel())) {
                tvLevel.setText(meaning.getLevel());
                tvLevel.setVisibility(View.VISIBLE);
            } else {
                tvLevel.setVisibility(View.GONE);
            }

            if (!TextUtils.isEmpty(meaning.getExample())) {
                tvExample.setText(meaning.getExample());
                layoutExample.setVisibility(View.VISIBLE);
            } else {
                layoutExample.setVisibility(View.GONE);
            }
        }
    }
}
