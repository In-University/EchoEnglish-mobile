package com.example.echoenglish_mobile.ui.chatbot;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface ChatbotApi {
    @POST("chatbot/sendMessage")
    @Headers("Content-Type: application/json")
    Call<ApiResponse> sendMessage(@Body String message);
}
