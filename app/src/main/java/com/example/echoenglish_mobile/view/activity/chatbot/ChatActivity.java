package com.example.echoenglish_mobile.view.activity.chatbot;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;
import com.example.echoenglish_mobile.model.ApiResponse;
import com.example.echoenglish_mobile.adapter.ChatAdapter;
import com.example.echoenglish_mobile.model.Message;
import com.example.echoenglish_mobile.network.ApiClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Call;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter chatAdapter;
    private List<Message> messages = new ArrayList<>();
    private EditText inputMessage;
    private Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);
        Intent intent = new Intent(this, ConversationActivity.class);
        String theContext = "Discuss favorite travel destinations and why you like them.";
        intent.putExtra(ConversationActivity.EXTRA_CONTEXT, theContext);
        startActivity(intent);
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(messages);
        recyclerView.setAdapter(chatAdapter);

        inputMessage = findViewById(R.id.input_message);
        sendButton = findViewById(R.id.send_button);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v){
                String userMessage = inputMessage.getText().toString().trim();
                if (userMessage.isEmpty()) return;
                Log.d("ChatActivity", "Nút Gửi đã được nhấn");

                // Gửi tin nhắn của người dùng vào RecyclerView
                Message message = new Message(userMessage, true);
                chatAdapter.addMessage(message);
                inputMessage.setText("");

                // Gọi API Gemini để lấy phản hồi
                sendToChatbotApi(userMessage);
            }
        });
    }

//    private void sendMessage(View v) {
//        String userMessage = inputMessage.getText().toString().trim();
//        if (userMessage.isEmpty()) return;
//
//        // Gửi tin nhắn của người dùng vào RecyclerView
//        Message message = new Message(userMessage, true);
//        chatAdapter.addMessage(message);
//        inputMessage.setText("");
//
//        // Gọi API Gemini để lấy phản hồi
//        sendToChatbotApi(userMessage);
//    }

    private void sendToChatbotApi(String message) {
        // Gọi API Gemini (sử dụng Retrofit) và nhận phản hồi từ chatbot
        Call<ApiResponse> call = ApiClient.getApiService().sendMessage(message);

        Log.d("ChatActivity", "Gọi API với message: " + message);
        Log.d("ChatActivity", "Call: " + call.request());

        call.enqueue(new Callback<ApiResponse>() {
            @Override
            public void onResponse(Call<ApiResponse> call, Response<ApiResponse> response) {
                Log.d("ChatActivity", "API response nhận được");
                if (response.isSuccessful() && response.body() != null) {
                    String botResponse = response.body().getText();
                    Log.d("ChatActivity", "Chatbot trả lời: " + botResponse);

                    Message message = new Message(botResponse, false);
                    chatAdapter.addMessage(message);
                    recyclerView.smoothScrollToPosition(chatAdapter.getItemCount() - 1);
                } else {
                    Log.e("ChatActivity", "Response error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ApiResponse> call, Throwable t) {
                Log.e("ChatActivity", "Error: " + t.getMessage());
            }
        });
    }
}
