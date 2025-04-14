package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.Manifest;
import android.app.Activity;
import android.content.ClipData;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
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
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager; // Use GridLayoutManager
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.util.FileUtils;

import java.util.ArrayList;

public class UploadNewWritingActivity extends AppCompatActivity {
    private static final String TAG = "CreatePostActivity";

    // Views
    private ImageButton btnClose;
    private EditText editTextTopic;
    private EditText editTextContent;
    private RecyclerView attachmentsWrapper; // Container for recycler and empty state
    private LinearLayout emptyAttachmentsState;
    private RecyclerView recyclerAttachments;
    private LinearLayout btnAddAttachment; // Main add button
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

    private Uri tempCameraUri; // To store URI for camera photo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Use the correct layout file name from your XML
        setContentView(R.layout.activity_upload_writing); // Or whatever you named it

        findViews();
        setupRecyclerView();
        setupResultLaunchers();
        setupClickListeners();
        updateAttachmentVisibility(); // Initial state
    }

    private void findViews() {
        btnClose = findViewById(R.id.btnClose);
        editTextTopic = findViewById(R.id.editTextTopic);
        editTextContent = findViewById(R.id.editTextContent);
        attachmentsWrapper = findViewById(R.id.recyclerAttachments); // Ensure this ID exists if needed
        emptyAttachmentsState = findViewById(R.id.emptyAttachmentsState);
        recyclerAttachments = findViewById(R.id.recyclerAttachments);
        btnAddAttachment = findViewById(R.id.btnAddAttachment);
        btnCamera = findViewById(R.id.btnCamera);
        btnGallery = findViewById(R.id.btnGallery);
        btnSubmit = findViewById(R.id.btnSubmit);
        // filePreviewContainer and fileItemsContainer are ignored based on analysis
    }

    private void setupRecyclerView() {
        attachmentAdapter = new AttachmentAdapter(attachmentsList, this); // Pass listener
        // Use GridLayoutManager for a grid appearance
        int numberOfColumns = 1; // Adjust as needed
        recyclerAttachments.setLayoutManager(new GridLayoutManager(this, numberOfColumns));
        recyclerAttachments.setAdapter(attachmentAdapter);
        // Add item decoration for spacing if needed
        // recyclerAttachments.addItemDecoration(new GridSpacingItemDecoration(numberOfColumns, spacingInPixels, true));
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

        // --- Gathered Data ---
        Log.i(TAG, "--- Submitting Post ---");
        Log.i(TAG, "Topic: " + (topic.isEmpty() ? "[None]" : topic));
        Log.i(TAG, "Content Length: " + content.length());
        Log.i(TAG, "Attachment Count: " + attachmentsList.size());
        for(Attachment att : attachmentsList) {
            Log.d(TAG, "  -> File: " + att.getFileName() + " (URI: " + att.getUri() + ")");
        }
        Log.i(TAG, "----------------------");

        // --- TODO: Actual Submission Implementation ---
        // 1. Show a loading indicator (ProgressBar, disable button).
        // 2. Handle File Uploads: This is the most complex part.
        //    - For each Uri in attachmentsList:
        //    - Get an InputStream using `getContentResolver().openInputStream(uri)`.
        //    - Upload the stream to your server (e.g., using Retrofit multipart request, Firebase Storage, S3).
        //    - This should ideally happen in a background thread or WorkManager job.
        //    - Collect the URLs or IDs of the uploaded files from the server response.
        // 3. Send Post Data to API:
        //    - Create a request object (e.g., `PostRequest`) containing topic, content, and the list of uploaded attachment URLs/IDs.
        //    - Use Retrofit to send this request to your backend API endpoint.
        // 4. Handle API Response:
        //    - On success: Show success message, maybe clear fields, finish activity or navigate elsewhere.
        //    - On failure: Show error message to the user, hide loading indicator.
        // 5. Hide loading indicator.
        // -----------------------------------------------

        // --- Placeholder ---
        Toast.makeText(this, "Submitting post... (Simulation)", Toast.LENGTH_LONG).show();
        // Disable button during fake "submission"
        btnSubmit.setEnabled(false);
        // Simulate network delay and finish
        new android.os.Handler().postDelayed(() -> {
            Toast.makeText(this, "Post Submitted Successfully! (Simulation)", Toast.LENGTH_SHORT).show();
            finish();
        }, 2000); // 2 second delay
        // -----------------
    }

    // --- Utility ---
    private void showNoFileManagerToast() {
        Toast.makeText(this, "No app found to handle this action. Please install a file manager or gallery app.", Toast.LENGTH_LONG).show();
    }
}