package com.example.echoenglish_mobile.network;

import com.example.echoenglish_mobile.model.ApiResponse;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.Word;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @POST("chatbot/sendMessage")
    @Headers("Content-Type: application/json")
    Call<ApiResponse> sendMessage(@Body String message);

    @Multipart
    @POST("/speech/analyze/word")
    Call<List<PhonemeComparison>> analyzeSpeech(
            @Part MultipartBody.Part audioFile,
            @Part("target_word") RequestBody targetWord
    );

    @Multipart
    @POST("/speech/analyze/sentences")
    Call<SentenceAnalysisResult> analyzeSentences(
            @Part MultipartBody.Part audioFile,
            @Part("target_word") RequestBody targetWord
    );

    @GET("words/{word}")
    Call<Word> getWordDetails(@Path("word") String word);
}