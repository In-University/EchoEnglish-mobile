package com.example.echoenglish_mobile.view.activity.flashcard;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;

public class LearnPagerAdapter extends RecyclerView.Adapter<LearnPagerAdapter.LearnCardViewHolder> {

    private Context context;
    private List<VocabularyResponse> vocabularyList;
    private List<Boolean> isFlippedList; // Track flipped state for each card

    public LearnPagerAdapter(Context context, List<VocabularyResponse> vocabularyList) {
        this.context = context;
        this.vocabularyList = vocabularyList != null ? vocabularyList : new ArrayList<>();
        this.isFlippedList = new ArrayList<>(this.vocabularyList.size());
        for (int i = 0; i < this.vocabularyList.size(); i++) {
            isFlippedList.add(false); // Initially not flipped
        }
    }

    @NonNull
    @Override
    public LearnCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_learn_card, parent, false);
        return new LearnCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LearnCardViewHolder holder, int position) {
        VocabularyResponse vocab = vocabularyList.get(position);
        holder.bind(vocab);

        // Set initial flip state based on tracked list
        boolean isFlipped = isFlippedList.get(position);
        holder.setFlipped(isFlipped);

        // Flip Animation Logic
        holder.cardContainer.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION) {
                boolean wasFlipped = isFlippedList.get(currentPosition);
                flipCard(holder, !wasFlipped); // Flip to the opposite state
                isFlippedList.set(currentPosition, !wasFlipped); // Update tracked state
            }
        });
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

    public VocabularyResponse getItem(int position) {
        if (position >= 0 && position < vocabularyList.size()) {
            return vocabularyList.get(position);
        }
        return null;
    }

    // Helper method for animation
    private void flipCard(LearnCardViewHolder holder, boolean showBack) {
        AnimatorSet outAnimator, inAnimator;
        View frontView = holder.frontView;
        View backView = holder.backView;

        if (showBack) {
            outAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_out);
            inAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in);
            outAnimator.setTarget(frontView);
            inAnimator.setTarget(backView);
        } else {
            outAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
            inAnimator = (AnimatorSet) AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);
            outAnimator.setTarget(backView);
            inAnimator.setTarget(frontView);
        }

        // Improve visual perspective
        float scale = context.getResources().getDisplayMetrics().density;
        frontView.setCameraDistance(8000 * scale);
        backView.setCameraDistance(8000 * scale);

        outAnimator.start();
        inAnimator.start();

        // Manage visibility after animation starts (or use listeners for precision)
        holder.setFlipped(showBack); // Update visibility in ViewHolder method

    }

    // ViewHolder Class
    static class LearnCardViewHolder extends RecyclerView.ViewHolder {
        FrameLayout cardContainer;
        LinearLayout frontView, backView;
        TextView wordText, phoneticText, definitionText, exampleText;

        LearnCardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.learnCardContainer);
            frontView = itemView.findViewById(R.id.viewLearnCardFront);
            backView = itemView.findViewById(R.id.viewLearnCardBack);
            wordText = itemView.findViewById(R.id.textViewLearnCardWord);
            phoneticText = itemView.findViewById(R.id.textViewLearnCardPhonetic);
            definitionText = itemView.findViewById(R.id.textViewLearnCardDefinition);
            exampleText = itemView.findViewById(R.id.textViewLearnCardExample);
        }

        void bind(VocabularyResponse vocab) {
            wordText.setText(vocab.getWord());
            phoneticText.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
            definitionText.setText(vocab.getDefinition());
            exampleText.setText(vocab.getExample() != null ? "Ví dụ: " + vocab.getExample() : "");
            exampleText.setVisibility(vocab.getExample() != null ? View.VISIBLE : View.GONE);
        }

        // Helper to set visibility and initial rotation based on flipped state
        void setFlipped(boolean isFlipped) {
            if(isFlipped) {
                frontView.setVisibility(View.GONE);
                backView.setVisibility(View.VISIBLE);
                backView.setRotationY(0); // Ensure back is visible correctly
                frontView.setRotationY(180); // Ensure front is hidden correctly
            } else {
                frontView.setVisibility(View.VISIBLE);
                backView.setVisibility(View.GONE);
                frontView.setRotationY(0);
                backView.setRotationY(-180); // // Set rotation for back when hidden
            }
        }
    }
}