package com.example.echoenglish_mobile.data.remote;

import com.example.echoenglish_mobile.data.model.PhonemeComparisonDTO;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ApiService {
//    @Multipart
//    @POST("uploadAudio")
//    Call<ResponseBody> uploadAudio(@Part MultipartBody.Part audio);

    @GET("/speech/analyze/word")
    Call<List<PhonemeComparisonDTO>> uploadAudio();
}
