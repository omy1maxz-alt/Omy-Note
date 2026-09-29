# Changelog

All notable changes to the AI Notes application are documented in this file.

## [1.0.0] - 2026-09-28

### Added
- Complete offline-first note taking engine using atomic JSON persistence.
- Rich blocks architecture: Plain text, checklists with toggles, bullet lists, inline media images, and handwriting vector strokes.
- Fluid handwriting and sketching canvas with custom brush widths, palettes, and undo/clear controls.
- Speech recognition dictation using Android's native SpeechRecognizer.
- Distraction-free Focus Mode with dark teal canvas, serif typography, and minimal toolbar.
- Multi-page PDF export using standard Android PdfDocument via Storage Access Framework.
- Safe backup and restore engine packing notes and media into a ZIP file with path traversal defense.
- Text statistics calculation: characters, words, sentences, and paragraphs.
- Local search with exact/prefix ranking, category filtering, and sorting modes.
- Optional Google Gemini AI integration over direct REST: Summarize, Rewrite, Grammar Fix, Translation, Auto-tagging, Auto-categorization, and Smart Search re-ranking.
- Masked API key storage in local DataStore Preferences with zero logging.
- System device credential app lock protection.
- Productivity achievements screen with local progress tracking.
- Custom adaptive Material You app icon with dark teal gradient and notebook motif.

## [1.1.0] - 2026-09-28

### Added
- Dashboard notes draggable on hold to reorder and adjust custom position with real-time translation and lift elevation.
- One-tap Up/Down reorder adjustment buttons on each note card header.
- Dedicated Split Box Button on note cards with complete click isolation from editor navigation.
- Character-based Note Split Option Box: separate text based on exact character count (e.g. 100 chars in original note, 150 chars into new note) with live preview and category/tint preservation.

## [1.2.0] - 2026-09-28

### Added
- Custom expressive AI emoticon badge (`ExpressiveAiIcon`) with smart animated/canvas smiling face, gradient visor, and star sparkle, distinct from standard emoji buttons.
- Natural-language AI writing assistant dialog accepting arbitrary user instructions (e.g. "correct my writing", "make this sound more natural", "translate into Indonesian", "turn this into lyrics").
- Automatic context text targeting: defaults to highlighted/selected text when active, or entire note content, with explicit UI target chips and snippet preview.
- Non-destructive AI Result Preview dialog with side-by-side review, Replace Selection / Apply to Note, Insert Below, Copy to Clipboard, and snapshot Undo integration.
- Graceful offline/disabled dialog when Gemini AI master switch or API key is not configured, preserving normal note editing and zero text loss.

## [1.3.0] - 2026-09-28

### Added
- Rectangular "Formatted View" toggle button in the note editor toolbar positioned between Redo and Search (`Undo → Redo → Formatted View → Search → other tools`).
- Clear visual state for the toggle button with active container highlight, border, and dynamic Edit/View label.
- Clean, read-only formatted reading mode preserving actual note content and formatting in memory.
- Rich Markdown and block rendering: headings (`#`, `##`, `###`), bold (`**`/`__`), italic (`*`/`_`), strikethrough (`~~`), inline code (``` ` ```), multiline code blocks (```` ``` ````), blockquotes (`>`), dividers (`---`), numbered lists (`1.`), bullet lists (`-`/`*`/`•`), checklists, images, and handwriting strokes.
- Seamless switching between edit and reading view without losing cursor position, selection, or unsaved changes.
- Safe fallback rendering for unsupported block types preventing content loss or UI crashes.

## [1.4.0] - 2026-09-28

### Added
- Zero-lag large text file engine capable of smoothly editing and previewing massive notes without UI frame drops.
- Automatic text virtualization: auto-chunks large text blocks (>6,000 characters) into granular blocks for smooth `LazyColumn` recycling.
- Debounced undo/redo snapshots (1.5s windowing) avoiding full array copies on every single keystroke.
- Zero-allocation character and word count calculations using `derivedStateOf` and fast single-pass `TextStats`.
- Bounded dashboard card previews (120 chars) and optimized history snapshots without full-text string joins.

## [1.5.0] - 2026-09-28

### Added
- Enhanced in-note Search bar with Find Previous (`↑`) and Find Next (`↓`) navigation buttons.
- Real-time match counter badge (`1/5` matches) with auto-scroll and highlight jumping to matched blocks.
- Granular Replace options: "Replace" (replaces the current active match) and "All" (replaces all occurrences note-wide).
- Clear action buttons for quick text reset in find and replace fields.
- Full snapshot integration with Undo / Redo history for all replace operations.

## [1.6.0] - 2026-09-28

### Added
- Phone / Emulator Keyboard enhancements: Tap-to-write on any empty note canvas area with instant software keyboard popup and sentence capitalization.
- Built-in Sample Image Preset Library with 5 instant vector & canvas artwork photos (Alpine Sunset Lake, Cozy Workspace Desk, Creative Mind Map, Vintage Polaroid, and Analytics Chart).
- Freely draggable image block positioning with `⠿ Drag to Move` vertical drag handle and instant Up (`↑`) / Down (`↓`) repositioning buttons.
- Real-time text re-flow: Surrounding text blocks dynamically adjust and flow around relocated images.
- Image caption support: Note text and captions stay linked to the image and travel together when moved.

## [1.7.0] - 2026-09-28

### Added
- Window-style interactive image resizing:
  - Drag-corner handle (↘️ `OpenInFull`) to smoothly scale image width from 25% to 100% dynamically.
  - Quick window size presets (25%, 50%, 75%, 100%) and Alignment controls (Left, Center, Right).
- Precise "Place Image Between Text" system:
  - Real-time visual landing drop marker (`📍 Placing image between next text...`) during drag gestures.
  - "Place Between Paragraphs..." dialog (`📍`) to instantly teleport an image between any specific paragraphs.
  - Cursor-aware insertion: Inserting an image while editing text automatically splits the paragraph and places the image directly between the top and bottom halves.

## [1.9.0] - 2026-09-28

### Changed
- Complete Build Configuration Alignment for AndroidIDE & On-Device Compilation:
  - Gradle Wrapper configured to official Gradle 8.6 distribution (`gradle-8.6-bin.zip`).
  - Android Gradle Plugin (AGP) pinned to `8.4.1`.
  - Kotlin pinned to `1.9.23` with Compose compiler extension `1.5.11`.
  - Java/JDK target compatibility set to JDK 17 (`JavaVersion.VERSION_17`, `jvmTarget = "17"`).
  - SDK parameters set to `compileSdk 34`, `targetSdk 34`, and `minSdk 24`.
  - Fully removed the `org.gradle.toolchains.foojay-resolver-convention` plugin and all automatic toolchain provisioning.
  - Enabled `android.useAndroidX=true` and `android.nonTransitiveRClass=true` in `gradle.properties`.
  - Verified clean compilation with zero warnings or errors.
