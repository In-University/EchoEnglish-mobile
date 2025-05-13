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

    // Animators for flipping
    private ObjectAnimator flipRightIn;
    private ObjectAnimator flipRightOut;
    private ObjectAnimator flipLeftIn;
    private ObjectAnimator flipLeftOut;

    private final Handler handler = new Handler(Looper.getMainLooper()); // Handler for postDelayed


    public static ReviewCardFragment newInstance(VocabularyReviewResponse vocabulary) {
        ReviewCardFragment fragment = new ReviewCardFragment();
        Bundle args = new Bundle();
        // VocabularyReviewResponse must be Serializable
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
        // Load flip animations here
        loadFlipAnimations();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the card layout
        View view = inflater.inflate(R.layout.item_review_card, container, false);

        // Find views from the inflated layout
        cardReviewFront = view.findViewById(R.id.cardReviewFront);
        cardReviewBack = view.findViewById(R.id.cardReviewBack);

        // Find views on the FRONT card (within cardReviewFront)
        imageReviewVocabulary = cardReviewFront.findViewById(R.id.imageReviewVocabulary);
        textReviewWord = cardReviewFront.findViewById(R.id.textReviewWord);
        textReviewPhonetic = cardReviewFront.findViewById(R.id.textReviewPhonetic);
        textReviewMemoryLevel = cardReviewFront.findViewById(R.id.textReviewMemoryLevel);
        textReviewFlashcardInfo = cardReviewFront.findViewById(R.id.textReviewFlashcardInfo);

        // Find views on the BACK card (within cardReviewBack)
        textReviewDefinition = cardReviewBack.findViewById(R.id.textReviewDefinition);
        textReviewExample = cardReviewBack.findViewById(R.id.textReviewExample);

        // Set tap listener on the cards
        cardReviewFront.setOnClickListener(v -> flipCard());
        cardReviewBack.setOnClickListener(v -> flipCard());


        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        // Set camera distance on the root view of the item
        float scale = getResources().getDisplayMetrics().density;
        float cameraDist = getResources().getDisplayMetrics().density * getResources().getDimension(R.dimen.camera_distance);
        view.setCameraDistance(cameraDist);

        // Display data after view is created
        displayVocabulary();

        // Set initial state (front visible, back hidden and rotated)
        isFront = true;
        cardReviewFront.setVisibility(View.VISIBLE);
        cardReviewFront.setRotationY(0);
        cardReviewBack.setVisibility(View.GONE);
        cardReviewBack.setRotationY(180); // Ensure back is rotated for 'flip in' animation
    }

    private void displayVocabulary() {
        if (vocabulary == null) {
            Log.e(TAG, "Vocabulary data is null!");
            // Display error state on the card
            textReviewWord.setText("Error loading word");
            textReviewDefinition.setText("Data unavailable.");
            // Hide other views
            imageReviewVocabulary.setVisibility(View.GONE);
            textReviewPhonetic.setVisibility(View.GONE);
            textReviewExample.setVisibility(View.GONE);
            textReviewMemoryLevel.setVisibility(View.GONE);
            if (textReviewFlashcardInfo != null) textReviewFlashcardInfo.setVisibility(View.GONE);
            // Make cards not clickable
            cardReviewFront.setClickable(false);
            cardReviewBack.setClickable(false);
            return;
        }

        // Update UI for the FRONT card
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
                Glide.with(getContext()).clear(imageReviewVocabulary); // Clear previous image
            }
        }

        textReviewMemoryLevel.setText(String.format(Locale.getDefault(), "Memory Level: Level %d", vocabulary.getRememberCount()));

        // Ensure this view is always hidden if not used
        if (textReviewFlashcardInfo != null) {
            // Assuming VocabularyReviewResponse does NOT have getFlashcardName
            textReviewFlashcardInfo.setVisibility(View.GONE);
        }


        // Update UI for the BACK card
        textReviewDefinition.setText(vocabulary.getDefinition());
        if (vocabulary.getExample() != null && !vocabulary.getExample().isEmpty()) {
            textReviewExample.setText(vocabulary.getExample());
            textReviewExample.setVisibility(View.VISIBLE);
        } else {
            textReviewExample.setVisibility(View.GONE);
        }

        // Ensure cards are clickable if data is valid
        cardReviewFront.setClickable(true);
        cardReviewBack.setClickable(true);
    }

    private void loadFlipAnimations() {
        if (getContext() == null) {
            Log.w(TAG, "Context is null, cannot load animations.");
            return;
        }
        try {
            // Load the specific ObjectAnimators from res/animator
            // Requires @animator/card_flip_right_out, @animator/card_flip_right_in, etc.
            // Requires @integer/card_flip_time_full resource used within the animator XMLs
            flipRightOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_out);
            flipRightIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_right_in);
            flipLeftOut = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_out);
            flipLeftIn = (ObjectAnimator) AnimatorInflater.loadAnimator(getContext(), R.animator.card_flip_left_in);
        } catch (Exception e) {
            Log.e(TAG, "Failed to load flip animations", e);
            // Disable flip if animations fail to load
            disableFlip();
        }
    }

    private void flipCard() {
        // Ensure animations are loaded and cards are clickable
        if (flipRightIn == null || flipRightOut == null || flipLeftIn == null || flipLeftOut == null) {
            Log.w(TAG, "Flip attempted but animations not loaded.");
            disableFlip(); // Ensure flip stays disabled
            return;
        }

        // Disable clicks on cards during animation
        cardReviewFront.setClickable(false);
        cardReviewBack.setClickable(false);

        final View viewToFlipOut = isFront ? cardReviewFront : cardReviewBack;
        final View viewToFlipIn = isFront ? cardReviewBack : cardReviewFront;

        // Determine which animations to use based on current state
        ObjectAnimator animatorOut;
        ObjectAnimator animatorIn;

        if (isFront) {
            // Flipping from Front to Back (e.g., Right flip perspective)
            animatorOut = flipRightOut; // Front flips OUT right (0 -> -180)
            animatorIn = flipRightIn;   // Back flips IN right (180 -> 0)
        } else {
            // Flipping from Back to Front (e.g., Right flip perspective)
            // Back flips OUT right (180 -> 0) - uses right_in animation logic
            // Front flips IN right (-180 -> 0) - uses left_in animation logic
            animatorOut = flipRightIn; // Re-using animation with 180->0
            animatorIn = flipLeftIn;   // Re-using animation with -180->0
        }

        animatorOut.setTarget(viewToFlipOut);
        animatorIn.setTarget(viewToFlipIn);

        animatorOut.start();
        animatorIn.start();

        // Change visibility and state after the animation duration
        // Requires @integer/card_flip_time_full resource
        int duration = getResources().getInteger(R.integer.card_flip_time_full);

        handler.postDelayed(() -> {
            if (isFront) { // If we just flipped Front -> Back
                cardReviewFront.setVisibility(View.GONE);
                cardReviewBack.setVisibility(View.VISIBLE);
            } else { // If we just flipped Back -> Front
                cardReviewBack.setVisibility(View.GONE);
                cardReviewFront.setVisibility(View.VISIBLE);
            }
            isFront = !isFront; // Toggle state
            // Re-enable clicks on cards after animation finishes
            cardReviewFront.setClickable(true);
            cardReviewBack.setClickable(true);
        }, duration); // Delay matches animation duration
    }


    // Helper to disable flip if animations didn't load
    private void disableFlip() {
        if (cardReviewFront != null) cardReviewFront.setClickable(false);
        if (cardReviewBack != null) cardReviewBack.setClickable(false);
        Log.w(TAG, "Card flip is disabled.");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove any pending PostDelayed callbacks
        handler.removeCallbacksAndMessages(null);
    }
}