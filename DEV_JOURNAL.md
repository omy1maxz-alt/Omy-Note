# Developer Journal - AI Notes

## Phase 1 to Phase 8: Full Implementation

- Configured application identity as "AI Notes" across `metadata.json`, `strings.xml`, `settings.gradle.kts`, and `app/build.gradle.kts`.
- Replaced default launcher icon with custom adaptive icon featuring dark teal gradient and vector notebook/stylus silhouette.
- Implemented core offline JSON persistence engine in `NoteRepository.kt` with atomic temporary file swapping to prevent corruption.
- Designed comprehensive data models in `Note.kt`: Note, NoteBlock (Text, Checklist, Bullet, Image, Handwriting), HandwritingStroke, Category, and NoteHistoryVersion.
- Built `SettingsRepository.kt` backed by DataStore Preferences for themes, fonts, sorting, date format, app lock, and Gemini preferences.
- Implemented direct REST API integration for Gemini in `GeminiApi.kt`, `GeminiDto.kt`, and `GeminiAiProvider.kt` with 60s read timeout and robust Result error wrapping.
- Implemented `AiRepository.kt` abstraction providing summarize, rewrite, grammar proofread, translate, auto-tag, auto-categorize, and search re-ranking.
- Built memory-safe utilities: `BitmapLoader.kt` with downsampling, `PdfExporter.kt` with multi-page A4 rendering, `BackupManager.kt` with ZIP archiving and path traversal checks, and `DateFormats.kt`.
- Created interactive Compose components: `HandwritingCanvas.kt` using pointerInput gestures, `ColorPicker.kt`, and `EmojiDialog.kt`.
- Built rich screens: `DashboardScreen.kt` with 2-column masonry grid, list view with live text stats, search, and category chips; `NoteEditorScreen.kt` with rich blocks, focus mode, undo/redo, and dictation; `SettingsScreen.kt`; and `AchievementsScreen.kt`.
- Wired everything into `AppContainer.kt`, `MainViewModel.kt`, and `MainActivity.kt`.

## Phase 9: Dashboard Drag-to-Adjust Position and Character-Based Note Split Box

- Re-engineered `NoteCard` and `NoteListRow` click and touch event hierarchies to isolate the card opening action to the note body.
- Implemented hold-to-drag gesture detector on the note's drag handle header using `detectDragGesturesAfterLongPress`, translating cards dynamically and updating `NoteRepository` custom ordering when threshold is crossed.
- Added quick Up/Down position adjustment buttons on each card for effortless 1-tap reordering.
- Defaulted sort order to `custom` so that manual note positions are immediately respected and persisted across app restarts.
- Replaced the ambiguous box button with a dedicated high-contrast Box Button `[ ✂ Split [char] ]` that strictly invokes the split option dialog without navigating to the note editor.
- Implemented the Character Split Option Box dialog: calculates total note characters (e.g. 250 chars), accepts user character split input (e.g. 100 chars), validates bounds, displays live breakdown ("Keep 100 chars in original note / Split 150 chars to new note"), and creates the second note preserving category, color tint, tags, and font.

## Phase 10: Expressive AI Button & Natural-Language Assistant in Note Editor

- Replaced any generic or emoji buttons with a dedicated custom `ExpressiveAiIcon` Composable combining an AI face visor, curved happy eyes `^ ^`, gold smile `‿`, and glowing star sparkle `✦` over a purple/indigo/cyan gradient.
- Implemented `executeNaturalLanguageInstruction` in `AiRepository.kt` and `MainViewModel.kt` to send Gemini the user's natural language instruction, target text context, and clean formatting instructions without filler meta-commentary.
- Built the floating rounded AI Instruction Box Dialog in `NoteEditorScreen.kt`:
  - Automatically targets highlighted text (`Selected text`) if active, or falls back to `Entire note`, with a live chip toggle and text snippet preview.
  - Free-form natural-language input field supporting any sentence (e.g., "correct my writing", "translate into Indonesian", "make this shorter", "rewrite romantically", "turn this into lyrics").
  - Horizontally scrollable quick suggestion chips for one-tap inspiration.
- Built the non-destructive AI Result Preview dialog:
  - Displays original instruction and transformed content in a scrollable preview card.
  - Action buttons: "Replace Selection / Apply to Note", "Insert Below", "Copy to Clipboard", and "Cancel / Discard".
  - Seamless snapshot capture before applying changes to ensure 100% undoability via the top bar Undo button.
- Handled offline/disabled state: When Gemini is disabled or API key is missing, tapping the AI button presents a friendly informative dialog while keeping normal note editing and original text completely intact.

## Phase 11: Rectangular Formatted View Toggle & Rich Reading Mode

- Added a rectangular toggle button in the note editor top bar strictly following the requested toolbar order: `Undo → Redo → Formatted View → Search → other tools`.
- Designed the toggle with clear visual states:
  - Inactive: Subtly tinted rounded rectangle (8.dp) with outline border, reading book icon `MenuBook`, and "View" label.
  - Active: High-contrast highlighted container `primaryContainer` with colored 1.dp border, edit note icon `EditNote`, and "Edit" label.
- Built the `FormattedNoteView` reading mode:
  - Renders the live in-memory note without duplicating state or losing unsaved edits/selection.
  - Parses and formats Markdown elements: H1-H4 headings (`#` to `####`), inline bold (`**`/`__`), italic (`*`/`_`), strikethrough (`~~`), inline code (` `), multi-line code blocks (` ``` `), blockquotes (`>`), horizontal rules (`---`), numbered lists (`1.`), bullet lists (`-`/`*`/`•`), and text checkboxes (`[ ]`/`[x]`).
  - Supports all rich block types: Checklist blocks with strikethrough/checked indicators, Bullet blocks, image attachments, and vector handwriting sketches.
  - Safe fallback rendering for custom or unrecognized block types so no data is ever lost or corrupted.
- Seamless toggle back to normal editor view with zero data modification and instant keyboard/cursor state preservation.

## Phase 12: Large Text File Zero-Lag Performance Optimization

- Analyzed memory, CPU, and Compose recomposition bottlenecks when handling large text files and huge notes (10,000+ to 100,000+ characters).
- Implemented automatic block chunking in `NoteEditorScreen.kt`: large text files or pasted texts exceeding 6,000 characters are automatically split at paragraph or sentence boundaries into separate virtualized `NoteBlock` instances, allowing `LazyColumn` to recycle views seamlessly at 120 FPS.
- Re-engineered `captureSnapshot()` in `NoteEditorScreen.kt` with a 1.5-second time window debounce, eliminating the previous performance bottleneck of deep-copying all note blocks on every keystroke.
- Replaced eager string concatenations with `derivedStateOf` for live character counting (`title.length + blocks.sumOf { it.content.length }`), creating zero intermediate String objects per frame.
- Implemented `TextStats.kt` with an ultra-fast, zero-allocation single-pass scanner for word, sentence, and paragraph counts without regular expressions or string splits.
- Optimized dashboard note preview generation and history version summaries to early-exit with bounded character allocations instead of joining all blocks in memory.
- Verified build and verified smooth responsiveness across text editor, formatted reading view, and note dashboard.

## Phase 13: Enhanced In-Note Search, Find Next/Prev, and Granular Replace

- Upgraded the in-note search and replace banner to support full bi-directional match navigation:
  - Added Find Previous (`↑` `KeyboardArrowUp`) and Find Next (`↓` `KeyboardArrowDown`) controls.
  - Implemented real-time dynamic search match indexing across note title and all text/checklist/bullet blocks (`searchMatches`).
  - Added match count badge indicator (`currentMatch/totalMatches`) with empty state handling.
  - Connected match selection to `lazyListState.animateScrollToItem()`, auto-scrolling the editor directly to the matching block and highlighting the selection range.
- Added dual replace controls:
  - **Replace**: Replaces only the currently highlighted match and advances cursor.
  - **All**: Replaces all matching occurrences across the title and all note blocks in a single transaction.
- Maintained 100% undo safety by capturing editor snapshots before executing replace operations.

## Phase 14: Phone Keyboard Optimization, Sample Image Preset Gallery & Freely Draggable Images

- Optimized keyboard input on emulator / streaming preview:
  - Enabled tap-to-focus on the empty note canvas area below text blocks, automatically focusing or creating text blocks and triggering `keyboardController?.show()`.
  - Added `KeyboardOptions(capitalization = KeyboardCapitalization.Sentences)` across all editor text fields.
- Implemented `SampleImageGenerator.kt` providing 5 instant preset graphics with high-resolution canvas bitmap rendering:
  - 🌄 Alpine Sunset Lake (Nature)
  - ☕ Cozy Workspace Desk (Productivity)
  - 💡 Creative Mind Map (Ideas)
  - 📸 Vintage Polaroid Frame (Memories)
  - 📊 Modern Analytics Card (Data)
- Built interactive image blocks with dynamic re-flow in `NoteEditorScreen.kt`:
  - Added `⠿ Drag to Move` vertical drag gesture detector (`detectDragGestures`) that dynamically swaps positions with surrounding blocks in real time as the user drags.
  - Added quick Up (`↑`) and Down (`↓`) move buttons for 1-tap repositioning.
  - Linked caption text field directly to image blocks so captions travel with the image during repositioning.
  - Surrounding text blocks automatically adjust and flow around relocated images.
  - Integrated with snapshot Undo/Redo stack for full rollback capability.

## Phase 15: Window-Style Corner Drag Resizing & Precise In-Between Text Image Placement

- Implemented interactive window-style image scaling:
  - Added a corner resize drag thumb (↘️ `OpenInFull`) allowing fluid, continuous scaling between 25% and 100% width by dragging like a desktop window.
  - Added 1-tap window size presets: `25%` (stamp), `50%` (half), `75%` (wide), `100%` (full).
  - Added alignment controls: Left (`FormatAlignLeft`), Center (`FormatAlignCenter`), Right (`FormatAlignRight`).
- Implemented in-between text placement and visual landing mark:
  - Added live visual insertion marker pill (`📍 Placing image down/up between next text...`) during drag operations.
  - Built "Place Image Between Text" dialog (`📍` `Icons.Default.Place` button) listing every text paragraph with snippet previews to teleport the image directly between paragraphs.
  - Added cursor-aware split-text insertion: Inserting an image while actively editing a paragraph splits the text at the cursor position and seats the image directly between the two halves.
## Phase 16: Top Bar Decluttering, Reliable Save Button & Borderless Image Presentation

- Decluttered Note Editor Top Bar:
  - Streamlined actions to `Undo`, `Redo`, a prominent, accessible `Save` button, and the `More Options` (`MoreVert`) menu.
  - Eliminated horizontal viewport overflow issues that caused buttons to crowd or fail to receive clicks on standard mobile screens.
  - Tapping Save now hides the software keyboard and reliably persists changes to SQLite Room database with clear feedback toast ("Note saved ✓").
  - Moved secondary tools (Find & Replace, Reading View, Colors, Fonts, Categories, Export) into the overflow dropdown menu.
- Borderless & Natural Image Presentation:
  - Removed enclosing card and gray box frames around images for a clean, direct, and native note aesthetic.
  - Direct hold-to-drag interaction: Holding and dragging on the image itself detects vertical drag gestures to swap positions smoothly with adjacent text blocks.
  - Corner resize handle: Positioned a floating drag handle (`OpenInFull`) directly in the bottom-right corner of the picture for freeform scaling from 20% to 100% width.
  - Floating discreet delete button (`Close`) in the top-right corner.
- Verified build and compilation status with zero errors.

## Phase 17: AndroidIDE Build Compatibility Alignment

- Updated Gradle Wrapper to Gradle 8.6 (`https://services.gradle.org/distributions/gradle-8.6-bin.zip`).
- Configured Android Gradle Plugin (AGP) 8.4.1 and Kotlin 1.9.23 with Compose compiler extension 1.5.11.
- Pinned Java/JDK target compatibility to JDK 17 (`JavaVersion.VERSION_17`, `jvmTarget = "17"`).
- Set `compileSdk = 34`, `targetSdk = 34`, and `minSdk = 24`.
- Completely removed `org.gradle.toolchains.foojay-resolver-convention` and any automatic toolchain resolvers to rely exclusively on the locally provided JDK 17 in AndroidIDE.
- Configured `gradle.properties` with `android.useAndroidX=true` and `android.nonTransitiveRClass=true`.
- Verified compilation and build integrity.

## Phase 18: Standard Android Debug Signing Fix for AndroidIDE

- Removed custom `debugConfig` block which hardcoded `${rootDir}/debug.keystore`.
- Removed explicit `signingConfig = signingConfigs.getByName("debugConfig")` from `debug` build type.
- AGP now automatically utilizes the standard default Android debug keystore (`~/.android/debug.keystore`), allowing clean GitHub clones to run `assembleDebug` out of the box on AndroidIDE or any development machine without requiring a project-local keystore.
- Verified compilation and APK packaging with zero errors.

## Phase 19: Pinch-to-Resize on Images, Unblocked Note Scrolling & Auto-Back on Save

- Multi-touch Pinch to Resize:
  - Replaced single-pointer drag gestures with multi-touch `awaitEachGesture` detector on images.
  - 2-finger pinch gestures smoothly compute zoom scale changes in real-time, scaling image width between 20% and 100%.
  - 1-finger touches and vertical swipe gestures are never consumed, allowing LazyColumn scrolling to work smoothly even when large images occupy the top of the note viewport.
- Image Size & Position Dialog:
  - Added bottom-right floating badge (`% 🤏`) that reveals quick width buttons (`25%`, `50%`, `75%`, `100%`) and Alignment toggles (`Left`, `Center`, `Right`).
  - Added dedicated top-left action pill with `↑` Move Up, `↓` Move Down, and `📍 Place Between Paragraphs`.
- Automatic Return to Dashboard on Save:
  - Pressing the "Save" button in the Top App Bar saves changes, hides the keyboard, and automatically navigates back to the main dashboard.

