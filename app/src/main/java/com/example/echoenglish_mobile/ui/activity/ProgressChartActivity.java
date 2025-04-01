package com.example.echoenglish_mobile.ui.activity;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.github.mikephil.charting.utils.Transformer;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProgressChartActivity extends AppCompatActivity {
    private static final int PRIMARY_COLOR = Color.parseColor("#2F3C7E");  // Deep navy blue
    private static final int SECONDARY_COLOR = Color.parseColor("#FBEAEB"); // Soft peach
    private static final String TYPEFACE = "sans-serif-medium";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.test_progress_chart);

        HorizontalBarChart chart = findViewById(R.id.progress_chart);
        configureMasterChart(chart);
        loadChartData(chart);
    }

    private void configureMasterChart(HorizontalBarChart chart) {
        // Fundamental setup
        chart.setBackgroundColor(Color.WHITE);
        chart.getDescription().setEnabled(false);
        chart.setDrawBarShadow(false);
        chart.setPinchZoom(false);
        chart.setDoubleTapToZoomEnabled(false);
        chart.setExtraOffsets(24f, 24f, 24f, 24f);

        // Advanced typography
        Typeface tf = Typeface.create(TYPEFACE, Typeface.NORMAL);

        // XAxis customization
        XAxis xAxis = chart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(tf);
        xAxis.setTextSize(12f);
        xAxis.setTextColor(Color.DKGRAY);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setValueFormatter(new IndexAxisValueFormatter(getLevelLabels()));
        xAxis.setLabelCount(6);

        // YAxis left customization
        YAxis leftAxis = chart.getAxisLeft();
        leftAxis.setTypeface(tf);
        leftAxis.setTextSize(12f);
        leftAxis.setTextColor(Color.DKGRAY);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisLineColor(Color.LTGRAY);
        leftAxis.setGridColor(Color.parseColor("#F3F4F6"));
        leftAxis.setGranularity(1f);

        // YAxis right customization
        chart.getAxisRight().setEnabled(false);

        // Legend engineering
        Legend legend = chart.getLegend();
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setTextSize(12f);
        legend.setTypeface(tf);
        legend.setTextColor(Color.DKGRAY);
    }

    private void loadChartData(HorizontalBarChart chart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        int[] levels = {12, 9, 6, 3, 1, 0}; // Sample data

        for (int i = 0; i < levels.length; i++) {
            entries.add(new BarEntry(i, levels[i]));
        }

        BarDataSet dataSet = new BarDataSet(entries, "CEFR Levels");
        dataSet.setColor(PRIMARY_COLOR);
        dataSet.setHighLightColor(SECONDARY_COLOR);
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.valueOf((int) value);
            }
        });

        // Bar styling perfection
        dataSet.setValueTextSize(12f);
        dataSet.setValueTypeface(Typeface.create(TYPEFACE, Typeface.NORMAL));
        dataSet.setValueTextColor(PRIMARY_COLOR);
        dataSet.setBarBorderWidth(0.8f);
        dataSet.setBarBorderColor(PRIMARY_COLOR);

        // Gradient fill for depth
        dataSet.setGradientColor(PRIMARY_COLOR, Color.argb(100,
                Color.red(PRIMARY_COLOR),
                Color.green(PRIMARY_COLOR),
                Color.blue(PRIMARY_COLOR)
        ));

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.65f);

        chart.setData(data);

        // Professional animation
//        chart.animateY(1200,
//                new Easing.EaseOutCubic());
        chart.invalidate();
    }

    private List<String> getLevelLabels() {
        return Arrays.asList("A1", "A2", "B1",
                "B2", "C1", "C2");
    }

}