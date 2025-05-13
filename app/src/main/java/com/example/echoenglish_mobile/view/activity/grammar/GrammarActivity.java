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

    // Custom Header Views
    private ImageView backButton;
    private TextView textScreenTitle;

    // Main Content Views
    private ViewPager2 viewPagerGrammar;
    private TextView textGrammarPageIndicator; // Added TextView for page indicator
    private TextView textGrammarStatusMessage; // For loading/empty/error messages

    // Logic
    private ApiService apiService;
    private List<Grammar> grammarData;
    private GrammarPagerAdapter pagerAdapter;

    // Loading Logic
    private int loadingApiCount = 0;

    // Keep a reference to the PageChangeCallback
    private ViewPager2.OnPageChangeCallback pageChangeCallback;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grammar);

        findViews();
        setupCustomHeader(); // Setup custom header

        apiService = ApiClient.getApiService();

        // Start loading process using DialogFragment
        startApiCall("Loading grammar...");

        fetchGrammarData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Optional: Reload data if needed on resume (e.g., if data can change)
        // Log.d(TAG, "onResume: GrammarActivity does not auto-reload data.");
    }


    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);
        viewPagerGrammar = findViewById(R.id.viewPagerGrammar); // ViewPager2
        textGrammarPageIndicator = findViewById(R.id.textGrammarPageIndicator); // Find the new TextView
        textGrammarStatusMessage = findViewById(R.id.textGrammarStatusMessage); // Status message TextView
    }

    private void setupCustomHeader() {
        textScreenTitle.setText("Grammar Guide"); // Default or initial title
        backButton.setOnClickListener(v -> onBackPressed()); // Use onBackPressed for consistency
    }

    // Override onBackPressed to handle the back button press
    @Override
    public void onBackPressed() {
        // For a grammar guide, usually just finishing the activity is fine.
        // No confirmation dialog needed unless user is in a specific state (e.g., editing)
        super.onBackPressed();
    }


    private void fetchGrammarData() {
        // startApiCall is already called before this method
        apiService.getGrammars().enqueue(new Callback<List<Grammar>>() {
            @Override
            public void onResponse(Call<List<Grammar>> call, Response<List<Grammar>> response) {
                finishApiCall(); // Finish loading regardless of success

                if (response.isSuccessful() && response.body() != null) {
                    grammarData = response.body();
                    if (grammarData != null && !grammarData.isEmpty()) {
                        setupViewPager(grammarData);
                        setUiState(UiState.CONTENT); // Show content
                        updatePageIndicator(0); // Set initial indicator for the first page
                    } else {
                        // API successful but list is empty
                        grammarData = new ArrayList<>(); // Ensure not null
                        setUiState(UiState.EMPTY); // Show empty state
                    }
                } else {
                    Log.e(TAG, "Failed to retrieve grammar data: " + response.code() + " - " + response.message());
                    grammarData = new ArrayList<>(); // Use empty list on failure
                    setUiState(UiState.ERROR); // Show error state message
                    Toast.makeText(GrammarActivity.this, "Failed to load grammar topics.", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<List<Grammar>> call, Throwable t) {
                finishApiCall(); // Finish loading on network failure
                Log.e(TAG, "Network Error fetching grammar data", t);
                grammarData = new ArrayList<>(); // Use empty list on network error
                setUiState(UiState.ERROR); // Show error state message
                Toast.makeText(GrammarActivity.this, "Network error loading grammar topics.", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setupViewPager(List<Grammar> grammars) {
        if (grammars == null || grammars.isEmpty()) {
            Log.w(TAG, "Attempted to setup ViewPager with empty or null list.");
            setUiState(UiState.EMPTY); // Should already be handled by fetchGrammarData, but safety check
            return;
        }

        pagerAdapter = new GrammarPagerAdapter(this, grammars);
        viewPagerGrammar.setAdapter(pagerAdapter);

        // Unregister previous callback if it exists (important for onResume reloading)
        if (pageChangeCallback != null) {
            viewPagerGrammar.unregisterOnPageChangeCallback(pageChangeCallback);
        }

        // Register new PageChangeCallback
        pageChangeCallback = new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Update the screen title and page indicator based on the current ViewPager page
                if (grammarData != null && position >= 0 && position < grammarData.size()) {
                    textScreenTitle.setText(grammarData.get(position).getName());
                    updatePageIndicator(position);
                }
            }
        };
        viewPagerGrammar.registerOnPageChangeCallback(pageChangeCallback);


        // Set initial title and indicator for the first page if list is not empty
        if (!grammars.isEmpty()) {
            textScreenTitle.setText(grammars.get(0).getName());
            updatePageIndicator(0);
        } else {
            // Should be in EMPTY state already, but hide indicator just in case
            textGrammarPageIndicator.setVisibility(View.GONE);
        }
    }

    // Update the text showing current page / total pages
    private void updatePageIndicator(int currentPosition) {
        if (grammarData != null && !grammarData.isEmpty()) {
            textGrammarPageIndicator.setText(String.format(Locale.getDefault(), "%d / %d",
                    currentPosition + 1, grammarData.size()));
            textGrammarPageIndicator.setVisibility(View.VISIBLE); // Ensure it's visible
        } else {
            textGrammarPageIndicator.setVisibility(View.GONE); // Hide if no data
        }
    }


    // Enum for managing UI states
    private enum UiState {
        LOADING, CONTENT, EMPTY, ERROR
    }

    // Method to manage overall UI state manually
    private void setUiState(UiState state) {
        // Hide/Show main content and status message based on state
        viewPagerGrammar.setVisibility(View.GONE);
        textGrammarPageIndicator.setVisibility(View.GONE); // Hide indicator by default
        textGrammarStatusMessage.setVisibility(View.GONE);
        // Header elements (backButton, textScreenTitle) usually remain visible

        switch (state) {
            case LOADING:
                // Visibility handled by startApiCall -> LoadingDialogFragment
                // Ensure ViewPager and message are hidden
                // Header is also partially disabled by startApiCall
                break;
            case CONTENT:
                viewPagerGrammar.setVisibility(View.VISIBLE);
                // Page indicator visibility handled by updatePageIndicator
                // Message is hidden
                // Interaction enabled by finishApiCall
                break;
            case EMPTY:
                textGrammarStatusMessage.setVisibility(View.VISIBLE);
                textGrammarStatusMessage.setText("No grammar topics available."); // Empty message
                // Interaction enabled by finishApiCall
                break;
            case ERROR:
                viewPagerGrammar.setVisibility(View.GONE);
                textGrammarStatusMessage.setVisibility(View.VISIBLE);
                textGrammarStatusMessage.setText("Failed to load grammar data."); // Generic error message
                // Interaction enabled by finishApiCall
                break;
            // FINISHED state from ReviewActivity is not applicable here
        }
        Log.d(TAG, "UI State changed to: " + state);
        // setUiEnabled calls are in start/finishApiCall
    }


    // Helper to enable/disable main interactive elements (Header back button, ViewPager swipe)
    private void setUiEnabled(boolean enabled) {
        backButton.setEnabled(enabled); // Enable/disable back button during loading
        // ViewPager2 user input is enabled/disabled via viewPagerGrammar.setUserInputEnabled()
        if (viewPagerGrammar != null) {
            viewPagerGrammar.setUserInputEnabled(enabled);
        }
        Log.d(TAG, "Main UI elements enabled: " + enabled);
    }


    // --- Loading Logic using DialogFragment ---
    // Only one startApiCall method accepting String message
    private synchronized void startApiCall(String message) {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            String displayMessage = (message != null && !message.isEmpty()) ? message : "Loading...";
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, displayMessage);
            setUiEnabled(false); // Disable UI interaction during loading
            setUiState(UiState.LOADING); // Set UI state to LOADING (hides content, shows nothing but dialog)
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            // UI state (CONTENT, EMPTY, ERROR) is set in fetchGrammarData's onResponse/onFailure
            // setUiEnabled is called inside startApiCall and finishApiCall
            // After loading finishes, re-enable UI interaction. The setUiState will then make
            // ViewPager/message visible as appropriate.
            setUiEnabled(true); // Re-enable UI interaction after loading finishes
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Unregister ViewPager2 callback to prevent leaks
        if (viewPagerGrammar != null && pageChangeCallback != null) {
            viewPagerGrammar.unregisterOnPageChangeCallback(pageChangeCallback);
        }
        // Ensure loading dialog is dismissed if activity is destroyed
        if (loadingApiCount > 0) {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }
}