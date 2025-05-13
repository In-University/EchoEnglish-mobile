package com.example.echoenglish_mobile.network;

import com.example.echoenglish_mobile.model.ApiResponse;
import com.example.echoenglish_mobile.model.LoginRequest;
import com.example.echoenglish_mobile.model.PageResponse;
import com.example.echoenglish_mobile.model.PhonemeComparison;
import com.example.echoenglish_mobile.model.ResetPasswordRequest;
import com.example.echoenglish_mobile.model.SentenceAnalysisResult;
import com.example.echoenglish_mobile.model.User;
import com.example.echoenglish_mobile.model.Word;
import com.example.echoenglish_mobile.model.request.ConverseRequest;
import com.example.echoenglish_mobile.model.request.StartConversationRequest;
import com.example.echoenglish_mobile.model.request.WritingAnalysisRequest;
import com.example.echoenglish_mobile.model.response.ConversationResponse;
import com.example.echoenglish_mobile.model.response.LoginResponse;
import com.example.echoenglish_mobile.view.activity.document_hub.dto.NewsItem;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.FlashcardUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.LearningRecordRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyCreateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.request.VocabularyUpdateRequest;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.CategoryResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.DueReviewCountResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardBasicResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.FlashcardDetailResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.LearningProgressResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.MemoryLevelsResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.dto.response.VocabularyReviewResponse;
import com.example.echoenglish_mobile.view.activity.flashcard.model.PexelsResponse;
import com.example.echoenglish_mobile.view.activity.grammar.model.Grammar;
import com.example.echoenglish_mobile.view.activity.quiz.StartTestRequest;
import com.example.echoenglish_mobile.view.activity.quiz.StartTestResponse;
import com.example.echoenglish_mobile.view.activity.quiz.SubmitAnswerRequest;
import com.example.echoenglish_mobile.view.activity.quiz.model.Test;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestHistory;
import com.example.echoenglish_mobile.view.activity.quiz.model.TestPart;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateRequest;
import com.example.echoenglish_mobile.view.activity.translate_text.TranslateResponse;
import com.example.echoenglish_mobile.view.activity.video_youtube.dto.TranscriptContent;

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
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface ApiService {
    @POST("/register")
    Call<ResponseBody> registerUser(@Body User user); // API trả về ResponseEntity<String> nên dùng ResponseBody

    @POST("/validate-otp-register")
    Call<ResponseBody> validateOtpRegister(@Body Map<String, String> requestBody);

    @POST("/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

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
    Call<String> analyzeSentences(
            @Part MultipartBody.Part audioFile,
            @Part("target_word") RequestBody targetWord
    );

    @GET("words/{word}")
    Call<Word> getWordDetails(@Path("word") String word);

    @GET("words/search/{prefix}")
    Call<List<Word>> getWordSuggestions(@Path("prefix") String prefix);

    @POST("/writing/analyze")
    Call<ResponseBody> analyzeWriting(@Body WritingAnalysisRequest request);

  	@POST("/chatbot/start")
    Call<ConversationResponse> startChat(@Body StartConversationRequest request);

    @POST("/chatbot/converse")
    Call<ConversationResponse> continueChat(@Body ConverseRequest request);

    @POST("/chatbot/review")
    Call<ConversationResponse> reviewConversation(@Body ConverseRequest request);

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

    @GET("categories/public") // Lưu ý: Bỏ "api/" theo yêu cầu của bạn
    Call<List<CategoryResponse>> getPublicCategories();

    @GET("api/flashcards/category/{categoryId}")
    Call<List<FlashcardBasicResponse>> getPublicFlashcardsByCategory(@Path("categoryId") Long categoryId);

    @PUT("api/flashcards/{id}") // Thường dùng PUT cho update toàn bộ hoặc PATCH cho update một phần
    Call<FlashcardDetailResponse> updateFlashcard(
            @Path("id") Long flashcardId,
            @Body FlashcardUpdateRequest request // Dùng DTO Update
    );

    @GET("api/learnings/progress/user/{userId}/flashcard/{flashcardId}")
    Call<LearningProgressResponse> getLearningProgress(
            @Path("userId") Long userId,
            @Path("flashcardId") Long flashcardId
    );

    @GET("pixels/search")
    Call<PexelsResponse> searchImagesViaBackend(
            @Query("query") String query,
            @Query("perPage") int perPage, // Có thể bỏ nếu backend dùng giá trị mặc định
            @Query("page") int page,       // Có thể bỏ
            @Query("orientation") String orientation // Có thể bỏ
    );

    @GET("api/flashcards/creator/{creatorId}")
    Call<List<FlashcardBasicResponse>> getFlashcardsByCreator(@Path("creatorId") Long creatorId);

    @PUT("vocabularies/{id}") // Endpoint backend mới
    Call<VocabularyResponse> updateVocabulary(
            @Path("id") Long vocabularyId,
            @Body VocabularyUpdateRequest request
    );

    @POST("chatbot/sendMessage")
    Call<TranslateResponse> translateText(@Body TranslateRequest request);

    // ---------------------------------------------------
    @GET("tests") // Đường dẫn API lấy danh sách Test
    Call<List<Test>> getAllTests();

    @GET("tests/{testId}/part-number/{partNumber}") // Đảm bảo URL khớp với Controller backend
    Call<TestPart> getDetailedTestPartByNumber(
            @Path("testId") int testId,
            @Path("partNumber") int partNumber);

    @GET("grammars")
    Call<List<Grammar>> getGrammars();

    @POST("api/learnings")
    Call<Void> recordLearning(@Body LearningRecordRequest recordRequest);

    @GET("api/learnings/review/user/{userId}")
    Call<List<VocabularyReviewResponse>> getDueVocabulariesForReview(@Path("userId") Long userId);


    @GET("api/learnings/memory-levels/user/{userId}")
    Call<MemoryLevelsResponse> getMemoryLevels(
            @Path("userId") Long userId
            // ** BỎ @Query("flashcardId") **
    );

    // getDueReviewCount (đã đúng)
    @GET("api/learnings/review/user/{userId}/count")
    Call<DueReviewCountResponse> getDueReviewCount(
            @Path("userId") Long userId
    );

    @GET("/speech/result/my")
    Call<List<SentenceAnalysisResult>> getSpeechAnalyzeResultList();

    @GET("/writing/result/my")
    Call<ResponseBody> getWritingAnalyzeResultList();

    @GET("/document/news")
    Call<PageResponse<NewsItem>> getNews(
            @Query("page") int page,
            @Query("size") int size
    );

    @GET("/document/youtube/{videoId}")
    Call<TranscriptContent> getYoutubeTranscript(@Path("videoId") String videoId);

    @PUT("/users/{id}")
    Call<User> updateUser(@Path("id") Long userId, @Body User updatedUser);
}