package com.example.echoenglish_mobile.view.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;

import java.util.ArrayList;
import java.util.List;

import android.graphics.Typeface;

import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.Arrays;

public class VocabularyResultFragment extends Fragment {
    private SentenceAnalysisResult result;
    private static final String ARG_RESULT = "sentence_analysis_result";

    private static final int PRIMARY_COLOR = Color.parseColor("#2F3C7E");
    private static final String TYPEFACE = "sans-serif-medium";
    private HorizontalBarChart chart;

    public static VocabularyResultFragment newInstance(SentenceAnalysisResult result) {
        VocabularyResultFragment fragment = new VocabularyResultFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_RESULT, result);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_vocabulary_result, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView headerTextFeedback = view.findViewById(R.id.headerText);
        headerTextFeedback.setText("Feedback");
        if (getArguments() != null && getArguments().containsKey(ARG_RESULT)) {
            result = (SentenceAnalysisResult) getArguments().getSerializable(ARG_RESULT);
            // TODO: Trong tương lai, bạn có thể muốn sử dụng 'result' để tạo dữ liệu cho biểu đồ
            // Ví dụ: int[] levels = result.getCefrLevelsDistribution();
        }

        chart = view.findViewById(R.id.wordFreqChart);

        configureMasterChart(chart);
        loadChartData(chart);
    }

    private void configureMasterChart(HorizontalBarChart chart) {
        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setExtraOffsets(24f, 24f, 24f, 24f); // Khoảng cách lề

        // Advanced typography
        Typeface tf = Typeface.create(TYPEFACE, Typeface.NORMAL);

        // XAxis customization
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawAxisLine(false); // Không vẽ đường trục X
        xAxis.setDrawGridLines(false); // Không vẽ lưới dọc từ trục X
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLevelLabels())); // Đặt nhãn A1, A2...
        xAxis.setLabelCount(getLevelLabels().size()); // Đảm bảo đủ không gian cho các nhãn
        xAxis.setGranularity(1f); // Đảm bảo mỗi nhãn cách nhau 1 đơn vị

        // YAxis left customization
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setAxisMinimum(0f); // Giá trị tối thiểu của trục Y (bên trái)
        // leftAxis.setSpaceTop(15f); // Bỏ đi nếu không muốn khoảng trắng thừa ở trên cùng
        leftAxis.setDrawAxisLine(true); // Vẽ đường trục Y
        leftAxis.setAxisLineColor(Color.LTGRAY);
        leftAxis.setDrawGridLines(true); // Vẽ lưới ngang từ trục Y
        leftAxis.setGridColor(Color.parseColor("#F3F4F6")); // Màu lưới nhạt
        leftAxis.setLabelCount(6, false); // Số lượng nhãn gợi ý trên trục Y
        // leftAxis.setGranularity(1f); // Có thể bỏ nếu muốn Android tự tính khoảng chia

        // YAxis right customization
        chart.getAxisRight().setEnabled(false); // Vô hiệu hóa trục Y bên phải

        // Legend engineering
        Legend legend = chart.getLegend();
        legend.setEnabled(true); // Bật chú thích (ví dụ: "CEFR Levels")
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP); // Vị trí chú thích
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        legend.setDrawInside(false); // Vẽ bên ngoài khu vực biểu đồ
        legend.setForm(Legend.LegendForm.CIRCLE); // Hình dạng của marker chú thích
        legend.setTextSize(12f);
        legend.setTypeface(tf);
        legend.setTextColor(Color.DKGRAY);
        legend.setXEntrySpace(7f); // Khoảng cách ngang giữa các mục chú thích
        legend.setYEntrySpace(5f); // Khoảng cách dọc giữa các mục chú thích
    }

    private void loadChartData(HorizontalBarChart chart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        int[] levelsData = {2, 5, 8, 12, 9, 6};

        List<String> labels = getLevelLabels(); // ["A1", "A2", "B1", "B2", "C1", "C2"]

        for (int i = 0; i < levelsData.length; i++) {
            entries.add(new BarEntry(i, levelsData[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Word Count by CEFR Level");
        dataSet.setColor(PRIMARY_COLOR);
        // dataSet.setHighLightColor(SECONDARY_COLOR);
        dataSet.setDrawValues(true); // Hiển thị giá trị trên các thanh

        // Định dạng giá trị hiển thị trên thanh (số nguyên)
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Bar styling perfection
        dataSet.setValueTextSize(11f); // Kích thước chữ giá trị trên thanh
        dataSet.setValueTypeface(Typeface.create(TYPEFACE, Typeface.BOLD));
        dataSet.setValueTextColor(Color.WHITE); // Màu chữ giá trị (trắng để nổi bật trên nền xanh)
        // dataSet.setBarBorderWidth(0.8f); // Viền thanh - có thể bỏ
        // dataSet.setBarBorderColor(PRIMARY_COLOR);

        // Gradient fill for depth - bỏ gradient nếu muốn màu đồng nhất
        // dataSet.setGradientColor(PRIMARY_COLOR, Color.argb(100,
        //         Color.red(PRIMARY_COLOR),
        //         Color.green(PRIMARY_COLOR),
        //         Color.blue(PRIMARY_COLOR)
        // ));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.7f); // Độ rộng của các thanh (0.0 - 1.0)

        chart.setData(data);
        chart.setFitBars(true); // Căn chỉnh độ rộng thanh cho vừa với không gian

        // Professional animation (Bật lại nếu muốn)
        chart.animateY(1200); // Animation từ dưới lên
        // chart.animateX(1200); // Animation từ trái qua
        chart.invalidate(); // Vẽ lại biểu đồ
    }

    private List<String> getLevelLabels() {
        return Arrays.asList("A1", "A2", "B1", "B2", "C1", "C2");
    }
}