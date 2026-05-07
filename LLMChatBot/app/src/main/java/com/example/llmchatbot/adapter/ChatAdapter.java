package com.example.llmchatbot.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmchatbot.R;
import com.example.llmchatbot.database.ChatMessage;
import com.example.llmchatbot.utils.DateTimeUtils;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    private final List<ChatMessage> messages;

    public ChatAdapter(List<ChatMessage> messages) {
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_chat_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ChatMessage msg = messages.get(position);
        String timestamp = DateTimeUtils.formatTime(msg.getTimestamp());

        if (msg.isUser()) {
            holder.llBot.setVisibility(View.GONE);
            holder.llUser.setVisibility(View.VISIBLE);
            holder.tvUserMessage.setText(msg.getMessage());
            holder.tvUserTimestamp.setText(timestamp);
        } else {
            holder.llUser.setVisibility(View.GONE);
            holder.llBot.setVisibility(View.VISIBLE);
            holder.tvBotMessage.setText(msg.getMessage());
            holder.tvBotTimestamp.setText(timestamp);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        LinearLayout llUser, llBot;
        TextView tvUserMessage, tvUserTimestamp;
        TextView tvBotMessage, tvBotTimestamp;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            llUser = itemView.findViewById(R.id.llUser);
            llBot = itemView.findViewById(R.id.llBot);
            tvUserMessage = itemView.findViewById(R.id.tvUserMessage);
            tvUserTimestamp = itemView.findViewById(R.id.tvUserTimestamp);
            tvBotMessage = itemView.findViewById(R.id.tvBotMessage);
            tvBotTimestamp = itemView.findViewById(R.id.tvBotTimestamp);
        }
    }
}
