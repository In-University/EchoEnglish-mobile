package com.example.echoenglish_mobile.view.dialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddWordToFlashcardDialog extends DialogFragment {
    private Long currentUserId = SharedPrefManager.getInstance(MyApp.getAppContext()).getUserInfo().getId();
    private static final String TAG = "AddToFlashcardDialog";
    private static final String ARG_WORD = "word_to_add";

    private ApiService apiService;
    private static final String ARG_WORD_DETAIL = "arg_word_detail";
    private Word currentWordDetail;

    private TextInputLayout textInputLayout;
    private AutoCompleteTextView autoCompleteTextView;
    private ProgressBar loadingIndicator;

    private List<FlashcardBasicResponse> loadedFlashcards;
    private FlashcardBasicResponse selectedFlashcard = null;

    public static AddWordToFlashcardDialog newInstance(String word) {
        AddWordToFlashcardDialog fragment = new AddWordToFlashcardDialog();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        fragment.setArguments(args);
        return fragment;
    }

    public static AddWordToFlashcardDialog newInstance(Word wordDetail) {
        AddWordToFlashcardDialog fragment = new AddWordToFlashcardDialog();
        Bundle args = new Bundle();
        args.putSerializable(ARG_WORD_DETAIL, wordDetail);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            currentWordDetail = (Word) getArguments().getSerializable(ARG_WORD_DETAIL);
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_to_flashcard, null);

        textInputLayout = dialogView.findViewById(R.id.dialog_flashcard_layout);
        autoCompleteTextView = dialogView.findViewById(R.id.dialog_flashcard_dropdown);
        loadingIndicator = dialogView.findViewById(R.id.dialog_loading_indicator);

        apiService = ApiClient.getApiService();

        builder.setView(dialogView)
                .setTitle("Add \"" + currentWordDetail.getWord() + "\" vào bộ thẻ")
                .setPositiveButton("Add", null)
                .setNegativeButton("Cancel", (dialog, which) -> dismiss());

        AlertDialog alertDialog = builder.create();

        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false);

            positiveButton.setOnClickListener(view -> {
                if (selectedFlashcard != null && currentWordDetail != null) {
                    VocabularyCreateRequest request = new VocabularyCreateRequest();
                    request.setWord(currentWordDetail.getWord());
                    request.setImageUrl(currentWordDetail.getImageUrl());
                    request.setPhonetic("/" + currentWordDetail.getUkPronunciation() + "/");
                    request.setType(currentWordDetail.getMeanings() != null && !currentWordDetail.getMeanings().isEmpty()
                            ? currentWordDetail.getMeanings().get(0).getPartOfSpeech()
                            : null);
                    request.setDefinition(currentWordDetail.getMeanings() != null && !currentWordDetail.getMeanings().isEmpty()
                            ? currentWordDetail.getMeanings().get(0).getDefinition()
                            : null);
                    request.setExample(currentWordDetail.getMeanings() != null && !currentWordDetail.getMeanings().isEmpty()
                            ? currentWordDetail.getMeanings().get(0).getExample()
                            : null);

                    apiService.addVocabulary(selectedFlashcard.getId(), request).enqueue(new Callback<VocabularyResponse>() {
                        @Override
                        public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Successfully added \"" + currentWordDetail.getWord() +
                                        "\" to flashcard \"" + selectedFlashcard.getName() + "\"", Toast.LENGTH_SHORT).show();
                                dismiss();
                            } else {
                                Toast.makeText(getContext(), "Failed to add vocabulary: " + response.code(), Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                            Toast.makeText(getContext(), "Network error while adding vocabulary: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

                } else {
                    textInputLayout.setError("Vui lòng chọn một bộ thẻ");
                }
            });

            if (currentUserId != null && apiService != null) {
                loadUserFlashcards(currentUserId, alertDialog);
            } else {
                Log.e(TAG, "Cannot load flashcards: Missing User ID or ApiService.");
                showErrorState("Cannot load flashcards!");
                positiveButton.setEnabled(false);
            }
        });


        return alertDialog;
    }

    private void loadUserFlashcards(Long creatorId, AlertDialog dialog) {
        showLoadingState(true);
        selectedFlashcard = null;
        autoCompleteTextView.setText("", false);
        textInputLayout.setError(null);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);


        apiService.getFlashcardsByCreator(creatorId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                if (!isAdded() || dialog == null || !dialog.isShowing()) return;

                showLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    loadedFlashcards = response.body();
                    if (loadedFlashcards.isEmpty()) {
                        showErrorState("You don't have any flashcards.");
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    } else {
                        populateDropdown(loadedFlashcards);
                        textInputLayout.setEnabled(true);
                    }
                } else {
                    Log.e(TAG, "Failed to load flashcards. Code: " + response.code());
                    String errorMsg = "Failed to load flashcards (" + response.code() + ")";
                    showErrorState(errorMsg);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                if (!isAdded() || dialog == null || !dialog.isShowing()) return;

                Log.e(TAG, "Network error loading flashcards", t);
                showLoadingState(false);
                showErrorState("Network error loading flashcards: " + t.getMessage());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Disable nút
            }
        });
    }

    private void populateDropdown(List<FlashcardBasicResponse> flashcards) {
        List<String> flashcardTitles = new ArrayList<>();
        for (FlashcardBasicResponse flashcard : flashcards) {
            flashcardTitles.add(flashcard.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_dropdown_item_1line,
                flashcardTitles
        );
        autoCompleteTextView.setAdapter(adapter);

        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            if (loadedFlashcards != null && position >= 0 && position < loadedFlashcards.size()) {
                selectedFlashcard = loadedFlashcards.get(position);
                textInputLayout.setError(null);
                Dialog currentDialog = getDialog();
                if (currentDialog instanceof AlertDialog) {
                    ((AlertDialog) currentDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                Log.d(TAG, "Selected Flashcard: " + selectedFlashcard.getName() + " (ID: " + selectedFlashcard.getId() + ")");
            } else {
                selectedFlashcard = null;
                Dialog currentDialog = getDialog();
                if (currentDialog instanceof AlertDialog) {
                    ((AlertDialog) currentDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    private void showLoadingState(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (textInputLayout != null) {
            textInputLayout.setEnabled(!isLoading);
            if(isLoading) {
                autoCompleteTextView.setText("Loading...", false);
            }
        }
    }

    // --- Hàm cập nhật UI cho trạng thái lỗi ---
    private void showErrorState(String message) {
        if (textInputLayout != null) {
            textInputLayout.setEnabled(false); // Disable dropdown khi lỗi
            autoCompleteTextView.setText(message, false); // Hiển thị lỗi
            // textInputLayout.setError(message); // Hoặc hiện lỗi dưới dạng error của TextInputLayout
        }
        // Đảm bảo loading ẩn đi
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(View.GONE);
        }
    }



    @Override
    public void onDetach() {
        super.onDetach();
    }
}
