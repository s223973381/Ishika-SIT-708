package com.example.quizzapp31c;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;

public class QuizActivity extends AppCompatActivity {

    TextView questionText, progressText, categoryTitle;
    ProgressBar progressBar;
    RadioGroup radioGroupOptions;
    RadioButton option1, option2, option3, option4;
    Button submitButton, nextButton;
    Switch themeSwitch;
    LinearLayout rootLayout;
    CardView questionCard;

    ArrayList<Question> questionList;
    int currentQuestionIndex = 0;
    int score = 0;
    boolean answered = false;
    String userName, category;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDarkMode = prefs.getBoolean("dark_mode", false);
        int targetMode = isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO;
        if (AppCompatDelegate.getDefaultNightMode() != targetMode) {
            AppCompatDelegate.setDefaultNightMode(targetMode);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        int selectedOptionId = -1;
        if (savedInstanceState != null) {
            currentQuestionIndex = savedInstanceState.getInt("currentQuestionIndex", 0);
            score = savedInstanceState.getInt("score", 0);
            answered = savedInstanceState.getBoolean("answered", false);
            selectedOptionId = savedInstanceState.getInt("selectedOptionId", -1);
        }

        userName = getIntent().getStringExtra("USERNAME");
        category = getIntent().getStringExtra("CATEGORY");

        rootLayout = findViewById(R.id.quizLayout);
        questionCard = findViewById(R.id.questionCard);
        questionText = findViewById(R.id.questionText);
        progressText = findViewById(R.id.progressText);
        progressBar = findViewById(R.id.progressBar);
        radioGroupOptions = findViewById(R.id.radioGroupOptions);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        submitButton = findViewById(R.id.submitButton);
        nextButton = findViewById(R.id.nextButton);
        categoryTitle = findViewById(R.id.categoryTitle);
        themeSwitch = findViewById(R.id.themeSwitch);

        applyThemeColors(isDarkMode);

        if (themeSwitch != null) {
            themeSwitch.setOnCheckedChangeListener(null);
            themeSwitch.setChecked(isDarkMode);
            themeSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("dark_mode", isChecked);
                editor.apply();
                AppCompatDelegate.setDefaultNightMode(isChecked ?
                        AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO);
            });
        }

        if (categoryTitle != null && category != null) {
            categoryTitle.setText(category + " Quiz");
        }

        questionList = new ArrayList<>();
        loadQuestionsByCategory(category);
        setupQuestionState(selectedOptionId);

        if (submitButton != null) submitButton.setOnClickListener(v -> checkAnswer());

        if (nextButton != null) {
            nextButton.setOnClickListener(v -> {
                currentQuestionIndex++;
                if (currentQuestionIndex < questionList.size()) {
                    answered = false;
                    showQuestion();
                } else {
                    Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
                    intent.putExtra("SCORE", score);
                    intent.putExtra("TOTAL", questionList.size());
                    intent.putExtra("USERNAME", userName);
                    intent.putExtra("CATEGORY", category);
                    startActivity(intent);
                    finish();
                }
            });
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isDark = (newConfig.uiMode & Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
        applyThemeColors(isDark);
        if (themeSwitch != null) themeSwitch.setChecked(isDark);
    }

    private void applyThemeColors(boolean isDark) {
        int bgColor      = ContextCompat.getColor(this, isDark ? R.color.dark_background : R.color.off_white);
        int cardColor    = ContextCompat.getColor(this, isDark ? R.color.dark_card_bg    : R.color.white);
        int textColor    = ContextCompat.getColor(this, isDark ? R.color.white           : R.color.black);
        int subTextColor = ContextCompat.getColor(this, isDark ? R.color.gray_text_light : R.color.gray_text);

        if (rootLayout != null)   rootLayout.setBackgroundColor(bgColor);
        if (questionCard != null) questionCard.setCardBackgroundColor(cardColor);
        if (questionText != null) questionText.setTextColor(textColor);
        if (progressText != null) progressText.setTextColor(subTextColor);

        RadioButton[] options = {option1, option2, option3, option4};
        for (RadioButton rb : options) {
            if (rb != null) {
                rb.setBackgroundColor(cardColor);
                rb.setTextColor(textColor);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("currentQuestionIndex", currentQuestionIndex);
        outState.putInt("score", score);
        outState.putBoolean("answered", answered);
        if (radioGroupOptions != null) {
            outState.putInt("selectedOptionId", radioGroupOptions.getCheckedRadioButtonId());
        }
    }

    private void setupQuestionState(int selectedOptionId) {
        if (questionList.isEmpty()) return;

        Question q = questionList.get(currentQuestionIndex);
        if (questionText != null) questionText.setText(q.getQuestionText());
        if (option1 != null) option1.setText(q.getOptions()[0]);
        if (option2 != null) option2.setText(q.getOptions()[1]);
        if (option3 != null) option3.setText(q.getOptions()[2]);
        if (option4 != null) option4.setText(q.getOptions()[3]);

        if (progressText != null)
            progressText.setText("Question " + (currentQuestionIndex + 1) + " of " + questionList.size());
        if (progressBar != null)
            progressBar.setProgress((currentQuestionIndex * 100) / questionList.size());

        if (answered) {
            if (selectedOptionId != -1 && radioGroupOptions != null)
                radioGroupOptions.check(selectedOptionId);
            displayAnswerResult(selectedOptionId);
        } else {
            showQuestion();
        }
    }

    private void loadQuestionsByCategory(String category) {
        questionList.clear();
        if (category == null) return;
        if (category.equals("Science")) {
            questionList.add(new Question("What planet is known as the Red Planet?", new String[]{"Earth", "Mars", "Jupiter", "Saturn"}, 1));
            questionList.add(new Question("What gas do humans breathe in?", new String[]{"Nitrogen", "Oxygen", "Hydrogen", "Carbon Dioxide"}, 1));
            questionList.add(new Question("What is H2O?", new String[]{"Salt", "Water", "Oxygen", "Acid"}, 1));
            questionList.add(new Question("How many legs does an insect have?", new String[]{"4", "6", "8", "10"}, 1));
            questionList.add(new Question("What force pulls objects to Earth?", new String[]{"Magnetism", "Gravity", "Pressure", "Heat"}, 1));
            questionList.add(new Question("Which organ pumps blood?", new String[]{"Lungs", "Brain", "Heart", "Liver"}, 2));
            questionList.add(new Question("What star gives us light?", new String[]{"Moon", "Mars", "Sun", "Venus"}, 2));
        } else if (category.equals("Geography")) {
            questionList.add(new Question("What is the capital of Australia?", new String[]{"Sydney", "Canberra", "Melbourne", "Perth"}, 1));
            questionList.add(new Question("Which is the largest ocean?", new String[]{"Atlantic", "Indian", "Arctic", "Pacific"}, 3));
            questionList.add(new Question("Mount Everest is in which mountain range?", new String[]{"Andes", "Alps", "Himalayas", "Rockies"}, 2));
            questionList.add(new Question("Which continent is Egypt in?", new String[]{"Asia", "Africa", "Europe", "South America"}, 1));
            questionList.add(new Question("Which country has Tokyo as capital?", new String[]{"China", "Korea", "Japan", "Thailand"}, 2));
            questionList.add(new Question("Which desert is in Africa?", new String[]{"Gobi", "Sahara", "Kalahari", "Both B and C"}, 3));
            questionList.add(new Question("How many continents are there?", new String[]{"5", "6", "7", "8"}, 2));
        } else if (category.equals("Literature")) {
            questionList.add(new Question("Who wrote Romeo and Juliet?", new String[]{"Dickens", "Shakespeare", "Austen", "Orwell"}, 1));
            questionList.add(new Question("Who wrote Harry Potter?", new String[]{"J.K. Rowling", "Tolkien", "Twain", "Lewis"}, 0));
            questionList.add(new Question("What type of book is a dictionary?", new String[]{"Poetry", "Reference", "Novel", "Drama"}, 1));
            questionList.add(new Question("Who wrote Pride and Prejudice?", new String[]{"Jane Austen", "Emily Brontë", "Virginia Woolf", "Mary Shelley"}, 0));
            questionList.add(new Question("Who is the author of 1984?", new String[]{"George Orwell", "Homer", "Plato", "Hemingway"}, 0));
            questionList.add(new Question("A story written by hand long ago is called?", new String[]{"Magazine", "Manuscript", "Poster", "Journal"}, 1));
            questionList.add(new Question("What is a poem collection called?", new String[]{"Anthology", "Atlas", "Index", "Manual"}, 0));
        }
    }

    private void showQuestion() {
        if (questionList.isEmpty() || currentQuestionIndex >= questionList.size()) return;
        if (radioGroupOptions != null) radioGroupOptions.clearCheck();

        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        boolean isDark = prefs.getBoolean("dark_mode", false);

        // Reset backgrounds and text colors
        int cardColor = ContextCompat.getColor(this, isDark ? R.color.dark_card_bg : R.color.white);
        int textColor = ContextCompat.getColor(this, isDark ? R.color.white : R.color.black);
        RadioButton[] options = {option1, option2, option3, option4};
        for (RadioButton rb : options) {
            if (rb != null) {
                rb.setBackgroundColor(cardColor);
                rb.setTextColor(textColor);
                rb.setEnabled(true);
            }
        }

        if (submitButton != null) submitButton.setEnabled(true);
        if (nextButton != null) nextButton.setVisibility(View.GONE);

        Question q = questionList.get(currentQuestionIndex);
        if (questionText != null) questionText.setText(q.getQuestionText());
        if (option1 != null) option1.setText(q.getOptions()[0]);
        if (option2 != null) option2.setText(q.getOptions()[1]);
        if (option3 != null) option3.setText(q.getOptions()[2]);
        if (option4 != null) option4.setText(q.getOptions()[3]);

        if (progressText != null)
            progressText.setText("Question " + (currentQuestionIndex + 1) + " of " + questionList.size());
        if (progressBar != null)
            progressBar.setProgress((currentQuestionIndex * 100) / questionList.size());
    }

    private void checkAnswer() {
        if (radioGroupOptions == null) return;
        int selectedId = radioGroupOptions.getCheckedRadioButtonId();
        if (selectedId == -1) {
            Toast.makeText(this, "Please select an answer", Toast.LENGTH_SHORT).show();
            return;
        }
        answered = true;
        Question q = questionList.get(currentQuestionIndex);
        int selectedIndex = radioGroupOptions.indexOfChild(findViewById(selectedId));
        if (selectedIndex == q.getCorrectAnswerIndex()) score++;
        displayAnswerResult(selectedId);
    }

    private void displayAnswerResult(int selectedId) {
        if (submitButton != null) submitButton.setEnabled(false);
        if (nextButton != null) nextButton.setVisibility(View.VISIBLE);

        RadioButton[] options = {option1, option2, option3, option4};
        for (RadioButton rb : options) {
            if (rb != null) rb.setEnabled(false);
        }

        Question q = questionList.get(currentQuestionIndex);
        int correctIndex = q.getCorrectAnswerIndex();
        if (options[correctIndex] != null)
            options[correctIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.green_correct));

        int selectedIndex = -1;
        if (radioGroupOptions != null && selectedId != -1)
            selectedIndex = radioGroupOptions.indexOfChild(findViewById(selectedId));

        if (selectedIndex != -1 && selectedIndex != correctIndex && options[selectedIndex] != null)
            options[selectedIndex].setBackgroundColor(ContextCompat.getColor(this, R.color.red_wrong));

        if (progressBar != null)
            progressBar.setProgress(((currentQuestionIndex + 1) * 100) / questionList.size());
    }
}