package com.example.echoenglish_mobile.view.activity.translate_text;

import androidx.activity.result.ActivityResultCallback; // Callback chung, vẫn dùng được nhưng không tối ưu bằng method reference
import androidx.activity.result.ActivityResultLauncher;
// Bỏ import này: import androidx.activity.result.contract.ActivityResultContracts;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull; // Cần thiết
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

// Import các lớp cần thiết từ thư viện CanHub Cropper
import com.canhub.cropper.CropImageContract; // <<--- QUAN TRỌNG: Contract mới
import com.canhub.cropper.CropImageContractOptions; // <<--- QUAN TRỌNG: Options mới
import com.canhub.cropper.CropImageOptions; // <<--- QUAN TRỌNG: Options chi tiết
import com.canhub.cropper.CropImageView; // <<--- QUAN TRỌNG: Để lấy CropResult
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
// Bỏ import này nếu không dùng: import com.example.echoenglish_mobile.view.activity.auth.MainActivity;
// Import đúng lớp Request/Response của bạn
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateRequest;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateResponse;

// Bỏ import BuildConfig của thư viện khác: import com.github.mikephil.charting.BuildConfig;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslateTextActivity extends AppCompatActivity {

    private static final String TAG = "TranslateTextActivity";

    // --- Views ---
    private EditText editTextSource;
    private RadioGroup radioGroupDirection;
    private RadioButton radioEngToVie;
    private RadioButton radioVieToEng;
    private Button buttonTranslate;
    private Button buttonCaptureImage;
    private ProgressBar progressBar;
    private TextView textViewResult;

    // --- Logic Components ---
    private ApiService apiService;
    private TextRecognizer textRecognizer;
    private Uri cameraImageUri;

    // --- ActivityResultLaunchers ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    // Sửa lại kiểu dữ liệu cho launcher cắt ảnh
    private ActivityResultLauncher<CropImageContractOptions> cropImageLauncher; // <<--- SỬA Ở ĐÂY

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_translate_text);

        // --- Ánh xạ View ---
        editTextSource = findViewById(R.id.editTextSource);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioEngToVie = findViewById(R.id.radioEngToVie);
        radioVieToEng = findViewById(R.id.radioVieToEng);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        progressBar = findViewById(R.id.progressBar);
        textViewResult = findViewById(R.id.textViewResult);

        // --- Khởi tạo ---
        apiService = ApiClient.getApiService(); // Đảm bảo ApiClient.getApiService() hoạt động đúng
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);
        initializeLaunchers();

        // --- Listeners ---
        buttonTranslate.setOnClickListener(v -> {
            String textToTranslate = editTextSource.getText().toString().trim();
            if (!textToTranslate.isEmpty()) {
                callTranslateApi(textToTranslate);
            } else {
                Toast.makeText(this, "Vui lòng nhập văn bản", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCaptureImage.setOnClickListener(v -> checkCameraPermissionAndOpenCamera());
    }

    private void initializeLaunchers() {
        // 1. Launcher xin quyền Camera (Giữ nguyên)
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        Log.d(TAG, "Camera permission granted.");
                        openCamera();
                    } else {
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_LONG).show();
                    }
                });

        // 2. Launcher nhận kết quả từ Camera (Giữ nguyên)
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Camera returned OK result. Image URI: " + cameraImageUri);
                        if (cameraImageUri != null) {
                            // Khởi chạy Activity cắt ảnh với URI vừa nhận được
                            startCropActivity(cameraImageUri);
                        } else {
                            Log.e(TAG, "Camera returned OK but cameraImageUri is null!");
                            Toast.makeText(this, "Lỗi không lấy được URI ảnh đã chụp", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d(TAG, "Camera returned Cancelled or Error result: " + result.getResultCode());
                        if (cameraImageUri != null) cameraImageUri = null; // Reset URI
                        Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });

        // 3. Launcher mới để nhận kết quả từ Activity Cắt Ảnh
        // SỬ DỤNG CropImageContract VÀ NHẬN TRỰC TIẾP CropImageView.CropResult
        cropImageLauncher = registerForActivityResult(
                new CropImageContract(), // <<--- SỬA Ở ĐÂY: Dùng contract của thư viện
                this::onCropImageResult // <<--- SỬA Ở ĐÂY: Gọi phương thức xử lý kết quả
        );
    }

    // Phương thức mới để xử lý kết quả từ CropImageContract
    private void onCropImageResult(@NonNull CropImageView.CropResult result) {
        if (result.isSuccessful()) {
            // Lấy URI thành công
            Uri croppedUri = result.getUriContent(); // <<--- SỬA Ở ĐÂY: Dùng getUriContent() hoặc getUriFilePath()
            // getOriginalUri() : Lấy URI gốc trước khi cắt
            // getBitmap() : Lấy ảnh bitmap đã cắt (nếu được yêu cầu trong options)
            // getError() : Lấy lỗi nếu isSuccessful() là false (nhưng nên check isSuccessful trước)

            Log.i(TAG, "Image cropping successful. Cropped URI: " + croppedUri);
            if (croppedUri != null) {
                try {
                    InputImage inputImage = InputImage.fromFilePath(TranslateTextActivity.this, croppedUri);
                    processImageWithMlKit(inputImage);
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create InputImage from cropped URI", e);
                    Toast.makeText(TranslateTextActivity.this, "Lỗi đọc ảnh đã cắt", Toast.LENGTH_SHORT).show();
                }
            } else {
                Log.e(TAG, "Crop was successful but getUriContent() returned null!");
                Toast.makeText(TranslateTextActivity.this, "Lỗi không lấy được URI ảnh đã cắt", Toast.LENGTH_SHORT).show();
            }
        } else {
            // Xử lý lỗi cắt ảnh
            Exception error = result.getError(); // <<--- SỬA Ở ĐÂY: Lấy lỗi từ CropResult
            Log.e(TAG, "Image cropping failed", error);
            String errorMessage = "Lỗi cắt ảnh";
            if (error != null) {
                errorMessage += ": " + error.getMessage();
            }
            Toast.makeText(TranslateTextActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        }
    }


    // checkCameraPermissionAndOpenCamera() (Giữ nguyên)
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Camera permission already granted. Opening camera.");
            openCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Log.i(TAG, "Showing rationale for camera permission.");
            Toast.makeText(this, "Ứng dụng cần quyền Camera để quét chữ từ ảnh.", Toast.LENGTH_LONG).show();
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        } else {
            Log.d(TAG, "Requesting camera permission.");
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    // openCamera() (Giữ nguyên)
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        cameraImageUri = createImageFileUri();
        if (cameraImageUri != null) {
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            Log.d(TAG, "Created cameraImageUri: " + cameraImageUri + ". Starting camera activity.");
            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                takePictureLauncher.launch(takePictureIntent);
            } else {
                Log.e(TAG, "No camera application found.");
                Toast.makeText(this, "Không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
                cameraImageUri = null;
            }
        } else {
            Log.e(TAG, "Failed to create image URI for camera.");
            Toast.makeText(this, "Không thể tạo file để lưu ảnh", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Khởi chạy Activity cắt ảnh bằng cách sử dụng cropImageLauncher và CropImageContractOptions.
     * @param sourceUri URI của ảnh gốc cần cắt (từ camera).
     */
    private void startCropActivity(Uri sourceUri) {
        Log.d(TAG, "Starting crop activity for source URI: " + sourceUri);

        // Tạo đối tượng Options chi tiết (tùy chọn)
        CropImageOptions cropOptions = new CropImageOptions();
        cropOptions.guidelines = CropImageView.Guidelines.ON; // Bật lưới hướng dẫn
        cropOptions.multiTouchEnabled = true; // Cho phép zoom/xoay đa điểm
        // cropOptions.aspectRatioX = 1; // Tỉ lệ X (ví dụ)
        // cropOptions.aspectRatioY = 1; // Tỉ lệ Y (ví dụ)
        cropOptions.fixAspectRatio = false; // Cho phép tỉ lệ tự do
        // cropOptions.outputCompressFormat = Bitmap.CompressFormat.PNG; // Định dạng output
        // cropOptions.outputCompressQuality = 90; // Chất lượng nén
        // ... và nhiều tùy chọn khác trong CropImageOptions

        // Tạo đối tượng Contract Options, truyền vào URI nguồn và options chi tiết
        CropImageContractOptions contractOptions = new CropImageContractOptions(sourceUri, cropOptions); // <<--- SỬA Ở ĐÂY

        // Khởi chạy launcher với contract options
        try {
            cropImageLauncher.launch(contractOptions); // <<--- SỬA Ở ĐÂY
        } catch (ActivityNotFoundException e) {
            Log.e(TAG, "CropImageActivity not found (Check Manifest declaration?)", e);
            Toast.makeText(this, "Lỗi: Không tìm thấy Activity cắt ảnh.", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Log.e(TAG, "Error launching crop activity", e);
            Toast.makeText(this, "Lỗi khi mở màn hình cắt ảnh.", Toast.LENGTH_SHORT).show();
        }
    }


    // createImageFileUri() (Giữ nguyên, nhưng kiểm tra lại import BuildConfig)
    private Uri createImageFileUri() {
        Uri contentUri = null;
        try {
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + "_";
            File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            if (storageDir == null) {
                Log.e(TAG, "getExternalFilesDir(Environment.DIRECTORY_PICTURES) returned null.");
                throw new IOException("Cannot access external files directory.");
            }
            File imageFile = File.createTempFile(imageFileName, ".jpg", storageDir);

            // Đảm bảo bạn đang import đúng BuildConfig từ package của bạn
            String authority = "com.example.echoenglish_mobile.provider"; // <<--- KIỂM TRA IMPORT
            contentUri = FileProvider.getUriForFile(this, authority, imageFile);
            Log.i(TAG, "Created temp image file: " + imageFile.getAbsolutePath() + ", Uri: " + contentUri);

        } catch (IOException ex) {
            Log.e(TAG, "Error creating image file", ex);
            Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
        } catch (IllegalArgumentException ex) {
            Log.e(TAG, "Error getting FileProvider URI. Check authority 'com.example.echoenglish_mobile.provider.provider' in Manifest?", ex);
            Toast.makeText(this, "Lỗi cấu hình FileProvider", Toast.LENGTH_SHORT).show();
        }
        return contentUri;
    }


    // processImageWithMlKit() (Giữ nguyên)
    private void processImageWithMlKit(InputImage image) {
        Log.d(TAG, "Processing CROPPED image with ML Kit Text Recognition.");
        showLoading(true);
        textViewResult.setText(""); // Xóa kết quả cũ

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> {
                    showLoading(false);
                    String extractedText = visionText.getText().trim();
                    Log.i(TAG, "ML Kit Text Recognition Success (from cropped). Extracted Text: \n" + extractedText);

                    if (!extractedText.isEmpty()) {
                        editTextSource.setText(extractedText);
                        callTranslateApi(extractedText);
                    } else {
                        Log.i(TAG, "ML Kit found no text in the cropped image.");
                        editTextSource.setText("");
                        Toast.makeText(TranslateTextActivity.this, "Không nhận diện được chữ nào trong vùng đã chọn", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Log.e(TAG, "ML Kit Text Recognition Failed (from cropped)", e);
                    editTextSource.setText("");
                    Toast.makeText(TranslateTextActivity.this, "Lỗi quét chữ từ vùng chọn: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    // callTranslateApi() (Giữ nguyên)
    private void callTranslateApi(String originalText) {
        Log.d(TAG, "Calling translate API for text: " + originalText);
        showLoading(true);
        textViewResult.setText("Đang dịch...");

        String prompt;
        String sourceLang, targetLang;
        if (radioEngToVie.isChecked()) { // Sử dụng biến radioEngToVie đã ánh xạ
            prompt = "Dịch đoạn tiếng Anh sau sang tiếng việt, chỉ thực hiện dịch đúng văn bản đó thôi, không cần làm gì thêm: ";
            sourceLang = "English";
            targetLang = "Vietnamese";
        } else { // Mặc định là Việt -> Anh nếu radioEngToVie không được chọn
            prompt = "Dịch đoạn tiếng Việt sau sang tiếng anh: chỉ thực hiện dịch đúng văn bản đó thôi, không cần làm gì thêm";
            sourceLang = "Vietnamese";
            targetLang = "English";
        }

        String fullMessage = prompt + originalText;

        Log.i(TAG, "Constructed API message (Translate " + sourceLang + " to " + targetLang + "): " + fullMessage);
        TranslateRequest request = new TranslateRequest(fullMessage); // Đảm bảo lớp này tồn tại và đúng cấu trúc

        apiService.translateText(request).enqueue(new Callback<TranslateResponse>() { // Đảm bảo ApiService có phương thức translateText
            @Override
            public void onResponse(@NonNull Call<TranslateResponse> call, @NonNull Response<TranslateResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getText() != null) {
                    String translatedText = response.body().getText().trim(); // Đảm bảo TranslateResponse có getText()
                    textViewResult.setText(translatedText);
                    Log.i(TAG, "API Translation Success. Result: " + translatedText);
                } else {
                    String errorBodyString = "Unknown error body";
                    int responseCode = response.code();
                    try {
                        if (response.errorBody() != null) errorBodyString = response.errorBody().string();
                    } catch (IOException e) { Log.e(TAG, "Error reading error body", e); }
                    String errorMessage = "Lỗi API: " + responseCode + " - " + response.message() + "\nDetails: " + errorBodyString;
                    textViewResult.setText(errorMessage);
                    Log.e(TAG, "API Response Error: " + errorMessage);
                    Toast.makeText(TranslateTextActivity.this, "Lỗi khi dịch (Code: " + responseCode + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslateResponse> call, @NonNull Throwable t) {
                showLoading(false);
                String networkErrorMessage = "Lỗi kết nối hoặc xử lý: " + t.getMessage();
                textViewResult.setText(networkErrorMessage);
                Log.e(TAG, "API Call Failure", t);
                Toast.makeText(TranslateTextActivity.this, "Lỗi kết nối mạng hoặc xử lý", Toast.LENGTH_LONG).show();
            }
        });
    }

    // showLoading() (Giữ nguyên)
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        buttonTranslate.setEnabled(!isLoading);
        buttonCaptureImage.setEnabled(!isLoading);
        editTextSource.setEnabled(!isLoading);
        for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
            View child = radioGroupDirection.getChildAt(i);
            if (child instanceof RadioButton) child.setEnabled(!isLoading);
        }
    }

    // onDestroy() (Giữ nguyên)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "TranslateTextActivity onDestroy called."); // Sửa lại tên Activity trong log
    }
}