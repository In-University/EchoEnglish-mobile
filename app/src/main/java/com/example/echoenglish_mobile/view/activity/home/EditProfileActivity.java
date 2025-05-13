package com.example.echoenglish_mobile.view.activity.home;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.network.ApiClient;
import com.example.echoenglish_mobile.network.ApiService;
import com.example.echoenglish_mobile.util.MyApp;
import com.example.echoenglish_mobile.util.SharedPrefManager;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import de.hdodenhof.circleimageview.CircleImageView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ImageView ivBack;
    private TextView tvHeaderTitle;
    private CircleImageView ivEditProfileAvatar;
    private TextInputLayout tilName, tilEmail, tilAvatarUrl;
    private TextInputEditText etName, etEmail, etAvatarUrl;
    private Button btnSaveProfile;
    private FrameLayout progressOverlay;

    private User currentUser;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        ivBack = findViewById(R.id.ivBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Edit Profile");

        ivBack.setOnClickListener(v -> onBackPressed());

        ivEditProfileAvatar = findViewById(R.id.ivEditProfileAvatar);
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilAvatarUrl = findViewById(R.id.tilAvatarUrl);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressOverlay = findViewById(R.id.progressOverlay);

        currentUser = SharedPrefManager.getInstance(this).getUserInfo();

        if (currentUser == null) {
            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        populateFields();

        apiService = ApiClient.getApiService();

        btnSaveProfile.setOnClickListener(v -> attemptSaveProfile());
    }

    private void populateFields() {
        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail());
            etAvatarUrl.setText(currentUser.getAvatar());

            if (!TextUtils.isEmpty(currentUser.getAvatar())) {
                Glide.with(this)
                        .load(currentUser.getAvatar())
                        .placeholder(R.drawable.image_profile)
                        .error(R.drawable.image_profile)
                        .into(ivEditProfileAvatar);
            } else {
                ivEditProfileAvatar.setImageResource(R.drawable.image_profile);
            }
        }
    }

    private void attemptSaveProfile() {
        tilName.setError(null);
        tilAvatarUrl.setError(null);

        String newName = etName.getText().toString().trim();
        String newAvatarUrl = etAvatarUrl.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        if (TextUtils.isEmpty(newName)) {
            tilName.setError("Name is required");
            focusView = etName;
            cancel = true;
        }

        if (!TextUtils.isEmpty(newAvatarUrl) && !Patterns.WEB_URL.matcher(newAvatarUrl).matches()) {
        }


        if (cancel) {
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            saveProfile(newName, newAvatarUrl);
        }
    }

    private void saveProfile(String name, String avatarUrl) {
        if (currentUser == null || currentUser.getId() == null) {
            Toast.makeText(this, "Cannot save: User data is missing or invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true);

        User updateUserBody = new User();
        updateUserBody.setName(name);

        if (currentUser.getEmail() != null) {
            updateUserBody.setEmail(currentUser.getEmail());
        }

        updateUserBody.setAvatar(avatarUrl.isEmpty() ? null : avatarUrl);

        Call<User> call = apiService.updateUser(currentUser.getId(), updateUserBody);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                showProgress(false);
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    SharedPrefManager.getInstance(MyApp.getAppContext()).saveUserInfo(updatedUser);
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditProfileActivity.this, HomeActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);

                } else {
                    String errorMsg = "Failed to update profile.";
                    try {
                        if (response.errorBody() != null) {
                            errorMsg = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(EditProfileActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                showProgress(false);
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!show);
        tilName.setEnabled(!show);
        tilAvatarUrl.setEnabled(!show);
        ivBack.setEnabled(!show);
    }
}