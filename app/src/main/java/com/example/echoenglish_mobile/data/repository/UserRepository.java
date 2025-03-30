package com.example.echoenglish_mobile.data.repository;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.echoenglish_mobile.data.model.User;
import com.example.echoenglish_mobile.data.network.ApiService;
import com.example.echoenglish_mobile.data.network.RetrofitClient;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private ApiService apiService;

    public UserRepository() {
        apiService = RetrofitClient.getRetrofit().create(ApiService.class);
    }

    public LiveData<List<User>> getUsers() {
        MutableLiveData<List<User>> data = new MutableLiveData<>();

        apiService.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    data.setValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                data.setValue(null);
            }
        });

        return data;
    }
}