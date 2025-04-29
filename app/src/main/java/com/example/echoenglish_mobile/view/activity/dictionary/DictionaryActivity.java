package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DictionaryActivity extends AppCompatActivity {

    private static final String TAG = "DictionaryActivity";
    private static final long SEARCH_DELAY_MS = 500;
    private static final int MIN_QUERY_LENGTH = 1;

    private static final String PREFS_NAME = "DictionaryPrefs";
    private static final String KEY_SEARCH_HISTORY = "searchHistory";
    private static final int MAX_HISTORY_SIZE = 20;
    private static final int REQUEST_CODE_SPEECH_INPUT = 100;

    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;
    private RecyclerView recyclerViewSuggestions;
    private ProgressBar progressBar;
    private Button btnWordPronunAnalyze;
    private WordSuggestionAdapter suggestionAdapter;
    private List<Word> currentDisplayedList;

    private long lastFocusChangeTime = 0; // Biến để tránh xử lý focus quá nhanh

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<?> currentApiCall;

    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        searchInputLayout = findViewById(R.id.search_input_layout);
        etSearch = findViewById(R.id.etSearch);
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions);
        progressBar = findViewById(R.id.progressBar);

        currentDisplayedList = new ArrayList<>();
        suggestionAdapter = new WordSuggestionAdapter(this, currentDisplayedList, new WordSuggestionAdapter.OnSuggestionClickListener() {
            @Override
            public void onSuggestionClick(Word word) {
                String wordToSearch = word.getWord();
                if (!TextUtils.isEmpty(wordToSearch)) {
                    handleSearchAction(wordToSearch);
                } else {
                    Log.w(TAG, "Clicked item has empty word string");
                }
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
                recyclerViewSuggestions.setVisibility(View.GONE);
            }
        });

        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        // --- SỬA LẠI FocusChangeListener ---
        etSearch.setOnFocusChangeListener((v, hasFocus) -> {
            long currentTime = SystemClock.elapsedRealtime();
            // Đôi khi sự kiện focus có thể bị gọi liên tục rất nhanh, thêm debounce nhỏ
            if (currentTime - lastFocusChangeTime < 200) {
                return; // Bỏ qua nếu sự kiện focus quá gần nhau
            }
            lastFocusChangeTime = currentTime;


            if (hasFocus) {
                Log.d(TAG, "EditText gained focus");
                String currentText = etSearch.getText().toString().trim();
                if (currentText.isEmpty()) {
                    // Text rỗng -> Hiển thị lịch sử
                    showSearchHistory();
                    progressBar.setVisibility(View.GONE); // Đảm bảo không có loading
                } else if (currentText.length() >= MIN_QUERY_LENGTH) {
                    // Text không rỗng và đủ dài -> Fetch lại gợi ý
                    Log.d(TAG, "Refocus with text: " + currentText + ". Fetching suggestions.");
                    // Ẩn gợi ý cũ (nếu có) và hiện loading ngay lập tức
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    suggestionAdapter.updateData(null); // Xóa data cũ khỏi adapter
                    progressBar.setVisibility(View.VISIBLE);
                    // Gọi API ngay lập tức (không cần debounce như khi gõ)
                    fetchSuggestionsFromApi(currentText);
                } else {
                    // Text không rỗng nhưng quá ngắn -> Không làm gì, chỉ ẩn progress bar
                    progressBar.setVisibility(View.GONE);
                    recyclerViewSuggestions.setVisibility(View.GONE); // Đảm bảo ẩn list
                }
            } else {
                Log.d(TAG, "EditText lost focus");
                // Khi mất focus, không cần làm gì ở đây vì dispatchTouchEvent đã xử lý việc ẩn list
                // Chỉ cần đảm bảo progress bar ẩn nếu đang hiện (ví dụ mất focus khi đang loading)
                if (progressBar.getVisibility() == View.VISIBLE) {
                    progressBar.setVisibility(View.GONE);
                    cancelPendingTasks(); // Hủy API call nếu đang chạy
                }
            }
            // Luôn cập nhật icon sau khi xử lý focus xong
            updateEndIcon(TextUtils.isEmpty(etSearch.getText()));
        });
        // ---------------------------------

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
                    progressBar.setVisibility(View.VISIBLE);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                } else if (query.isEmpty() && etSearch.hasFocus()) {
                    showSearchHistory();
                    progressBar.setVisibility(View.GONE);
                } else {
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    suggestionAdapter.updateData(null);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Luôn cập nhật icon sau khi text thay đổi
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
                    Toast.makeText(DictionaryActivity.this, "Vui lòng nhập từ để tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
            return false;
        });

        // Gọi lần đầu để đặt icon ban đầu (Mic)
        updateEndIcon(TextUtils.isEmpty(etSearch.getText()));
    }

    private void handleSearchAction(String query) {
        cancelPendingTasks();
        recyclerViewSuggestions.setVisibility(View.GONE);
        suggestionAdapter.updateData(null);
        hideKeyboard(etSearch);
        progressBar.setVisibility(View.VISIBLE);
        saveWordStringToHistory(query);
        fetchWordDetailsAndNavigate(query);
    }

    // --- SỬA LẠI HÀM NÀY ĐỂ LUÔN DÙNG CUSTOM ICON ---
    private void updateEndIcon(boolean isEmpty) {
        Log.d(TAG, "updateEndIcon called, isEmpty: " + isEmpty); // Thêm log
        searchInputLayout.setEndIconMode(TextInputLayout.END_ICON_CUSTOM); // Luôn đặt là CUSTOM

        if (isEmpty) {
            // Text rỗng -> Hiện icon Voice
            searchInputLayout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_xml_mic_24px)); // Đặt icon voice
            searchInputLayout.setEndIconOnClickListener(v -> startVoiceInput()); // Gắn listener voice
            Log.d(TAG, "updateEndIcon: Setting Voice Icon");
        } else {
            // Text không rỗng -> Hiện icon Clear (X)
            searchInputLayout.setEndIconDrawable(ContextCompat.getDrawable(this, R.drawable.ic_xml_close_24px)); // Đặt icon clear (X)
            searchInputLayout.setEndIconOnClickListener(v -> { // Gắn listener clear
                etSearch.setText(""); // Xóa text
                // TextWatcher sẽ tự động gọi lại updateEndIcon(true) để đổi về icon voice
                // Có thể thêm logic ẩn bàn phím hoặc hiện lịch sử ở đây nếu muốn
                hideKeyboard(etSearch);
                if (etSearch.hasFocus()) { // Chỉ hiện lịch sử nếu vẫn còn focus
                    showSearchHistory();
                }
            });
            Log.d(TAG, "updateEndIcon: Setting Clear Icon");
        }
    }
    // ---------------------------------------------------

    private void startVoiceInput() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak the word you want to search...");
        try {
            startActivityForResult(intent, REQUEST_CODE_SPEECH_INPUT);
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "Speech recognition not supported", e);
            Toast.makeText(this, "Sorry, your device doesn't support speech input", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SPEECH_INPUT) {
            if (resultCode == RESULT_OK && data != null) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                if (result != null && !result.isEmpty()) {
                    String recognizedText = result.get(0).trim();
                    Log.d(TAG, "Voice Search Result: " + recognizedText);
                    etSearch.setText(recognizedText);
                    etSearch.setSelection(recognizedText.length());
                    if (!recognizedText.isEmpty()) {
                        handleSearchAction(recognizedText);
                    }
                } else {
                    Log.w(TAG, "Voice search returned empty results.");
                    Toast.makeText(this, "Didn't catch that. Please try again.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.w(TAG, "Voice search failed or cancelled. ResultCode: " + resultCode);
            }
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
                        historyWord.setFromHistory(true); // Đánh dấu từ lịch sử
                        return historyWord;
                    })
                    .collect(Collectors.toList());

            suggestionAdapter.updateData(currentDisplayedList);
            recyclerViewSuggestions.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            // searchInputLayout.setHint("Search word... (showing history)"); // Có thể bỏ hint này
        } else {
            Log.d(TAG, "No search history found.");
            recyclerViewSuggestions.setVisibility(View.GONE);
            suggestionAdapter.updateData(null);
            // searchInputLayout.setHint("Search word..."); // Có thể bỏ hint này
        }
        progressBar.setVisibility(View.GONE);
    }

    private void fetchSuggestionsFromApi(String query) {
        Log.d(TAG, "Fetching SUGGESTIONS for: " + query);
        ApiService apiService = ApiClient.getApiService();
        Call<List<Word>> suggestionCall = apiService.getWordSuggestions(query);
        currentApiCall = suggestionCall;

        suggestionCall.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                if (currentApiCall != call) return;
                progressBar.setVisibility(View.GONE);
                currentApiCall = null;
                if (response.isSuccessful() && response.body() != null) {
                    // Word từ API sẽ có isFromHistory là false (mặc định)
                    currentDisplayedList = response.body();
                    Log.d(TAG, "Suggestions received: " + currentDisplayedList.size());
                    if (!currentDisplayedList.isEmpty()) {
                        suggestionAdapter.updateData(currentDisplayedList);
                        recyclerViewSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        recyclerViewSuggestions.setVisibility(View.GONE);
                    }
                } else {
                    Log.w(TAG, "API suggestion response error: " + response.code());
                    recyclerViewSuggestions.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                if (currentApiCall != call) return;
                if (call.isCanceled()) {
                    Log.d(TAG, "Suggestion API call was cancelled.");
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Suggestion API call failed: ", t);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                }
                currentApiCall = null;
            }
        });
    }

    private void fetchWordDetailsAndNavigate(String query) {
        Log.d(TAG, "Fetching word DETAILS for: " + query);
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = ApiClient.getApiService();
        Call<Word> detailCall = apiService.getWordDetails(query);
        currentApiCall = detailCall;
        detailCall.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (currentApiCall != call) return;
                progressBar.setVisibility(View.GONE);
                currentApiCall = null;
                if (response.isSuccessful() && response.body() != null) {
                    Word wordDetails = response.body();
                    Log.d(TAG, "Exact word details received for: " + wordDetails.getWord());
                    navigateToDetail(wordDetails);
                } else {
                    Log.w(TAG, "API detail response error or word not found: " + response.code());
                    String errorMsg = "Không tìm thấy từ '" + query + "'";
                    if (!response.isSuccessful()) {
                        errorMsg = "Lỗi khi tìm kiếm từ (" + response.code() + ")";
                    }
                    Toast.makeText(DictionaryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                if (currentApiCall != call) return;
                if (call.isCanceled()) {
                    Log.d(TAG, "Detail API call was cancelled.");
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Detail API call failed: ", t);
                    Toast.makeText(DictionaryActivity.this, "Lỗi mạng, không thể tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                currentApiCall = null;
            }
        });
    }

    private void navigateToDetail(Word wordData) {
        Intent intent = new Intent(DictionaryActivity.this, DictionaryWordDetailActivity.class);
        intent.putExtra("word_data", wordData);
        startActivity(intent);
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
            showSearchHistory(); // Refresh the list
        } else {
            Log.w(TAG, "Word '" + wordKey + "' not found in history to delete.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingTasks();
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView instanceof EditText && focusedView == etSearch) {
                Rect outRectEditText = new Rect();
                etSearch.getGlobalVisibleRect(outRectEditText);
                Rect outRectRecyclerView = new Rect();
                boolean isRecyclerViewVisible = recyclerViewSuggestions.getVisibility() == View.VISIBLE;
                if (isRecyclerViewVisible) {
                    recyclerViewSuggestions.getGlobalVisibleRect(outRectRecyclerView);
                }
                if (!outRectEditText.contains((int) ev.getRawX(), (int) ev.getRawY()) &&
                        (!isRecyclerViewVisible || !outRectRecyclerView.contains((int) ev.getRawX(), (int) ev.getRawY()))) {
                    etSearch.clearFocus();
                    hideKeyboard(etSearch);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                }
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}