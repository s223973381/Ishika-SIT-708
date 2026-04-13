# QuizzApp31C

A simple Android Quiz App built in Java with light/dark mode support.

---

## App Flow

```
Main Screen (Enter Name)
        ↓
Category Screen (Science / Geography / Literature)
        ↓
Quiz Screen (7 Questions with MCQ)
        ↓
Result Screen (Score + Comment)
```

---

## Features

- Enter your name before starting
- Choose from 3 categories: Science, Geography, Literature
- 7 multiple choice questions per category
- Correct answer highlighted in green, wrong in red after submitting
- Progress bar showing quiz completion
- Score and personalized comment on result screen
- Dark / Light mode toggle that works across all screens without flickering
- Dark mode preference saved and restored on app relaunch

---

## Project Structure

```
app/src/main/
├── AndroidManifest.xml
├── java/com/example/quizzapp31c/
│   ├── MainActivity.java         # Name entry + dark mode toggle
│   ├── CategoryActivity.java     # Category selection
│   ├── QuizActivity.java         # Quiz logic + questions
│   ├── ResultActivity.java       # Score display
│   └── Question.java             # Question model class
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_category.xml
    │   ├── activity_quiz.xml
    │   └── activity_result.xml
    ├── values/
    │   ├── colors.xml
    │   ├── strings.xml
    │   └── themes.xml
    └── values-night/
        ├── colors.xml            # Dark mode option_bg override
        └── themes.xml            # Dark mode window background
```

---

## How Dark Mode Works

Dark mode is implemented without activity recreation to avoid screen flickering.

**Key technique:** `android:configChanges="uiMode"` is added to every activity in `AndroidManifest.xml`. This tells Android not to destroy and recreate the activity when the theme changes. Instead, `onConfigurationChanged()` fires and each activity manually updates its background, card, and text colors via `applyThemeColors()`.

**Preference storage:** The dark mode state is saved in `SharedPreferences` under the key `dark_mode` in a file called `settings`. This persists across app restarts.

**Theme application order:** In every `onCreate()`, the saved preference is read and `AppCompatDelegate.setDefaultNightMode()` is called **before** `super.onCreate()` to ensure the correct theme is applied from the start without any flash.

---

## Technologies Used

| Tech | Usage |
|---|---|
| Java | Primary language |
| Android SDK | UI, Activities, Intents |
| Material3 (DayNight) | Base theme |
| AppCompatDelegate | Dark/light mode switching |
| SharedPreferences | Persisting dark mode setting |
| CardView | UI cards for questions and categories |
| RadioGroup / RadioButton | Answer selection |
| ProgressBar | Quiz progress indicator |

---

## How to Run

### Requirements
- Android Studio (Hedgehog or newer recommended)
- JDK 17+
- Android Emulator or physical device running API 24+

### Steps

1. Extract the project zip and open Android Studio
2. Go to **File → Open** and select the `QuizzApp31C` folder
3. Wait for Gradle sync to complete (bottom progress bar)
4. Create or select an emulator via **Device Manager** (Pixel 6, API 33+ recommended)
5. Press the **▶ Run** button or use `Shift + F10`

### Recommended Emulator Settings
To avoid "System UI not responding" on slower machines:
- RAM: 2048 MB or higher
- Graphics: Hardware (GLES 2.0)

---

## Known Behaviour

- Dark mode toggle is available on the **Main screen** and **Quiz screen**
- Toggling dark mode mid-quiz preserves your current question and score
- The Result screen respects the saved dark mode preference but has no toggle (by design — it's a results page)
- On a real Android device the app runs smoothly; "System UI not responding" popups are emulator-specific