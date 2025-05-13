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
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse; // Ensure VocabularyResponse is Serializable
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsPhoto;
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment; // Import Loading Dialog

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

    private Long currentUserId = SharedPrefManager.getInstance(this).getUserInfo().getId();
    private static final String ACTIVITY_TAG = "AddVocabActivity";
    private static final long TEXT_CHANGED_DELAY = 700;

    private static final String LOADING_DIALOG_TAG = "AddVocabularyLoadingDialog";

    public static final String EXTRA_EDIT_MODE = "IS_EDIT_MODE";
    public static final String EXTRA_VOCABULARY_TO_EDIT = "VOCABULARY_TO_EDIT";
    public static final String EXTRA_PARENT_FLASHCARD_ID = "PARENT_FLASHCARD_ID";


    private ImageView backButton;
    private TextView textScreenTitle;

    private TextInputLayout textFieldLayoutSelectFlashcard, textFieldLayoutVocabWord, textFieldLayoutVocabDefinition, textFieldLayoutVocabType, textFieldLayoutVocabPhonetic, textFieldLayoutVocabExample;
    private AutoCompleteTextView autoCompleteTextViewSelectFlashcard, autoCompleteTextViewVocabType;
    private TextInputEditText editTextWord, editTextDefinition, editTextPhonetic, editTextExample;
    private EditText editTextSelectedImageUrl;
    private ImageView imageViewSelectedPreview;
    private RecyclerView recyclerViewImageSuggestions;
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

    private int loadingApiCount = 0;

    private View formScrollView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vocabulary);

        findViews();
        apiService = ApiClient.getApiService();

        parentFlashcardId = getIntent().getLongExtra(EXTRA_PARENT_FLASHCARD_ID, -1L);
        Log.d(ACTIVITY_TAG, "Received parentFlashcardId: " + parentFlashcardId);

        if (getIntent().hasExtra(EXTRA_EDIT_MODE) && getIntent().getBooleanExtra(EXTRA_EDIT_MODE, false)) {
            isEditMode = true;
            Object extraVocab = getIntent().getSerializableExtra(EXTRA_VOCABULARY_TO_EDIT);
            if (extraVocab instanceof VocabularyResponse) {
                editingVocabulary = (VocabularyResponse) extraVocab;
            }

            if (editingVocabulary == null || editingVocabulary.getId() == null) {
                handleInvalidEditData();
                return;
            }
            setupEditModeUI();
            // Load user flashcards to populate the dropdown (IT WILL BE ENABLED IN EDIT MODE NOW)
            // The preselection will happen in loadUserFlashcards's callback
            loadUserFlashcards(currentUserId);
        } else {
            isEditMode = false;
            selectedFlashcardId = parentFlashcardId != -1L ? parentFlashcardId : null;
            setupAddModeUI();
            loadUserFlashcards(currentUserId);
        }

        setupRecyclerView();
        setupTextWatcher();
        setupVocabTypeDropdown();
        setupFlashcardSelectionDropdown();

        buttonSubmit.setOnClickListener(v -> {
            if (isEditMode) {
                attemptUpdateVocabulary();
            } else {
                attemptAddVocabulary();
            }
        });

        backButton.setOnClickListener(v -> finish());
    }

    private void findViews() {
        backButton = findViewById(R.id.backButton);
        textScreenTitle = findViewById(R.id.textScreenTitle);

        formScrollView = findViewById(R.id.formScrollView);

        textFieldLayoutSelectFlashcard = findViewById(R.id.textFieldLayoutSelectFlashcard);
        autoCompleteTextViewSelectFlashcard = findViewById(R.id.autoCompleteTextViewSelectFlashcard);
        textFieldLayoutVocabWord = findViewById(R.id.textFieldLayoutVocabWord);
        editTextWord = findViewById(R.id.editTextVocabWord);
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
                String query = s.toString().trim();
                searchRunnable = () -> {
                    if (query.length() > 1) {
                        searchImages(query);
                        if (!isEditMode) {
                            resetSelectedImage();
                        }
                    } else {
                        clearImageSuggestions();
                        if (!isEditMode) {
                            resetSelectedImage();
                        }
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
                Log.d(ACTIVITY_TAG, "Selected Flashcard: Name=" + selectedName + ", ID=" + selectedFlashcardId);
                textFieldLayoutSelectFlashcard.setError(null);
            } else {
                Log.w(ACTIVITY_TAG, "Selected flashcard name not found in map: " + selectedName);
                autoCompleteTextViewSelectFlashcard.setText("", false);
                selectedFlashcardId = null;
                textFieldLayoutSelectFlashcard.setError("Invalid selection.");
            }
        });
    }

    private void loadUserFlashcards(Long creatorId) {
        startApiCall();

        apiService.getFlashcardsByCreator(creatorId).enqueue(new Callback<List<FlashcardBasicResponse>>() {
            @Override
            public void onResponse(Call<List<FlashcardBasicResponse>> call, Response<List<FlashcardBasicResponse>> response) {
                finishApiCall();

                if (response.isSuccessful() && response.body() != null) {
                    userFlashcards = response.body();
                    populateFlashcardDropdown();
                    Log.d(ACTIVITY_TAG, "Loaded " + userFlashcards.size() + " flashcards for creator " + creatorId);
                    preselectFlashcard();

                    boolean hasFlashcards = userFlashcards != null && !userFlashcards.isEmpty();
                    setFlashcardDropdownEnabled(hasFlashcards);
                    if (!hasFlashcards) {
                        textFieldLayoutSelectFlashcard.setError("You don't have any flashcard sets.");
                    } else {
                        textFieldLayoutSelectFlashcard.setError(null);
                    }


                } else {
                    Log.e(ACTIVITY_TAG, "Failed to load flashcards for creator " + creatorId + ": " + response.code());
                    Toast.makeText(AddVocabularyActivity.this, "Failed to load flashcard list.", Toast.LENGTH_SHORT).show();
                    // On failure, disable and show error
                    setFlashcardDropdownEnabled(false);
                    textFieldLayoutSelectFlashcard.setError("Failed to load flashcard list.");
                }
            }
            @Override
            public void onFailure(Call<List<FlashcardBasicResponse>> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Error loading flashcards for creator " + creatorId, t);
                Toast.makeText(AddVocabularyActivity.this, "Network error loading flashcard list.", Toast.LENGTH_SHORT).show();
                // On network error, disable and show error
                setFlashcardDropdownEnabled(false);
                textFieldLayoutSelectFlashcard.setError("Network error loading flashcard list.");
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
        Log.d(ACTIVITY_TAG, "Flashcard dropdown populated with " + flashcardNames.size() + " items.");
    }

    private void setFlashcardDropdownEnabled(boolean enabled) {
        textFieldLayoutSelectFlashcard.setEnabled(enabled);
        autoCompleteTextViewSelectFlashcard.setEnabled(enabled);

        if (!enabled && !isEditMode) {
            autoCompleteTextViewSelectFlashcard.setHint("No flashcard sets available");
            autoCompleteTextViewSelectFlashcard.setFocusable(false);
        } else {
            autoCompleteTextViewSelectFlashcard.setHint("Select Flashcard Set (*)");
            autoCompleteTextViewSelectFlashcard.setFocusableInTouchMode(enabled);
        }
    }

    private void setupEditModeUI() {
        textScreenTitle.setText("Edit Vocabulary");
        buttonSubmit.setText("Save Changes");

        textFieldLayoutSelectFlashcard.setVisibility(View.VISIBLE);
        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);

        Log.d(ACTIVITY_TAG, "Entering setupEditModeUI. editingVocabulary is null: " + (editingVocabulary == null));

        if (editingVocabulary != null) {
            Log.d(ACTIVITY_TAG, "Populating fields for Edit Mode. Vocab ID: " + editingVocabulary.getId());

            editTextWord.setText(editingVocabulary.getWord());
            editTextDefinition.setText(editingVocabulary.getDefinition());
            editTextPhonetic.setText(editingVocabulary.getPhonetic());
            autoCompleteTextViewVocabType.setText(editingVocabulary.getType(), false);
            editTextExample.setText(editingVocabulary.getExample());

            String imageUrl = editingVocabulary.getImageUrl();
            editTextSelectedImageUrl.setText(imageUrl);
            Log.d(ACTIVITY_TAG, "Setting initial imageUrl: " + imageUrl);

            if (!TextUtils.isEmpty(imageUrl)) {
                imageViewSelectedPreview.setVisibility(View.VISIBLE);
                Log.d(ACTIVITY_TAG, "Loading existing image into preview: " + imageUrl);
                Glide.with(this)
                        .load(imageUrl)
                        .placeholder(R.drawable.ic_placeholder_image)
                        .error(R.drawable.ic_placeholder_image)
                        .into(imageViewSelectedPreview);
            } else {
                imageViewSelectedPreview.setVisibility(View.GONE);
                Log.d(ACTIVITY_TAG, "No existing image URL found.");
            }

        } else {
            Log.e(ACTIVITY_TAG, "editingVocabulary is NULL inside setupEditModeUI! Cannot populate fields.");
            Toast.makeText(this, "Error loading vocabulary data for editing.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void setupAddModeUI() {
        textScreenTitle.setText("Add New Vocabulary");
        buttonSubmit.setText("Add Vocabulary to Set");

        textFieldLayoutSelectFlashcard.setVisibility(View.VISIBLE);

        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);
        imageViewSelectedPreview.setVisibility(View.GONE);
        resetSelectedImage();
    }

    private void handleInvalidEditData() {
        Log.e(ACTIVITY_TAG, "Invalid data received for edit mode.");
        Toast.makeText(this, "Error: Invalid data for editing.", Toast.LENGTH_LONG).show();
        finish();
    }

    private void preselectFlashcard() {
        Long idToSelect = null;
        if (isEditMode && editingVocabulary != null) {
            idToSelect = parentFlashcardId;
            Log.d(ACTIVITY_TAG, "Edit mode preselect target ID: " + idToSelect + " from editingVocabulary");
        } else if (parentFlashcardId != null && parentFlashcardId != -1L) {
            idToSelect = parentFlashcardId;
            Log.d(ACTIVITY_TAG, "Add mode preselect target ID: " + idToSelect + " from parentFlashcardId");
        } else {
            Log.d(ACTIVITY_TAG, "No specific flashcard ID to preselect.");
        }

        if (idToSelect != null && idToSelect != -1L && userFlashcards != null) {
            String preselectedName = null;
            for (FlashcardBasicResponse fc : userFlashcards) {
                if (idToSelect.equals(fc.getId())) {
                    preselectedName = fc.getName();
                    selectedFlashcardId = fc.getId();
                    break;
                }
            }
            if (preselectedName != null) {
                autoCompleteTextViewSelectFlashcard.setText(preselectedName, false);
                Log.d(ACTIVITY_TAG, "Preselected flashcard: " + preselectedName + " (ID: " + idToSelect + ")");
                textFieldLayoutSelectFlashcard.setError(null);

            } else {
                Log.w(ACTIVITY_TAG, "Flashcard ID " + idToSelect + " not found in loaded list (" + (userFlashcards != null ? userFlashcards.size() : 0) + " items) for preselection.");
                autoCompleteTextViewSelectFlashcard.setText("", false);
                selectedFlashcardId = null;
            }
        } else if (!isEditMode && (userFlashcards == null || userFlashcards.isEmpty())) {
            Log.w(ACTIVITY_TAG, "No user flashcards available for selection in Add mode (preselect).");
            selectedFlashcardId = null;
        } else {
            Log.d(ACTIVITY_TAG, "No preselection needed based on intent or editing vocabulary.");
            selectedFlashcardId = null;
        }
    }


    private void searchImages(String query) {
        startApiCall();

        Log.d(ACTIVITY_TAG, "Searching images via backend for query: " + query);

        String encodedQuery;
        try {
            encodedQuery = URLEncoder.encode(query, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(ACTIVITY_TAG, "Error encoding image search query", e);
            Toast.makeText(this, "Error encoding search query.", Toast.LENGTH_SHORT).show();
            finishApiCall();
            return;
        }


        apiService.searchImagesViaBackend(encodedQuery, 15, 1, "landscape")
                .enqueue(new Callback<PexelsResponse>() {
                    @Override
                    public void onResponse(Call<PexelsResponse> call, Response<PexelsResponse> response) {
                        finishApiCall();
                        if (response.isSuccessful() && response.body() != null && response.body().getPhotos() != null) {
                            List<PexelsPhoto> photos = response.body().getPhotos();
                            Log.d(ACTIVITY_TAG, "Received " + photos.size() + " image suggestions from backend.");
                            imageSuggestionAdapter.updateData(photos);
                            boolean hasSuggestions = !photos.isEmpty();
                            recyclerViewImageSuggestions.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
                            textViewPexelsCredit.setVisibility(hasSuggestions ? View.VISIBLE : View.GONE);
                        } else {
                            Log.e(ACTIVITY_TAG, "Backend image search failed: " + response.code());
                            Toast.makeText(AddVocabularyActivity.this, "Image search failed: " + response.code(), Toast.LENGTH_SHORT).show();
                            clearImageSuggestions();
                        }
                    }

                    @Override
                    public void onFailure(Call<PexelsResponse> call, Throwable t) {
                        finishApiCall();
                        Log.e(ACTIVITY_TAG, "Backend image search network error", t);
                        Toast.makeText(AddVocabularyActivity.this, "Image search network error.", Toast.LENGTH_SHORT).show();
                        clearImageSuggestions();
                    }
                });
    }

    @Override
    public void onImageSelected(PexelsPhoto image) {
        if (image == null || image.getSrc() == null) {
            Log.w(ACTIVITY_TAG, "Selected image or its source is null.");
            return;
        }
        String url = image.getSrc().getMedium();
        if (TextUtils.isEmpty(url)) url = image.getSrc().getLarge();
        if (TextUtils.isEmpty(url)) url = image.getSrc().getOriginal();
        if (TextUtils.isEmpty(url)) {
            Log.w(ACTIVITY_TAG, "No suitable image URL found for selected photo.");
            Toast.makeText(this, "Error getting image URL.", Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d(ACTIVITY_TAG, "Pexels Image selected: " + url);
        editTextSelectedImageUrl.setText(url);
        imageViewSelectedPreview.setVisibility(View.VISIBLE);
        Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_image).error(R.drawable.ic_placeholder_image).into(imageViewSelectedPreview);
    }

    private void resetSelectedImage() {
        editTextSelectedImageUrl.setText("");
        imageViewSelectedPreview.setVisibility(View.GONE);
        imageViewSelectedPreview.setImageResource(0);
    }

    private void clearImageSuggestions() {
        recyclerViewImageSuggestions.setVisibility(View.GONE);
        textViewPexelsCredit.setVisibility(View.GONE);
        if (imageSuggestionAdapter != null) {
            imageSuggestionAdapter.updateData(new ArrayList<>());
        }
    }

    private void attemptUpdateVocabulary() {
        if (editingVocabulary == null || editingVocabulary.getId() == null) {
            Log.e(ACTIVITY_TAG, "Attempted to update null or invalid vocabulary.");
            Toast.makeText(this, "Error: Cannot update invalid vocabulary.", Toast.LENGTH_SHORT).show();
            return;
        }

        Long targetFlashcardId = selectedFlashcardId;

        String word = editTextWord.getText().toString().trim();
        String definition = editTextDefinition.getText().toString().trim();
        String phonetic = editTextPhonetic.getText().toString().trim();
        String type = autoCompleteTextViewVocabType.getText().toString().trim();
        String example = editTextExample.getText().toString().trim();
        String selectedImageUrl = editTextSelectedImageUrl.getText().toString().trim();

        // Validation
        boolean valid = true;
        if (targetFlashcardId == null || targetFlashcardId == -1L) { textFieldLayoutSelectFlashcard.setError("Please select a flashcard set."); valid = false; } else { textFieldLayoutSelectFlashcard.setError(null); }
        if (TextUtils.isEmpty(word)) { textFieldLayoutVocabWord.setError("Vocabulary word cannot be empty."); valid = false; } else { textFieldLayoutVocabWord.setError(null); }
        if (TextUtils.isEmpty(definition)) { textFieldLayoutVocabDefinition.setError("Definition cannot be empty."); valid = false; } else { textFieldLayoutVocabDefinition.setError(null); }
        if (TextUtils.isEmpty(type)) { textFieldLayoutVocabType.setError("Please select a type."); valid = false; } else { textFieldLayoutVocabType.setError(null); }
        if (!valid) {
            Log.w(ACTIVITY_TAG, "Update validation failed.");
            return;
        }

        VocabularyUpdateRequest updateRequest = new VocabularyUpdateRequest();
        updateRequest.setWord(word);
        updateRequest.setDefinition(definition);
        updateRequest.setPhonetic(phonetic);
        updateRequest.setType(type);
        updateRequest.setExample(example);
        updateRequest.setImageUrl(selectedImageUrl);
        updateRequest.setFlashcardId(targetFlashcardId);

        Log.d(ACTIVITY_TAG, "Attempting to update vocabulary ID: " + editingVocabulary.getId() + " | Target Flashcard ID: " + targetFlashcardId +" | Request: " + updateRequest);
        startApiCall();

        apiService.updateVocabulary(editingVocabulary.getId(), updateRequest).enqueue(new Callback<VocabularyResponse>() {
            @Override
            public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                finishApiCall();
                Log.d(ACTIVITY_TAG, "Update API onResponse - Code: " + response.code() + ", Successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(ACTIVITY_TAG, "Vocabulary updated successfully. Finishing activity.");
                    Toast.makeText(AddVocabularyActivity.this, "Updated successfully!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    String errorMsg = "Failed to update: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Log.e(ACTIVITY_TAG, "Failed update vocabulary API response: " + errorMsg);
                    Toast.makeText(AddVocabularyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Error updating vocabulary (Network Failure or other exception)", t);
                Toast.makeText(AddVocabularyActivity.this, "Network error while updating.", Toast.LENGTH_SHORT).show();
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
        if (targetFlashcardId == null || targetFlashcardId == -1L) { textFieldLayoutSelectFlashcard.setError("Please select a flashcard set."); valid = false; } else { textFieldLayoutSelectFlashcard.setError(null); }
        if (TextUtils.isEmpty(word)) { textFieldLayoutVocabWord.setError("Vocabulary word cannot be empty."); valid = false; } else { textFieldLayoutVocabWord.setError(null); }
        if (TextUtils.isEmpty(definition)) { textFieldLayoutVocabDefinition.setError("Definition cannot be empty."); valid = false; } else { textFieldLayoutVocabDefinition.setError(null); }
        if (TextUtils.isEmpty(type)) { textFieldLayoutVocabType.setError("Please select a type."); valid = false; } else { textFieldLayoutVocabType.setError(null); }

        if (!valid) {
            Log.w(ACTIVITY_TAG, "Add validation failed.");
            return;
        }

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        request.setWord(word);
        request.setDefinition(definition);
        if (!TextUtils.isEmpty(phonetic)) request.setPhonetic(phonetic);
        if (!TextUtils.isEmpty(type)) request.setType(type);
        if (!TextUtils.isEmpty(example)) request.setExample(example);
        if (!TextUtils.isEmpty(selectedImageUrl)) request.setImageUrl(selectedImageUrl);

        Log.d(ACTIVITY_TAG, "Attempting add vocab to Flashcard ID: " + targetFlashcardId + " | Request: " + request);
        startApiCall();

        apiService.addVocabulary(targetFlashcardId, request).enqueue(new Callback<VocabularyResponse>() {
            @Override
            public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                finishApiCall();
                Log.d(ACTIVITY_TAG, "Add API onResponse - Code: " + response.code() + ", Successful: " + response.isSuccessful());
                if (response.isSuccessful() && response.body() != null) {
                    Log.i(ACTIVITY_TAG, "Vocabulary added successfully. Finishing activity.");
                    Toast.makeText(AddVocabularyActivity.this, "Vocabulary added successfully!", Toast.LENGTH_SHORT).show();
                    setResult(Activity.RESULT_OK);
                    finish();
                } else {
                    String errorMsg = "Failed to add vocabulary: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            String errorBody = response.errorBody().string();
                            Log.e(ACTIVITY_TAG, "Error Body: " + errorBody);
                        }
                    } catch (Exception e) {
                        Log.e(ACTIVITY_TAG, "Error reading error body", e);
                    }
                    Log.e(ACTIVITY_TAG, "Failed add vocabulary API response: " + errorMsg);
                    Toast.makeText(AddVocabularyActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                finishApiCall();
                Log.e(ACTIVITY_TAG, "Error adding vocab (Network Failure or other exception)", t);
                Toast.makeText(AddVocabularyActivity.this, "Network error while adding vocabulary.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Loading Logic using DialogFragment ---

    private synchronized void startApiCall() {
        loadingApiCount++;
        if (loadingApiCount == 1) {
            showLoading(true);
        }
        setFormEnabled(false);
    }

    private synchronized void finishApiCall() {
        loadingApiCount--;
        if (loadingApiCount <= 0) {
            loadingApiCount = 0;
            showLoading(false);
            setFormEnabled(true);
            // preselectFlashcard() is called in loadUserFlashcards callback
            // It is not needed here after every API call finishes
        }
    }

    // Controls the visibility of the loading dialog and message
    private void showLoading(boolean isLoading) {
        String message = isLoading ? (isEditMode ? "Saving changes..." : "Adding vocabulary...") : "";
        if (isLoading) {
            LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, message);
        } else {
            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
        }
    }

    // Utility to enable/disable form fields and submit button
    private void setFormEnabled(boolean enabled) {
        // Always enable/disable these based on overall loading state
        editTextWord.setEnabled(enabled);
        editTextDefinition.setEnabled(enabled);
        editTextPhonetic.setEnabled(enabled);
        autoCompleteTextViewVocabType.setEnabled(enabled);
        editTextExample.setEnabled(enabled);
        buttonSubmit.setEnabled(enabled);
        backButton.setEnabled(enabled);

        // Flashcard dropdown enabled logic is different for Add vs Edit
        if (isEditMode) {
            // In Edit mode, dropdown is ALWAYS enabled unless the form is globally disabled by loading
            setFlashcardDropdownEnabled(enabled); // Enable/disable based on overall 'enabled' status
        } else {
            // In Add mode, dropdown enabled state depends on 'enabled' AND userFlashcards availability
            boolean hasFlashcards = userFlashcards != null && !userFlashcards.isEmpty();
            boolean canEnableDropdown = enabled && hasFlashcards;
            setFlashcardDropdownEnabled(canEnableDropdown);

            if (!canEnableDropdown && enabled) { // If form enabled, but dropdown disabled due to no flashcards
                textFieldLayoutSelectFlashcard.setError("You don't have any flashcard sets.");
            } else {
                // Clear error if re-enabled or disabled by loading
                textFieldLayoutSelectFlashcard.setError(null);
            }
        }
        // Image search RecyclerView and preview visibility are handled by their own logic
    }


    @Override protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
        LoadingDialogFragment.hideLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG);
    }
}