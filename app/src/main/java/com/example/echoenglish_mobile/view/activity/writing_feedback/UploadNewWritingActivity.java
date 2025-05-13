package com.example.echoenglish_mobile.view.activity.writing_feedback;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.request.WritingAnalysisRequest;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.view.activity.webview.WebViewFragment;
import com.example.echoenglish_mobile.view.dialog.LoadingDialogFragment;

import java.io.IOException;
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
    private TextView btnSubmit;

    private static final String LOADING_DIALOG_TAG = "PronunciationLoading";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_writing);

        findViews();
        setupClickListeners();
    }

    private void findViews() {
        btnClose = findViewById(R.id.btnClose);
        editTextTopic = findViewById(R.id.editTextTopic);
        editTextContent = findViewById(R.id.editTextContent);
        editTextContent.setText("Social media is very popular today especialy among young persons. It have changed the way we communicate signifcantly. One main advantage are connecting with friends and family who live far away. People shares photos updates and keep in touch easily. Also businesses can use platforms like facebook or instagram for reach customers and promote there products cheap. However there is also negative sides. Too much time spent on social media might leads to addiction and affect real life relationships. Comparing yourself to others online perfect lifes can cause feelings of inadequacy or depression." +
                "" +
                "Another problem are the spread of fake news and misinformation which is difficult controlling. Privacy concern is also a big issue because personal datas can be misused. In conclude social media has both good points and bad points. Using it moderation and being aware of the risks seem the best approach. We must to learn how use these tools responsible for maximize benefits and minimize harmfull effects.");
        btnSubmit = findViewById(R.id.btnSubmit);
    }

    private void setupClickListeners() {
        btnClose.setOnClickListener(v -> finish());
        btnSubmit.setOnClickListener(v -> handleSubmit());
    }

    private void handleSubmit() {
        String topic = editTextTopic.getText().toString().trim();
        String content = editTextContent.getText().toString().trim();

        if (content.isEmpty()) {
            Toast.makeText(this, "Please enter the post content.", Toast.LENGTH_SHORT).show();
            editTextContent.requestFocus();
            return;
        }
        callWritingAnalysisApi(content, topic);
        LoadingDialogFragment.showLoading(getSupportFragmentManager(), "ConversationLoading", "...");
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
                                jsCode = jsonResponse;
                                processingSuccess = true;
                                LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
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
                            LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
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
                    LoadingDialogFragment.hideLoading(getSupportFragmentManager(), "ConversationLoading");
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

                transaction.replace(android.R.id.content, fragment);
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
        }
    }

    private void showError(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Log.e(TAG, "Error Displayed: " + message);
    }
}