package com.example.echoenglish_mobile.ui.dictionary;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {
    @GET("words/{word}")
    Call<Word> getWordDetails(@Path("word") String word);
}