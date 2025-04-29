package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.request.WritingAnalysisRequest;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.util.FileUtils;
import com.example.echoenglish_mobile.view.activity.webview.WebViewFragment;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadNewWritingActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());
    private ImageButton btnClose;
    private EditText editTextTopic;
    private EditText editTextContent;
    private RecyclerView attachmentsWrapper;
    private LinearLayout emptyAttachmentsState;
    private RecyclerView recyclerAttachments;
    private LinearLayout btnAddAttachment;
    private ImageButton btnCamera;
    private ImageButton btnGallery;
    private TextView btnSubmit;

    // Data & Adapter
    private ArrayList<Attachment> attachmentsList = new ArrayList<>();
    private AttachmentAdapter attachmentAdapter;

    // Activity Result Launchers
    private ActivityResultLauncher<Intent> generalFilePickerLauncher;
    private ActivityResultLauncher<Intent> galleryPickerLauncher;
    private ActivityResultLauncher<Uri> cameraLauncher;
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;

    private Uri tempCameraUri;
    private static final String LOADING_DIALOG_TAG = "PronunciationLoading";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_writing);
//        LoadingDialogFragment.showLoading(getSupportFragmentManager(), LOADING_DIALOG_TAG, "Analyzing pronunciation..."); // Có thể truyền message tùy chỉnh

        findViews();
        setupRecyclerView();
        setupResultLaunchers();
        setupClickListeners();
        updateAttachmentVisibility();
    }

    private void findViews() {
        btnClose = findViewById(R.id.btnClose);
        editTextTopic = findViewById(R.id.editTextTopic);
        editTextContent = findViewById(R.id.editTextContent);
        editTextContent.setText("Social media is very popular today especialy among young persons. It have changed the way we communicate signifcantly. One main advantage are connecting with friends and family who live far away. People shares photos updates and keep in touch easily. Also businesses can use platforms like facebook or instagram for reach customers and promote there products cheap. However there is also negative sides. Too much time spent on social media might leads to addiction and affect real life relationships. Comparing yourself to others online perfect lifes can cause feelings of inadequacy or depression." +
                "" +
                "Another problem are the spread of fake news and misinformation which is difficult controlling. Privacy concern is also a big issue because personal datas can be misused. In conclude social media has both good points and bad points. Using it moderation and being aware of the risks seem the best approach. We must to learn how use these tools responsible for maximize benefits and minimize harmfull effects.");
        attachmentsWrapper = findViewById(R.id.recyclerAttachments);
        emptyAttachmentsState = findViewById(R.id.emptyAttachmentsState);
        recyclerAttachments = findViewById(R.id.recyclerAttachments);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupRecyclerView() {
        attachmentAdapter = new AttachmentAdapter(attachmentsList, this);
        int numberOfColumns = 1;
        recyclerAttachments.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerAttachments.setAdapter(attachmentAdapter);
    }

    private void setupResultLaunchers() {
        // General File Picker (*/*)
        generalFilePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleFileSelectionResult(result.getResultCode(), result.getData()));

        // Gallery Picker (Images Only)
        galleryPickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> handleFileSelectionResult(result.getResultCode(), result.getData()));

        // Camera Launcher
        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.TakePicture(),
                success -> {
                    if (success && tempCameraUri != null) {
                        Log.d(TAG, "Camera photo taken successfully: " + tempCameraUri);
                        addAttachmentFromUri(tempCameraUri);
                        tempCameraUri = null; // Reset temp URI
                    } else {
                        Log.d(TAG, "Camera capture cancelled or failed.");
                        tempCameraUri = null; // Reset temp URI
                    }
                });

        // Camera Permission Launcher
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted.");
                        launchCamera(); // Try launching again
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Camera permission is required to take photos.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());
        btnAddAttachment.setOnClickListener(v -> openGeneralFilePicker());
        btnGallery.setOnClickListener(v -> openGalleryPicker());
        btnCamera.setOnClickListener(v -> checkCameraPermissionAndLaunch());
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    // --- File Selection Logic ---

    private void openGeneralFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*"); // All file types
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            generalFilePickerLauncher.launch(Intent.createChooser(intent, "Select Files"));
        } catch (android.content.ActivityNotFoundException ex) {
            showNoFileManagerToast();
        }
    }

    private void openGalleryPicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        // For modern Android, ACTION_GET_CONTENT is often preferred even for gallery
        // Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        // intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        try {
            galleryPickerLauncher.launch(Intent.createChooser(intent, "Select Images from Gallery"));
        } catch (android.content.ActivityNotFoundException ex) {
            showNoFileManagerToast(); // Or specific message for gallery app
        }
    }

    private void checkCameraPermissionAndLaunch() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            launchCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Optional: Show explanation dialog before requesting again
            Toast.makeText(this, "Camera access is needed to take photos.", Toast.LENGTH_LONG).show();
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA); // Request again
        }
        else {
            // Request permission
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    private void launchCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "New Picture");
        values.put(MediaStore.Images.Media.DESCRIPTION, "From Camera");
        tempCameraUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (tempCameraUri != null) {
            cameraLauncher.launch(tempCameraUri);
        } else {
            Log.e(TAG,"Could not create URI for camera image.");
            Toast.makeText(this, "Failed to prepare camera.", Toast.LENGTH_SHORT).show();
        }
    }


    // Common handler for file/gallery picker results
    private void handleFileSelectionResult(int resultCode, @Nullable Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            if (data.getClipData() != null) {
                // Multiple files
                ClipData clipData = data.getClipData();
                int count = clipData.getItemCount();
                Log.d(TAG, "Multiple files selected: " + count);
                for (int i = 0; i < count; i++) {
                    Uri fileUri = clipData.getItemAt(i).getUri();
                    addAttachmentFromUri(fileUri);
                }
            } else if (data.getData() != null) {
                // Single file
                Uri fileUri = data.getData();
                Log.d(TAG, "Single file selected: " + fileUri);
                addAttachmentFromUri(fileUri);
            } else {
                Log.w(TAG, "File selection OK, but no data URI or ClipData found.");
            }
        } else {
            Log.d(TAG, "File selection cancelled or failed. Result code: " + resultCode);
        }
    }

    // Processes a selected URI and adds it to the list
    private void addAttachmentFromUri(Uri uri) {
        if (uri == null) return;

        try {
            String fileName = FileUtils.getFileName(this, uri);
            String mimeType = FileUtils.getMimeType(this, uri);
            long fileSize = FileUtils.getFileSize(this, uri); // Optional

            Attachment newAttachment = new Attachment(uri, fileName, mimeType, fileSize);

            // Avoid adding duplicates based on URI
            if (!attachmentsList.contains(newAttachment)) {
                attachmentsList.add(newAttachment);
                attachmentAdapter.notifyItemInserted(attachmentsList.size() - 1);
                updateAttachmentVisibility(); // Update container visibility
                Log.d(TAG, "Added attachment: " + fileName + " (Type: " + mimeType + ")");
            } else {
                Log.d(TAG, "Attachment already added: " + fileName);
                // Maybe show a subtle toast?
                // Toast.makeText(this, "'" + fileName + "' already added.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing attachment URI: " + uri, e);
            Toast.makeText(this, "Could not process selected file.", Toast.LENGTH_SHORT).show();
        }
    }

    // --- RecyclerView Interaction (Callback from Adapter) ---
//    @Override
//    public void onRemoveClicked(int position) {
//        if (position >= 0 && position < attachmentsList.size()) {
//            Attachment removed = attachmentsList.remove(position);
//            attachmentAdapter.notifyItemRemoved(position);
//            // Important: Notify range change if positions shift
//            attachmentAdapter.notifyItemRangeChanged(position, attachmentsList.size());
//            updateAttachmentVisibility();
//            Log.d(TAG, "Removed attachment: " + removed.getFileName());
//        }
//    }

    // --- UI State Update ---
    private void updateAttachmentVisibility() {
        if (attachmentsList.isEmpty()) {
            emptyAttachmentsState.setVisibility(View.VISIBLE);
            recyclerAttachments.setVisibility(View.GONE);
        } else {
            emptyAttachmentsState.setVisibility(View.GONE);
            recyclerAttachments.setVisibility(View.VISIBLE);
        }
        // You might want to update a counter TextView if you add one
    }

    // --- Submission ---
    private void handleSubmit() {
        String topic = editTextTopic.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter the post content.", Toast.LENGTH_SHORT).show();
            editTextContent.requestFocus();
            return;
        }
        callWritingAnalysisApi(content, topic);
//
//        for(Attachment att : attachmentsList) {
//            Log.d(TAG, "  -> File: " + att.getFileName() + " (URI: " + att.getUri() + ")");
//        }
//        Log.i(TAG, "----------------------");


    }
    private void callWritingAnalysisApi(String inputText, String inputContext) {
        WritingAnalysisRequest requestBody = new WritingAnalysisRequest(
                inputText,
                inputContext
        );
        Call<ResponseBody> call = ApiClient.getApiService().analyzeWriting(requestBody);

        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    final ResponseBody responseBody = response.body();

                    backgroundExecutor.execute(() -> {
                        String jsonResponse = null;
                        String jsCode = null;
                        boolean processingSuccess = false;
                        String errorMsg = null;

                        try {
                            jsonResponse = responseBody.string();
                            if (jsonResponse != null && !jsonResponse.isEmpty()) {
                                // Tạm thời chưa escape, chỉ tạo jsCode
                                jsCode = jsonResponse;
                                processingSuccess = true;
                                Log.d(TAG, "Background: jsCode prepared (length=" + jsCode.length() + ")");
                            } else {
                                errorMsg = "API response body string is empty after reading.";
                                Log.e(TAG, "Background: " + errorMsg);
                            }
                        } catch (IOException e) {
                            errorMsg = "IOException while reading ResponseBody";
                            Log.e(TAG, "Background: " + errorMsg, e);
                        } catch (OutOfMemoryError e) {
                            errorMsg = "OutOfMemoryError while reading ResponseBody to string";
                            Log.e(TAG, "Background: " + errorMsg, e);
                        } finally {
                            responseBody.close();
                            Log.d(TAG,"Background: ResponseBody closed.");
                        }

                        final String finalJsonData = jsonResponse;
                        final boolean success = processingSuccess;
                        final String finalErrorMsg = errorMsg;

                        mainThreadHandler.post(() -> {
                            Log.d(TAG, "UI Thread: Received result from background processing. Success: " + success);

                            if (success && finalJsonData != null) {
                                openAnalysisFragment(finalJsonData);
                            } else {
                                showError(finalErrorMsg != null ? finalErrorMsg : "Failed to process server response.");
                            }
                        });
                    });

                } else {
                    String errorBodyStr = "Unknown error";
                    int responseCode = response.code();
                    if (!response.isSuccessful() && response.errorBody() != null) {
                        try {
                            errorBodyStr = response.errorBody().string();
                        } catch (IOException e) {
                            Log.e(TAG, "Error reading errorBody", e);
                            errorBodyStr = "Error reading error body";
                        }
                    } else if(response.isSuccessful() && response.body() == null) {
                        errorBodyStr = "Response successful but body is null";
                        Log.e(TAG, errorBodyStr);
                    }
                    Log.e(TAG, "API Error Response Code: " + responseCode + " - Body: " + errorBodyStr);
                    showError("API Error: " + responseCode);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(TAG, "<<< onFailure CALLED on Thread: " + Thread.currentThread().getName(), t);
                showError("Network error: " + t.getMessage());
            }
        });
    }

    private void openAnalysisFragment(String finalJsonData) {
        Log.d(TAG, "UI Thread: Attempting to open Analysis Fragment...");
        if (!isFinishing() && !isDestroyed()) {
            try {
                WebViewFragment fragment = WebViewFragment.newInstance(finalJsonData);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();

                transaction.replace(android.R.id.content, fragment); // Hoặc ID container của bạn
                transaction.addToBackStack(null);
                transaction.commit();
                Log.d(TAG, "UI Thread: Fragment transaction committed.");
            } catch (IllegalStateException e) {
                Log.e(TAG, "Error committing Fragment transaction (Activity state issue?): ", e);
                showError("Could not display results. Please try again.");
            } catch (Exception e) {
                Log.e(TAG, "Error during Fragment handling: ", e);
                showError("An unexpected error occurred while showing results.");
            }
        } else {
            Log.w(TAG, "Activity was finishing or destroyed before Fragment could be shown.");
            // Có thể thông báo cho người dùng hoặc không làm gì cả
        }
    }


    // Hàm hiển thị lỗi đơn giản
    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error Displayed: " + message);
    }
    // --- Utility ---
    private void showNoFileManagerToast() {
        Toast.makeText(this, "No app found to handle this action. Please install a file manager or gallery app.", Toast.LENGTH_LONG).show();
    }
}