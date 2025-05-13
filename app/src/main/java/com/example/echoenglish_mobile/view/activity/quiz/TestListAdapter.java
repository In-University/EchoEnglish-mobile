package com.example.echoenglish_mobile.adapters; // Thay package phù hợp

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.quiz.Constants;
import com.example.echoenglish_mobile.view.activity.quiz.TestActivity;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;

import java.util.List;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.TestViewHolder> {

    private List<Test> testList;
    private Context context;
    private int partNumberToStart; // Số part sẽ bắt đầu khi click

    public TestListAdapter(Context context, List<Test> testList, int partNumberToStart) {
        this.context = context;
        this.testList = testList;
        this.partNumberToStart = partNumberToStart;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = testList.get(position);

        if (test != null) {
            holder.testName.setText(test.getName() != null ? test.getName() : "Unnamed Test");
            holder.testSlug.setText(test.getSlug() != null ? test.getSlug() : "");

            holder.itemView.setOnClickListener(v -> {
                if (test.getTestId() != null) {
                    Log.d("TestListAdapter", "Starting TestActivity for testId: " + test.getTestId() + ", partNumber: " + partNumberToStart);
                    Intent intent = new Intent(context, TestActivity.class);
                    intent.putExtra(Constants.EXTRA_TEST_ID, test.getTestId());
                    intent.putExtra(Constants.EXTRA_PART_NUMBER, partNumberToStart);
                    context.startActivity(intent);
                } else {
                    Log.e("TestListAdapter", "Cannot start test, testId is null for test: " + test.getName());
                    Toast.makeText(context, "Error: Invalid test data.", Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return testList != null ? testList.size() : 0;
    }

    public void updateData(List<Test> newTestList) {
        this.testList = newTestList;
        notifyDataSetChanged();
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView testName, testSlug;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.tv_test_name);
            testSlug = itemView.findViewById(R.id.tv_test_slug);
        }
    }
}