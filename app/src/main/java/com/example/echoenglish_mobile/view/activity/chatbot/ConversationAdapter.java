package com.example.echoenglish_mobile.view.activity.chatbot;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R; // Ensure R is imported correctly

import java.util.List;

public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ScenarioViewHolder> {

    private final List<ConversationScenario> scenarios;
    private final OnScenarioClickListener listener;
    private final Context context;
    private final String categoryColorHex;

    public interface OnScenarioClickListener {
        void onScenarioClick(ConversationScenario scenario);
    }

    public ConversationAdapter(Context context, List<ConversationScenario> scenarios, String categoryColorHex, OnScenarioClickListener listener) {
        this.context = context;
        this.scenarios = scenarios;
        this.categoryColorHex = categoryColorHex;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ScenarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation_card, parent, false);
        return new ScenarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ScenarioViewHolder holder, int position) {
        holder.bind(scenarios.get(position), categoryColorHex, listener, context);
    }

    @Override
    public int getItemCount() {
        return scenarios.size();
    }

    static class ScenarioViewHolder extends RecyclerView.ViewHolder {
        private final View viewColorAccent;
        private final ImageView ivScenarioIcon;
        private final TextView tvScenarioTitle;
        private final TextView tvScenarioDescription;
        private final TextView tvDifficulty;
        private final TextView tvDuration;
        private final TextView btnStartConversation;

        public ScenarioViewHolder(@NonNull View itemView) {
            super(itemView);
            viewColorAccent = itemView.findViewById(R.id.viewColorAccent);
            ivScenarioIcon = itemView.findViewById(R.id.ivScenarioIcon);
            tvScenarioTitle = itemView.findViewById(R.id.tvScenarioTitle);
            tvScenarioDescription = itemView.findViewById(R.id.tvScenarioDescription);
            tvDifficulty = itemView.findViewById(R.id.tvDifficulty);
            tvDuration = itemView.findViewById(R.id.tvDuration);
            btnStartConversation = itemView.findViewById(R.id.btnStartConversation);
        }

        public void bind(final ConversationScenario scenario, final String colorHex, final OnScenarioClickListener listener, Context context) {
            tvScenarioTitle.setText(scenario.getTitle());
            tvScenarioDescription.setText(scenario.getDescription());
            tvDifficulty.setText(scenario.getDifficulty());
            tvDuration.setText(scenario.getDuration());

            int color = Color.parseColor(colorHex);

            viewColorAccent.setBackgroundColor(color);
            ivScenarioIcon.setColorFilter(color);

            int iconResId = context.getResources().getIdentifier(scenario.getIconName(), "drawable", context.getPackageName());
            if (iconResId != 0) {
                ivScenarioIcon.setImageResource(iconResId);
            } else {
                ivScenarioIcon.setImageResource(R.drawable.ic_attachment); // Fallback icon
                Log.w("ConvAdapter", "Icon not found: " + scenario.getIconName());
            }

            // Style Tags (Difficulty & Duration) - Using XML colors for closer match
            // If you prefer dynamic coloring based on categoryColorHex, uncomment below
            /*
            int lighterColor = getLighterColor(color); // Helper function needed for this
            tvDifficulty.setBackgroundColor(lighterColor);
            tvDifficulty.setTextColor(color);
            tvDuration.setBackgroundColor(lighterColor);
            tvDuration.setTextColor(color);
            */

            // Use XML defined colors for tags for now, they look good
            tvDifficulty.setTextColor(Color.parseColor("#4F46E5"));
            tvDifficulty.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEF2FF")));
            tvDuration.setTextColor(Color.parseColor("#4F46E5"));
            tvDuration.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#EEF2FF")));

            // Use XML defined button color
            btnStartConversation.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#3730A3")));


            // --- Set click listener ONLY on the "Start" button ---
            btnStartConversation.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onScenarioClick(scenario);
                }
            });
        }
        // Optional: Helper to generate lighter background for tags if needed
        // private int getLighterColor(int color) {
        //     float[] hsv = new float[3];
        //     Color.colorToHSV(color, hsv);
        //     hsv[1] *= 0.2f; // Reduce saturation
        //     hsv[2] = 0.95f; // Set brightness high
        //     return Color.HSVToColor(hsv);
        // }
    }
}