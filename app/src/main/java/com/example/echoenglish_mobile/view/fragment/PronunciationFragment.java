package com.example.echoenglish_mobile.view.fragment;

import androidx.fragment.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.WordDetail;
import com.example.echoenglish_mobile.adapter.ProgressChartAdapter;
import com.example.echoenglish_mobile.view.customview.PhonemeTextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.google.android.flexbox.FlexboxLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PronunciationFragment extends Fragment {
    private SentenceAnalysisResult result;
    private static final String ARG_RESULT = "sentence_analysis_result";
    private FlexboxLayout container;
    private RecyclerView recyclerViewPhonemeChart;
    private ProgressChartAdapter adapter;

    public static PronunciationFragment newInstance(SentenceAnalysisResult result) {
        PronunciationFragment fragment = new PronunciationFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_pronunciation_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerViewPhonemeChart = view.findViewById(R.id.recyclerViewPhonemeChart);
        recyclerViewPhonemeChart.setLayoutManager(new LinearLayoutManager(this.getContext()));

        if (getArguments() != null && getArguments().containsKey(ARG_RESULT)) {
            result = (SentenceAnalysisResult) getArguments().getSerializable(ARG_RESULT);
        }

        PieChart pieChart = view.findViewById(R.id.pieChart);
        float score = (float) calculateAverageSimilarity(result);
        setupPieChart(pieChart, score);
        container = view.findViewById(R.id.myAnswerContainer);
        adapter = new ProgressChartAdapter(this.getContext(), result.getPhonemeStatsList().stream().filter(s -> s.getCorrectCount() > 0).collect(Collectors.toList()));
        recyclerViewPhonemeChart.setAdapter(adapter);
        addPhonemeTextView();
    }
    public double calculateAverageSimilarity(SentenceAnalysisResult result) {
        if (result == null || result.getChunks() == null || result.getChunks().isEmpty()) {
            return 0.0;
        }

        double totalSimilarity = 0.0;
        int count = 0;

        for (WordDetail word : result.getChunks()) {
            if (word != null && word.getPronunciation() != null) {
                Double similarity = word.getPronunciation().getSimilarity();
                if (similarity != null && similarity > 0) {
                    totalSimilarity += similarity;
                    count++;
                }
            }
        }
        System.out.println("ssg");
        return count > 0 ? Math.min(1, totalSimilarity / count + 0.1) * 100 : 0.0;
    }
    private void addPhonemeTextView() {
        if (container != null) {
            List<WordDetail> phonemeData = result.getChunks();
            for (WordDetail wordDetail : phonemeData) {
                PhonemeTextView phonemeTextView = new PhonemeTextView(requireContext());
                phonemeTextView.setTextSize(18);

                List<PhonemeComparison> mapping = null;
                if (wordDetail.getPronunciation() != null) {
                    mapping = wordDetail.getPronunciation().getMapping();
                }

                phonemeTextView.setPhonemeData(wordDetail.getText(), mapping);

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
        String levelText = "Fixed level"; // Lấy cấp độ dựa trên điểm số
        SpannableString centerText = new SpannableString(scoreText + "\n" + levelText); // Kết hợp điểm số và cấp độ

        // Định dạng kích thước và màu sắc
        centerText.setSpan(new RelativeSizeSpan(1.5f), 0, scoreText.length(), 0); // Phần trăm lớn hơn 1.5 lần
        centerText.setSpan(new ForegroundColorSpan(Color.parseColor("#3F51B5")), 0, scoreText.length(), 0); // Màu xanh đậm cho phần trăm
        centerText.setSpan(new ForegroundColorSpan(Color.parseColor("#78909C")), scoreText.length(), centerText.length(), 0); // Màu xám xanh cho cấp độ

        return centerText;
    }
}
