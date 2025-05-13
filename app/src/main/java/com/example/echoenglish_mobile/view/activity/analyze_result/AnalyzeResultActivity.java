package com.example.echoenglish_mobile.view.activity.analyze_result;

import android.os.Bundle;
import android.util.Log; // Chỉ dùng để debug nếu thật sự cần
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast; // Để thông báo lỗi

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult; // Import model thật
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
// Bỏ import các Fragment vì không dùng nữa
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AnalyzeResultActivity extends AppCompatActivity {

    private static final String TAG = "AnalyzeResultActivity";

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private TextView tvSummary, tvPronunciationCount, tvWritingCount;
    private AnalyzeViewPagerAdapter pagerAdapter;
    private ApiService apiService;

    private List<SentenceAnalysisResult> pronunciationData = new ArrayList<>();
    private List<WritingResult> writingData = new ArrayList<>();

    private final String[] tabTitles = new String[]{"Pronunciation", "Writing"};

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

        apiService = ApiClient.getApiService();
        setupHeaderData();

        pagerAdapter = new AnalyzeViewPagerAdapter(this, pronunciationData, new ArrayList<>()); // Truyền danh sách rỗng ban đầu
        viewPager.setAdapter(pagerAdapter);
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(tabTitles[position])
        ).attach();

        fetchPronunciationResults();
        fetchWritingResults();
    }

    private void setupHeaderData() {
        int totalExercises = 14;
        int pronunciationExercises = 0; 
        int writingExercises = 0;
        int overallAccuracyPercent = 85;

        tvSummary.setText(String.format(Locale.getDefault(), "You have completed %d practice exercises this week", totalExercises));
        tvPronunciationCount.setText(String.valueOf(pronunciationExercises));
        tvWritingCount.setText(String.valueOf(writingExercises));
    }

    private void fetchPronunciationResults() {
        apiService.getSpeechAnalyzeResultList().enqueue(new Callback<List<SentenceAnalysisResult>>() {
            @Override
            public void onResponse(Call<List<SentenceAnalysisResult>> call, Response<List<SentenceAnalysisResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SentenceAnalysisResult> results = response.body();

                    pronunciationData.clear();
                    pronunciationData.addAll(results);

                    pagerAdapter.setPronunciationResults(pronunciationData);
                    tvPronunciationCount.setText(String.valueOf(pronunciationData.size()));
                    updateTotalExercisesSummary();

                } else {
                    showError("Cannot get data. Error code: " + response.code());
                    pagerAdapter.setPronunciationResults(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<List<SentenceAnalysisResult>> call, Throwable t) {
                Log.e(TAG, "Error: ", t);
                showError("Error when fetch data.");
                pagerAdapter.setPronunciationResults(new ArrayList<>());
            }
        });
    }

    private void updateTotalExercisesSummary() {
        try {
            int pronCount = Integer.parseInt(tvPronunciationCount.getText().toString());
            int writeCount = Integer.parseInt(tvWritingCount.getText().toString());
            tvSummary.setText(String.format(Locale.getDefault(), "You have completed %d practice exercises this week", pronCount + writeCount));
        } catch (NumberFormatException e) {
        }
    }

    private void fetchWritingResults() {
        apiService.getWritingAnalyzeResultList().enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        parseWritingJson(jsonString);
                        pagerAdapter.setWritingResults(writingData);
                        tvWritingCount.setText(String.valueOf(writingData.size()));
                        updateTotalExercisesSummary();
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading writing API response body", e);
                        showError("Failed to process writing data.");
                        pagerAdapter.setWritingResults(new ArrayList<>());
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing JSON from writing API", e);
                        showError("Invalid writing data format.");
                        pagerAdapter.setWritingResults(new ArrayList<>());
                    }
                } else {
                    showError("Unable to load writing data. Error code: " + response.code());
                    pagerAdapter.setWritingResults(new ArrayList<>());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error when calling the writing API: ", t);
                showError("Network error while loading writing data.");
                pagerAdapter.setWritingResults(new ArrayList<>());
            }
        });
    }
    private void parseWritingJson(String jsonString) throws JSONException {
        writingData.clear();
        JSONArray resultsArray = new JSONArray(jsonString);

        for (int i = 0; i < resultsArray.length(); i++) {
            JSONObject resultObject = resultsArray.getJSONObject(i);
            String title = "N/A";
            String dateStr = "";
            String formattedDate = "N/A"; 
            String type = "Essay";
            int wordCount = 0;
            String feedbackJson = null;

            try {
                if (resultObject.has("topic")) {
                    title = resultObject.getString("topic");
                }
                if (title.length() > 60) { 
                    title = title.substring(0, 57) + "...";
                }


                if (resultObject.has("date")) {
                    dateStr = resultObject.getString("date");
                    DateTimeFormatter sourceDateFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME;
                    DateTimeFormatter targetDateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy · HH:mm");

                    try {
                        OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateStr, sourceDateFormatter);
                        formattedDate = offsetDateTime.format(targetDateFormatter);
                    } catch (DateTimeParseException e) {
                        Log.w(TAG, "Cannot format date: " + dateStr, e);
                        formattedDate = dateStr;
                    }
                }


                if (resultObject.has("exerciseType")) {
                    type = resultObject.getString("exerciseType");
                }

                if (resultObject.has("wordCount")) {
                    wordCount = resultObject.getInt("wordCount");
                }
                if (resultObject.has("feedback")) { 
                    Object feedbackObj = resultObject.get("feedback");
                    if (feedbackObj != null) {
                        feedbackJson = feedbackObj.toString(); 
                    }
                }
                writingData.add(new WritingResult(title, formattedDate, type, wordCount, feedbackJson));
            } catch (JSONException e) {
                Log.e(TAG, "Error parsing writing result: " + resultObject.toString(), e);
            }
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}