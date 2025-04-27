package com.example.echoenglish_mobile.view.activity.grammar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;


import com.example.echoenglish_mobile.view.activity.grammar.model.Grammar;

import java.util.List;

public class GrammarPagerAdapter extends FragmentStateAdapter {

    private final List<Grammar> grammarList;

    public GrammarPagerAdapter(@NonNull FragmentActivity fragmentActivity, List<Grammar> grammarList) {
        super(fragmentActivity);
        this.grammarList = grammarList;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return GrammarContentFragment.newInstance(grammarList.get(position));
    }

    @Override
    public int getItemCount() {
        return grammarList.size();
    }
}