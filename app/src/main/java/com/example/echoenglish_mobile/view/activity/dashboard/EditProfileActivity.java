package com.example.echoenglish_mobile.view.activity.dashboard;

import androidx.appcompat.app.AppCompatActivity;
// import androidx.appcompat.widget.Toolbar; // No longer needed

import android.content.Intent; // Import Intent
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
// import android.view.MenuItem; // No longer needed for Toolbar menu
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
// import android.widget.ProgressBar; // No longer needed
import android.widget.FrameLayout; // Added for overlay
import android.widget.ImageView; // Added for custom back button
import android.widget.TextView; // Added for custom title
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

    private ImageView ivBack; // Custom back button
    private TextView tvHeaderTitle; // Custom header title
    private CircleImageView ivEditProfileAvatar;
    private TextInputLayout tilName, tilEmail, tilAvatarUrl;
    private TextInputEditText etName, etEmail, etAvatarUrl;
    private Button btnSaveProfile;
    private FrameLayout progressOverlay; // Use FrameLayout for progress overlay

    private User currentUser;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        // Initialize Custom Header Views
        ivBack = findViewById(R.id.ivBack);
        tvHeaderTitle = findViewById(R.id.tvHeaderTitle);
        tvHeaderTitle.setText("Edit Profile"); // Set title text

        // Set click listener for custom back button
        ivBack.setOnClickListener(v -> onBackPressed());

        // Initialize other Views
        ivEditProfileAvatar = findViewById(R.id.ivEditProfileAvatar);
        tilName = findViewById(R.id.tilName);
        etName = findViewById(R.id.etName);
        tilEmail = findViewById(R.id.tilEmail);
        etEmail = findViewById(R.id.etEmail);
        tilAvatarUrl = findViewById(R.id.tilAvatarUrl);
        etAvatarUrl = findViewById(R.id.etAvatarUrl);
        btnSaveProfile = findViewById(R.id.btnSaveProfile);
        progressOverlay = findViewById(R.id.progressOverlay); // Initialize overlay

        // Get current user data from SharedPrefManager
        currentUser = SharedPrefManager.getInstance(this).getUserInfo();

        if (currentUser == null) {
            Toast.makeText(this, "User data not found.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Populate fields with current user data
        populateFields();

        // Initialize API Service
        apiService = ApiClient.getApiService();

        // Set Save Button Click Listener
        btnSaveProfile.setOnClickListener(v -> attemptSaveProfile());

        // Optional: Add listener to avatar image if you want to allow picking images
        // ivEditProfileAvatar.setOnClickListener(...) // Implement image picking logic here
    }

    private void populateFields() {
        if (currentUser != null) {
            etName.setText(currentUser.getName());
            etEmail.setText(currentUser.getEmail()); // Email might be disabled in XML
            etAvatarUrl.setText(currentUser.getAvatar());

            // Load avatar image using Glide
            if (!TextUtils.isEmpty(currentUser.getAvatar())) {
                Glide.with(this)
                        .load(currentUser.getAvatar())
                        .placeholder(R.drawable.image_profile) // Default placeholder
                        .error(R.drawable.image_profile) // Error placeholder
                        .into(ivEditProfileAvatar);
            } else {
                ivEditProfileAvatar.setImageResource(R.drawable.image_profile); // Set default if avatar URL is empty
            }
        }
    }

    private void attemptSaveProfile() {
        // Reset errors
        tilName.setError(null);
        tilAvatarUrl.setError(null);

        // Get new values
        String newName = etName.getText().toString().trim();
        String newAvatarUrl = etAvatarUrl.getText().toString().trim();

        boolean cancel = false;
        View focusView = null;

        // Validate Name
        if (TextUtils.isEmpty(newName)) {
            tilName.setError("Name is required");
            focusView = etName;
            cancel = true;
        }

        // Validate Avatar URL (basic check, could be more robust)
        if (!TextUtils.isEmpty(newAvatarUrl) && !Patterns.WEB_URL.matcher(newAvatarUrl).matches()) {
            // Basic URL pattern check. More sophisticated validation might be needed.
            // Note: An empty avatar URL might be allowed by the server to revert to default.
            // Adjust validation based on your API's requirements.
            // tilAvatarUrl.setError("Invalid URL format"); // Uncomment if strict URL validation is needed
            // focusView = etAvatarUrl; // Uncomment if strict URL validation is needed
            // cancel = true; // Uncomment if strict URL validation is needed
        }


        if (cancel) {
            // There was an error; don't attempt save and focus the first field with error.
            if (focusView != null) {
                focusView.requestFocus();
            }
        } else {
            // Proceed with save
            saveProfile(newName, newAvatarUrl);
        }
    }

    private void saveProfile(String name, String avatarUrl) {
        if (currentUser == null || currentUser.getId() == null) {
            Toast.makeText(this, "Cannot save: User data is missing or invalid.", Toast.LENGTH_SHORT).show();
            return;
        }

        showProgress(true); // Show overlay progress

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
                showProgress(false); // Hide overlay progress
                if (response.isSuccessful() && response.body() != null) {
                    User updatedUser = response.body();
                    SharedPrefManager.getInstance(MyApp.getAppContext()).saveUserInfo(updatedUser);
                    Toast.makeText(EditProfileActivity.this, "Profile updated successfully!", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(EditProfileActivity.this, DashboardActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa toàn bộ stack cũ
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
                showProgress(false); // Hide overlay progress
                Toast.makeText(EditProfileActivity.this, "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Updated showProgress to handle FrameLayout overlay
    private void showProgress(boolean show) {
        progressOverlay.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSaveProfile.setEnabled(!show); // Disable button while saving
        // You might also want to disable input fields while loading
        tilName.setEnabled(!show);
        // tilEmail remains disabled
        tilAvatarUrl.setEnabled(!show);
        ivBack.setEnabled(!show); // Disable back button too
    }

    // Removed onOptionsItemSelected as Toolbar is gone
    // @Override
    // public boolean onOptionsItemSelected(MenuItem item) {
    //     if (item.getItemId() == android.R.id.home) {
    //         onBackPressed();
    //         return true;
    //     }
    //     return super.onOptionsItemSelected(item);
    // }
}