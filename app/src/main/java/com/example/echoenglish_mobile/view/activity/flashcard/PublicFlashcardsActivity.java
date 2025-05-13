package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicFlashcardsActivity extends AppCompatActivity implements PublicFlashcardAdapter.OnPublicFlashcardClickListener {

    private static final String ACTIVITY_TAG = "PublicFlashcards";
    public static final String CATEGORY_ID_EXTRA = "CATEGORY_ID";

    public static final String CATEGORY_NAME_EXTRA = "CATEGORY_NAME";
    private static final String LOADING_DIALOG_TAG = "PublicFlashcardsLoadingDialog";


    private ImageView backButton;
    private TextView textScreenTitle;

    private RecyclerView recyclerViewPublicFlashcards;
    private PublicFlashcardAdapter adapter;
    private TextView textViewNoFlashcards;
    private ApiService apiService;

    private Long categoryId;
    private String categoryName;

    private int loadingApiCount = 0;
    private View contentFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_flashcards);

        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        recyclerViewPublicFlashcards = findViewById(R.id.recyclerViewPublicFlashcards);
        textViewNoFlashcards = findViewById(R.id.textViewNoPublicFlashcards);
        contentFrame = findViewById(R.id.contentFrame);


        apiService = ApiClient.getApiService();

        categoryId = getIntent().getLongExtra(CATEGORY_ID_EXTRA, -1L);
        categoryName = getIntent().getStringExtra(CATEGORY_NAME_EXTRA);

        if (categoryId == -1L) {
            Log.e(ACTIVITY_TAG, "Invalid Category ID received.");
            Toast.makeText(this, "Invalid category.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        if (categoryName != null) {
            textScreenTitle.setText(categoryName);
        } else {
            textScreenTitle.setText("Public Flashcards");
        }


        setupRecyclerView();
        loadPublicFlashcards();

        backButton.setOnClickListener(v -> finish());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(ACTIVITY_TAG, "onResume: Loading public flashcards...");
        loadPublicFlashcards();
    }


    private void setupRecyclerView() {
        recyclerViewPublicFlashcards.setLayoutManager(new LinearLayoutManager(this));
        adapter = new PublicFlashcardAdapter(this, new ArrayList<>(), this);
        recyclerViewPublicFlashcards.setAdapter(adapter);
    }


    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
        }
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading flashcards...");
            contentFrame.setVisibility(View.INVISIBLE);
            recyclerViewPublicFlashcards.setEnabled(false);
            backButton.setEnabled(false);


        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            contentFrame.setVisibility(View.VISIBLE);
            recyclerViewPublicFlashcards.setEnabled(true);
            backButton.setEnabled(true);

        }
    }


    @Override
    public void onPublicFlashcardClick(FlashcardBasicResponse flashcard) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId());
        intent.putExtra(FlashcardDetailActivity.EXTRA_IS_PUBLIC, true);
        startActivity(intent);
    }

    private void loadPublicFlashcards() {
        startApiCall();

        apiService.getPublicFlashcardsByCategory(categoryId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                finishApiCall();
                if (response.isSuccessful() && response.body() != null) {
                    List<FlashcardBasicResponse> flashcards = response.body();
                    adapter.updateData(flashcards);
                    textViewNoFlashcards.setVisibility(flashcards.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewPublicFlashcards.setVisibility(flashcards.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load flashcards: " + response.code());
                    Toast.makeText(PublicFlashcardsActivity.this, "Failed to load flashcards.", Toast.LENGTH_SHORT).show();
                    textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    recyclerViewPublicFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error loading flashcards", t);
                Toast.makeText(PublicFlashcardsActivity.this, "Network error.", Toast.LENGTH_SHORT).show();
                textViewNoFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerViewPublicFlashcards.setVisibility(adapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }
}