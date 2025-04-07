package com.example.echoenglish_mobile.ui.chatbot;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://mobile.mkhoavo.space";  // Gõ ipconfig để lấy IP máy chủ của bạn
    private static RetrofitClient instance;
    private Retrofit retrofit;
    private ChatbotApi api;

    private RetrofitClient() {
        retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ChatbotApi.class);
    }

    public static synchronized RetrofitClient getInstance() {
        if (instance == null) {
            instance = new RetrofitClient();
        }
        return instance;
    }

    public ChatbotApi getApi() {
        return api;
    }
}