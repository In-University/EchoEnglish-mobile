package com.example.echoenglish_mobile.view.activity.grammar;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.grammar.model.Grammar;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GrammarActivity extends AppCompatActivity {

    private static final String TAG = "GrammarActivity";
    private static final String LOADING_DIALOG_TAG = "GrammarLoadingDialog";

    private ImageView backButton;
    private TextView textScreenTitle;

    private ViewPager2 viewPagerGrammar;
    private TextView textGrammarPageIndicator;
    private TextView textGrammarStatusMessage;

    private ApiService apiService;
    private List<Grammar> grammarData;
    private GrammarPagerAdapter pagerAdapter;

    private int loadingApiCount = 0;

    private ViewPager2.OnPageChangeCallback pageChangeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        findViews();
        setupCustomHeader();

        apiService = ApiClient.getApiService();

        startApiCall("Loading grammar...");

        fetchGrammarData();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }


    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);
        viewPagerGrammar = findViewById(R.id.viewPagerGrammar);
        textGrammarPageIndicator = findViewById(R.id.textGrammarPageIndicator);
        textGrammarStatusMessage = findViewById(R.id.textGrammarStatusMessage);
    }

    private void setupCustomHeader() {
        textScreenTitle.setText("Grammar Guide");
        backButton.setOnClickListener(v -> onBackPressed());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }


    private void fetchGrammarData() {
        apiService.getGrammars().enqueue(new Callback<List<Grammar>>() {
            @Override
            public void onResponse(Call<List<Grammar>> call, Response<List<Grammar>> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    grammarData = response.body();
                    if (grammarData != null && !grammarData.isEmpty()) {
                        setupViewPager(grammarData);
                        setUiState(UiState.CONTENT);
                        updatePageIndicator(0);
                    } else {
                        grammarData = new ArrayList<>();
                        setUiState(UiState.EMPTY);
                    }
                } else {
                    Log.e(TAG, "Failed to retrieve grammar data: " + response.code() + " - " + response.message());
                    grammarData = new ArrayList<>();
                    setUiState(UiState.ERROR);
                    Toast.makeText(GrammarActivity.this, "Failed to load grammar topics.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Grammar>> call, Throwable t) {
                finishApiCall();
                Log.e(TAG, "Network Error fetching grammar data", t);
                grammarData = new ArrayList<>();
                setUiState(UiState.ERROR);
                Toast.makeText(GrammarActivity.this, "Network error loading grammar topics.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupViewPager(List<Grammar> grammars) {
        if (grammars == null || grammars.isEmpty()) {
            Log.w(TAG, "Attempted to setup ViewPager with empty or null list.");
            setUiState(UiState.EMPTY);
            return;
        }

        pagerAdapter = new GrammarPagerAdapter(this, grammars);
        viewPagerGrammar.setAdapter(pagerAdapter);

        if (pageChangeCallback != null) {
            viewPagerGrammar.unregisterOnPageChangeCallback(pageChangeCallback);
        }

        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (grammarData != null && position >= 0 && position < grammarData.size()) {
                    textScreenTitle.setText(grammarData.get(position).getName());
                    updatePageIndicator(position);
                }
            }
        };
        viewPagerGrammar.registerOnPageChangeCallback(pageChangeCallback);


        if (!grammars.isEmpty()) {
            textScreenTitle.setText(grammars.get(0).getName());
            updatePageIndicator(0);
        } else {
            textGrammarPageIndicator.setVisibility(View.GONE);
        }
    }

    private void updatePageIndicator(int currentPosition) {
        if (grammarData != null && !grammarData.isEmpty()) {
            textGrammarPageIndicator.setText(String.format(Locale.getDefault(), "%d / %d",
                    currentPosition + 1, grammarData.size()));
            textGrammarPageIndicator.setVisibility(View.VISIBLE);
        } else {
            textGrammarPageIndicator.setVisibility(View.GONE);
        }
    }


    private enum UiState {
        LOADING, CONTENT, EMPTY, ERROR
    }

    private void setUiState(UiState state) {
        viewPagerGrammar.setVisibility(View.GONE);
        textGrammarPageIndicator.setVisibility(View.GONE);
        textGrammarStatusMessage.setVisibility(View.GONE);

        switch (state) {
            case LOADING:
                break;
            case CONTENT:
                viewPagerGrammar.setVisibility(View.VISIBLE);
                break;
            case EMPTY:
                textGrammarStatusMessage.setVisibility(View.VISIBLE);
                textGrammarStatusMessage.setText("No grammar topics available.");
                break;
            case ERROR:
                viewPagerGrammar.setVisibility(View.GONE);
                textGrammarStatusMessage.setVisibility(View.VISIBLE);
                textGrammarStatusMessage.setText("Failed to load grammar data.");
                break;
        }
        Log.d(TAG, "UI State changed to: " + state);
    }


    private void setUiEnabled(boolean enabled) {
        backButton.setEnabled(enabled);
        if (viewPagerGrammar != null) {
            viewPagerGrammar.setUserInputEnabled(enabled);
        }
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false);
            setUiState(UiState.LOADING);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            setUiEnabled(true);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (viewPagerGrammar != null && pageChangeCallback != null) {
            viewPagerGrammar.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }
}