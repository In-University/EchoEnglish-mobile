package com.example.echoenglish_mobile.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    private static final String BASE_URL = "https://mobile.mkhoavo.space";
//    private static final String BASE_URL = "http://192.168.1.102:8080";
    private static Retrofit retrofit = null;


    // ** THÊM TYPE ADAPTER CHO LOCALDATETIME ĐỂ GSON HIỂU ĐỊNH DẠNG NGÀY GIỜ TỪ BACKEND **
    private static class LocalDateTimeAdapter implements JsonDeserializer<LocalDateTime>, JsonSerializer<LocalDateTime> {

        // Định dạng DateTimeFormatter phải khớp chính xác với định dạng JSON từ backend.
        // Định dạng "2025-04-27T10:10:47.693597" là chuẩn ISO 8601.
        // java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME xử lý được định dạng này.
        private final DateTimeFormatter formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME;
        // Nếu backend trả về định dạng khác (ví dụ: "yyyy-MM-dd HH:mm:ss"), bạn cần dùng:
        // private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        @Override
        public LocalDateTime deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
            try {
                // Chuyển đổi chuỗi JSON thành LocalDateTime.
                return LocalDateTime.parse(json.getAsString(), formatter);
            } catch (Exception e) {
                throw new JsonParseException("Không thể phân tích chuỗi ngày giờ: " + json.getAsString(), e);
            }
        }

        @Override
        public JsonElement serialize(LocalDateTime src, Type typeOfSrc, JsonSerializationContext context) {
            // Chuyển đổi LocalDateTime thành chuỗi JSON
            return new JsonPrimitive(formatter.format(src));
        }
    }


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

            // ** TẠO GSON INSTANCE TÙY CHỈNH VỚI LOCALDATETIME ADAPTER **
            Gson customGson = new GsonBuilder()
                    .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                    // .setPrettyPrinting() // Nếu bạn muốn debug quá trình parsing của Gson, thêm dòng này:
                    .create();

            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create(customGson))
                    .build();
        }
        return retrofit.create(ApiService.class);
    }

}
