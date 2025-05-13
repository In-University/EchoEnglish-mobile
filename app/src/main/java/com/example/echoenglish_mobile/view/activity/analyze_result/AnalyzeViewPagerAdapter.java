package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;

import java.util.ArrayList;
import java.util.List;

public class AnalyzeViewPagerAdapter extends RecyclerView.Adapter<AnalyzeViewPagerAdapter.PageViewHolder> {

    private static final int SPEAKING_PAGE = 0;
    private static final int WRITING_PAGE = 1;
    private final Context context;

    private List<SentenceAnalysisResult> pronunciationResults;
    private List<WritingResult> writingResults;
    public AnalyzeViewPagerAdapter(Context context,
                                   List<SentenceAnalysisResult> initialPronunciation,
                                   List<WritingResult> initialWriting) {
        this.context = context;
        this.pronunciationResults = new ArrayList<>(initialPronunciation);
        this.writingResults = new ArrayList<>(initialWriting);
    }

    @Override
    public int getItemViewType(int position) {
        return position == 0 ? SPEAKING_PAGE : WRITING_PAGE;
    }

    @NonNull
    @Override
    public PageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView recyclerView = new RecyclerView(context);
        recyclerView.setLayoutParams(new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        int padding = (int) (16 * context.getResources().getDisplayMetrics().density); // 16dp
        int topBottomPadding = (int) (8 * context.getResources().getDisplayMetrics().density); // 8dp
        recyclerView.setPadding(padding, topBottomPadding, padding, topBottomPadding);
        recyclerView.setClipToPadding(false);
        recyclerView.setId(View.generateViewId());

        return new PageViewHolder(recyclerView);
    }

    @Override
    public void onBindViewHolder(@NonNull PageViewHolder holder, int position) {
        RecyclerView currentPageRecyclerView = holder.recyclerView;

        currentPageRecyclerView.setLayoutManager(new LinearLayoutManager(context));
        currentPageRecyclerView.setHasFixedSize(true);

        if (position == SPEAKING_PAGE) {
            SpeakingResultAdapter speakingAdapter = new SpeakingResultAdapter(context, this.pronunciationResults);
            currentPageRecyclerView.setAdapter(speakingAdapter);
        } else { 
                        WritingResultAdapter writingAdapter = new WritingResultAdapter(context, this.writingResults);
            currentPageRecyclerView.setAdapter(writingAdapter);
        }
    }

    @Override
    public int getItemCount() {
        return 2;
    }

    public void setPronunciationResults(List<SentenceAnalysisResult> newResults) {
        this.pronunciationResults = new ArrayList<>(newResults); 
        notifyItemChanged(SPEAKING_PAGE);
    }

    public void setWritingResults(List<WritingResult> newResults) {
        this.writingResults = new ArrayList<>(newResults);
        notifyItemChanged(WRITING_PAGE);
    }


    static class PageViewHolder extends RecyclerView.ViewHolder {
        RecyclerView recyclerView;

        public PageViewHolder(@NonNull View itemView) {
            super(itemView);
            this.recyclerView = (RecyclerView) itemView;
        }
    }
}