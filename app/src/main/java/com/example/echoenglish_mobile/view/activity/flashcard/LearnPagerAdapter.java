package com.example.echoenglish_mobile.view.activity.flashcard;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.ArrayList;
import java.util.List;

public class LearnPagerAdapter extends RecyclerView.Adapter<LearnPagerAdapter.LearnCardViewHolder> {

    private static final String TAG = "LearnPagerAdapter";
    private Context context;
    private List<VocabularyResponse> vocabularyList;
    private List<Boolean> isFlippedList;

    public LearnPagerAdapter(Context context, List<VocabularyResponse> vocabularyList) {
        this.context = context;
        this.vocabularyList = vocabularyList != null ? vocabularyList : new ArrayList<>();
        this.isFlippedList = new ArrayList<>(this.vocabularyList.size());
        for (int i = 0; i < this.vocabularyList.size(); i++) {
            isFlippedList.add(false);
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
        if (position < 0 || position >= vocabularyList.size() || position >= isFlippedList.size()) {
            Log.e(TAG, "Invalid position in onBindViewHolder: " + position + " Vocab Size: " + vocabularyList.size() + " Flipped Size: " + isFlippedList.size());
            holder.itemView.setVisibility(View.GONE);
            return;
        }
        holder.itemView.setVisibility(View.VISIBLE);

        VocabularyResponse vocab = vocabularyList.get(position);
        holder.bind(vocab, context);

        holder.cardContainer.setTag(false);

        boolean isFlipped = isFlippedList.get(position);
        holder.setFlipped(isFlipped);

        View.OnClickListener flipClickListener = v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition != RecyclerView.NO_POSITION && currentPosition < isFlippedList.size()) {
                Object tag = holder.cardContainer.getTag();
                Boolean isAnimating = (tag instanceof Boolean) ? (Boolean) tag : false;
                if (isAnimating) {
                    return;
                }
                boolean wasFlipped = isFlippedList.get(currentPosition);
                isFlippedList.set(currentPosition, !wasFlipped);
                flipCard(holder, !wasFlipped);
            }
        };

        holder.frontCardView.setOnClickListener(flipClickListener);
        holder.backCardView.setOnClickListener(flipClickListener);
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

    public void updateData(List<VocabularyResponse> newVocabularies) {
        this.vocabularyList.clear();
        this.isFlippedList.clear();
        if (newVocabularies != null) {
            this.vocabularyList.addAll(newVocabularies);
            for (int i = 0; i < this.vocabularyList.size(); i++) {
                isFlippedList.add(false);
            }
        }
        notifyDataSetChanged();
        Log.d(TAG, "Data updated. New size: " + vocabularyList.size());
    }


    private void flipCard(LearnCardViewHolder holder, boolean showBack) {
        holder.cardContainer.setTag(true);
        Log.d(TAG, "flipCard called at position " + holder.getAdapterPosition() + ". ShowBack: " + showBack);


        Animator outAnimator, inAnimator;
        View frontView = holder.frontCardView;
        View backView = holder.backCardView;


        if (showBack) {
            outAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_out);
            inAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_right_in);
        } else {
            outAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_out);
            inAnimator = AnimatorInflater.loadAnimator(context, R.animator.card_flip_left_in);
        }

        if (outAnimator == null || inAnimator == null) {
            Log.e(TAG, "Failed to load animators!");
            holder.cardContainer.setTag(false);
            holder.setFlipped(showBack);
            return;
        }


        outAnimator.setTarget(showBack ? frontView : backView);
        inAnimator.setTarget(showBack ? backView : frontView);

        float scale = context.getResources().getDisplayMetrics().density;
        frontView.setCameraDistance(8000 * scale);
        backView.setCameraDistance(8000 * scale);

        AnimatorListenerAdapter listener = new AnimatorListenerAdapter() {
            private boolean outAnimationEnded = false;
            private boolean inAnimationEnded = false;

            private void checkAndCleanup() {
                if (outAnimationEnded && inAnimationEnded) {
                    Log.d(TAG, "Both animations ended for position " + holder.getAdapterPosition() + ". ShowBack: " + showBack);

                    if (showBack) {
                        frontView.setVisibility(View.GONE);
                    } else {
                        backView.setVisibility(View.GONE);
                    }

                    frontView.setRotationY(0);
                    backView.setRotationY(0);

                    frontView.setAlpha(1f);
                    backView.setAlpha(1f);

                    holder.cardContainer.setTag(false);
                }
            }

            @Override
            public void onAnimationStart(Animator animation) {
                if (animation == inAnimator) {
                    Log.d(TAG, "In-Animation started. Making target view visible.");
                    if (showBack) backView.setVisibility(View.VISIBLE); else frontView.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                Log.d(TAG, "One animation ended for position " + holder.getAdapterPosition());
                if (animation == outAnimator) {
                    outAnimationEnded = true;
                } else if (animation == inAnimator) {
                    inAnimationEnded = true;
                }
                checkAndCleanup();
            }

            @Override
            public void onAnimationCancel(Animator animation) {
                Log.w(TAG, "Animation cancelled for position " + holder.getAdapterPosition());
                outAnimationEnded = true;
                inAnimationEnded = true;
                checkAndCleanup();
            }
        };

        outAnimator.addListener(listener);
        inAnimator.addListener(listener);

        outAnimator.start();
        inAnimator.start();
    }

    static class LearnCardViewHolder extends RecyclerView.ViewHolder {
        FrameLayout cardContainer;
        View frontCardView, backCardView;
        ImageView imageView;
        TextView wordText, phoneticText, typeText, definitionText, exampleText;

        LearnCardViewHolder(@NonNull View itemView) {
            super(itemView);
            cardContainer = itemView.findViewById(R.id.learnCardContainer);
            frontCardView = itemView.findViewById(R.id.viewLearnCardFront);
            backCardView = itemView.findViewById(R.id.viewLearnCardBack);

            imageView = itemView.findViewById(R.id.imageViewLearnCard);
            wordText = itemView.findViewById(R.id.textViewLearnCardWord);
            phoneticText = itemView.findViewById(R.id.textViewLearnCardPhonetic);
            typeText = itemView.findViewById(R.id.textViewLearnCardType);

            definitionText = itemView.findViewById(R.id.textViewLearnCardDefinition);
            exampleText = itemView.findViewById(R.id.textViewLearnCardExample);
        }

        void bind(VocabularyResponse vocab, Context context) {
            wordText.setText(vocab.getWord());
            phoneticText.setText(vocab.getPhonetic() != null ? vocab.getPhonetic() : "");
            phoneticText.setVisibility(vocab.getPhonetic() != null ? View.VISIBLE : View.GONE);
            typeText.setText(vocab.getType() != null ? ("("+vocab.getType()+")") : "");
            typeText.setVisibility(vocab.getType() != null ? View.VISIBLE : View.GONE);

            if (imageView != null && vocab.getImageUrl() != null && !vocab.getImageUrl().isEmpty()) {
                Glide.with(context)
                        .load(vocab.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(imageView);
                imageView.setVisibility(View.VISIBLE);
            } else if (imageView != null) {
                imageView.setVisibility(View.GONE);
            }

            definitionText.setText(vocab.getDefinition());
            exampleText.setText(vocab.getExample() != null ? "Ví dụ: " + vocab.getExample() : "");
            exampleText.setVisibility(vocab.getExample() != null ? View.VISIBLE : View.GONE);
        }

        void setFlipped(boolean isFlipped) {
            frontCardView.setRotationY(0);
            backCardView.setRotationY(0);
            frontCardView.setAlpha(1f);
            backCardView.setAlpha(1f);

            frontCardView.setVisibility(isFlipped ? View.GONE : View.VISIBLE);
            backCardView.setVisibility(isFlipped ? View.VISIBLE : View.GONE);
            Log.d("SetFlipped", "Position " + getAdapterPosition() + " set to flipped: " + isFlipped);

        }
    }
}