package com.example.echoenglish_mobile.ui.chatbot;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.echoenglish_mobile.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_USER = 0;
    private static final int VIEW_TYPE_CHATBOT = 1;

    private List<Message> messages;

    public ChatAdapter(List<Message> messages) {
        this.messages = messages;
    }

    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isUserMessage() ? VIEW_TYPE_USER : VIEW_TYPE_CHATBOT;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_USER) {
            View view = inflater.inflate(R.layout.item_user_message, parent, false);
            return new UserMessageViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_chatbot_message, parent, false);
            return new ChatbotMessageViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Message message = messages.get(position);
        if (holder instanceof UserMessageViewHolder) {
            ((UserMessageViewHolder) holder).bind(message.getText());
        } else {
            ((ChatbotMessageViewHolder) holder).bind(message.getText().replaceAll("\\n+$", "")); // Xóa "\n" ở cuối message
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public void addMessage(Message message) {
        messages.add(message);
        notifyItemInserted(messages.size() - 1);
    }

    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        UserMessageViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.user_message_text);  // user_message_text
        }

        void bind(String text) {
            textView.setText(text);
        }
    }


    static class ChatbotMessageViewHolder extends RecyclerView.ViewHolder {
        private TextView textView;

        ChatbotMessageViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.chatbot_message_text);  // chatbot_message_text
        }

        void bind(String text) {
            textView.setText(text);
        }
    }
}
