package com.example.echoenglish_mobile.data.remote;

import com.example.echoenglish_mobile.data.model.PhonemeComparison;
import com.example.echoenglish_mobile.data.model.SentenceAnalysisResult;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiService {
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
}
