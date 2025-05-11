package com.example.echoenglish_mobile.model.response;

import com.example.echoenglish_mobile.model.User;

public class LoginResponse {
    private String token;
    private User user;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
