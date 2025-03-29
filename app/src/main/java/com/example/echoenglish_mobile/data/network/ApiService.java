package com.example.echoenglish_mobile.data.network;

import com.example.echoenglish_mobile.data.model.User;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
    @GET("users") // API giả định
    Call<List<User>> getUsers();
}