package com.example.echoenglish_mobile.view.activity.pronunciation_assessment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadSpeechActivity extends AppCompatActivity {

    private static final String TAG = "UploadSpeechActivity";

    private EditText etTargetWord;
    private LinearLayout btnSelectAttachment;
    private RelativeLayout attachmentDisplayArea;
    private LinearLayout emptyAttachmentState;
    private LinearLayout selectedAttachmentState;
    private TextView txtSelectedFileName;
    private ImageButton btnRemoveAttachment;
    ImageView btnBack;
    private Button btnUpload;
    private ProgressBar progressBar;

    private Uri selectedAudioUri = null;
    private ApiService apiService;

    private final ActivityResultLauncher<Intent> audioPickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    Uri uri = result.getData().getData();
                    if (uri != null) {
                        handleSelectedAudio(uri);
                    } else {
                        Toast.makeText(this, "Failed to get audio file", Toast.LENGTH_SHORT).show();
                    }
                }
            });


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_speech);

        // Initialize Retrofit Service
        apiService = ApiClient.getApiService();

        // Find Views
        etTargetWord = findViewById(R.id.etTargetWord);
        btnSelectAttachment = findViewById(R.id.btnSelectAttachment);
        attachmentDisplayArea = findViewById(R.id.attachmentDisplayArea);
        emptyAttachmentState = findViewById(R.id.emptyAttachmentState);
        selectedAttachmentState = findViewById(R.id.selectedAttachmentState);
        txtSelectedFileName = findViewById(R.id.txtSelectedFileName);
        btnRemoveAttachment = findViewById(R.id.btnRemoveAttachment);
        btnUpload = findViewById(R.id.btnUpload);
        btnBack = findViewById(R.id.btnBack);
        progressBar = findViewById(R.id.progressBar);

        setupButtonClickListeners();
        updateAttachmentUI();
    }

    private void setupButtonClickListeners() {
        btnSelectAttachment.setOnClickListener(v -> openAudioPicker());
        btnBack.setOnClickListener(v -> finish());
        btnRemoveAttachment.setOnClickListener(v -> clearSelectedAudio());

        btnUpload.setOnClickListener(v -> attemptUpload());
    }

    private void openAudioPicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("audio/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            audioPickerLauncher.launch(Intent.createChooser(intent, "Select Audio File"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSelectedAudio(Uri uri) {
        String fileName = getFileName(this, uri);
        if (fileName == null) {
            fileName = "audio_file"; // Fallback name
        }
        Log.d(TAG, "Selected audio: " + uri.toString() + ", Name: " + fileName);
        selectedAudioUri = uri;
        updateAttachmentUI();
    }

    private void clearSelectedAudio() {
        selectedAudioUri = null;
        updateAttachmentUI();
    }

    private void updateAttachmentUI() {
        if (selectedAudioUri != null) {
            String fileName = getFileName(this, selectedAudioUri);
            if (fileName == null) fileName = "Selected Audio File";
            txtSelectedFileName.setText(fileName);
            emptyAttachmentState.setVisibility(View.GONE);
            selectedAttachmentState.setVisibility(View.VISIBLE);
        } else {
            emptyAttachmentState.setVisibility(View.VISIBLE);
            selectedAttachmentState.setVisibility(View.GONE);
            txtSelectedFileName.setText("");
        }
    }

    private void attemptUpload() {
        String targetWord = etTargetWord.getText().toString().trim();

        if (targetWord.isEmpty()) {
            etTargetWord.setError("Target word cannot be empty");
            etTargetWord.requestFocus();
            return;
        } else {
            etTargetWord.setError(null);
        }

        if (selectedAudioUri == null) {
            Toast.makeText(this, "Please select an audio file", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String fileName = getFileName(this, selectedAudioUri);
            if (fileName == null) fileName = "audio_upload.tmp";

            RequestBody targetWordRequestBody = RequestBody.create(MediaType.parse("text/plain"), targetWord);

            RequestBody audioFileRequestBody = createRequestBodyFromUri(this, selectedAudioUri);
            if (audioFileRequestBody == null) {
                Toast.makeText(this, "Error preparing audio file", Toast.LENGTH_SHORT).show();
                return;
            }

            MultipartBody.Part audioFilePart = MultipartBody.Part.createFormData(
                    "audio_file",
                    fileName,
                    audioFileRequestBody
            );

            uploadFile(audioFilePart, targetWordRequestBody);
        } catch (Exception e) {
            Log.e(TAG, "Error preparing upload data", e);
            Toast.makeText(this, "Error preparing upload: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private RequestBody createRequestBodyFromUri(Context context, Uri uri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            if (inputStream == null) return null;

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            byte[] data = new byte[4096];
            int nRead;
            while ((nRead = inputStream.read(data, 0, data.length)) != -1) {
                buffer.write(data, 0, nRead);
            }
            buffer.flush();
            byte[] fileBytes = buffer.toByteArray();
            inputStream.close();

            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType == null) {
                String fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString());
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension);
            }
            if (mimeType == null) {
                mimeType = "application/octet-stream";
            }

            Log.d(TAG, "Creating RequestBody with MIME type: " + mimeType);
            return RequestBody.create(MediaType.parse(mimeType), fileBytes);

        } catch (IOException e) {
            Log.e(TAG, "IOException while creating RequestBody from URI", e);
            return null;
        }
    }

    private void uploadFile(MultipartBody.Part audioFilePart, RequestBody targetWordRequestBody) {
        setLoadingState(true);

        Call<SentenceAnalysisResult> call = apiService.analyzeSentences(audioFilePart, targetWordRequestBody);

        call.enqueue(new Callback<SentenceAnalysisResult>() {
            @Override
            public void onResponse(@NonNull Call<SentenceAnalysisResult> call, @NonNull Response<SentenceAnalysisResult> response) {
                setLoadingState(false);
                if (response.isSuccessful() && response.body() != null) {
                    SentenceAnalysisResult analysisResult = response.body();
                    Intent intent = new Intent(UploadSpeechActivity.this, SummaryResultsActivity.class);
                    intent.putExtra(SummaryResultsActivity.ANALYSIS_RESULT, analysisResult);
                    startActivity(intent);

                    Log.i(TAG, "Upload successful");
                    Toast.makeText(UploadSpeechActivity.this, "Analysis complete! Status: ", Toast.LENGTH_LONG).show();
                } else {
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.errorBody());
                    Toast.makeText(UploadSpeechActivity.this, "API Error: " + response.code(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<SentenceAnalysisResult> call, @NonNull Throwable t) {
                setLoadingState(false);
                Log.e(TAG, "Upload failed", t);
                Toast.makeText(UploadSpeechActivity.this, "Upload failed: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setLoadingState(boolean isLoading) {
        if (isLoading) {
            progressBar.setVisibility(View.VISIBLE);
            btnUpload.setEnabled(false);
            btnSelectAttachment.setEnabled(false);
            btnRemoveAttachment.setEnabled(false);
            etTargetWord.setEnabled(false);
        } else {
            progressBar.setVisibility(View.GONE);
            btnUpload.setEnabled(true);
            btnSelectAttachment.setEnabled(true);
            btnRemoveAttachment.setEnabled(selectedAudioUri != null);
            etTargetWord.setEnabled(true);
        }
    }

    @SuppressLint("Range")
    public static String getFileName(Context context, Uri uri) {
        String result = null;
        if (uri.getScheme() != null && uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            } catch (Exception e) {
                Log.e(TAG, "Error getting filename from ContentResolver", e);
            }
        }
        if (result == null) {
            result = uri.getPath();
            if (result != null) {
                int cut = result.lastIndexOf('/');
                if (cut != -1) {
                    result = result.substring(cut + 1);
                }
            } else {
                result = "unknown_file";
            }
        }
        return result;
    }

}