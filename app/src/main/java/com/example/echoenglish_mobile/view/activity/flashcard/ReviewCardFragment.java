package com.example.echoenglish_mobile.view.activity.flashcard;

import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;
import com.google.android.material.card.MaterialCardView;

import java.util.Locale;

public class ReviewCardFragment extends Fragment {

    private static final String TAG = "ReviewCardFragment";
    private static final String ARG_VOCABULARY = "vocabulary";

    private VocabularyReviewResponse vocabulary;

    private MaterialCardView cardReviewFront;
    private MaterialCardView cardReviewBack;

    private ImageView imageReviewVocabulary;
    private TextView textReviewWord;
    private TextView textReviewPhonetic;
    private TextView textReviewMemoryLevel;
    private TextView textReviewFlashcardInfo;

    private TextView textReviewDefinition;
    private TextView textReviewExample;

    private boolean isFront = true;

    private ObjectAnimator flipRightIn;
    private ObjectAnimator flipRightOut;
    private ObjectAnimator flipLeftIn;
    private ObjectAnimator flipLeftOut;

    private final Handler handler = new Handler(Looper.getMainLooper());


    public static ReviewCardFragment newInstance(VocabularyReviewResponse vocabulary) {
        ReviewCardFragment fragment = new ReviewCardFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_VOCABULARY, vocabulary);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            vocabulary = (VocabularyReviewResponse) getArguments().getSerializable(ARG_VOCABULARY);
        }
        loadFlipAnimations();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.item_review_card, container, false);

        cardReviewFront = view.findViewById(R.id.cardReviewFront);
        cardReviewBack = view.findViewById(R.id.cardReviewBack);

        imageReviewVocabulary = cardReviewFront.findViewById(R.id.imageReviewVocabulary);
        textReviewWord = cardReviewFront.findViewById(R.id.textReviewWord);
        textReviewPhonetic = cardReviewFront.findViewById(R.id.textReviewPhonetic);
        textReviewMemoryLevel = cardReviewFront.findViewById(R.id.textReviewMemoryLevel);
        textReviewFlashcardInfo = cardReviewFront.findViewById(R.id.textReviewFlashcardInfo);

        textReviewDefinition = cardReviewBack.findViewById(R.id.textReviewDefinition);
        textReviewExample = cardReviewBack.findViewById(R.id.textReviewExample);

        cardReviewFront.setOnClickListener(v -> flipCard());
        cardReviewBack.setOnClickListener(v -> flipCard());


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        float scale = getResources().getDisplayMetrics().density;
        float cameraDist = getResources().getDisplayMetrics().density * getResources().getDimension(R.dimen.camera_distance);
        view.setCameraDistance(cameraDist);

        displayVocabulary();

        isFront = true;
        cardReviewFront.setVisibility(View.VISIBLE);
        cardReviewFront.setRotationY(0);
        cardReviewBack.setVisibility(View.GONE);
        cardReviewBack.setRotationY(180);
    }

    private void displayVocabulary() {
        if (vocabulary == null) {
            Log.e(TAG, "Vocabulary data is null!");
            textReviewWord.setText("Error loading word");
            textReviewDefinition.setText("Data unavailable.");
            imageReviewVocabulary.setVisibility(View.GONE);
            textReviewPhonetic.setVisibility(View.GONE);
            textReviewExample.setVisibility(View.GONE);
            textReviewMemoryLevel.setVisibility(View.GONE);
            if (textReviewFlashcardInfo != null) textReviewFlashcardInfo.setVisibility(View.GONE);
            cardReviewFront.setClickable(false);
            cardReviewBack.setClickable(false);
            return;
        }

        textReviewWord.setText(vocabulary.getWord());
        if (vocabulary.getPhonetic() != null && !vocabulary.getPhonetic().isEmpty()) {
            textReviewPhonetic.setText(vocabulary.getPhonetic());
            textReviewPhonetic.setVisibility(View.VISIBLE);
        } else {
            textReviewPhonetic.setVisibility(View.GONE);
        }

        if (vocabulary.getImageUrl() != null && !vocabulary.getImageUrl().isEmpty()) {
            imageReviewVocabulary.setVisibility(View.VISIBLE);
            if (getContext() != null) {
                Glide.with(getContext())
                        .load(vocabulary.getImageUrl())
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(imageReviewVocabulary);
            }
        } else {
            imageReviewVocabulary.setVisibility(View.GONE);
            if (getContext() != null) {
                Glide.with(getContext()).clear(imageReviewVocabulary);
            }
        }

        textReviewMemoryLevel.setText(String.format(Locale.getDefault(), "Memory Level: Level %d", vocabulary.getRememberCount()));

        if (textReviewFlashcardInfo != null) {
            textReviewFlashcardInfo.setVisibility(View.GONE);
        }

        textReviewDefinition.setText(vocabulary.getDefinition());
        if (vocabulary.getExample() != null && !vocabulary.getExample().isEmpty()) {
            textReviewExample.setText(vocabulary.getExample());
            textReviewExample.setVisibility(View.VISIBLE);
        } else {
            textReviewExample.setVisibility(View.GONE);
        }

        cardReviewFront.setClickable(true);
        cardReviewBack.setClickable(true);
    }

    private void loadFlipAnimations() {
        if (getContext() == null) {
            Log.w(TAG, "Context is null, cannot load animations.");
            return;
        }
        try {
            flipRightOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_out);
            flipRightIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_in);
            flipLeftOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_out);
            flipLeftIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_in);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load flip animations", e);
            disableFlip();
        }
    }

    private void flipCard() {
        if (flipRightIn == null || flipRightOut == null || flipLeftIn == null || flipLeftOut == null) {
            Log.w(TAG, "Flip attempted but animations not loaded.");
            disableFlip();
            return;
        }

        cardReviewFront.setClickable(false);
        cardReviewBack.setClickable(false);

        final View viewToFlipOut = isFront ? cardReviewFront : cardReviewBack;
        final View viewToFlipIn = isFront ? cardReviewBack : cardReviewFront;

        ObjectAnimator animatorOut;
        ObjectAnimator animatorIn;

        if (isFront) {
            animatorOut = flipRightOut;
            animatorIn = flipRightIn;
        } else {
            animatorOut = flipRightIn;
            animatorIn = flipLeftIn;
        }

        animatorOut.setTarget(viewToFlipOut);
        animatorIn.setTarget(viewToFlipIn);

        animatorOut.start();
        animatorIn.start();

        int duration = getResources().getInteger(R.integer.card_flip_time_full);

        handler.postDelayed(() -> {
            if (isFront) {
                cardReviewFront.setVisibility(View.GONE);
                cardReviewBack.setVisibility(View.VISIBLE);
            } else {
                cardReviewBack.setVisibility(View.GONE);
                cardReviewFront.setVisibility(View.VISIBLE);
            }
            isFront = !isFront;
            cardReviewFront.setClickable(true);
            cardReviewBack.setClickable(true);
        }, duration);
    }


    private void disableFlip() {
        if (cardReviewFront != null) cardReviewFront.setClickable(false);
        if (cardReviewBack != null) cardReviewBack.setClickable(false);
        Log.w(TAG, "Card flip is disabled.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}