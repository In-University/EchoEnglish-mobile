package com.example.echoenglish_mobile.view.activity.flashcard;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddVocabularyActivity extends AppCompatActivity {

    private static final String TAG = "AddVocabulary";
    public static final String FLASHCARD_ID_EXTRA = "FLASHCARD_ID";

    private TextInputLayout textFieldLayoutWord, textFieldLayoutDefinition;
    private TextInputEditText editTextWord, editTextDefinition, editTextPhonetic, editTextType, editTextExample;
    private Button buttonAdd;
    private ProgressBar progressBar;

    private ApiService apiService;
    private Long flashcardId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_vocabulary);

        textFieldLayoutWord = findViewById(R.id.textFieldLayoutVocabWord);
        textFieldLayoutDefinition = findViewById(R.id.textFieldLayoutVocabDefinition);
        editTextWord = findViewById(R.id.editTextVocabWord);
        editTextDefinition = findViewById(R.id.editTextVocabDefinition);
        editTextPhonetic = findViewById(R.id.editTextVocabPhonetic);
        editTextType = findViewById(R.id.editTextVocabType);
        editTextExample = findViewById(R.id.editTextVocabExample);
        buttonAdd = findViewById(R.id.buttonAddVocabSubmit);
        progressBar = findViewById(R.id.progressBarAddVocab);

        apiService = ApiClient.getApiService();
        flashcardId = getIntent().getLongExtra(FLASHCARD_ID_EXTRA, -1);

        if (flashcardId == -1) {
            Log.e(TAG, "Invalid Flashcard ID received for adding vocabulary.");
            Toast.makeText(this, "ID bộ thẻ không hợp lệ.", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        buttonAdd.setOnClickListener(v -> attemptAddVocabulary());
    }

    private void attemptAddVocabulary() {
        String word = editTextWord.getText().toString().trim();
        String definition = editTextDefinition.getText().toString().trim();
        String phonetic = editTextPhonetic.getText().toString().trim();
        String type = editTextType.getText().toString().trim();
        String example = editTextExample.getText().toString().trim();

        boolean valid = true;

        if (TextUtils.isEmpty(word)) {
            textFieldLayoutWord.setError("Từ vựng không được để trống");
            valid = false;
        } else {
            textFieldLayoutWord.setError(null);
        }

        if (TextUtils.isEmpty(definition)) {
            textFieldLayoutDefinition.setError("Định nghĩa không được để trống");
            valid = false;
        } else {
            textFieldLayoutDefinition.setError(null);
        }

        if (!valid) return;

        VocabularyCreateRequest request = new VocabularyCreateRequest();
        request.setWord(word);
        request.setDefinition(definition);
        if (!TextUtils.isEmpty(phonetic)) request.setPhonetic(phonetic);
        if (!TextUtils.isEmpty(type)) request.setType(type);
        if (!TextUtils.isEmpty(example)) request.setExample(example);

        showLoading(true);

        apiService.addVocabulary(flashcardId, request).enqueue(new Callback<VocabularyResponse>() {
            @Override
            public void onResponse(Call<VocabularyResponse> call, Response<VocabularyResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(AddVocabularyActivity.this, "Thêm từ vựng thành công!", Toast.LENGTH_SHORT).show();

                    // *** Trả kết quả về cho Activity gọi ***
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                } else {
                    Log.e(TAG, "Failed to add vocabulary: " + response.code() + " - " + response.message());
                    Toast.makeText(AddVocabularyActivity.this, "Lỗi thêm từ vựng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<VocabularyResponse> call, Throwable t) {
                showLoading(false);
                Log.e(TAG, "Error adding vocabulary", t);
                Toast.makeText(AddVocabularyActivity.this, "Lỗi mạng khi thêm từ vựng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonAdd.setEnabled(!isLoading);
    }
}
