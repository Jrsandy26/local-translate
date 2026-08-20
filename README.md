# Riva Translate 🌐🎙️

An offline-first Android translation application built with **Jetpack Compose**, **Google Translation Engines**, and **Material Design 3**. Riva Translate enables real-time speech recognition, instant text-to-speech, live streaming translation sessions with waveform visualizers, conversation split-mode, offline language package downloads, and exportable transcript logs.

---

## 🌟 Key Features

### 1. 🎙️ Live Translation & Real-Time Audio Recording
- **Streaming Live Transcription**: Continuous on-device voice recognition with live streaming speech-to-text.
- **Audio Waveform Visualizer**: Real-time waveform amplitude rendering synced with live speech input.
- **Session Playback & Export**: Record, play back, and export sessions as formatted subtitle (.SRT), plain text (.TXT), CSV, and PDF transcripts.
- **Live Background Notification**: Interactive custom notifications with live timer, pause/resume, and stop controls.

### 2. 💬 Two-Way Conversation Mode
- **Dual-Speaker Split Layout**: Interactive split-screen layout designed for face-to-face cross-language dialogues.
- **Single-Tap Mic Input**: Instant speech recognition per speaker with automatic translation playback.
- **Transcript History**: Full turn-by-turn conversation log with audio re-listen capabilities.

### 3. 🌐 Neural Translation & Offline Language Models
- **Google ML Kit Offline Models**: Fast on-device neural machine translation without requiring an internet connection.
- **Multi-Language Support**: Support for English, Spanish, French, German, Japanese, Chinese, Hindi, Arabic, Russian, Portuguese, Korean, Italian, Dutch, and Turkish.
- **Offline Language Pack Manager**: On-device model manager with download progress tracking, storage management, and package deletion.

### 4. 🎨 Dynamic Theme Switcher & Accessibility
- **Theme Modes**: Choose between **Light Mode (`☀️`)**, **Dark Mode (`🌙`)**, and **System Default (`⚙️`)**.
- **Quick-Toggle Buttons**: Instant theme switching from the home screen and live session top bars.
- **Speech & Pitch Customization**: Adjustable speech speed (0.5x – 1.5x), pitch modulation, and preferred voice tone (Default, Female, Male).

### 5. 📁 Local Room Database Persistence
- **Offline Storage**: SQLite database managed via Android Jetpack Room with automatic schema migrations.
- **Translation History & Favorites**: Star favorite phrases, search past translations, and perform bulk export actions.

---

## 🏗️ Architecture & Tech Stack

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3)
- **Architecture**: MVVM (Model-View-ViewModel) + StateFlow & Coroutines
- **Local Database**: [Android Room Database](https://developer.android.com/training/data-storage/room)
- **Translation Engine**: Google Translation Engine & On-Device Language Translation models
- **Audio & Speech**: Android SpeechRecognizer & Text-To-Speech (TTS) engine
- **Build System**: Gradle Kotlin DSL (`build.gradle.kts`) with Version Catalog (`libs.versions.toml`)

---

## 📱 Project Architecture & Module Structure

```
app/src/main/
├── java/com/example/        # Kotlin Application Source
│   ├── audio/               # Audio recording, waveform capture, TTS, and SpeechRecognition
│   │   ├── AudioPlaybackHelper.kt
│   │   ├── AudioRecorderHelper.kt
│   │   ├── SpeechRecognitionHelper.kt
│   │   └── TextToSpeechHelper.kt
│   ├── data/                # Local Room Database, DAOs, and Repositories
│   │   └── db/
│   │       ├── AppDatabase.kt
│   │       └── TranslationDao.kt
│   ├── model/               # Domain entities, session records, and language models
│   │   ├── ActiveScreen.kt
│   │   ├── AppThemeMode.kt
│   │   ├── Language.kt
│   │   └── TranslationSession.kt
│   ├── service/             # Background foreground service & notification system
│   │   ├── LiveSessionManager.kt
│   │   ├── LiveTranslationService.kt
│   │   └── WaveformBitmapGenerator.kt
│   ├── translation/         # Offline neural translation engine & download worker
│   │   ├── GoogleTranslationEngine.kt
│   │   ├── LanguageModelDownloadWorker.kt
│   │   └── OfflineTranslationManager.kt
│   └── ui/                  # Jetpack Compose UI Layer
│       ├── components/      # Reusable cards, dialogs, pills, and audio visualizers
│       ├── screens/         # Home, Live Translate, Conversation, Languages, History, Profile
│       ├── theme/           # Dynamic Material 3 ColorSchemes, dark/light palettes, Typography
│       └── viewmodel/       # State management, coroutines, and preferences
└── res/                     # Android Vector Drawables, Layouts, Values, and Icons
    ├── drawable/            # Vector graphics, notification icons, adaptive artwork
    ├── layout/              # Custom notification layouts
    ├── mipmap-anydpi-v26/   # Adaptive launcher icon configurations
    └── values/              # Strings, theme definitions, and color constants
```

---

## 🚀 Getting Started

1. **Prerequisites**: Android Studio Jellyfish or later with Android SDK 34+.
2. **Build and Run**:
   ```bash
   gradle assembleDebug
   ```
3. **Permissions**: The application will request `RECORD_AUDIO` and `POST_NOTIFICATIONS` at runtime when starting voice features or live translation sessions.
