package com.example.echoenglish_mobile.ui.quizz_app;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.databinding.QuizItemRecyclerRowBinding;

import java.util.List;


public class QuizListAdapter extends RecyclerView.Adapter<QuizListAdapter.MyViewHolder> {

    private final List<QuizModel> quizModelList;

    public QuizListAdapter(List<QuizModel> quizModelList) {
        this.quizModelList = quizModelList;
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {
        private final QuizItemRecyclerRowBinding binding;

        public MyViewHolder(QuizItemRecyclerRowBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(QuizModel model) {
            binding.quizTitleText.setText(model.getTitle());
            binding.quizSubtitleText.setText(model.getSubtitle());
            binding.quizTimeText.setText(model.getTime() + " min");

            binding.getRoot().setOnClickListener(v -> {
                Intent intent = new Intent(v.getContext(), QuizActivity.class);
                QuizActivity.setQuestionModelList(model.getQuestionList());
                QuizActivity.setTime(model.getTime());
                v.getContext().startActivity(intent);
            });
        }
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        QuizItemRecyclerRowBinding binding = QuizItemRecyclerRowBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false
        );
        return new MyViewHolder(binding);
    }

    @Override
    public int getItemCount() {
        return quizModelList.size();
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(quizModelList.get(position));
    }
}