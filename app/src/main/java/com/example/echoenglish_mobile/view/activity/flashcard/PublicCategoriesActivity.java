package com.example.echoenglish_mobile.view.activity.flashcard;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.CategoryAdapter;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.CategoryResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.PublicFlashcardsActivity;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PublicCategoriesActivity extends AppCompatActivity implements CategoryAdapter.OnCategoryClickListener<CategoryResponse> {

    private static final String ACTIVITY_TAG = "PublicCategories";
    private static final String LOADING_DIALOG_TAG = "PublicCategoriesLoadingDialog";


    private View backButton;
    private TextView textScreenTitle;


    private RecyclerView recyclerViewCategories;
    private CategoryAdapter<CategoryResponse> categoryAdapter;
    private TextView textViewNoCategories;

    private ApiService apiService;

    private View contentFrame;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_public_categories);


        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);
        textScreenTitle.setText("Flashcard Categories");

        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        textViewNoCategories = findViewById(R.id.textViewNoCategories);
        contentFrame = findViewById(R.id.contentFrame);


        apiService = ApiClient.getApiService();

        setupRecyclerView();
        loadPublicCategories();

        backButton.setOnClickListener(v -> finish());
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.d(ACTIVITY_TAG, "onResume: Loading public categories...");
        loadPublicCategories();
    }


    private void setupRecyclerView() {
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this));
        categoryAdapter = new CategoryAdapter<>(this, new ArrayList<>(), category -> {
            if (category == null || category.getId() == null) {
                Log.e(ACTIVITY_TAG, "Category or Category ID is null on click.");
                Toast.makeText(this, "Cannot open this category.", Toast.LENGTH_SHORT).show();
                return;
            }
            Log.d(ACTIVITY_TAG, "Category clicked: ID=" + category.getId() + ", Name=" + category.getName());
            Intent intent = new Intent(this, PublicFlashcardsActivity.class);
            intent.putExtra(PublicFlashcardsActivity.CATEGORY_ID_EXTRA, category.getId());
            intent.putExtra(PublicFlashcardsActivity.CATEGORY_NAME_EXTRA, category.getName());
            startActivity(intent);
        });
        recyclerViewCategories.setAdapter(categoryAdapter);
    }


    private int loadingApiCount = 0;

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
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Loading categories...");
            contentFrame.setVisibility(View.INVISIBLE);
            backButton.setEnabled(false);
            recyclerViewCategories.setEnabled(false);
        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
            contentFrame.setVisibility(View.VISIBLE);
            backButton.setEnabled(true);
            recyclerViewCategories.setEnabled(true);
        }
    }


    private void loadPublicCategories() {
        startApiCall();

        apiService.getPublicCategories().enqueue(new Callback<List<CategoryResponse>>() {
            @Override
            public void onResponse(Call<List<CategoryResponse>> call, Response<List<CategoryResponse>> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    List<CategoryResponse> categories = response.body();
                    categoryAdapter.updateData(categories);

                    textViewNoCategories.setVisibility(categories.isEmpty() ? View.VISIBLE : View.GONE);
                    recyclerViewCategories.setVisibility(categories.isEmpty() ? View.GONE : View.VISIBLE);

                    Log.d(ACTIVITY_TAG, "Loaded " + categories.size() + " public categories.");
                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load categories: " + response.code() + " - " + response.message());
                    Toast.makeText(PublicCategoriesActivity.this, "Failed to load categories.", Toast.LENGTH_SHORT).show();

                    if (categoryAdapter.getItemCount() == 0) {
                        textViewNoCategories.setVisibility(View.VISIBLE);
                        recyclerViewCategories.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(Call<List<CategoryResponse>> call, Throwable t) {
                finishApiCall();

                Log.e(ACTIVITY_TAG, "Network error loading categories", t);
                Toast.makeText(PublicCategoriesActivity.this, "Network error.", Toast.LENGTH_SHORT).show();

                if (categoryAdapter.getItemCount() == 0) {
                    textViewNoCategories.setVisibility(View.VISIBLE);
                    recyclerViewCategories.setVisibility(View.GONE);
                }
            }
        });
    }

    @Override
    public void onCategoryClick(CategoryResponse category) {
        Log.d(ACTIVITY_TAG, "Category clicked: ID=" + category.getId() + ", Name=" + category.getName());
        Intent intent = new Intent(this, PublicFlashcardsActivity.class);
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_ID_EXTRA, category.getId());
        intent.putExtra(PublicFlashcardsActivity.CATEGORY_NAME_EXTRA, category.getName());
        startActivity(intent);
    }
}