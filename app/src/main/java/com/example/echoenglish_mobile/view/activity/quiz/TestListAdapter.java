package com.example.echoenglish_mobile.view.activity.quiz;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestPart;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestQuestionGroup;

import java.util.List;
import java.util.Locale;

public class TestListAdapter extends RecyclerView.Adapter<TestListAdapter.TestViewHolder> {

    private List<Test> testList; // Assuming API returns List<Test>
    private int targetPartNumber; // 1 or 5
    private Context context;

    public TestListAdapter(Context context, List<Test> testList, int targetPartNumber) {
        this.context = context;
        this.testList = testList;
        this.targetPartNumber = targetPartNumber;
    }

    @NonNull
    @Override
    public TestViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_test_part, parent, false);
        return new TestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TestViewHolder holder, int position) {
        Test test = testList.get(position);
        // Find the specific part (Part 1 or Part 5) within this Test object
        TestPart targetPart = findTargetPart(test, targetPartNumber);

        if (targetPart != null) {
            holder.testName.setText(String.format(Locale.getDefault(), "%s - Part %d", test.getName(), targetPart.getPartNumber()));

            int questionCount = 0;
            if (targetPart.getGroups() != null) {
                for (TestQuestionGroup group : targetPart.getGroups()) {
                    if (group.getQuestions() != null) {
                        questionCount += group.getQuestions().size();
                    }
                }
            }
            holder.questionCount.setText(String.format(Locale.getDefault(), "%d Questions", questionCount));

            int finalQuestionCount = questionCount; // For use in lambda
            holder.itemView.setOnClickListener(v -> {
                if (finalQuestionCount > 0) {
                    Intent intent = new Intent(context, TestActivity.class);
                    intent.putExtra(Constants.EXTRA_TEST_ID, test.getTestId());
                    intent.putExtra(Constants.EXTRA_PART_ID, targetPart.getPartId());
                    intent.putExtra(Constants.EXTRA_PART_NUMBER, targetPart.getPartNumber()); // Pass part number
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, "This part has no questions.", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            // Hide or show a message if the test doesn't contain the target part
            holder.itemView.setVisibility(View.GONE);
            holder.itemView.setLayoutParams(new RecyclerView.LayoutParams(0, 0));

        }
    }

    @Override
    public int getItemCount() {
        return testList != null ? testList.size() : 0;
    }

    // Helper to find the correct TestPart within a Test
    private TestPart findTargetPart(Test test, int partNum) {
        if (test.getParts() == null) return null;
        for (TestPart part : test.getParts()) {
            if (part.getPartNumber() != null && part.getPartNumber() == partNum) {
                return part;
            }
        }
        return null; // Part not found in this test
    }

    static class TestViewHolder extends RecyclerView.ViewHolder {
        TextView testName, questionCount;

        public TestViewHolder(@NonNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.test_name_text);
            questionCount = itemView.findViewById(R.id.test_question_count_text);
            // Initialize other views from item_test_part.xml if needed
        }
    }

    // Method to update data if needed
    public void updateData(List<Test> newTestList) {
        this.testList = newTestList;
        notifyDataSetChanged(); // Be mindful of performance for large lists
    }
}