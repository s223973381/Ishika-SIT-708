package com.example.llmchatbot.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.llmchatbot.R;
import com.example.llmchatbot.adapter.ChatAdapter;
import com.example.llmchatbot.database.ChatDatabase;
import com.example.llmchatbot.database.ChatMessage;
import com.example.llmchatbot.network.LLMApiService;
import com.example.llmchatbot.network.LLMRequest;
import com.example.llmchatbot.network.LLMResponse;
import com.example.llmchatbot.network.RetrofitClient;
import com.example.llmchatbot.utils.DateTimeUtils;
import com.example.llmchatbot.utils.SessionManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {

    private static final String MODEL_NAME = "llama3.2:1b";

    private RecyclerView rvMessages;
    private EditText etMessage;
    private TextView tvTyping;
    private ChatAdapter adapter;
    private final List<ChatMessage> messageList = new ArrayList<>();

    private SessionManager sessionManager;
    private ChatDatabase database;
    private LLMApiService apiService;
    private String username;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        sessionManager = new SessionManager(this);
        database = ChatDatabase.getInstance(this);
        apiService = RetrofitClient.getInstance().getApiService();
        username = sessionManager.getUsername();

        initViews();
        loadChatHistory();
    }

    private void initViews() {
        rvMessages = findViewById(R.id.rvMessages);
        etMessage = findViewById(R.id.etMessage);
        tvTyping = findViewById(R.id.tvTyping);
        ImageView btnSend = findViewById(R.id.btnSend);
        TextView tvUsername = findViewById(R.id.tvUsername);
        TextView btnLogout = findViewById(R.id.btnLogout);
        TextView btnClear = findViewById(R.id.btnClear);

        tvUsername.setText(getString(R.string.login_subtitle) + " · " + username);

        adapter = new ChatAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        rvMessages.setLayoutManager(layoutManager);
        rvMessages.setAdapter(adapter);

        btnSend.setOnClickListener(v -> sendMessage());
        btnLogout.setOnClickListener(v -> logout());
        btnClear.setOnClickListener(v -> clearHistory());
    }

    private void loadChatHistory() {
        executor.execute(() -> {
            List<ChatMessage> history = database.chatDao().getMessagesForUser(username);
            runOnUiThread(() -> {
                messageList.addAll(history);
                adapter.notifyDataSetChanged();
                scrollToBottom();
            });
        });
    }

    private void sendMessage() {
        String text = etMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        etMessage.setText("");
        addMessage(text, true);
        callLLM(text);
    }

    private void callLLM(String userText) {
        tvTyping.setVisibility(View.VISIBLE);

        // System instruction always prepended
        List<LLMRequest.Message> messages = new ArrayList<>();
        messages.add(new LLMRequest.Message("system",
                "You are a helpful assistant. Always reply in 50 words or fewer. Be concise and direct."));

        // Keep only the last 6 messages to stay within the 1b model's context limit
        int startIdx = Math.max(0, messageList.size() - 6);
        for (int i = startIdx; i < messageList.size(); i++) {
            ChatMessage cm = messageList.get(i);
            String role = cm.isUser() ? "user" : "assistant";
            messages.add(new LLMRequest.Message(role, cm.getMessage()));
        }

        LLMRequest request = new LLMRequest(MODEL_NAME, messages);
        apiService.sendMessage(request).enqueue(new Callback<LLMResponse>() {
            @Override
            public void onResponse(Call<LLMResponse> call, Response<LLMResponse> response) {
                tvTyping.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null
                        && response.body().getMessage() != null) {
                    String reply = response.body().getMessage().getContent();
                    addMessage(reply, false);
                } else {
                    String errorMsg = "No response from model";
                    try {
                        if (response.errorBody() != null)
                            errorMsg = response.errorBody().string();
                    } catch (Exception ignored) {}
                    Toast.makeText(ChatActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<LLMResponse> call, Throwable t) {
                tvTyping.setVisibility(View.GONE);
                Toast.makeText(ChatActivity.this,
                        "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        ChatMessage msg = new ChatMessage(username, text, isUser, DateTimeUtils.now());
        messageList.add(msg);
        adapter.notifyItemInserted(messageList.size() - 1);
        scrollToBottom();
        executor.execute(() -> database.chatDao().insertMessage(msg));
    }

    private void scrollToBottom() {
        if (!messageList.isEmpty()) {
            rvMessages.smoothScrollToPosition(messageList.size() - 1);
        }
    }

    private void clearHistory() {
        new AlertDialog.Builder(this)
                .setTitle("Clear Chat")
                .setMessage("Delete all messages in this conversation?")
                .setPositiveButton("Clear", (d, w) -> {
                    executor.execute(() -> {
                        database.chatDao().deleteMessagesForUser(username);
                        runOnUiThread(() -> {
                            int size = messageList.size();
                            messageList.clear();
                            adapter.notifyItemRangeRemoved(0, size);
                        });
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void logout() {
        new AlertDialog.Builder(this)
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (d, w) -> {
                    sessionManager.logout();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
