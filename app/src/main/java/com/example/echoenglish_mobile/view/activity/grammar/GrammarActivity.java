package com.example.echoenglish_mobile.view.activity.grammar;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.grammar.model.Grammar;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrammarActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ProgressBar progressBar;
    private List<Grammar> grammarData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        // Find views by ID
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);
        progressBar = findViewById(R.id.progressBar);

        fetchGrammarData();
    }

    private void fetchGrammarData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Grammar>> call = apiService.getGrammars();

        call.enqueue(new Callback<List<Grammar>>() {
            @Override
            public void onResponse(Call<List<Grammar>> call, Response<List<Grammar>> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    grammarData = response.body();
                    setupViewPager(grammarData);
                } else {
                    showError("Failed to retrieve data: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Grammar>> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network Error: " + t.getMessage());
                Log.e("MainActivity", "API Call failed", t);
            }
        });
    }

    private void setupViewPager(List<Grammar> grammars) {
        if (grammars == null || grammars.isEmpty()) {
            showError("No grammar data available.");
            return;
        }

        GrammarPagerAdapter pagerAdapter = new GrammarPagerAdapter(this, grammars);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(grammars.get(position).getName())
        ).attach();

        if (grammars.size() > 4) { // Adjust threshold as needed
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        } else {
            tabLayout.setTabMode(TabLayout.MODE_FIXED);
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
}
