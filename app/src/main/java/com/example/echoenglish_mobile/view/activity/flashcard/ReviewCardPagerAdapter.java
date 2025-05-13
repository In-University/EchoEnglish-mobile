package com.example.echoenglish_mobile.view.activity.flashcard;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;

import java.util.List;

public class ReviewCardPagerAdapter extends FragmentStateAdapter {

    private final List<VocabularyReviewResponse> vocabularyList;

    public ReviewCardPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<VocabularyReviewResponse> vocabularyList) {
        super(fragmentActivity);
        this.vocabularyList = vocabularyList;
    }

    // If using Fragments in ViewPager inside another Fragment, use this constructor:
    // public ReviewCardPagerAdapter(@NonNull Fragment fragment, List<VocabularyReviewResponse> vocabularyList) {
    //     super(fragment);
    //     this.vocabularyList = vocabularyList;
    // }

    // If using FragmentManager and Lifecycle directly, use this constructor:
    // public ReviewCardPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, List<VocabularyReviewResponse> vocabularyList) {
    //     super(fragmentManager, lifecycle);
    //     this.vocabularyList = vocabularyList;
    // }


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // Create a new ReviewCardFragment for each vocabulary word
        VocabularyReviewResponse vocab = vocabularyList.get(position);
        return ReviewCardFragment.newInstance(vocab);
    }

    @Override
    public int getItemCount() {
        // Return the total number of vocabulary words
        return vocabularyList.size();
    }

    // Optional: Implement methods for handling list updates if the list can change
    // public void updateList(List<VocabularyReviewResponse> newList) { ... notifyDataSetChanged() ... }
}