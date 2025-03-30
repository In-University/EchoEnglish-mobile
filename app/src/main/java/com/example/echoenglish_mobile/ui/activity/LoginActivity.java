package com.example.echoenglish_mobile.ui.activity;

import android.content.Intent;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.data.model.User;
import com.example.echoenglish_mobile.databinding.ActivityLoginBinding;
import com.example.echoenglish_mobile.ui.viewmodel.LoginViewModel;

import java.util.List;

public class LoginActivity extends AppCompatActivity {
    private LoginViewModel viewModel;

    private Button btnLogin;
    private Button btnSignup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        btnLogin = findViewById(R.id.btnLogin);
        btnSignup = findViewById(R.id.btnSignup);

        btnLogin.setOnClickListener(v -> {
            Toast.makeText(LoginActivity.this,"Log in success!",Toast.LENGTH_SHORT).show();
        });

        btnSignup.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SignupActivity.class);
            startActivity(intent);
        });


//        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
//
//        viewModel = new LoginViewModel();
//        binding.setViewModel(viewModel); // Gán ViewModel vào layout để EditText và Button có thể truy cập.
//        binding.setLifecycleOwner(this); // Giúp LiveData tự động cập nhật UI khi dữ liệu thay đổi.
//
//        viewModel.isLoggedIn.observe(this, new Observer<Boolean>() {
//            @Override
//            public void onChanged(Boolean isLoggedIn) {
//                if (isLoggedIn) {
//                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
//                    // Chuyển sang màn hình khác khi cần
//                }
//            }
//        });
    }
}
