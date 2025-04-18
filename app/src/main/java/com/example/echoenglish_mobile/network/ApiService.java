package com.example.echoenglish_mobile.network;

import com.example.echoenglish_mobile.model.ApiResponse;
import com.example.echoenglish_mobile.model.LoginRequest;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.model.ResetPasswordRequest;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.model.request.WritingAnalysisRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningHistoryResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ApiService {
    @POST("/register")
    Call<ResponseBody> registerUser(@Body User user); // API trả về ResponseEntity<String> nên dùng ResponseBody

    @POST("/validate-otp-register")
    Call<ResponseBody> validateOtpRegister(@Body Map<String, String> requestBody);

    // Returns 200 OK with JSON {"token": "..."} in body on success
    // Returns 401 Unauthorized with JSON {"message": "..."} on failure
    @POST("/auth/login")
    Call<Map<String, String>> loginUser(@Body LoginRequest loginRequest);

    @POST("/forgot-password")
    Call<ResponseBody> forgotPassword(@Body Map<String, String> requestBody);

    @POST("/reset-password")
    Call<ResponseBody> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

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

    @GET("words/search/{prefix}")
    Call<List<Word>> getWordSuggestions(@Path("prefix") String prefix);

    @POST("/writing/analyze")
    Call<ResponseBody> analyzeWriting(@Body WritingAnalysisRequest request);

    // --- Flashcards ---
    @POST("api/flashcards/user-defined")
    Call<FlashcardDetailResponse> createFlashcard(@Body FlashcardCreateRequest request);

    @GET("api/flashcards/user-defined")
    Call<List<FlashcardBasicResponse>> getUserDefinedFlashcards();

    @GET("api/flashcards/public")
    Call<List<FlashcardBasicResponse>> getPublicFlashcards();

    @GET("api/flashcards/{flashcardId}")
    Call<FlashcardDetailResponse> getFlashcardDetails(@Path("flashcardId") Long flashcardId);

    @DELETE("api/flashcards/{flashcardId}")
    Call<Void> deleteFlashcard(@Path("flashcardId") Long flashcardId);

    // --- Vocabularies ---
    @POST("api/flashcards/{flashcardId}/vocabularies")
    Call<VocabularyResponse> addVocabulary(
            @Path("flashcardId") Long flashcardId,
            @Body VocabularyCreateRequest request
    );

    @GET("api/flashcards/{flashcardId}/vocabularies")
    Call<List<VocabularyResponse>> getVocabularies(@Path("flashcardId") Long flashcardId);

    @DELETE("api/flashcards/vocabularies/{vocabularyId}")
    Call<Void> deleteVocabulary(@Path("vocabularyId") Long vocabularyId);

    // --- Learning History ---
    @POST("api/learnings")
    Call<Void> recordLearning(@Body LearningRecordRequest request);

    @GET("api/learnings/history/user/{userId}")
    Call<List<LearningHistoryResponse>> getLearningHistory(@Path("userId") Long userId);
}