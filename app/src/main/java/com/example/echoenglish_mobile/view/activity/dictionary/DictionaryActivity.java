package com.example.echoenglish_mobile.view.activity.dictionary;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.Word;
// Đảm bảo import đúng ApiClient và ApiService
// Import Adapter mới
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
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
    private static final long SEARCH_DELAY_MS = 500; // Thời gian chờ trước khi gọi API (ms)
    private static final int MIN_QUERY_LENGTH = 2; // Số ký tự tối thiểu để bắt đầu tìm kiếm

    private TextInputLayout searchInputLayout;
    private TextInputEditText etSearch;
    private RecyclerView recyclerViewSuggestions;
    private ProgressBar progressBar;
    private WordSuggestionAdapter suggestionAdapter;
    private List<Word> suggestionList;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<List<Word>> currentApiCall; // Để hủy bỏ yêu cầu cũ

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dictionary);

        // Ánh xạ Views
        searchInputLayout = findViewById(R.id.search_input_layout); // Layout chứa EditText
        etSearch = findViewById(R.id.etSearch);
        recyclerViewSuggestions = findViewById(R.id.recyclerViewSuggestions); // Đổi ID RecyclerView
        progressBar = findViewById(R.id.progressBar);

        // Khởi tạo danh sách và Adapter
        suggestionList = new ArrayList<>();
        suggestionAdapter = new WordSuggestionAdapter(this, suggestionList);
        recyclerViewSuggestions.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewSuggestions.setAdapter(suggestionAdapter);

        // Xử lý sự kiện nhập text với Debouncing
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Hủy bỏ Runnable cũ nếu có
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                // Hủy bỏ API call cũ nếu đang chạy
                if (currentApiCall != null && !currentApiCall.isCanceled()) {
                    currentApiCall.cancel();
                }

                String query = s.toString().trim();

                if (query.length() >= MIN_QUERY_LENGTH) {
                    // Tạo Runnable mới để gọi API sau một khoảng delay
                    searchRunnable = () -> fetchSuggestionsFromApi(query);
                    searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
                } else {
                    // Nếu query quá ngắn hoặc bị xóa hết, ẩn gợi ý
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    suggestionAdapter.updateData(null); // Xóa danh sách cũ
                    progressBar.setVisibility(View.GONE); // Ẩn progress bar
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Optional: Xử lý nút Search trên bàn phím (nếu cần)
        etSearch.setOnEditorActionListener((v, actionId, event) -> {
            // if (actionId == EditorInfo.IME_ACTION_SEARCH) {
            //     String query = etSearch.getText().toString().trim();
            //     if (!query.isEmpty()) {
            //         fetchSuggestionsFromApi(query); // Gọi API ngay lập tức
            //         // Ẩn bàn phím
            //         // InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            //         // imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
            //     }
            //     return true;
            // }
            return false;
        });
    }

    // Hàm gọi API để lấy gợi ý
    private void fetchSuggestionsFromApi(String query) {
        Log.d(TAG, "Fetching suggestions for: " + query);
        progressBar.setVisibility(View.VISIBLE); // Hiển thị loading
        recyclerViewSuggestions.setVisibility(View.GONE); // Ẩn danh sách cũ

        ApiService apiService = ApiClient.getApiService();
        currentApiCall = apiService.getWordSuggestions(query); // Gọi API gợi ý

        currentApiCall.enqueue(new Callback<List<Word>>() {
            @Override
            public void onResponse(@NonNull Call<List<Word>> call, @NonNull Response<List<Word>> response) {
                progressBar.setVisibility(View.GONE); // Ẩn loading
                currentApiCall = null; // Xóa tham chiếu call hiện tại

                if (response.isSuccessful() && response.body() != null) {
                    suggestionList = response.body();
                    Log.d(TAG, "Suggestions received: " + suggestionList.size());
                    if (!suggestionList.isEmpty()) {
                        suggestionAdapter.updateData(suggestionList);
                        recyclerViewSuggestions.setVisibility(View.VISIBLE); // Hiển thị RecyclerView
                    } else {
                        Log.d(TAG, "No suggestions found.");
                        recyclerViewSuggestions.setVisibility(View.GONE); // Không có kết quả thì ẩn
                        // Toast.makeText(DictionaryActivity.this, "No suggestions found", Toast.LENGTH_SHORT).show(); // Optional
                    }
                } else {
                    Log.w(TAG, "API response error: " + response.code());
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    Toast.makeText(DictionaryActivity.this, "Error fetching suggestions", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Word>> call, @NonNull Throwable t) {
                if (call.isCanceled()) {
                    Log.d(TAG, "API call was cancelled.");
                } else {
                    progressBar.setVisibility(View.GONE); // Ẩn loading
                    Log.e(TAG, "API call failed: ", t);
                    recyclerViewSuggestions.setVisibility(View.GONE);
                    Toast.makeText(DictionaryActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                }
                currentApiCall = null; // Xóa tham chiếu call hiện tại
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Dọn dẹp Handler và hủy Call API nếu Activity bị hủy
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (currentApiCall != null) {
            currentApiCall.cancel();
        }
    }

    // --- PHẦN THÊM MỚI ---
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        // Chỉ xử lý khi sự kiện là chạm xuống (ACTION_DOWN)
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            // Lấy View đang có focus hiện tại
            View focusedView = getCurrentFocus();

            // Kiểm tra xem View đang focus có phải là EditText tìm kiếm không
            if (focusedView instanceof EditText && focusedView == etSearch) {
                Rect outRectEditText = new Rect();
                etSearch.getGlobalVisibleRect(outRectEditText); // Lấy tọa độ của EditText trên màn hình

                Rect outRectRecyclerView = new Rect();
                // Chỉ lấy tọa độ RecyclerView nếu nó đang hiển thị
                if (recyclerViewSuggestions.getVisibility() == View.VISIBLE) {
                    recyclerViewSuggestions.getGlobalVisibleRect(outRectRecyclerView);
                }

                // Kiểm tra xem vị trí chạm (ev.getRawX(), ev.getRawY()) có nằm NGOÀI EditText
                // VÀ cũng nằm NGOÀI RecyclerView (nếu nó đang hiển thị) không
                if (!outRectEditText.contains((int) ev.getRawX(), (int) ev.getRawY()) &&
                        (recyclerViewSuggestions.getVisibility() != View.VISIBLE || !outRectRecyclerView.contains((int) ev.getRawX(), (int) ev.getRawY())))
                {
                    // Nếu chạm ra ngoài -> Xóa focus khỏi EditText
                    etSearch.clearFocus();

                    // Ẩn bàn phím mềm
                    hideKeyboard(etSearch);

                    // Ẩn danh sách gợi ý
                    recyclerViewSuggestions.setVisibility(View.GONE);

                    // (Tùy chọn) Bạn có thể muốn xóa luôn danh sách gợi ý trong adapter
                    // suggestionAdapter.updateData(new ArrayList<>());
                }
            }
            // Trường hợp click ra ngoài khi không có View nào focus (hoặc focus ở đâu đó khác)
            // mà bàn phím đang hiện -> ẩn bàn phím
            else if (focusedView == null || !(focusedView instanceof EditText)) {
                hideKeyboard(etSearch); // Thử ẩn bàn phím nếu không có focus hoặc focus không phải EditText
            }
        }
        // Luôn gọi phương thức gốc để sự kiện chạm được xử lý bình thường
        return super.dispatchTouchEvent(ev);
    }

    // Hàm tiện ích để ẩn bàn phím
    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null && view != null) {
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}