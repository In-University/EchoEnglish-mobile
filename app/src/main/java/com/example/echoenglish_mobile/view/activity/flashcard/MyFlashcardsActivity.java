package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.CreateFlashcardActivity;
import com.example.echoenglish_mobile.view.activity.flashcard.FlashcardDetailActivity;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;


import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyFlashcardsActivity extends AppCompatActivity implements
        FlashcardAdapter.OnFlashcardClickListener,
        FlashcardAdapter.OnFlashcardDeleteClickListener,
        FlashcardAdapter.OnFlashcardEditClickListener {

    private static final String ACTIVITY_TAG = "MyFlashcardsActivity";
    private static final String LOADING_DIALOG_TAG = "MyFlashcardsLoadingDialog";

    private ImageView backButton;
    private TextView textScreenTitle;


    private RecyclerView recyclerViewMyDecks;
    private FlashcardAdapter myDecksAdapter;
    private FloatingActionButton fabCreate;
    private TextView textViewNoDecks;

    private ApiService apiService;

    private View contentConstraintLayout;

    private int loadingApiCount = 0;

    private final ActivityResultLauncher<Intent> flashcardActivityResultLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    Log.d(ACTIVITY_TAG, "Returned from Create/Edit Flashcard with RESULT_OK. Reloading list...");
                    loadMyFlashcards();
                } else {
                    Log.d(ACTIVITY_TAG, "Returned from Create/Edit Flashcard without RESULT_OK (resultCode=" + result.getResultCode() + ")");
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_flashcards);

        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);


        recyclerViewMyDecks = findViewById(R.id.recyclerViewMyDecks);
        fabCreate = findViewById(R.id.fabCreateFlashcard);
        textViewNoDecks = findViewById(R.id.textViewNoDecks);
        contentConstraintLayout = findViewById(R.id.contentConstraintLayout);


        apiService = ApiClient.getApiService();

        setupRecyclerView();

        fabCreate.setOnClickListener(v -> {
            Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
            flashcardActivityResultLauncher.launch(intent);
        });

        loadMyFlashcards();

        textScreenTitle.setText("My Flashcards");

        backButton.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(ACTIVITY_TAG, "onResume: Loading my flashcards...");
        loadMyFlashcards();
    }


    private void setupRecyclerView() {
        recyclerViewMyDecks.setLayoutManager(new LinearLayoutManager(this));
        myDecksAdapter = new FlashcardAdapter(this, new ArrayList<>(), this, this, this);
        recyclerViewMyDecks.setAdapter(myDecksAdapter);
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
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading your flashcards...");
            contentConstraintLayout.setVisibility(View.INVISIBLE);
            fabCreate.setEnabled(false);
            backButton.setEnabled(false);


        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            contentConstraintLayout.setVisibility(View.VISIBLE);
            fabCreate.setEnabled(true);
            backButton.setEnabled(true);
        }
    }


    private void loadMyFlashcards() {
        startApiCall();

        apiService.getUserDefinedFlashcards().enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                finishApiCall();
                if (response.isSuccessful() && response.body() != null) {
                    List<FlashcardBasicResponse> flashcards = response.body();
                    myDecksAdapter.updateData(flashcards);
                    textViewNoDecks.setVisibility(flashcards.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewMyDecks.setVisibility(flashcards.isEmpty() ? View.GONE : View.VISIBLE);
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load flashcards: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Failed to load your flashcards.", Toast.LENGTH_SHORT).show();
                    textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                    recyclerViewMyDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                showLoading(false);
                Log.e(ACTIVITY_TAG, "Network error loading flashcards", t);
                Toast.makeText(MyFlashcardsActivity.this, "Network error loading your flashcards.", Toast.LENGTH_SHORT).show();
                textViewNoDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
                recyclerViewMyDecks.setVisibility(myDecksAdapter.getItemCount() == 0 ? View.GONE : View.VISIBLE);
            }
        });
    }

    @Override
    public void onFlashcardClick(FlashcardBasicResponse flashcard) {
        Intent intent = new Intent(this, FlashcardDetailActivity.class);
        intent.putExtra(FlashcardDetailActivity.FLASHCARD_ID, flashcard.getId());
        intent.putExtra(FlashcardDetailActivity.EXTRA_IS_PUBLIC, false);
        startActivity(intent);
    }

    @Override
    public void onFlashcardDeleteClick(FlashcardBasicResponse flashcard, int position) {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete the flashcard set '" + flashcard.getName() + "'?")
                .setPositiveButton("Delete", (dialog, which) -> deleteFlashcardApiCall(flashcard.getId(), position))
                .setNegativeButton("Cancel", null)
                .setIcon(R.drawable.ic_delete)
                .show();
    }

    @Override
    public void onFlashcardEditClick(FlashcardBasicResponse flashcard, int position) {
        Intent intent = new Intent(MyFlashcardsActivity.this, CreateFlashcardActivity.class);
        intent.putExtra(CreateFlashcardActivity.EXTRA_EDIT_MODE, true);
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_ID, flashcard.getId());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_NAME, flashcard.getName());
        intent.putExtra(CreateFlashcardActivity.EXTRA_FLASHCARD_IMAGE_URL, flashcard.getImageUrl());
        flashcardActivityResultLauncher.launch(intent);
    }

    private void deleteFlashcardApiCall(Long flashcardId, int position) {
        startApiCall();

        apiService.deleteFlashcard(flashcardId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                finishApiCall();

                if (response.isSuccessful()) {
                    Toast.makeText(MyFlashcardsActivity.this, "Flashcard set deleted successfully.", Toast.LENGTH_SHORT).show();
                    if (myDecksAdapter != null) {
                        loadMyFlashcards();
                    }
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to delete flashcard: " + response.code());
                    Toast.makeText(MyFlashcardsActivity.this, "Failed to delete: " + response.code(), Toast.LENGTH_SHORT).show();
                    loadMyFlashcards();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Network error deleting flashcard", t);
                Toast.makeText(MyFlashcardsActivity.this, "Network error while deleting.", Toast.LENGTH_SHORT).show();
                loadMyFlashcards();
            }
        });
    }
}