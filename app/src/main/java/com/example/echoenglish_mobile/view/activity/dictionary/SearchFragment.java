package com.example.echoenglish_mobile.view.activity.dictionary;


import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.speech.RecognizerIntent;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.dictionary.WordSuggestionAdapter;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private static final String TAG = "SearchFragment";
    private static final long SEARCH_DELAY_MS = 500;
    private static final int MIN_QUERY_LENGTH = 1;

    private static final String PREFS_NAME = "DictionaryPrefs";
    private static final String KEY_SEARCH_HISTORY = "searchHistory";
    private static final int MAX_HISTORY_SIZE = 20;


    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;

    private RecyclerView recyclerViewSuggestionsOverlay;

    private WordSuggestionAdapter suggestionAdapter;
    private List<Word> currentDisplayedList;

    private long lastFocusChangeTime = 0;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<?> currentApiCall;

    private SharedPreferences sharedPreferences;

    private ActivityResultLauncher<Intent> speechRecognitionLauncher;


    public interface SearchListener {
        void onWordDetailRequested(Word wordData);

        void showSuggestionsOverlay(RecyclerView recyclerViewSuggestions, int x, int y, int width);
        void hideSuggestionsOverlay();
    }

    private SearchListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SearchListener) {
            listener = (SearchListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement SearchListener and manage suggestions overlay");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        searchInputLayout = view.findViewById(R.id.search_input_layout);
        etSearch = view.findViewById(R.id.etSearch);


        currentDisplayedList = new ArrayList<>();
        suggestionAdapter = new WordSuggestionAdapter(requireContext(), currentDisplayedList, new WordSuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClick(Word word) {
                String wordToSearch = word.getWord();
                if (!TextUtils.isEmpty(wordToSearch)) {
                    handleSearchAction(wordToSearch);
                } else {
                    Log.w(TAG, "Clicked item has empty word string");
                }
                hideSuggestionsList();
            }

            @Override
            public void onDeleteHistoryClick(String wordToDelete) {
                Log.d(TAG, "Request to delete history item: " + wordToDelete);
                deleteWordFromHistory(wordToDelete);
            }

            @Override
            public void onArrowClick(String wordText) {
                Log.d(TAG, "Arrow clicked for: " + wordText);
                etSearch.setText(wordText);
                etSearch.setSelection(wordText.length());
                hideKeyboard(etSearch);
                hideSuggestionsList();
            }
        });


        speechRecognitionLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        handleSpeechRecognitionResult(result);
                    }
                }
        );



        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            long currentTime = SystemClock.elapsedRealtime();
            if (currentTime - lastFocusChangeTime < 200) {
                return;
            }
            lastFocusChangeTime = currentTime;

            if (hasFocus) {
                Log.d(TAG, "EditText gained focus");
                String currentText = etSearch.getText().toString().trim();
                if (currentText.isEmpty()) {
                    showSearchHistory();
                } else if (currentText.length() >= MIN_QUERY_LENGTH) {
                    Log.d(TAG, "Refocus with text: " + currentText + ". Fetching suggestions.");
                    hideSuggestionsList();
                    suggestionAdapter.updateData(null);
                    fetchSuggestionsFromApi(currentText);
                } else {
                    hideSuggestionsList();
                }
            } else {
                Log.d(TAG, "EditText lost focus");
                cancelPendingTasks();
            }
            updateEndIcon(TextUtils.isEmpty(etSearch.getText()));
        });

        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cancelPendingTasks();
                String query = s.toString().trim();

                if (query.length() >= MIN_QUERY_LENGTH) {
                    searchRunnable = () -> fetchSuggestionsFromApi(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                    hideSuggestionsList();
                } else if (query.isEmpty() && etSearch.hasFocus()) {
                    showSearchHistory();
                } else {
                    hideSuggestionsList();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                updateEndIcon(s.toString().isEmpty());
            }
        });

        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                String query = Objects.requireNonNull(etSearch.getText()).toString().trim();
                if (!query.isEmpty()) {
                    handleSearchAction(query);
                } else {
                    Toast.makeText(requireContext(), "Vui lòng nhập từ để tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                hideKeyboard(etSearch);
                hideSuggestionsList();
                return true;
            }
            return false;
        });

        updateEndIcon(TextUtils.isEmpty(etSearch.getText()));
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
        hideSuggestionsList();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (recyclerViewSuggestionsOverlay != null && recyclerViewSuggestionsOverlay.getParent() != null) {
            ((ViewGroup) recyclerViewSuggestionsOverlay.getParent()).removeView(recyclerViewSuggestionsOverlay);
            recyclerViewSuggestionsOverlay = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        cancelPendingTasks();
    }


    private void handleSpeechRecognitionResult(ActivityResult result) {
        if (result.getResultCode() == requireActivity().RESULT_OK && result.getData() != null) {
            ArrayList<String> recognizedTextList = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (recognizedTextList != null && !recognizedTextList.isEmpty()) {
                String recognizedText = recognizedTextList.get(0).trim();
                Log.d(TAG, "Voice Search Result: " + recognizedText);
                etSearch.setText(recognizedText);
                etSearch.setSelection(recognizedText.length());
                if (!recognizedText.isEmpty()) {
                    handleSearchAction(recognizedText);
                }
            } else {
                Log.w(TAG, "Voice search returned empty results.");
                Toast.makeText(requireContext(), "Didn't catch that. Please try again.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.w(TAG, "Voice search failed or cancelled. ResultCode: " + result.getResultCode());
        }
    }



    private void showSuggestionsList() {
        if (recyclerViewSuggestionsOverlay == null) {
            recyclerViewSuggestionsOverlay = new RecyclerView(requireContext());
            recyclerViewSuggestionsOverlay.setLayoutManager(new LinearLayoutManager(requireContext()));
            recyclerViewSuggestionsOverlay.setAdapter(suggestionAdapter);


            recyclerViewSuggestionsOverlay.setBackgroundResource(R.drawable.rounded_corners_drawable);

            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                recyclerViewSuggestionsOverlay.setClipToOutline(true);
            } else {
                float scale = requireContext().getResources().getDisplayMetrics().density;
                int paddingPx = (int) (4 * scale + 0.5f);
                recyclerViewSuggestionsOverlay.setPadding(paddingPx, paddingPx, paddingPx, paddingPx);
            }



        }

        int[] location = new int[2];
        searchInputLayout.getLocationOnScreen(location);
        int searchBarBottomY = location[1] + searchInputLayout.getHeight()-30;
        int searchBarLeftX = location[0];
        int searchBarWidth = searchInputLayout.getWidth();

        if (listener != null) {
            listener.showSuggestionsOverlay(recyclerViewSuggestionsOverlay, searchBarLeftX, searchBarBottomY, searchBarWidth);
            Log.d(TAG, "Notifying Activity to show overlay at x=" + searchBarLeftX + ", y=" + searchBarBottomY + ", width=" + searchBarWidth);
        }
    }


    public void hideSuggestionsList() {
        if (listener != null) {
            listener.hideSuggestionsOverlay();
        }
        if (suggestionAdapter != null) {
            suggestionAdapter.updateData(null);
        }
    }


    private void handleSearchAction(String query) {
        cancelPendingTasks();
        hideSuggestionsList();
        hideKeyboard(etSearch);
        saveWordStringToHistory(query);
        fetchWordDetailsAndNavigate(query);
    }

    private void updateEndIcon(boolean isEmpty) {
        Log.d(TAG, "updateEndIcon called, isEmpty: " + isEmpty);
        searchInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM);

        if (isEmpty) {
            searchInputLayout.setEndIconDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_xml_mic_24px));
            searchInputLayout.setEndIconOnClickListener(v -> startVoiceInput());
            Log.d(TAG, "updateEndIcon: Setting Voice Icon");
        } else {
            searchInputLayout.setEndIconDrawable(ContextCompat.getDrawable(requireContext(), R.drawable.ic_xml_close_24px));
            searchInputLayout.setEndIconOnClickListener(v -> {
                etSearch.setText("");
                hideKeyboard(etSearch);
                hideSuggestionsList();
                if (etSearch.hasFocus()) {
                    showSearchHistory();
                }
            });
            Log.d(TAG, "updateEndIcon: Setting Clear Icon");
        }
    }

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the word you want to search...");
        try {
            speechRecognitionLauncher.launch(intent);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Speech recognition not supported", e);
            Toast.makeText(requireContext(), "Sorry, your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSearchHistory() {
        Log.d(TAG, "Showing search history (from Strings)");
        List<String> historyStrings = getSearchHistoryStrings();
        if (!historyStrings.isEmpty()) {
            currentDisplayedList = historyStrings.stream()
                    .map(historyString -> {
                        Word historyWord = new Word();
                        historyWord.setWord(historyString);
                        historyWord.setFromHistory(true);
                        return historyWord;
                    })
                    .collect(Collectors.toList());

            suggestionAdapter.updateData(currentDisplayedList);
            showSuggestionsList();
        } else {
            Log.d(TAG, "No search history found.");
            hideSuggestionsList();
        }
    }

    private void fetchSuggestionsFromApi(String query) {
        Log.d(TAG, "Fetching SUGGESTIONS for: " + query);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Word>> suggestionCall = apiService.getWordSuggestions(query);
        currentApiCall = suggestionCall;

        hideSuggestionsList();

        suggestionCall.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                if (currentApiCall != call) return;
                currentApiCall = null;
                if (response.isSuccessful() && response.body() != null) {
                    currentDisplayedList = response.body();
                    Log.d(TAG, "Suggestions received: " + currentDisplayedList.size());
                    if (!currentDisplayedList.isEmpty()) {
                        suggestionAdapter.updateData(currentDisplayedList);
                        showSuggestionsList();
                    } else {
                        hideSuggestionsList();
                    }
                } else {
                    Log.w(TAG, "API suggestion response error: " + response.code());
                    hideSuggestionsList();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                if (currentApiCall != call) return;
                if (call.isCanceled()) {
                    Log.d(TAG, "Suggestion API call was cancelled.");
                } else {
                    Log.e(TAG, "Suggestion API call failed: ", t);
                    Toast.makeText(requireContext(), "Lỗi mạng, không thể lấy gợi ý", Toast.LENGTH_SHORT).show();
                    hideSuggestionsList();
                }
                currentApiCall = null;
            }
        });
    }

    private void fetchWordDetailsAndNavigate(String query) {
        Log.d(TAG, "Fetching word DETAILS for: " + query);

        ApiService apiService = ApiClient.getApiService();
        Call<Word> detailCall = apiService.getWordDetails(query);
        currentApiCall = detailCall;
        detailCall.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (currentApiCall != call) return;
                currentApiCall = null;
                if (response.isSuccessful() && response.body() != null) {
                    Word wordDetails = response.body();
                    Log.d(TAG, "Exact word details received for: " + wordDetails.getWord());
                    if (listener != null) {
                        listener.onWordDetailRequested(wordDetails);
                    }
                } else {
                    Log.w(TAG, "API detail response error or word not found: " + response.code());
                    String errorMsg = "Không tìm thấy từ '" + query + "'";
                    if (!response.isSuccessful()) {
                        errorMsg = "Lỗi khi tìm kiếm từ (" + response.code() + ")";
                    }
                    Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                if (currentApiCall != call) return;
                if (call.isCanceled()) {
                    Log.d(TAG, "Detail API call was cancelled.");
                } else {
                    Log.e(TAG, "Detail API call failed: ", t);
                    Toast.makeText(requireContext(), "Lỗi mạng, không thể tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                currentApiCall = null;
            }
        });
    }

    private void saveWordStringToHistory(String word) {
        if (word == null || word.trim().isEmpty()) return;
        String wordToSave = word.trim().toLowerCase();
        Set<String> historySet = new HashSet<>(sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new HashSet<>()));
        List<String> historyList = new ArrayList<>(historySet);
        historyList.remove(wordToSave);
        historyList.add(0, wordToSave);
        while (historyList.size() > MAX_HISTORY_SIZE) {
            historyList.remove(historyList.size() - 1);
        }
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putStringSet(KEY_SEARCH_HISTORY, new HashSet<>(historyList));
        editor.apply();
        Log.d(TAG, "Saved string '" + wordToSave + "' to history. New size: " + historyList.size());
    }

    private List<String> getSearchHistoryStrings() {
        Set<String> historySet = sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new HashSet<>());
        List<String> historyList = new ArrayList<>(historySet);
        Log.d(TAG, "Retrieved history strings: " + historyList.size() + " items");
        return historyList;
    }

    private void deleteWordFromHistory(String wordToDelete) {
        if (wordToDelete == null || wordToDelete.trim().isEmpty()) return;
        String wordKey = wordToDelete.trim().toLowerCase();
        Log.d(TAG, "Deleting '" + wordKey + "' from history");
        Set<String> historySet = new HashSet<>(sharedPreferences.getStringSet(KEY_SEARCH_HISTORY, new HashSet<>()));
        boolean removed = historySet.remove(wordKey);
        if (removed) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putStringSet(KEY_SEARCH_HISTORY, historySet);
            editor.apply();
            Log.d(TAG, "Successfully deleted '" + wordKey + "'.");
            if (etSearch.getText().toString().trim().isEmpty() && etSearch.hasFocus()) {
                showSearchHistory();
            } else {
                if (!etSearch.getText().toString().trim().isEmpty()) {
                    fetchSuggestionsFromApi(etSearch.getText().toString().trim());
                } else {
                    showSearchHistory();
                }
            }
        } else {
            Log.w(TAG, "Word '" + wordKey + "' not found in history to delete.");
        }
    }

    private void cancelPendingTasks() {
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null;
        }
        if (currentApiCall != null && !currentApiCall.isCanceled()) {
            Log.d(TAG, "Cancelling previous API call");
            currentApiCall.cancel();
        }
        currentApiCall = null;
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}