package com.example.echoenglish_mobile.ui.pronun_summary_result;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.echoenglish_mobile.R;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.Locale;

public class FluencyFragment extends Fragment {

    private PieChart pieChart;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate layout của Fragment
        View view = inflater.inflate(R.layout.fragment_fluency_result, container, false);

        // Lấy tham chiếu đến PieChart từ layout đã inflate
        pieChart = view.findViewById(R.id.pieChart);

        // Cấu hình cơ bản cho PieChart
        pieChart.setUsePercentValues(true);
        pieChart.getDescription().setEnabled(false);
        pieChart.setExtraOffsets(5, 10, 5, 5);
        pieChart.setDragDecelerationFrictionCoef(0.95f);

        // Tạo hiệu ứng “doughnut” (vòng tròn với lỗ ở giữa)
        pieChart.setDrawHoleEnabled(true);
        pieChart.setHoleColor(Color.WHITE);
        pieChart.setHoleRadius(60f); // Điều chỉnh độ lớn của lỗ
        pieChart.setTransparentCircleRadius(65f);

        // Tạo dữ liệu mẫu
        ArrayList<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(40f, "Category A"));
        entries.add(new PieEntry(30f, "Category B"));
        entries.add(new PieEntry(20f, "Category C"));
        entries.add(new PieEntry(10f, "Category D"));

        // Tạo DataSet và cấu hình hiển thị
        PieDataSet dataSet = new PieDataSet(entries, "Sample Data");
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // Thiết lập màu sắc cho các phần của biểu đồ
        ArrayList<Integer> colors = new ArrayList<>();
        colors.add(Color.parseColor("#2ecc71")); // màu xanh lá
        colors.add(Color.parseColor("#3498db")); // màu xanh dương
        colors.add(Color.parseColor("#e74c3c")); // màu đỏ
        colors.add(Color.parseColor("#f1c40f")); // màu vàng
        dataSet.setColors(colors);

        // Tạo PieData từ DataSet
        PieData data = new PieData(dataSet);
        data.setValueTextSize(12f);
        data.setValueTextColor(Color.WHITE);

        // Gán dữ liệu cho PieChart và refresh
        pieChart.setData(data);
        pieChart.invalidate(); // vẽ lại biểu đồ

        return view;
    }
}
