package com.example.echoenglish_mobile.network;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://mobile.mkhoavo.space";
//    private static final String BASE_URL = "http://196.169.3.107:8080";
    private static Retrofit retrofit = null;
    
    public static ApiService getApiService() {
        if (retrofit == null) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                    .addInterceptor(loggingInterceptor) // Giữ lại logging
                    .connectTimeout(30, TimeUnit.SECONDS) // Tăng thời gian kết nối (ví dụ 30s)
                    .readTimeout(60, TimeUnit.SECONDS)    // **QUAN TRỌNG:** Tăng thời gian chờ đọc (ví dụ 60s)
                    .writeTimeout(60, TimeUnit.SECONDS)   // Tăng thời gian chờ ghi (ví dụ 60s)
                    .build();
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(ApiService.class);
    }
 
}
