package com.example.echoenglish_mobile.ui.activity;

import android.os.Bundle;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);

        viewModel = new LoginViewModel();
        binding.setViewModel(viewModel); // Gán ViewModel vào layout để EditText và Button có thể truy cập.
        binding.setLifecycleOwner(this); // Giúp LiveData tự động cập nhật UI khi dữ liệu thay đổi.

        viewModel.isLoggedIn.observe(this, new Observer<Boolean>() {
            @Override
            public void onChanged(Boolean isLoggedIn) {
                if (isLoggedIn) {
                    Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                    // Chuyển sang màn hình khác khi cần
                }
            }
        });
    }

//    private UserViewModel userViewModel;
//    private TextView textViewUsers;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
//
//        textViewUsers = findViewById(R.id.textViewUsers);
//        userViewModel = new ViewModelProvider(this).get(UserViewModel.class);
//
//        // Quan sát LiveData để cập nhật UI khi dữ liệu thay đổi
//        userViewModel.getUsers().observe(this, new Observer<List<User>>() {
//            @Override
//            public void onChanged(List<User> users) {
//                if (users != null) {
//                    StringBuilder builder = new StringBuilder();
//                    for (User user : users) {
//                        builder.append(user.getName()).append("\n");
//                    }
//                    textViewUsers.setText(builder.toString());
//                } else {
//                    textViewUsers.setText("Lỗi khi tải dữ liệu!");
//                }
//            }
//        });
//    }
}
