package com.example.voyage.ui.ai;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.voyage.R;
import com.example.voyage.adapter.AiChatAdapter;
import com.example.voyage.ai.AiRepository;
import com.example.voyage.database.entities.AiChatMessage;
import com.example.voyage.util.SessionManager;
import com.example.voyage.viewmodel.AiChatViewModel;

import java.util.List;

public class AiChatFragment extends Fragment {

    private AiChatViewModel viewModel;
    private AiChatAdapter adapter;
    private SessionManager session;
    private Handler mainHandler;

    private EditText etMessage;
    private LinearLayout btnSend;
    private LinearLayout typingIndicator;
    private TextView tvModeName, tvModeIcon;
    private RecyclerView rvMessages;

    private AiRepository.Mode currentMode = AiRepository.Mode.AUTO;
    private boolean isSending = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_ai_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        session = new SessionManager(requireContext());
        mainHandler = new Handler(Looper.getMainLooper());
        viewModel = new ViewModelProvider(this).get(AiChatViewModel.class);

        // Load saved AI mode
        currentMode = AiRepository.fromString(session.getAiMode());

        bindViews(view);
        setupRecyclerView();
        setupModeSelector();
        setupInput();
        setupQuickChips(view);
        setupSettings(view);
        observeMessages();
        animateEntrance(view);
    }

    private void bindViews(View view) {
        etMessage = view.findViewById(R.id.etMessage);
        btnSend = view.findViewById(R.id.btnSend);
        typingIndicator = view.findViewById(R.id.typingIndicator);
        tvModeName = view.findViewById(R.id.tvModeName);
        tvModeIcon = view.findViewById(R.id.tvModeIcon);
        rvMessages = view.findViewById(R.id.rvMessages);
    }

    private void setupRecyclerView() {
        adapter = new AiChatAdapter();
        LinearLayoutManager lm = new LinearLayoutManager(requireContext());
        lm.setStackFromEnd(true);
        rvMessages.setLayoutManager(lm);
        rvMessages.setAdapter(adapter);
    }

    private void setupModeSelector() {
        updateModeUI();
        requireView().findViewById(R.id.btnModeSelector).setOnClickListener(v -> showModeDialog());
    }

    private void setupInput() {
        btnSend.setOnClickListener(v -> sendMessage());

        etMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER
                        && event.getAction() == KeyEvent.ACTION_DOWN)) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void setupQuickChips(View view) {
        view.findViewById(R.id.chipPlanDay).setOnClickListener(v ->
                prefillAndSend("Plan my day as a traveller. Give me a morning, afternoon, and evening schedule with activity suggestions."));
        view.findViewById(R.id.chipPacking).setOnClickListener(v ->
                prefillAndSend("Create a packing checklist for a 5-day trip. Include clothes, toiletries, documents, and travel essentials."));
        view.findViewById(R.id.chipBudget).setOnClickListener(v ->
                prefillAndSend("Give me practical budget-saving tips for travel. How can I save money on food, accommodation, and activities?"));
        view.findViewById(R.id.chipCheapFood).setOnClickListener(v ->
                prefillAndSend("What are the best tips for finding cheap, good food while travelling? How do I avoid tourist traps?"));
        view.findViewById(R.id.chipTranslate).setOnClickListener(v ->
                prefillAndSend("Teach me 10 essential travel phrases in Spanish (or ask me which language I want)."));
    }

    private void setupSettings(View view) {
        view.findViewById(R.id.btnAiSettings).setOnClickListener(v -> showSettingsDialog());
    }

    private void observeMessages() {
        viewModel.getGlobalMessages().observe(getViewLifecycleOwner(), messages -> {
            adapter.setMessages(messages);
            if (messages != null && !messages.isEmpty()) {
                rvMessages.smoothScrollToPosition(messages.size() - 1);
            }

            // Show welcome message if chat is empty
            if (messages == null || messages.isEmpty()) {
                postWelcomeMessage();
            }
        });
    }

    private void postWelcomeMessage() {
        // Insert a welcome AI message so the screen isn't blank
        AiChatMessage welcome = new AiChatMessage();
        welcome.sender = "ai";
        welcome.message = "👋 Hi! I'm Voyage AI, your travel companion.\n\n"
                + "I can help you:\n"
                + "• Plan your day and itinerary\n"
                + "• Create packing checklists\n"
                + "• Give budget-saving tips\n"
                + "• Summarise journal entries\n"
                + "• Answer travel questions\n\n"
                + "Tap a quick prompt below or type your own question!";
        welcome.aiMode = AiRepository.toString(currentMode);
        welcome.timestamp = System.currentTimeMillis();
        viewModel.sendMessage(welcome);
    }

    private void prefillAndSend(String text) {
        etMessage.setText(text);
        sendMessage();
    }

    private void sendMessage() {
        if (isSending) {
            Toast.makeText(requireContext(), "Please wait for the current response", Toast.LENGTH_SHORT).show();
            return;
        }

        String text = etMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        etMessage.setText("");
        isSending = true;

        // Save user message
        AiChatMessage userMsg = new AiChatMessage();
        userMsg.sender = "user";
        userMsg.message = text;
        userMsg.aiMode = AiRepository.toString(currentMode);
        userMsg.timestamp = System.currentTimeMillis();
        viewModel.sendMessage(userMsg);

        // Show typing
        typingIndicator.setVisibility(View.VISIBLE);
        btnSend.setAlpha(0.5f);

        // Call AI
        AiRepository.sendMessage(requireContext(), text, currentMode, new AiRepository.AiCallback() {
            @Override
            public void onResponse(String responseText, String usedMode) {
                mainHandler.post(() -> {
                    AiChatMessage aiMsg = new AiChatMessage();
                    aiMsg.sender = "ai";
                    aiMsg.message = responseText;
                    aiMsg.aiMode = usedMode;
                    aiMsg.timestamp = System.currentTimeMillis();
                    viewModel.sendMessage(aiMsg);
                    finishSending();
                });
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> {
                    AiChatMessage errMsg = new AiChatMessage();
                    errMsg.sender = "ai";
                    errMsg.message = "⚠️ " + error;
                    errMsg.aiMode = AiRepository.toString(currentMode);
                    errMsg.timestamp = System.currentTimeMillis();
                    viewModel.sendMessage(errMsg);
                    finishSending();
                });
            }
        });
    }

    private void finishSending() {
        typingIndicator.setVisibility(View.GONE);
        btnSend.setAlpha(1.0f);
        isSending = false;
    }

    private void showModeDialog() {
        String[] options = {"🤖 Auto (smart routing)", "📴 Offline (Ollama)", "🌐 Online (ChatGPT)"};
        int checked = currentMode == AiRepository.Mode.AUTO ? 0
                    : currentMode == AiRepository.Mode.OFFLINE ? 1 : 2;

        new AlertDialog.Builder(requireContext())
                .setTitle("AI Mode")
                .setSingleChoiceItems(options, checked, null)
                .setPositiveButton("Select", (d, w) -> {
                    int sel = ((AlertDialog) d).getListView().getCheckedItemPosition();
                    switch (sel) {
                        case 0: currentMode = AiRepository.Mode.AUTO; break;
                        case 1: currentMode = AiRepository.Mode.OFFLINE; break;
                        case 2: currentMode = AiRepository.Mode.ONLINE; break;
                    }
                    session.setAiMode(AiRepository.toString(currentMode));
                    updateModeUI();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showSettingsDialog() {
        EditText etHost = new EditText(requireContext());
        etHost.setHint("Ollama host (e.g. http://10.0.2.2:11434)");
        etHost.setText(session.getOllamaHost());

        EditText etModel = new EditText(requireContext());
        etModel.setHint("Model (e.g. llama3.2)");
        etModel.setText(session.getOllamaModel());

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        int pad = (int) (16 * requireContext().getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad, pad, 0);
        layout.addView(etHost);
        layout.addView(etModel);

        new AlertDialog.Builder(requireContext())
                .setTitle("⚙️ Ollama Settings")
                .setMessage("Configure your local Ollama server.\n"
                        + "Emulator: http://10.0.2.2:11434\n"
                        + "Device: http://YOUR_PC_IP:11434")
                .setView(layout)
                .setPositiveButton("Save", (d, w) -> {
                    String host = etHost.getText().toString().trim();
                    String model = etModel.getText().toString().trim();
                    if (!host.isEmpty()) session.setOllamaHost(host);
                    if (!model.isEmpty()) session.setOllamaModel(model);
                    Toast.makeText(requireContext(), "Ollama settings saved", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Clear Chat", (d, w) -> {
                    viewModel.clearGlobalChat();
                    Toast.makeText(requireContext(), "Chat cleared", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateModeUI() {
        switch (currentMode) {
            case OFFLINE:
                tvModeName.setText("Offline");
                tvModeIcon.setText("📴");
                break;
            case ONLINE:
                tvModeName.setText("Online");
                tvModeIcon.setText("🌐");
                break;
            case AUTO:
            default:
                tvModeName.setText("Auto");
                tvModeIcon.setText("🤖");
                break;
        }
    }

    private void animateEntrance(View view) {
        view.findViewById(R.id.headerLayout)
                .startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in));
    }
}
