package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.content.Intent; // Thêm import
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent; // Thêm import
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView; // Thêm import (cho listener)
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.dictionary.DictionaryWordDetailActivity; // Đảm bảo import đúng
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DictionaryActivity extends AppCompatActivity {

    private static final String TAG = "DictionaryActivity";
    private static final long SEARCH_DELAY_MS = 500;
    private static final int MIN_QUERY_LENGTH = 2;

    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;
    private RecyclerView recyclerViewSuggestions;
    private ProgressBar progressBar;
    private WordSuggestionAdapter suggestionAdapter;
    private List<Word> suggestionList;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<?> currentApiCall; // Sử dụng wildcard '?' để có thể hủy cả suggestion và detail call

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        // Ánh xạ Views
        searchInputLayout = findViewById(R.id.search_input_layout);
        etSearch = findViewById(R.id.etSearch);
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions);
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo danh sách và Adapter
        suggestionList = new ArrayList<>();
        suggestionAdapter = new WordSuggestionAdapter(this, suggestionList);
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        // Xử lý sự kiện nhập text với Debouncing (cho gợi ý)
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                cancelPendingTasks(); // Hủy tác vụ cũ

                String query = s.toString().trim();

                if (query.length() >= MIN_QUERY_LENGTH) {
                    searchRunnable = () -> fetchSuggestionsFromApi(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    suggestionAdapter.updateData(null);
                    progressBar.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // --- XỬ LÝ NÚT SEARCH TRÊN BÀN PHÍM ---
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                // Kiểm tra actionId hoặc sự kiện nhấn Enter
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == EditorInfo.IME_ACTION_DONE ||
                        (event != null && event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {

                    String query = Objects.requireNonNull(etSearch.getText()).toString().trim();

                    if (query.length() >= MIN_QUERY_LENGTH) {
                        cancelPendingTasks(); // Hủy debounce và API gợi ý cũ

                        // Ẩn gợi ý và bàn phím ngay lập tức
                        recyclerViewSuggestions.setVisibility(View.GONE);
                        suggestionAdapter.updateData(null); // Xóa data gợi ý cũ
                        hideKeyboard(etSearch);

                        // Gọi API lấy chi tiết từ và điều hướng
                        fetchWordDetailsAndNavigate(query);

                    } else {
                        Toast.makeText(DictionaryActivity.this, "Vui lòng nhập ít nhất " + MIN_QUERY_LENGTH + " ký tự", Toast.LENGTH_SHORT).show();
                    }
                    return true; // Đã xử lý sự kiện
                }
                return false; // Không xử lý, để hệ thống tự xử lý (nếu cần)
            }
        });
    }

    // Hàm gọi API để lấy GỢI Ý
    private void fetchSuggestionsFromApi(String query) {
        Log.d(TAG, "Fetching SUGGESTIONS for: " + query);
        progressBar.setVisibility(View.VISIBLE);
        recyclerViewSuggestions.setVisibility(View.GONE);

        ApiService apiService = ApiClient.getApiService();
        Call<List<Word>> suggestionCall = apiService.getWordSuggestions(query);
        currentApiCall = suggestionCall; // Lưu lại để có thể hủy

        suggestionCall.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                if (currentApiCall != call) return; // Nếu call này đã bị hủy thì bỏ qua
                progressBar.setVisibility(View.GONE);
                currentApiCall = null;

                if (response.isSuccessful() && response.body() != null) {
                    suggestionList = response.body();
                    Log.d(TAG, "Suggestions received: " + suggestionList.size());
                    if (!suggestionList.isEmpty()) {
                        suggestionAdapter.updateData(suggestionList);
                        recyclerViewSuggestions.setVisibility(View.VISIBLE);
                    } else {
                        Log.d(TAG, "No suggestions found.");
                        recyclerViewSuggestions.setVisibility(View.GONE);
                        // Không cần Toast ở đây vì có thể người dùng nhấn Search
                    }
                } else {
                    Log.w(TAG, "API suggestion response error: " + response.code());
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    // Không nên Toast lỗi ở đây khi chỉ là gợi ý
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                if (currentApiCall != call) return; // Nếu call này đã bị hủy thì bỏ qua
                if (call.isCanceled()) {
                    Log.d(TAG, "Suggestion API call was cancelled.");
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Suggestion API call failed: ", t);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    // Không nên Toast lỗi ở đây khi chỉ là gợi ý
                }
                currentApiCall = null;
            }
        });
    }

    // --- HÀM MỚI: Gọi API lấy CHI TIẾT TỪ và điều hướng ---
    private void fetchWordDetailsAndNavigate(String query) {
        Log.d(TAG, "Fetching word DETAILS for: " + query);
        progressBar.setVisibility(View.VISIBLE); // Hiển thị loading
        recyclerViewSuggestions.setVisibility(View.GONE); // Đảm bảo gợi ý bị ẩn

        ApiService apiService = ApiClient.getApiService();
        // *** Giả sử bạn có endpoint trong ApiService trả về một Word duy nhất ***
        Call<Word> detailCall = apiService.getWordDetails(query); // <--- THAY THẾ BẰNG ENDPOINT ĐÚNG CỦA BẠN
        currentApiCall = detailCall; // Lưu lại để có thể hủy

        detailCall.enqueue(new Callback<Word>() {
            @Override
            public void onResponse(@NonNull Call<Word> call, @NonNull Response<Word> response) {
                if (currentApiCall != call) return; // Bỏ qua nếu đã bị hủy
                progressBar.setVisibility(View.GONE);
                currentApiCall = null;

                if (response.isSuccessful() && response.body() != null) {
                    Word wordDetails = response.body();
                    Log.d(TAG, "Exact word details received for: " + wordDetails.getWord());

                    // --- Điều hướng sang Detail Activity ---
                    Intent intent = new Intent(DictionaryActivity.this, DictionaryWordDetailActivity.class);
                    intent.putExtra("word_data", wordDetails); // Truyền dữ liệu từ đầy đủ
                    startActivity(intent);

                    // (Tùy chọn) Xóa text trong ô tìm kiếm sau khi chuyển trang thành công
                    // etSearch.setText("");

                } else {
                    // Xử lý lỗi: Không tìm thấy từ (404) hoặc lỗi khác
                    Log.w(TAG, "API detail response error or word not found: " + response.code());
                    String errorMsg = "Không tìm thấy từ '" + query + "'";
                    if (!response.isSuccessful()) {
                        // Có thể thêm chi tiết lỗi dựa vào response.code() nếu cần
                        errorMsg = "Lỗi khi tìm kiếm từ (" + response.code() + ")";
                    }
                    // Chỉ hiển thị Toast lỗi khi người dùng nhấn Search và thất bại
                    Toast.makeText(DictionaryActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Word> call, @NonNull Throwable t) {
                if (currentApiCall != call) return; // Bỏ qua nếu đã bị hủy
                if (call.isCanceled()) {
                    Log.d(TAG, "Detail API call was cancelled.");
                } else {
                    progressBar.setVisibility(View.GONE);
                    Log.e(TAG, "Detail API call failed: ", t);
                    // Chỉ hiển thị Toast lỗi khi người dùng nhấn Search và thất bại
                    Toast.makeText(DictionaryActivity.this, "Lỗi mạng, không thể tìm kiếm", Toast.LENGTH_SHORT).show();
                }
                currentApiCall = null;
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelPendingTasks(); // Hủy các tác vụ khi Activity bị hủy
    }

    // Hàm tiện ích để hủy các tác vụ đang chờ hoặc đang chạy
    private void cancelPendingTasks() {
        // Hủy bỏ Runnable debounce cũ nếu có
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
            searchRunnable = null; // Clear runnable
        }
        // Hủy bỏ API call cũ nếu đang chạy
        if (currentApiCall != null && !currentApiCall.isCanceled()) {
            Log.d(TAG, "Cancelling previous API call");
            currentApiCall.cancel();
        }
        currentApiCall = null; // Clear call reference
    }


    // --- PHẦN dispatchTouchEvent (giữ nguyên) ---
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View focusedView = getCurrentFocus();
            if (focusedView instanceof EditText && focusedView == etSearch) {
                Rect outRectEditText = new Rect();
                etSearch.getGlobalVisibleRect(outRectEditText);

                Rect outRectRecyclerView = new Rect();
                if (recyclerViewSuggestions.getVisibility() == View.VISIBLE) {
                    recyclerViewSuggestions.getGlobalVisibleRect(outRectRecyclerView);
                }

                if (!outRectEditText.contains((int) ev.getRawX(), (int) ev.getRawY()) &&
                        (recyclerViewSuggestions.getVisibility() != View.VISIBLE || !outRectRecyclerView.contains((int) ev.getRawX(), (int) ev.getRawY())))
                {
                    etSearch.clearFocus();
                    hideKeyboard(etSearch);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    // Không cần xóa data adapter ở đây nếu muốn giữ lại khi focus lại
                }
            } else if (focusedView == null || !(focusedView instanceof EditText)) {
                // Cân nhắc việc có nên ẩn bàn phím khi chạm ra ngoài nếu focus không phải EditText không
                // hideKeyboard(getWindow().getDecorView()); // Ẩn bàn phím cho bất kỳ view nào
            }
        }
        return super.dispatchTouchEvent(ev);
    }

    // Hàm tiện ích để ẩn bàn phím (giữ nguyên)
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}