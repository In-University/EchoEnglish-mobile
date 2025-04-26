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
import com.example.echoenglish_mobile.view.activity.quiz.model.TestHistory;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private List<TestHistory> historyList;
    private Context context;
    // Định dạng ngày giờ mong muốn hiển thị
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm", Locale.ENGLISH);
    // Các định dạng có thể nhận được từ API (thêm vào nếu cần)
    private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;


    public HistoryAdapter(Context context, List<TestHistory> historyList) {
        this.context = context;
        this.historyList = historyList;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new HistoryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        TestHistory history = historyList.get(position);

        // Set Test Name (kiểm tra null)
        if (history.getTest() != null && history.getTest().getName() != null) {
            holder.testName.setText(history.getTest().getName());
            // Có thể thêm Part number nếu cần và nếu TestHistory có thông tin đó
        } else {
            holder.testName.setText("Unknown Test");
        }

        // Set Date (ưu tiên completedAt, nếu không có thì dùng startedAt)
        String dateString = history.getCompletedAt() != null ? history.getCompletedAt() : history.getStartedAt();
        String prefix = history.getCompletedAt() != null ? "Completed: " : "Started: ";
        holder.date.setText(prefix + formatDateString(dateString));

        // Set Score and Details
        if (history.getScore() != null) {
            holder.score.setText(String.format(Locale.getDefault(), "%.0f%%", history.getScore()));
        } else {
            holder.score.setText("--%"); // Hoặc "N/A"
        }

        if (history.getTotalQuestions() != null && history.getCorrectAnswers() != null) {
            holder.resultDetails.setText(String.format(Locale.getDefault(), "%d/%d Correct",
                    history.getCorrectAnswers(), history.getTotalQuestions()));
            holder.resultDetails.setVisibility(View.VISIBLE);
        } else {
            holder.resultDetails.setVisibility(View.GONE); // Ẩn nếu không có chi tiết
        }


        // Set OnClickListener to view details (hoặc mở lại kết quả)
        holder.itemView.setOnClickListener(v -> {
            // TODO: Implement navigation to ExplanationActivity or ResultActivity
            Intent intent = new Intent(context, ResultActivity.class); // Hoặc ExplanationActivity
            intent.putExtra(Constants.EXTRA_HISTORY_ID, history.getId());
            // Truyền thêm thông tin điểm số nếu ResultActivity cần
            if (history.getScore() != null) {
                intent.putExtra(Constants.EXTRA_SCORE, history.getScore());
            }
            if (history.getTotalQuestions() != null) {
                intent.putExtra(Constants.EXTRA_TOTAL_QUESTIONS, history.getTotalQuestions());
            }
            if (history.getCorrectAnswers() != null) {
                intent.putExtra(Constants.EXTRA_CORRECT_ANSWERS, history.getCorrectAnswers());
            }
            context.startActivity(intent);

            // Hoặc chỉ hiện Toast tạm thời
            // Toast.makeText(context, "Clicked History ID: " + history.getId(), Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return historyList != null ? historyList.size() : 0;
    }

    // Helper method to format date string
    private String formatDateString(String dateString) {
        if (dateString == null) {
            return "N/A";
        }
        try {
            // Thử parse theo định dạng ISO_LOCAL_DATE_TIME trước
            LocalDateTime dateTime = LocalDateTime.parse(dateString, ISO_DATE_TIME_FORMATTER);
            return dateTime.format(DISPLAY_FORMATTER);
        } catch (DateTimeParseException e) {
            // Nếu parse lỗi, thử các định dạng khác nếu cần hoặc trả về chuỗi gốc
            // Log.w("HistoryAdapter", "Could not parse date string: " + dateString);
            return dateString; // Trả về chuỗi gốc nếu không parse được
        }
    }

    // Method to update the list data
    public void updateData(List<TestHistory> newHistoryList) {
        this.historyList.clear();
        if (newHistoryList != null) {
            this.historyList.addAll(newHistoryList);
        }
        notifyDataSetChanged(); // Thông báo cho RecyclerView cập nhật
    }

    static class HistoryViewHolder extends RecyclerView.ViewHolder {
        TextView testName, date, score, resultDetails;

        public HistoryViewHolder(@NonNull View itemView) {
            super(itemView);
            testName = itemView.findViewById(R.id.tv_history_test_name);
            date = itemView.findViewById(R.id.tv_history_date);
            score = itemView.findViewById(R.id.tv_history_score);
            resultDetails = itemView.findViewById(R.id.tv_history_result_details);
        }
    }
}