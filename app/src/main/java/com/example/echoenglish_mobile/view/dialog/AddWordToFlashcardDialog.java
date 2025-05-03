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
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddWordToFlashcardDialog extends DialogFragment {

    private static final String TAG = "AddToFlashcardDialog";
    private static final String ARG_WORD = "word_to_add";

    private String wordToAdd;
    private ApiService apiService; // Cần được khởi tạo hoặc inject

    private TextInputLayout textInputLayout;
    private AutoCompleteTextView autoCompleteTextView;
    private ProgressBar loadingIndicator;

    private List<FlashcardBasicResponse> loadedFlashcards;
    private FlashcardBasicResponse selectedFlashcard = null;

    // --- Listener để trả kết quả về Activity/Fragment gọi ---
    public interface OnFlashcardSelectedListener {
        void onFlashcardSelectedForWord(String word, FlashcardBasicResponse selectedFlashcard);
        // Có thể thêm hàm onError nếu cần
        // void onFlashcardSelectionError(String errorMessage);
    }

    private OnFlashcardSelectedListener listener;

    // --- Phương thức khởi tạo an toàn (Factory Pattern) ---
    public static AddWordToFlashcardDialog newInstance(String word) {
        AddWordToFlashcardDialog fragment = new AddWordToFlashcardDialog();
        Bundle args = new Bundle();
        args.putString(ARG_WORD, word);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        // Đảm bảo Activity/Fragment gọi đã implement Listener
        try {
            // Thử ép kiểu context trực tiếp (nếu gọi từ Activity)
            listener = (OnFlashcardSelectedListener) context;
        } catch (ClassCastException e) {
            // Thử lấy từ Fragment cha (nếu gọi từ Fragment khác)
            if (getParentFragment() != null) {
                try {
                    listener = (OnFlashcardSelectedListener) getParentFragment();
                } catch (ClassCastException e2) {
                    throw new ClassCastException("Calling Fragment or Activity must implement OnFlashcardSelectedListener");
                }
            } else {
                throw new ClassCastException("Calling Activity must implement OnFlashcardSelectedListener");
            }
        }

        // Khởi tạo ApiService (ví dụ, bạn có thể dùng DI)
        apiService = ApiClient.getApiService(); // Hoặc cách khởi tạo khác
        if (apiService == null) {
            Log.e(TAG, "ApiService is null! Cannot load flashcards.");
            // Có thể đóng dialog hoặc báo lỗi ngay lập tức
        }
    }


    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (getArguments() != null) {
            wordToAdd = getArguments().getString(ARG_WORD);
        } else {
            Log.e(TAG, "Word argument is missing!");
            // Nên đóng dialog hoặc báo lỗi
            return super.onCreateDialog(savedInstanceState); // Trả về dialog rỗng hoặc xử lý khác
        }

        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(requireActivity());
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_to_flashcard, null);

        textInputLayout = dialogView.findViewById(R.id.dialog_flashcard_layout);
        autoCompleteTextView = dialogView.findViewById(R.id.dialog_flashcard_dropdown);
        loadingIndicator = dialogView.findViewById(R.id.dialog_loading_indicator);

        builder.setView(dialogView)
                .setTitle("Thêm \"" + wordToAdd + "\" vào bộ thẻ")
                .setPositiveButton("Thêm", null) // Listener sẽ được override trong onShow
                .setNegativeButton("Hủy", (dialog, which) -> dismiss());

        AlertDialog alertDialog = builder.create();

        // Override listener của nút Positive sau khi dialog hiển thị để ngăn đóng tự động
        alertDialog.setOnShowListener(dialogInterface -> {
            Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setEnabled(false); // Vô hiệu hóa ban đầu

            positiveButton.setOnClickListener(view -> {
                if (selectedFlashcard != null) {
                    // Gọi listener để trả kết quả về
                    if (listener != null) {
                        listener.onFlashcardSelectedForWord(wordToAdd, selectedFlashcard);
                    }
                    dismiss(); // Đóng dialog
                } else {
                    textInputLayout.setError("Vui lòng chọn một bộ thẻ");
                    // Toast.makeText(getContext(), "Vui lòng chọn một bộ thẻ", Toast.LENGTH_SHORT).show();
                }
            });

            // Bắt đầu load dữ liệu khi dialog hiển thị
            Long currentUserId = getCurrentUserId(); // Lấy ID user (CẦN IMPLEMENT)
            if (currentUserId != null && apiService != null) {
                loadUserFlashcards(currentUserId, alertDialog); // Truyền cả dialog vào để enable/disable nút
            } else {
                Log.e(TAG, "Cannot load flashcards: Missing User ID or ApiService.");
                showErrorState("Không thể tải danh sách thẻ (Lỗi cấu hình)");
                positiveButton.setEnabled(false);
            }
        });


        return alertDialog;
    }

    // --- Hàm tải danh sách flashcard (tương tự như trước) ---
    private void loadUserFlashcards(Long creatorId, AlertDialog dialog) {
        showLoadingState(true);
        selectedFlashcard = null; // Reset lựa chọn
        autoCompleteTextView.setText("", false); // Clear text cũ
        textInputLayout.setError(null); // Clear lỗi cũ
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Disable nút Thêm khi đang load


        apiService.getFlashcardsByCreator(creatorId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                if (!isAdded() || dialog == null || !dialog.isShowing()) return; // Kiểm tra Fragment/Dialog còn tồn tại không

                showLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    loadedFlashcards = response.body();
                    Log.d(TAG, "Loaded " + loadedFlashcards.size() + " flashcards.");
                    if (loadedFlashcards.isEmpty()) {
                        showErrorState("Bạn chưa có bộ thẻ nào");
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Vẫn disable nút
                    } else {
                        populateDropdown(loadedFlashcards);
                        textInputLayout.setEnabled(true); // Enable dropdown
                        // Không cần enable nút Positive ở đây, chỉ enable khi user chọn item
                    }
                } else {
                    Log.e(TAG, "Failed to load flashcards. Code: " + response.code());
                    String errorMsg = "Lỗi tải bộ thẻ (" + response.code() + ")";
                    showErrorState(errorMsg);
                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Disable nút
                }
            }

            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                if (!isAdded() || dialog == null || !dialog.isShowing()) return;

                Log.e(TAG, "Network error loading flashcards", t);
                showLoadingState(false);
                showErrorState("Lỗi mạng: " + t.getMessage());
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false); // Disable nút
            }
        });
    }

    // --- Hàm đổ dữ liệu vào dropdown ---
    private void populateDropdown(List<FlashcardBasicResponse> flashcards) {
        List<String> flashcardTitles = new ArrayList<>();
        for (FlashcardBasicResponse flashcard : flashcards) {
            flashcardTitles.add(flashcard.getName()); // Giả sử có getTitle()
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
                textInputLayout.setError(null); // Xóa lỗi nếu có
                // Enable nút "Thêm" khi đã chọn
                Dialog currentDialog = getDialog();
                if (currentDialog instanceof AlertDialog) {
                    ((AlertDialog) currentDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                }
                Log.d(TAG, "Selected Flashcard: " + selectedFlashcard.getName() + " (ID: " + selectedFlashcard.getId() + ")");
            } else {
                selectedFlashcard = null;
                // Disable nút "Thêm" nếu lựa chọn không hợp lệ (ít khi xảy ra)
                Dialog currentDialog = getDialog();
                if (currentDialog instanceof AlertDialog) {
                    ((AlertDialog) currentDialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                }
            }
        });
    }

    // --- Hàm cập nhật UI cho trạng thái loading ---
    private void showLoadingState(boolean isLoading) {
        if (loadingIndicator != null) {
            loadingIndicator.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        }
        if (textInputLayout != null) {
            textInputLayout.setEnabled(!isLoading); // Disable dropdown khi loading
            if(isLoading) {
                autoCompleteTextView.setText("Đang tải...", false);
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


    // --- CẦN IMPLEMENT: Hàm lấy User ID hiện tại ---
    private Long getCurrentUserId() {
        // Lấy ID từ SharedPreferences, ViewModel, Argument của Activity/Fragment cha...
        // Ví dụ:
        // SharedPreferences prefs = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE);
        // return prefs.getLong("user_id", -1L); // Trả về -1 nếu không tìm thấy
        return 27L; // *** THAY BẰNG LOGIC THỰC TẾ ***
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null; // Tránh memory leak
    }
}
