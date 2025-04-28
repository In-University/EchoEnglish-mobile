package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.Locale;

public class AnalyzeResultActivity extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvSummary, tvPronunciationCount, tvWritingCount, tvOverallAccuracy;
    private Spinner spinnerFilter;
    private TextView tvSortBy;
    // Thay đổi kiểu của pagerAdapter
    private AnalyzeViewPagerAdapter pagerAdapter; // Thay đổi kiểu

    private final String[] tabTitles = new String[]{"Phát âm", "Viết"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analyze_result);

        // --- Find Views ---
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        tvSummary = findViewById(R.id.tvSummary);
        tvPronunciationCount = findViewById(R.id.tvPronunciationCount);
        tvWritingCount = findViewById(R.id.tvWritingCount);
        tvOverallAccuracy = findViewById(R.id.tvOverallAccuracy);
        spinnerFilter = findViewById(R.id.spinnerFilter);
        tvSortBy = findViewById(R.id.tvSortBy);

        // --- Setup Header (Fake Data) ---
        setupHeaderData();

        // --- Setup Spinner ---
        setupSpinner();

        // --- Setup ViewPager and Tabs ---
        // Khởi tạo adapter mới (kế thừa RecyclerView.Adapter)
        pagerAdapter = new AnalyzeViewPagerAdapter(this); // Khởi tạo adapter mới
        viewPager.setAdapter(pagerAdapter);

        // TabLayoutMediator vẫn hoạt động bình thường với RecyclerView.Adapter
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        // Optional: Add listeners for spinner or sort text view if needed
        // spinnerFilter.setOnItemSelectedListener(...)
        // tvSortBy.setOnClickListener(...)
    }

    private void setupHeaderData() {
        // Replace with actual data retrieval logic later
        int totalExercises = 14;
        int pronunciationExercises = 8;
        int writingExercises = 6;
        int overallAccuracyPercent = 85;

        tvSummary.setText(String.format(Locale.getDefault(), "Bạn đã hoàn thành %d bài luyện tập trong tuần này", totalExercises));
        tvPronunciationCount.setText(String.valueOf(pronunciationExercises));
        tvWritingCount.setText(String.valueOf(writingExercises));
        tvOverallAccuracy.setText(String.format(Locale.getDefault(), "%d%%", overallAccuracyPercent));
    }

    private void setupSpinner() {
        // Assuming you have the string-array "filter_options" in arrays.xml
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.filter_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerFilter.setAdapter(adapter);
    }
}