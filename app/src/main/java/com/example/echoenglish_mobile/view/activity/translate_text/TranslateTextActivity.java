package com.example.echoenglish_mobile.view.activity.translate_text;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
// (Optional - Nếu dùng FileProvider cho ảnh full-res)
// import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
// (Optional - Nếu dùng FileProvider)
// import android.os.Environment;
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

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.view.activity.auth.MainActivity;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateRequest;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateResponse;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.text.Text;
import com.google.mlkit.vision.text.TextRecognition;
import com.google.mlkit.vision.text.TextRecognizer;
import com.google.mlkit.vision.text.latin.TextRecognizerOptions;

// (Optional - Nếu dùng FileProvider)
// import java.io.File;
import java.io.IOException;
// (Optional - Nếu dùng FileProvider)
// import java.text.SimpleDateFormat;
// import java.util.Date;
// import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TranslateTextActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // --- Khai báo các View ---
    private EditText editTextSource;
    private RadioGroup radioGroupDirection;
    private RadioButton radioEngToVie;
    private RadioButton radioVieToEng;
    private Button buttonTranslate;
    private Button buttonCaptureImage;
    private ProgressBar progressBar;
    private TextView textViewResult;
    // --- ---

    // --- Khai báo các thành phần khác ---
    private ApiService apiService;
    private TextRecognizer textRecognizer;
    private Uri imageUri; // Để lưu URI ảnh chụp (nếu dùng FileProvider)
    // --- ---

    // --- ActivityResultLaunchers ---
    private ActivityResultLauncher<String> requestCameraPermissionLauncher;
    private ActivityResultLauncher<Intent> takePictureLauncher;
    // --- ---

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sử dụng setContentView với layout ID
        setContentView(R.layout.activity_translate_text); // <<--- Đảm bảo tên layout của bạn là activity_main.xml

        // --- Ánh xạ View bằng findViewById ---
        editTextSource = findViewById(R.id.editTextSource);
        radioGroupDirection = findViewById(R.id.radioGroupDirection);
        radioEngToVie = findViewById(R.id.radioEngToVie);
        radioVieToEng = findViewById(R.id.radioVieToEng);
        buttonTranslate = findViewById(R.id.buttonTranslate);
        buttonCaptureImage = findViewById(R.id.buttonCaptureImage);
        progressBar = findViewById(R.id.progressBar);
        textViewResult = findViewById(R.id.textViewResult);
        // --- ---

        // Khởi tạo Retrofit Service
        // Đảm bảo bạn đã có lớp RetrofitClient và ApiService
        apiService = ApiClient.getApiService();

        // Khởi tạo ML Kit Text Recognizer
        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS);

        // --- Khởi tạo Launchers ---
        initializeLaunchers();

        // --- Set Listeners cho Buttons ---
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

    /**
     * Khởi tạo các ActivityResultLaunchers để xử lý quyền và kết quả camera.
     */
    private void initializeLaunchers() {
        // 1. Launcher xin quyền Camera
        requestCameraPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        // Quyền được cấp, mở camera
                        Log.d(TAG, "Camera permission granted.");
                        openCamera();
                    } else {
                        // Quyền bị từ chối
                        Log.w(TAG, "Camera permission denied.");
                        Toast.makeText(this, "Cần quyền Camera để chụp ảnh", Toast.LENGTH_LONG).show();
                    }
                });

        // 2. Launcher để nhận kết quả từ Camera
        takePictureLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        Log.d(TAG, "Camera returned OK result.");
                        // Chụp ảnh thành công
                        Bundle extras = result.getData() != null ? result.getData().getExtras() : null;
                        InputImage inputImage = null;

                        // Ưu tiên lấy ảnh bitmap (thumbnail) từ data trước
                        if (extras != null && extras.get("data") != null) {
                            Bitmap imageBitmap = (Bitmap) extras.get("data");
                            inputImage = InputImage.fromBitmap(imageBitmap, 0);
                            Log.d(TAG,"Got bitmap from camera intent data.");
                        }
                        // (Optional) Nếu dùng FileProvider và EXTRA_OUTPUT, xử lý URI ở đây
                        /* else if (imageUri != null) {
                            try {
                                // Xóa cache cũ của URI nếu có để đảm bảo đọc file mới nhất
                                getContentResolver().notifyChange(imageUri, null);
                                inputImage = InputImage.fromFilePath(this, imageUri);
                                Log.d(TAG,"Created InputImage from URI: " + imageUri);
                            } catch (IOException e) {
                                Log.e(TAG, "Error creating InputImage from URI", e);
                                Toast.makeText(this, "Lỗi đọc ảnh từ URI", Toast.LENGTH_SHORT).show();
                                imageUri = null; // Reset URI nếu lỗi
                                return; // Dừng lại nếu lỗi
                            }
                        } */

                        // Nếu lấy được ảnh thì xử lý bằng ML Kit
                        if (inputImage != null) {
                            processImageWithMlKit(inputImage);
                        } else {
                            Toast.makeText(this, "Không thể lấy dữ liệu ảnh", Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Could not get image data (bitmap or URI) from camera result.");
                        }
                    } else {
                        Log.d(TAG, "Camera returned Cancelled or Error result: " + result.getResultCode());
                        Toast.makeText(this, "Hủy chụp ảnh", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * Kiểm tra quyền Camera. Nếu chưa có thì yêu cầu, nếu có thì mở Camera.
     */
    private void checkCameraPermissionAndOpenCamera() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            // Đã có quyền, mở camera
            Log.d(TAG, "Camera permission already granted. Opening camera.");
            openCamera();
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            // Giải thích tại sao cần quyền (nếu người dùng đã từ chối trước đó)
            Log.i(TAG, "Showing rationale for camera permission.");
            Toast.makeText(this, "Ứng dụng cần quyền Camera để quét chữ từ ảnh.", Toast.LENGTH_LONG).show();
            // Bạn có thể hiện Dialog giải thích kỹ hơn ở đây trước khi gọi launch
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA); // Xin lại quyền
        } else {
            // Chưa có quyền hoặc người dùng chọn "Don't ask again", xin quyền
            Log.d(TAG, "Requesting camera permission.");
            requestCameraPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    /**
     * Mở ứng dụng Camera mặc định của hệ thống.
     */
    private void openCamera() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // (Optional) Nếu dùng FileProvider để lấy ảnh full resolution:
        /*
        imageUri = createImageFileUri(); // Hàm tạo file và lấy URI (xem code ở dưới)
        if (imageUri != null) {
             takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
             Log.d(TAG, "Putting image URI into camera intent: " + imageUri);
        } else {
             Log.e(TAG, "Could not create image URI, cannot start camera for full-res.");
             Toast.makeText(this, "Lỗi tạo file lưu ảnh", Toast.LENGTH_SHORT).show();
             return; // Không mở camera nếu không tạo được file
        }
        */

        // Kiểm tra xem có ứng dụng nào xử lý được Intent này không
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            Log.d(TAG, "Launching camera intent.");
            takePictureLauncher.launch(takePictureIntent);
        } else {
            Log.e(TAG, "No camera application found to handle the intent.");
            Toast.makeText(this, "Không tìm thấy ứng dụng Camera", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý ảnh đầu vào bằng ML Kit Text Recognition.
     * @param image InputImage đã được chuẩn bị từ Bitmap hoặc URI.
     */
    private void processImageWithMlKit(InputImage image) {
        Log.d(TAG, "Processing image with ML Kit Text Recognition.");
        showLoading(true);
        textViewResult.setText(""); // Xóa kết quả cũ

        textRecognizer.process(image)
                .addOnSuccessListener(visionText -> { // Sử dụng lambda cho gọn
                    showLoading(false);
                    String extractedText = visionText.getText().trim();
                    Log.d(TAG, "ML Kit Text Recognition Success. Extracted Text: \n" + extractedText);

                    if (!extractedText.isEmpty()) {
                        editTextSource.setText(extractedText);
                        // Tự động gọi dịch sau khi quét thành công
                        callTranslateApi(extractedText);
                    } else {
                        Log.i(TAG, "ML Kit found no text in the image.");
                        editTextSource.setText(""); // Xóa text cũ nếu có
                        Toast.makeText(TranslateTextActivity.this, "Không nhận diện được chữ nào trong ảnh", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> { // Sử dụng lambda cho gọn
                    showLoading(false);
                    Log.e(TAG, "ML Kit Text Recognition Failed", e);
                    editTextSource.setText(""); // Xóa text cũ nếu có
                    Toast.makeText(TranslateTextActivity.this, "Lỗi quét chữ: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    /**
     * Gọi API backend để dịch văn bản.
     * @param originalText Văn bản gốc cần dịch (không bao gồm chỉ dẫn).
     */
    private void callTranslateApi(String originalText) {
        Log.d(TAG, "Calling translate API for text: " + originalText);
        showLoading(true);
        textViewResult.setText("Đang dịch..."); // Thông báo trạng thái

        // --- Xây dựng message đầy đủ với chỉ dẫn ---
        String prompt;
        String sourceLang, targetLang; // Để log cho dễ nhìn
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
        // --- ---

        Log.i(TAG, "Constructed API message (Translate " + sourceLang + " to " + targetLang + "): " + fullMessage);
        TranslateRequest request = new TranslateRequest(fullMessage); // Dùng lớp Request đã tạo

        // Thực hiện gọi API bất đồng bộ
        apiService.translateText(request).enqueue(new Callback<TranslateResponse>() {
            @Override
            public void onResponse(@NonNull Call<TranslateResponse> call, @NonNull Response<TranslateResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null && response.body().getText() != null) {
                    // Thành công và có dữ liệu trả về
                    String translatedText = response.body().getText().trim();
                    textViewResult.setText(translatedText); // Sử dụng biến textViewResult
                    Log.i(TAG, "API Translation Success. Result: " + translatedText);
                } else {
                    // Xử lý lỗi từ phía server (API trả về lỗi 4xx, 5xx)
                    String errorBodyString = "Unknown error body";
                    int responseCode = response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorBodyString = response.errorBody().string();
                        }
                    } catch (IOException e) {
                        Log.e(TAG, "Error reading error body from response", e);
                        errorBodyString = "Error reading response body";
                    }
                    String errorMessage = "Lỗi API: " + responseCode + " - " + response.message() + "\nDetails: " + errorBodyString;
                    textViewResult.setText(errorMessage); // Hiển thị lỗi chi tiết
                    Log.e(TAG, "API Response Error: " + errorMessage);
                    Toast.makeText(TranslateTextActivity.this, "Lỗi khi dịch (Code: " + responseCode + ")", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<TranslateResponse> call, @NonNull Throwable t) {
                // Lỗi kết nối mạng hoặc lỗi khi thực hiện request/nhận response
                showLoading(false);
                String networkErrorMessage = "Lỗi kết nối hoặc xử lý: " + t.getMessage();
                textViewResult.setText(networkErrorMessage); // Hiển thị lỗi mạng
                Log.e(TAG, "API Call Failure (Network/Processing Error)", t);
                Toast.makeText(TranslateTextActivity.this, "Lỗi kết nối mạng hoặc xử lý", Toast.LENGTH_LONG).show();
            }
        });
    }

    /**
     * Hiển thị hoặc ẩn ProgressBar và bật/tắt các nút điều khiển.
     * @param isLoading True để hiển thị loading, False để ẩn.
     */
    private void showLoading(boolean isLoading) {
        Log.d(TAG, "Setting loading state: " + isLoading);
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE); // Sử dụng biến progressBar
        buttonTranslate.setEnabled(!isLoading); // Sử dụng biến buttonTranslate
        buttonCaptureImage.setEnabled(!isLoading); // Sử dụng biến buttonCaptureImage
        editTextSource.setEnabled(!isLoading); // Sử dụng biến editTextSource

        // Disable/Enable các RadioButton trong RadioGroup
        for (int i = 0; i < radioGroupDirection.getChildCount(); i++) {
            View child = radioGroupDirection.getChildAt(i);
            if (child instanceof RadioButton) { // Kiểm tra chắc chắn là RadioButton
                child.setEnabled(!isLoading);
            }
        }
        // Có thể disable cả RadioGroup để tránh tương tác khi đang load
        // radioGroupDirection.setEnabled(!isLoading);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "MainActivity onDestroy called.");
        // (Optional) Giải phóng tài nguyên nếu cần, ví dụ recognizer nếu API yêu cầu
        // if (textRecognizer != null) {
        //     textRecognizer.close();
        //     Log.d(TAG, "ML Kit TextRecognizer closed.");
        // }
    }


    // --- (Optional) Hàm tạo file và lấy URI nếu bạn muốn ảnh full resolution ---
    /*
     private Uri createImageFileUri() {
         Uri contentUri = null;
         try {
             // Tạo tên file duy nhất
             String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
             String imageFileName = "JPEG_" + timeStamp;
             // Lấy thư mục lưu trữ ảnh (nên dùng thư mục private của ứng dụng)
             File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
             if (storageDir == null) {
                  Log.e(TAG, "External storage directory is null (or not mounted?). Cannot create image file.");
                  Toast.makeText(this, "Không thể truy cập bộ nhớ ngoài", Toast.LENGTH_SHORT).show();
                  return null;
             }
              // Đảm bảo thư mục tồn tại
             if (!storageDir.exists() && !storageDir.mkdirs()) {
                  Log.e(TAG, "Failed to create storage directory: " + storageDir.getPath());
                  Toast.makeText(this, "Không thể tạo thư mục lưu ảnh", Toast.LENGTH_SHORT).show();
                  return null;
             }

             File imageFile = File.createTempFile(
                     imageFileName,  // prefix
                     ".jpg",         // suffix
                     storageDir      // directory
             );

             // Lấy URI qua FileProvider (Cần khai báo FileProvider trong Manifest và tạo file paths.xml)
             // THAY THẾ "com.your_app_package.provider" BẰNG AUTHORITY ĐÚNG CỦA BẠN
             String authority = BuildConfig.APPLICATION_ID + ".provider";
             contentUri = FileProvider.getUriForFile(this,
                     authority,
                     imageFile);
             Log.i(TAG, "Created image file: " + imageFile.getAbsolutePath() + ", URI: " + contentUri);

         } catch (IOException ex) {
             Log.e(TAG, "Error creating image file for camera", ex);
             Toast.makeText(this, "Lỗi tạo file ảnh", Toast.LENGTH_SHORT).show();
             contentUri = null; // Đảm bảo trả về null nếu lỗi
         } catch (IllegalArgumentException ex) {
             Log.e(TAG, "Error getting FileProvider URI. Check authority?", ex);
             Toast.makeText(this, "Lỗi cấu hình FileProvider", Toast.LENGTH_SHORT).show();
             contentUri = null;
         }
         return contentUri;
     }
     */
    // Đừng quên thêm FileProvider vào AndroidManifest.xml và tạo file res/xml/file_paths.xml
    // nếu bạn sử dụng phương thức createImageFileUri() ở trên.
    // Manifest (<application>...):
     /*
        <provider
            android:name="androidx.core.content.FileProvider"
            android:authorities="${applicationId}.provider" // Thay thế bằng authority của bạn
            android:exported="false"
            android:grantUriPermissions="true">
            <meta-data
                android:name="android.support.FILE_PROVIDER_PATHS"
                android:resource="@xml/file_paths" /> // Trỏ đến file paths
        </provider>
     */
    // res/xml/file_paths.xml:
     /*
     <?xml version="1.0" encoding="utf-8"?>
     <paths>
         <external-files-path name="my_images" path="Pictures" />
         <!-- Hoặc <cache-path name="my_cached_images" path="images/" /> -->
     </paths>
     */

}