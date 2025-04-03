package com.example.echoenglish_mobile.ui.pronun_summary_result;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.PhonemeStats;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ProgressChartAdapter extends RecyclerView.Adapter<ProgressChartAdapter.ViewHolder> {

    private List<PhonemeStats> itemList;
    private Context context;

    public ProgressChartAdapter(Context context, List<PhonemeStats> itemList) {
        this.context = context;
        itemList.sort(Comparator.comparingDouble(PhonemeStats::getPercentage));
        this.itemList = itemList.size() > 5 ? itemList.subList(0, 5) : itemList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_progress_bar, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PhonemeStats currentItem = itemList.get(position);

        holder.textViewLabel.setText("/" + currentItem.getPhoneme() + "/");
        holder.textViewPercentage.setText(String.format(Locale.getDefault(), "%d%%", currentItem.getPercentage()));

        holder.progressBar.setProgress(0);
        holder.progressBar.post(() -> {
            ObjectAnimator animation = ObjectAnimator.ofInt(
                    holder.progressBar,          // View cần animate
                    "progress",                  // Thuộc tính cần animate ("progress" của ProgressBar)
                    currentItem.getPercentage()  // Giá trị đích
            );
            animation.setDuration(1000);
            animation.setInterpolator(new DecelerateInterpolator()); // Hiệu ứng chạy chậm dần
            animation.start();
        });


    }

    @Override
    public int getItemCount() {
        return itemList == null ? 0 : itemList.size();
    }

    public void updateData(List<PhonemeStats> newItemList) {
        this.itemList = newItemList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewLabel;
        TextView textViewPercentage;
        ProgressBar progressBar;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewLabel = itemView.findViewById(R.id.textViewLabel);
            textViewPercentage = itemView.findViewById(R.id.textViewPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }
}