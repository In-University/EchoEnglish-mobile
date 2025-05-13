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


    @NonNull
    @Override
    public Fragment createFragment(int position) {
        VocabularyReviewResponse vocab = vocabularyList.get(position);
        return ReviewCardFragment.newInstance(vocab);
    }

    @Override
    public int getItemCount() {
        return vocabularyList.size();
    }

}