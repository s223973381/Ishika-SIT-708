package com.example.voyage.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.database.entities.AiChatMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AiChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_USER = 0;
    private static final int TYPE_AI   = 1;

    private List<AiChatMessage> messages = new ArrayList<>();
    private final SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());

    public void setMessages(List<AiChatMessage> list) {
        messages = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return "user".equals(messages.get(position).sender) ? TYPE_USER : TYPE_AI;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_USER) {
            View v = inflater.inflate(R.layout.item_message_user, parent, false);
            return new UserViewHolder(v);
        } else {
            View v = inflater.inflate(R.layout.item_message_ai, parent, false);
            return new AiViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        AiChatMessage msg = messages.get(position);
        String time = sdf.format(new Date(msg.timestamp));

        if (holder instanceof UserViewHolder) {
            UserViewHolder h = (UserViewHolder) holder;
            h.tvMessage.setText(msg.message);
            h.tvTime.setText(time);
        } else {
            AiViewHolder h = (AiViewHolder) holder;
            h.tvMessage.setText(msg.message);
            h.tvTime.setText(time);
            String modeLabel = "offline".equals(msg.aiMode) ? "📴 Offline"
                    : "online".equals(msg.aiMode) ? "🌐 Online" : "🤖 AI";
            h.tvModeBadge.setText(modeLabel);
        }
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime;
        UserViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvUserMessage);
            tvTime = v.findViewById(R.id.tvUserTime);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage, tvTime, tvModeBadge;
        AiViewHolder(View v) {
            super(v);
            tvMessage = v.findViewById(R.id.tvAiMessage);
            tvTime = v.findViewById(R.id.tvAiTime);
            tvModeBadge = v.findViewById(R.id.tvAiModeBadge);
        }
    }
}
