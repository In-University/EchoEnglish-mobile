package com.example.echoenglish_mobile.ui.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData; // Một dạng LiveData có thể thay đổi giá trị và thông báo cho Observer khi dữ liệu thay đổi.
import androidx.lifecycle.ViewModel;

import com.example.echoenglish_mobile.data.model.User;
import com.example.echoenglish_mobile.data.repository.UserRepository;

import java.util.List;

public class LoginViewModel extends ViewModel {
    public MutableLiveData<String> email = new MutableLiveData<>();
    public MutableLiveData<String> password = new MutableLiveData<>();
    public MutableLiveData<String> errorMessage = new MutableLiveData<>();
    public MutableLiveData<Boolean> isLoggedIn = new MutableLiveData<>(false);

    private static final String FIXED_EMAIL = "vantri@gmail.com";
    private static final String FIXED_PASSWORD = "123";

    public void onLoginClick() {
        if (email.getValue() != null && password.getValue() != null) {
            if (email.getValue().equals(FIXED_EMAIL) && password.getValue().equals(FIXED_PASSWORD)) {
                isLoggedIn.setValue(true);
                errorMessage.setValue(null);
            } else {
                errorMessage.setValue("Sai tài khoản hoặc mật khẩu");
                isLoggedIn.setValue(false);
            }
        } else {
            errorMessage.setValue("Vui lòng nhập email và mật khẩu");
        }
    }

//    private UserRepository userRepository;
//    private LiveData<List<User>> users;
//
//    public UserViewModel() {
//        userRepository = new UserRepository();
//        users = userRepository.getUsers();
//    }
//
//    public LiveData<List<User>> getUsers() {
//        return users;
//    }
}