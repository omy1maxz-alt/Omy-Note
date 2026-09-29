# AI Notes ✍️

A fast, private, offline-first notes application built for Android with Jetpack Compose and Material 3, featuring rich note blocks, handwriting doodles, PDF export, ZIP backup/restore, and optional Google Gemini AI intelligence via REST.

---

## 🌟 Highlights

- **100% Offline-First Core**: Note creation, rich text blocks, checklists, handwriting sketches, local search, tags, categories, PDF export, and full backup/restore work completely without an internet connection.
- **Atomic Local Persistence**: Notes and media are saved locally in private JSON files (`notes/notes.json` and `media/`) with atomic rename logic to eliminate file corruption risks.
- **Rich Editor**: Default warm cream canvas (`#F6E7B2`) with customizable color tints, serif/sans/monospace fonts, undo/redo stacks, live character counts, in-note find-and-replace, and full-screen distraction-free Focus Mode (`#1E5B5B`).
- **Handwriting & Doodling**: Smooth pointer-input canvas storing vector stroke coordinates rather than heavy bitmaps.
- **Audio Dictation**: Integrated speech recognition using Android's native `SpeechRecognizer` (no Gemini or cloud service required).
- **Portable Backups**: SAF ZIP export packaging all notes, categories, tags, handwriting vectors, and media images into a single file with path-traversal validation.
- **Optional Google Gemini AI**: Connect your own Gemini API key in Settings to unlock executive summaries, clear rewrites, grammar proofreading, auto-tagging, auto-categorization, and semantic search re-ranking.
- **Privacy First**: The API key is stored only in private DataStore, masked in the UI, never logged, never included in backups, and never hardcoded.

---

## 🏗️ Architecture

```
com.example/
├── MainActivity.kt           # Single-activity container with navigation drawer
├── MainViewModel.kt          # UI state, search filtering, and actions
├── AppContainer.kt           # Explicit manual dependency injection
│
├── data/
│   ├── model/
│   │   └── Note.kt           # Note, NoteBlock, HandwritingStroke, Category models
│   ├── local/
│   │   ├── NoteRepository.kt     # Atomic JSON file persistence (Moshi reflection)
│   │   └── SettingsRepository.kt # DataStore preferences (themes, keys, settings)
│   ├── remote/
│   │   ├── GeminiApi.kt          # Retrofit REST interface (v1beta/models)
│   │   ├── GeminiDto.kt          # Moshi-serializable DTOs
│   │   └── GeminiAiProvider.kt   # OkHttp with timeouts and safe Result mapping
│   └── ai/
│       └── AiRepository.kt       # Decoupled AI provider interface & implementation
│
├── ui/
│   ├── dashboard/
│   │   └── DashboardScreen.kt    # Balanced 2-column masonry grid, list view, stats
│   ├── editor/
│   │   └── NoteEditorScreen.kt   # Rich blocks, handwriting, focus mode, dictation
│   ├── settings/
│   │   ├── SettingsScreen.kt     # Grouped preferences, backup/restore, AI settings
│   │   └── AchievementsScreen.kt # Local badge unlocks and productivity stats
│   ├── components/
│   │   ├── HandwritingCanvas.kt  # Drawing canvas with color & width controls
│   │   ├── ColorPicker.kt        # Note tint swatch selection
│   │   └── EmojiDialog.kt        # Quick emoji insertion grid
│   └── theme/
│       ├── Color.kt              # Material 3 light/dark palette and note tints
│       ├── Theme.kt              # Dynamic and custom color scheme definitions
│       └── Type.kt               # Sans, Serif, and Monospace typography
│
└── util/
    ├── TextStats.kt          # Characters, words, sentences, paragraphs calculator
    ├── BitmapLoader.kt       # Memory-safe downscaled BitmapFactory
    ├── PdfExporter.kt        # Multi-page A4 PDF rendering via PdfDocument
    ├── BackupManager.kt      # Safe ZIP archiving and extraction with security checks
    ├── DateFormats.kt        # Human-readable timestamps ("Today, 20:33")
    └── ReminderReceiver.kt   # NotificationManager and AlarmManager integration
```

---

## 📱 AndroidIDE & Environment Requirements

This project is structured for building in both **Google AI Studio** and **AndroidIDE** on an ARM64/aarch64 Android phone:

- **Target Device**: ARM64 Android phone running AndroidIDE (or Android Studio on desktop)
- **Minimum SDK**: 24 (Android 7.0)
- **Target / Compile SDK**: 34 / 36
- **JDK Compatibility**: Java 17
- **Gradle**: 8.6+ (wrapper included)
- **Android Gradle Plugin (AGP)**: 8.4.1+ (built with platform toolchain)
- **Kotlin**: 1.9.23 / 2.x
- **Jetpack Compose**: Compose BOM 2024.04.01+ with Material 3

---

## 🚀 How to Build and Run in AndroidIDE

1. **Clone the Repository**:
   ```bash
   git clone <YOUR_REPOSITORY_URL> ai-notes
   cd ai-notes
   ```

2. **Open in AndroidIDE**:
   - Launch **AndroidIDE** on your Android device.
   - Tap **Open Project** and navigate to the cloned `ai-notes` directory.

3. **Sync & Build with the Included Gradle Wrapper**:
   - AndroidIDE will detect the Gradle project and trigger a sync.
   - Alternatively, open the terminal in AndroidIDE and execute:
     ```bash
     ./gradlew assembleDebug
     ```

4. **Install & Run**:
   - Tap **Run** in AndroidIDE or install the built APK from:
     `app/build/outputs/apk/debug/app-debug.apk`

---

## 🔑 Gemini AI Configuration (Optional)

Google Gemini AI is **completely optional**. All core note-taking capabilities operate 100% offline.

To enable AI features:
1. Open the app and navigate to **Settings** (via the bottom navigation or side drawer).
2. Scroll to the **Google Gemini AI** section.
3. Turn on the **Enable Gemini Features** switch.
4. Tap **Gemini API Key** and paste your API key from Google AI Studio.
5. Choose your desired model (default: `gemini-2.5-flash`).

### Privacy Policy for AI
- Note content is **never automatically sent** to external servers.
- AI requests (Summarize, Rewrite, Fix Grammar, Auto-tag, Auto-categorize) are only dispatched when explicitly triggered by you.
- Candidate results for Smart Search are filtered locally first; only a minimal candidate subset is sent for semantic ranking if Smart Search is enabled.
- Your API key is stored solely in local DataStore on your device and is excluded from ZIP backups.

---

## 💾 Backup and Restore Format

Backups are exported via the Storage Access Framework (SAF) as a standard `.zip` file containing:
- `notes.json`: All notes, categories, tags, timestamps, and handwriting stroke vectors.
- `media/`: All locally imported image attachments.

When restoring, you can choose between:
- **Merge**: Integrates backup notes and categories without overwriting existing notes.
- **Replace**: Cleanly swaps your local notes database with the archive.
All archive paths are validated before extraction to protect against path traversal exploits.

---

## 📄 License

Open-source under the Apache License 2.0.
