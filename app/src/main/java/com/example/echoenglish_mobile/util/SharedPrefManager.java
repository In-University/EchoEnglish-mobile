package com.example.echoenglish_mobile.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.echoenglish_mobile.model.User;
import com.google.gson.Gson;

public class SharedPrefManager {
    private static final String PREF_NAME = "echo_prefs";
    private static final String KEY_AUTH_TOKEN = "AUTH_TOKEN";
    private static final String KEY_USER_INFO = "USER_INFO";
    private static SharedPrefManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson = new Gson();

    private SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SharedPrefManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPrefManager(context.getApplicationContext());
        }
        return instance;
    }

    public void saveAuthToken(String token) {
        sharedPreferences.edit().putString(KEY_AUTH_TOKEN, token).apply();
    }

    public String getAuthToken() {
        return sharedPreferences.getString(KEY_AUTH_TOKEN, null);
    }

    public void saveUserInfo(User user) {
        String userJson = gson.toJson(user);
        sharedPreferences.edit().putString(KEY_USER_INFO, userJson).apply();
    }

    public User getUserInfo() {
        String userJson = sharedPreferences.getString(KEY_USER_INFO, null);
        if (userJson != null) {
            return gson.fromJson(userJson, User.class);
        }
        return null;
    }


    public void clear() {
        sharedPreferences.edit().clear().apply();
    }
}
