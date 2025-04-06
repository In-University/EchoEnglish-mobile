package com.example.echoenglish_mobile.ui.pronun_summary_result;

import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.data.model.WordDetail;
import com.example.echoenglish_mobile.ui.pronunciation_assessment.PhonemeTextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FluencyResultFragment extends Fragment {
    private float totalDuration;
    private int wordCount;
    private float speakingRate;
    private int filterWordCount;
    private SentenceAnalysisResult result;
    private LinearLayout llSkillContainer;
    private FlexboxLayout container;
    private static final String ARG_RESULT = "sentence_analysis_result";

    private static class SkillData {
        String label;
        String value;
        String feedback;

        SkillData(String label, String value, String feedback) {
            this.label = label;
            this.value = value;
            this.feedback = feedback;
        }
    }

    public static FluencyResultFragment newInstance(SentenceAnalysisResult result) {
        FluencyResultFragment fragment = new FluencyResultFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_fluency_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        llSkillContainer = view.findViewById(R.id.llSkillContainer);
        container = view.findViewById(R.id.myAnswerContainer);

        // Lấy dữ liệu từ Bundle
        if (getArguments() != null && getArguments().containsKey(ARG_RESULT)) {
            result = (SentenceAnalysisResult) getArguments().getSerializable(ARG_RESULT);

            totalDuration = (float) result.getSummary().getTotal_duration();
            wordCount = result.getSummary().getWord_count();
            speakingRate = (float) result.getSummary().getSpeaking_rate_wpm();
            filterWordCount = result.getSummary().getFilter_word_count();
//            phonemeComparisons = result.getChunks().;
        }

        List<SkillData> skillDataList = new ArrayList<>();
        skillDataList.add(new SkillData("Duration", String.format(Locale.US, "%.1f s", totalDuration), result.getSummary().getTotal_duration_feedback()));
        skillDataList.add(new SkillData("Words", String.format(Locale.US, "%d", wordCount), result.getSummary().getWord_count_feedback()));
        skillDataList.add(new SkillData("Speed", String.format(Locale.US, "%.2f WPM", speakingRate), result.getSummary().getSpeaking_rate_wpm_feedback()));
        skillDataList.add(new SkillData("Filtered", String.format(Locale.US, "%d", filterWordCount), result.getSummary().getFilter_word_count_feedback()));

        LayoutInflater inflater = LayoutInflater.from(getContext());
        for (SkillData skill : skillDataList) {
            View skillView = inflater.inflate(R.layout.skill_item, llSkillContainer, false);
            TextView tvSkillTitle = skillView.findViewById(R.id.tvSkillTitle);
            TextView tvSkillFeedback = skillView.findViewById(R.id.tvSkillFeedback);
            TextView tvSkillProgress = skillView.findViewById(R.id.tvSkillProgress);

            tvSkillTitle.setText(skill.label);
            tvSkillFeedback.setText(skill.feedback);
            tvSkillProgress.setText(skill.value);

            llSkillContainer.addView(skillView);
        }

        addPhonemeTextView();

        // Cấu hình PieChart với điểm tổng hợp
        PieChart pieChart = view.findViewById(R.id.pieChart);
        float score = calculateOverallScore();
        setupPieChart(pieChart, score);
    }

    private float calculateOverallScore() {
        float durationScore = Math.min(totalDuration / 3f * 100, 100);
        float wordScore = (wordCount / 10f) * 100;
        float rateScore = (speakingRate / 200f) * 100;
        float filterScore = 100 - (filterWordCount * 10);

        return (durationScore * 0.2f) + (wordScore * 0.3f) +
                (rateScore * 0.4f) + (filterScore * 0.1f);
    }

    private String getScoreLevel(float score) {
        if (score >= 90) return "Expert";
        if (score >= 75) return "Advanced";
        if (score >= 50) return "Intermediate";
        return "Beginner";
    }

    private void addPhonemeTextView() {
        if (container != null) {
            List<WordDetail> phonemeData = result.getChunks();
            for (WordDetail wordDetail : phonemeData) {
                PhonemeTextView phonemeTextView = new PhonemeTextView(requireContext());
                phonemeTextView.setTextSize(18);
                phonemeTextView.setPhonemeData(wordDetail.getText(), wordDetail.getError());

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                params.setMarginEnd(12);
                phonemeTextView.setLayoutParams(params);

                container.addView(phonemeTextView);
            }
        }
    }


    private void setupPieChart(PieChart pieChart, float score) {
        // Cấu hình cơ bản cho PieChart
        pieChart.setUsePercentValues(false); // Không hiển thị giá trị dưới dạng phần trăm tự động
        pieChart.getDescription().setEnabled(false); // Tắt mô tả mặc định
        pieChart.setExtraOffsets(0,5,0,0); // Thêm khoảng cách xung quanh biểu đồ
        pieChart.setDragDecelerationFrictionCoef(0.95f); // Hiệu ứng kéo mượt mà
        pieChart.setDrawHoleEnabled(true); // Hiển thị lỗ ở trung tâm
        pieChart.setHoleColor(Color.TRANSPARENT); // Lỗ trong suốt
        pieChart.setTransparentCircleRadius(70f); // Bán kính vòng trong suốt
        pieChart.setHoleRadius(70f); // Bán kính lỗ trung tâm
        pieChart.setRotationAngle(0); // Góc xoay ban đầu
        pieChart.setRotationEnabled(false); // Tắt khả năng xoay bằng tay
        pieChart.setHighlightPerTapEnabled(false); // Tắt hiệu ứng khi chạm

        // Thiết lập văn bản trung tâm
        pieChart.setDrawCenterText(true); // Hiển thị văn bản ở trung tâm
        pieChart.setCenterText(generateCenterText(score)); // Đặt văn bản tùy chỉnh
        pieChart.setCenterTextColor(Color.parseColor("#3F51B5")); // Màu xanh đậm cho văn bản
        pieChart.setCenterTextSize(16f); // Kích thước chữ lớn hơn cho nổi bật

        // Dữ liệu cho biểu đồ
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(score, "Score")); // Phần điểm số
        entries.add(new PieEntry(100 - score, "")); // Phần còn lại

        // Thiết lập DataSet
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(Color.parseColor("#3F51B5"), Color.parseColor("#ECEFF1")); // Màu xanh đậm và xám nhạt
        dataSet.setDrawValues(false); // Không hiển thị giá trị trên biểu đồ

        // Gán dữ liệu vào PieChart
        PieData data = new PieData(dataSet);
        pieChart.setData(data);
        pieChart.invalidate(); // Cập nhật giao diện
    }

    private SpannableString generateCenterText(float score) {
        String scoreText = String.format(Locale.US, "%.0f%%", score); // Định dạng điểm số thành phần trăm
        String levelText = getScoreLevel(score); // Lấy cấp độ dựa trên điểm số
        SpannableString centerText = new SpannableString(scoreText + "\n" + levelText); // Kết hợp điểm số và cấp độ

        // Định dạng kích thước và màu sắc
        centerText.setSpan(new RelativeSizeSpan(1.5f), 0, scoreText.length(), 0); // Phần trăm lớn hơn 1.5 lần
        centerText.setSpan(new ForegroundColorSpan(Color.parseColor("#3F51B5")), 0, scoreText.length(), 0); // Màu xanh đậm cho phần trăm
        centerText.setSpan(new ForegroundColorSpan(Color.parseColor("#78909C")), scoreText.length(), centerText.length(), 0); // Màu xám xanh cho cấp độ

        return centerText;
    }
}
