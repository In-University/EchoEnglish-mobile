package com.example.echoenglish_mobile.view.activity.flashcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsPhoto;
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVocabularyActivity extends AppCompatActivity implements ImageSuggestionAdapter.OnImageSelectedListener {

    private static final String TAG = "AddVocabularyActivity";
    private static final long TEXT_CHANGED_DELAY = 700;
    private static final Long DEFAULT_USER_ID_TO_FETCH = 27L;

    public static final String EXTRA_EDIT_MODE = "IS_EDIT_MODE";
    public static final String EXTRA_VOCABULARY_TO_EDIT = "VOCABULARY_TO_EDIT";
    public static final String EXTRA_PARENT_FLASHCARD_ID = "PARENT_FLASHCARD_ID";

    private TextInputLayout textFieldLayoutSelectFlashcard, textFieldLayoutVocabWord, textFieldLayoutVocabDefinition, textFieldLayoutVocabType, textFieldLayoutVocabPhonetic, textFieldLayoutVocabExample;
    private AutoCompleteTextView autoCompleteTextViewSelectFlashcard, autoCompleteTextViewVocabType;
    private TextInputEditText editTextWord, editTextDefinition, editTextPhonetic, editTextExample;
    private EditText editTextSelectedImageUrl;
    private ImageView imageViewSelectedPreview;
    private RecyclerView recyclerViewImageSuggestions;
    private ProgressBar progressBarImageSearch, progressBarAddVocab;
    private TextView textViewPexelsCredit;
    private Button buttonSubmit;

    private ApiService apiService;
    private ImageSuggestionAdapter imageSuggestionAdapter;
    private Long selectedFlashcardId = null;
    private Long parentFlashcardId = null;
    private List<FlashcardBasicResponse> userFlashcards = new ArrayList<>();
    private Map<String, Long> flashcardNameToIdMap = new HashMap<>();
    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private boolean isEditMode = false;
    private VocabularyResponse editingVocabulary = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vocabulary);

        findViews();
        apiService = ApiClient.getApiService();

        parentFlashcardId = getIntent().getLongExtra(EXTRA_PARENT_FLASHCARD_ID, -1L);
        Log.d(TAG, "Received parentFlashcardId: " + parentFlashcardId);

        editingVocabulary = (VocabularyResponse) getIntent().getSerializableExtra(EXTRA_VOCABULARY_TO_EDIT);

        // Kiểm tra chế độ Edit hay Add VÀ gọi hàm setup UI tương ứng
        if (getIntent().hasExtra(EXTRA_EDIT_MODE) && getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false)) {
            isEditMode = true;
            // ... (lấy editingVocabulary) ...
            if (editingVocabulary == null || editingVocabulary.getId() == null || parentFlashcardId == -1L) {
                handleInvalidEditData(); return;
            }
            setupEditModeUI();
            loadUserFlashcards(DEFAULT_USER_ID_TO_FETCH); // Vẫn load để preselect dropdown (dù bị disable)
        } else {
            isEditMode = false;
            selectedFlashcardId = parentFlashcardId != -1L ? parentFlashcardId : null;
            setupAddModeUI();
            loadUserFlashcards(DEFAULT_USER_ID_TO_FETCH);
        }

        // Cài đặt các thành phần UI khác
        setupRecyclerView();
        setupTextWatcher();
        setupVocabTypeDropdown();
        setupFlashcardSelectionDropdown();

        // *** CHỈ ĐẶT LISTENER Ở ĐÂY MỘT LẦN ***
        buttonSubmit.setOnClickListener(v -> {
            Log.d(TAG, "buttonSubmit is null: " + (buttonSubmit == null));
            if (isEditMode) {
                attemptUpdateVocabulary();
            } else {
                attemptAddVocabulary();
            }
        });
        // *** KHÔNG GỌI setOnClickListener Ở BẤT KỲ CHỖ NÀO KHÁC ***
    }

    private void findViews() {
        textFieldLayoutSelectFlashcard = findViewById(R.id.textFieldLayoutSelectFlashcard);
        autoCompleteTextViewSelectFlashcard = findViewById(R.id.autoCompleteTextViewSelectFlashcard);
        textFieldLayoutVocabWord = findViewById(R.id.textFieldLayoutVocabWord);
        editTextWord = findViewById(R.id.editTextVocabWord);
        progressBarImageSearch = findViewById(R.id.progressBarImageSearch);
        recyclerViewImageSuggestions = findViewById(R.id.recyclerViewImageSuggestions);
        textViewPexelsCredit = findViewById(R.id.textViewPexelsCredit);
        imageViewSelectedPreview = findViewById(R.id.imageViewSelectedPreview);
        editTextSelectedImageUrl = findViewById(R.id.editTextSelectedImageUrl);
        textFieldLayoutVocabDefinition = findViewById(R.id.textFieldLayoutVocabDefinition);
        editTextDefinition = findViewById(R.id.editTextVocabDefinition);
        textFieldLayoutVocabPhonetic = findViewById(R.id.textFieldLayoutVocabPhonetic);
        editTextPhonetic = findViewById(R.id.editTextVocabPhonetic);
        textFieldLayoutVocabType = findViewById(R.id.textFieldLayoutVocabType);
        autoCompleteTextViewVocabType = findViewById(R.id.autoCompleteTextViewVocabType);
        textFieldLayoutVocabExample = findViewById(R.id.textFieldLayoutVocabExample);
        editTextExample = findViewById(R.id.editTextVocabExample);
        progressBarAddVocab = findViewById(R.id.progressBarAddVocab);
        buttonSubmit = findViewById(R.id.buttonAddVocabSubmit);
    }

    private void setupRecyclerView() {
        recyclerViewImageSuggestions.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        imageSuggestionAdapter = new ImageSuggestionAdapter(this, new ArrayList<>(), this);
        recyclerViewImageSuggestions.setAdapter(imageSuggestionAdapter);
    }

    private void setupTextWatcher() {
        editTextWord.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
            }
            @Override public void afterTextChanged(Editable s) {
                // *** LUÔN KÍCH HOẠT TÌM KIẾM (HOẶC THÊM LOGIC KIỂM TRA KHÁC) ***
                String query = s.toString().trim();
                searchRunnable = () -> {
                    if (query.length() > 1) {
                        searchImages(query); // Luôn gọi searchImages
                        // Chỉ reset ảnh đã chọn khi Thêm MỚI để giữ ảnh cũ khi Sửa
                    } else {
                        clearImageSuggestions(); // Xóa gợi ý nếu query ngắn
                    }
                };
                searchHandler.postDelayed(searchRunnable, TEXT_CHANGED_DELAY);
            }
        });
    }

    private void setupVocabTypeDropdown() {
        String[] vocabTypes = getResources().getStringArray(R.array.vocabulary_types);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, vocabTypes);
        autoCompleteTextViewVocabType.setAdapter(adapter);
        autoCompleteTextViewVocabType.setOnItemClickListener((parent, view, position, id) -> textFieldLayoutVocabType.setError(null));
    }

    private void setupFlashcardSelectionDropdown() {
        autoCompleteTextViewSelectFlashcard.setOnItemClickListener((parent, view, position, id) -> {
            String selectedName = (String) parent.getItemAtPosition(position);
            selectedFlashcardId = flashcardNameToIdMap.get(selectedName);
            if (selectedFlashcardId != null) {
                Log.d(TAG, "Selected Flashcard: Name=" + selectedName + ", ID=" + selectedFlashcardId);
                textFieldLayoutSelectFlashcard.setError(null);
            } else {
                Log.w(TAG, "Selected flashcard name not found in map: " + selectedName);
                autoCompleteTextViewSelectFlashcard.setText("", false);
                selectedFlashcardId = null;
                textFieldLayoutSelectFlashcard.setError("Lựa chọn không hợp lệ");
            }
        });
    }

    private void loadUserFlashcards(Long creatorId) {
        // Không cần disable dropdown ở đây nếu gọi từ cả Add và Edit
        showSubmitLoading(true);

        apiService.getFlashcardsByCreator(creatorId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                showSubmitLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    userFlashcards = response.body();
                    populateFlashcardDropdown();
                    Log.d(TAG, "Loaded " + userFlashcards.size() + " flashcards for creator " + creatorId);
                    preselectFlashcard(); // Luôn gọi để xử lý cả Add và Edit
                    if(userFlashcards.isEmpty() && !isEditMode){
                        textFieldLayoutSelectFlashcard.setError("Bạn chưa có bộ thẻ nào");
                    }
                } else {
                    Log.e(TAG, "Failed to load flashcards for creator " + creatorId + ": " + response.code());
                    Toast.makeText(AddVocabularyActivity.this, "Lỗi tải danh sách bộ thẻ", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                showSubmitLoading(false);
                if (!isEditMode) setFlashcardDropdownEnabled(true); // Chỉ enable lại khi Add
                Log.e(TAG, "Error loading flashcards for creator " + creatorId, t);
                Toast.makeText(AddVocabularyActivity.this, "Lỗi mạng khi tải bộ thẻ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateFlashcardDropdown() {
        List<String> flashcardNames = new ArrayList<>();
        flashcardNameToIdMap.clear();
        if (userFlashcards != null) {
            for (FlashcardBasicResponse flashcard : userFlashcards) {
                if (flashcard != null && flashcard.getName() != null && flashcard.getId() != null) {
                    flashcardNames.add(flashcard.getName());
                    flashcardNameToIdMap.put(flashcard.getName(), flashcard.getId());
                }
            }
        }
        Collections.sort(flashcardNames);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_dropdown_item_1line, flashcardNames);
        autoCompleteTextViewSelectFlashcard.setAdapter(adapter);
        Log.d(TAG, "Flashcard dropdown populated with " + flashcardNames.size() + " items.");
    }

    private void setFlashcardDropdownEnabled(boolean enabled) {
        textFieldLayoutSelectFlashcard.setEnabled(enabled);
        autoCompleteTextViewSelectFlashcard.setEnabled(enabled);
    }

    private void setupEditModeUI() {
        setTitle("Sửa từ vựng");
        buttonSubmit.setText("Lưu thay đổi");

        // Hiển thị và kích hoạt dropdown flashcard (cho phép chọn lại)
        textFieldLayoutSelectFlashcard.setVisibility(View.VISIBLE);
        setFlashcardDropdownEnabled(true);

        // Các view tìm ảnh cũng hiển thị (trạng thái ban đầu là ẩn)
        progressBarImageSearch.setVisibility(View.GONE);
        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);

        // Log kiểm tra editingVocabulary trước khi sử dụng
        Log.d(TAG, "Entering setupEditModeUI. editingVocabulary is null: " + (editingVocabulary == null));

        // Điền dữ liệu cũ vào các trường
        if (editingVocabulary != null) {
            Log.d(TAG, "Populating fields for Edit Mode. Vocab ID: " + editingVocabulary.getId());

            // Điền các trường Text
            editTextWord.setText(editingVocabulary.getWord() != null ? editingVocabulary.getWord() : "");
            editTextDefinition.setText(editingVocabulary.getDefinition() != null ? editingVocabulary.getDefinition() : "");
            editTextPhonetic.setText(editingVocabulary.getPhonetic() != null ? editingVocabulary.getPhonetic() : "");
            // Đặt giá trị cho AutoCompleteTextView Type (quan trọng là dùng setText)
            autoCompleteTextViewVocabType.setText(editingVocabulary.getType() != null ? editingVocabulary.getType() : "", false);
            editTextExample.setText(editingVocabulary.getExample() != null ? editingVocabulary.getExample() : "");

            // Lưu URL ảnh cũ vào EditText ẩn (có thể null)
            String imageUrl = editingVocabulary.getImageUrl();
            editTextSelectedImageUrl.setText(imageUrl);
            Log.d(TAG, "Setting initial imageUrl: " + imageUrl);


            // *** HIỂN THỊ ẢNH CŨ (NẾU CÓ) BẰNG GLIDE ***
            if (imageUrl != null && !imageUrl.isEmpty()) {
                imageViewSelectedPreview.setVisibility(View.VISIBLE); // Hiển thị ImageView
                Log.d(TAG, "Loading existing image into preview: " + imageUrl);
                Glide.with(this) // Sử dụng context của Activity
                        .load(imageUrl) // Load URL ảnh cũ
                        .placeholder(R.drawable.ic_placeholder_image) // Ảnh chờ
                        .error(R.drawable.ic_placeholder_image) // Ảnh lỗi
                        .into(imageViewSelectedPreview); // Đặt vào ImageView preview
            } else {
                // Nếu không có ảnh cũ, ẩn ImageView đi
                imageViewSelectedPreview.setVisibility(View.GONE);
                Log.d(TAG, "No existing image URL found.");
            }
            // *** KẾT THÚC PHẦN GLIDE ***

            // Tự động chọn flashcard hiện tại sẽ được xử lý trong preselectFlashcard
            // sau khi loadUserFlashcards hoàn tất.

        } else {
            Log.e(TAG, "editingVocabulary is NULL inside setupEditModeUI! Cannot populate fields.");
            // Xử lý lỗi nghiêm trọng nếu editingVocabulary là null ở đây
            Toast.makeText(this, "Lỗi tải dữ liệu từ vựng để sửa.", Toast.LENGTH_LONG).show();
            finish(); // Đóng activity nếu không có dữ liệu sửa
        }
    }
    private void setupAddModeUI() {
        setTitle("Thêm từ vựng mới");
        buttonSubmit.setText("Thêm từ vào bộ thẻ");
        textFieldLayoutSelectFlashcard.setVisibility(View.VISIBLE);
        setFlashcardDropdownEnabled(true); // Cho phép chọn
        // Các view ảnh sẽ ẩn/hiện theo logic tìm kiếm
        progressBarImageSearch.setVisibility(View.GONE);
        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);
        imageViewSelectedPreview.setVisibility(View.GONE);
    }

    private void handleInvalidEditData() {
        Log.e(TAG, "Invalid data received for edit mode.");
        Toast.makeText(this, "Lỗi: Dữ liệu sửa không hợp lệ.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void preselectFlashcard() {
        Long idToSelect = isEditMode ? parentFlashcardId : selectedFlashcardId; // Lấy ID cần chọn

        if (idToSelect != null && idToSelect != -1L && !userFlashcards.isEmpty()) {
            String preselectedName = null;
            for (FlashcardBasicResponse fc : userFlashcards) {
                if (idToSelect.equals(fc.getId())) {
                    preselectedName = fc.getName();
                    break;
                }
            }
            if (preselectedName != null) {
                autoCompleteTextViewSelectFlashcard.setText(preselectedName, false);
                // Nếu là chế độ Add, cập nhật selectedFlashcardId
                selectedFlashcardId = idToSelect;
                Log.d(TAG, "Preselected flashcard: " + preselectedName + " (ID: " + idToSelect + ")");
                textFieldLayoutSelectFlashcard.setError(null);
            } else {
                Log.w(TAG, "Flashcard ID " + idToSelect + " not found in list for preselection.");
                if (isEditMode) {
                    textFieldLayoutSelectFlashcard.setError("Không tìm thấy bộ thẻ gốc");
                } else {
                    selectedFlashcardId = null; // Reset ở chế độ Add nếu không tìm thấy
                }
            }
        } else if (!isEditMode) {
            selectedFlashcardId = null; // Reset ở chế độ Add nếu không có ID ban đầu
        }
    }

    private void searchImages(String query) {
        showImageSearchLoading(true);
        Log.d(TAG, "Searching images via backend for query: " + query);

        // Gọi API backend mới
        apiService.searchImagesViaBackend(query, 15, 1, "landscape") // Truyền tham số cần thiết
            .enqueue(new Callback<PexelsResponse>() { // Vẫn nhận PexelsResponse từ backend
                @Override
                public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                    showImageSearchLoading(false);
                    if (response.isSuccessful() && response.body() != null && response.body().getPhotos() != null) {
                        List<PexelsPhoto> photos = response.body().getPhotos();
                        Log.d(TAG, "Received " + photos.size() + " image suggestions from backend.");
                        imageSuggestionAdapter.updateData(photos);
                        boolean hasSuggestions = !photos.isEmpty();
                        recyclerViewImageSuggestions.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
                        // Hiển thị credit "Ảnh từ Pexels" vì nguồn gốc vẫn là Pexels
                        textViewPexelsCredit.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
                    } else {
                        Log.e(TAG, "Backend image search failed: " + response.code());
                        Toast.makeText(AddVocabularyActivity.this, "Lỗi tìm ảnh từ server: " + response.code(), Toast.LENGTH_SHORT).show();
                        clearImageSuggestions();
                    }
                }

                @Override
                public void onFailure(Call<PexelsResponse> call, Throwable t) {
                    showImageSearchLoading(false);
                    Log.e(TAG, "Backend image search network error", t);
                    Toast.makeText(AddVocabularyActivity.this, "Lỗi mạng khi tìm ảnh", Toast.LENGTH_SHORT).show();
                    clearImageSuggestions();
                }
            });
    }

    @Override
    public void onImageSelected(PexelsPhoto image) {
//        if (isEditMode) return; // Không cho chọn ảnh khi sửa

        if (image == null || image.getSrc() == null) return;
        String url = image.getSrc().getMedium();
        if (url == null || url.isEmpty()) url = image.getSrc().getLarge();
        if (url == null || url.isEmpty()) return;

        Log.d(TAG, "Pexels Image selected: " + url);
        editTextSelectedImageUrl.setText(url);
        imageViewSelectedPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_placeholder_image).into(imageViewSelectedPreview);
        Toast.makeText(this, "Ảnh: " + image.getPhotographer() + " / Pexels", Toast.LENGTH_SHORT).show();
    }

    private void resetSelectedImage() {
        editTextSelectedImageUrl.setText("");
        imageViewSelectedPreview.setVisibility(View.GONE);
        imageViewSelectedPreview.setImageResource(R.drawable.ic_placeholder_image);
    }

    private void clearImageSuggestions() {
        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);
        if (imageSuggestionAdapter != null) {
            imageSuggestionAdapter.updateData(new ArrayList<>());
        }
    }

    private void attemptUpdateVocabulary() {
        if (editingVocabulary == null || editingVocabulary.getId() == null) { /* Báo lỗi */ return; }

        // Lấy dữ liệu đã sửa
        // *** LẤY FLASHCARD ID MỚI NHẤT TỪ DROPDOWN ***
        Long targetFlashcardId = selectedFlashcardId; // Lấy ID từ biến toàn cục đã được cập nhật bởi dropdown listener
        String word = editTextWord.getText().toString().trim();
        String definition = editTextDefinition.getText().toString().trim();
        String phonetic = editTextPhonetic.getText().toString().trim();
        String type = autoCompleteTextViewVocabType.getText().toString().trim();
        String example = editTextExample.getText().toString().trim();
        String selectedImageUrl = editTextSelectedImageUrl.getText().toString().trim();

        // Validation (Thêm validation cho flashcard đã chọn)
        boolean valid = true;
        if (targetFlashcardId == null) { textFieldLayoutSelectFlashcard.setError("Vui lòng chọn bộ thẻ"); valid = false; } else { textFieldLayoutSelectFlashcard.setError(null); }
        if (TextUtils.isEmpty(word)) { textFieldLayoutVocabWord.setError("Từ vựng trống"); valid = false; } else { textFieldLayoutVocabWord.setError(null); }
        if (TextUtils.isEmpty(definition)) { textFieldLayoutVocabDefinition.setError("Định nghĩa trống"); valid = false; } else { textFieldLayoutVocabDefinition.setError(null); }
        if (TextUtils.isEmpty(type)) { textFieldLayoutVocabType.setError("Vui lòng chọn loại từ"); valid = false; } else { textFieldLayoutVocabType.setError(null); }
        if (!valid) return;

        // Tạo Request Body Update
        VocabularyUpdateRequest updateRequest = new VocabularyUpdateRequest();
        updateRequest.setWord(word);
        updateRequest.setDefinition(definition);
        updateRequest.setPhonetic(phonetic);
        updateRequest.setType(type);
        updateRequest.setExample(example);
        updateRequest.setImageUrl(selectedImageUrl);
        // *** GỬI FLASHCARD ID MỚI ĐÃ CHỌN ***
        updateRequest.setFlashcardId(targetFlashcardId);

        Log.d(TAG, "Attempting to update vocabulary ID: " + editingVocabulary.getId() + " | Target Flashcard ID: " + targetFlashcardId +" | Request: " + updateRequest);
        showSubmitLoading(true);

        // Gọi API Update
        apiService.updateVocabulary(editingVocabulary.getId(), updateRequest).enqueue(new Callback<VocabularyResponse>() {
            @Override
            public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                showSubmitLoading(false);
                Log.d(TAG, "Update API onResponse - Code: " + response.code() + ", Successful: " + response.isSuccessful()); // Log mã và trạng thái
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(TAG, "Vocabulary updated successfully. Finishing activity.");
                    Toast.makeText(AddVocabularyActivity.this, "Cập nhật thành công!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    String errorBody = "";
                    try { if (response.errorBody() != null) errorBody = response.errorBody().string(); }
                    catch (Exception e) { Log.e(TAG, "Error reading error body", e); }
                    Log.e(TAG, "Failed update vocabulary API response. Code: " + response.code() + ", Message: " + response.message() + ", Error Body: " + errorBody);
                    Toast.makeText(AddVocabularyActivity.this, "Lỗi cập nhật: " + response.code(), Toast.LENGTH_SHORT).show();
                    // KHÔNG finish() khi lỗi
                }
            }
            @Override
            public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                showSubmitLoading(false);
                Log.e(TAG, "Error updating vocabulary (Network Failure or other exception)", t); // Log cả Throwable
                Toast.makeText(AddVocabularyActivity.this, "Lỗi mạng khi cập nhật", Toast.LENGTH_SHORT).show();
                // KHÔNG finish() khi lỗi
            }
        });
    }


    private void attemptAddVocabulary() {
        Long targetFlashcardId = selectedFlashcardId;
        String word = editTextWord.getText().toString().trim();
        String definition = editTextDefinition.getText().toString().trim();
        String phonetic = editTextPhonetic.getText().toString().trim();
        String type = autoCompleteTextViewVocabType.getText().toString().trim();
        String example = editTextExample.getText().toString().trim();
        String selectedImageUrl = editTextSelectedImageUrl.getText().toString().trim();

        boolean valid = true;
        if (targetFlashcardId == null) { textFieldLayoutSelectFlashcard.setError("Vui lòng chọn bộ thẻ"); valid = false; } else { textFieldLayoutSelectFlashcard.setError(null); }
        if (TextUtils.isEmpty(word)) { textFieldLayoutVocabWord.setError("Từ vựng trống"); valid = false; } else { textFieldLayoutVocabWord.setError(null); }
        if (TextUtils.isEmpty(definition)) { textFieldLayoutVocabDefinition.setError("Định nghĩa trống"); valid = false; } else { textFieldLayoutVocabDefinition.setError(null); }
        if (TextUtils.isEmpty(type)) { textFieldLayoutVocabType.setError("Vui lòng chọn loại từ"); valid = false; } else { textFieldLayoutVocabType.setError(null); }
        if (!valid) return;

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        request.setWord(word);
        request.setDefinition(definition);
        if (!TextUtils.isEmpty(phonetic)) request.setPhonetic(phonetic);
        if (!TextUtils.isEmpty(type)) request.setType(type);
        if (!TextUtils.isEmpty(example)) request.setExample(example);
        if (!TextUtils.isEmpty(selectedImageUrl)) request.setImageUrl(selectedImageUrl);

        Log.d(TAG, "Attempting add vocab to Flashcard ID: " + targetFlashcardId + " | Request: " + request);
        showSubmitLoading(true);

        apiService.addVocabulary(targetFlashcardId, request).enqueue(new Callback<VocabularyResponse>() {
            @Override
            public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                showSubmitLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddVocabularyActivity.this, "Thêm từ thành công!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    Log.e(TAG, "Failed add vocab: " + response.code() + " " + response.message());
                    Toast.makeText(AddVocabularyActivity.this, "Lỗi thêm từ vựng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                showSubmitLoading(false);
                Log.e(TAG, "Error adding vocab", t);
                Toast.makeText(AddVocabularyActivity.this, "Lỗi mạng khi thêm", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showImageSearchLoading(boolean isLoading) {
        progressBarImageSearch.setVisibility(isLoading ? View.VISIBLE : View.GONE);
    }
    private void showSubmitLoading(boolean isLoading) {
        progressBarAddVocab.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonSubmit.setEnabled(!isLoading);
    }
    @Override protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }
}