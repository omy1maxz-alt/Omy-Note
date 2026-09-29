package com.example

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.local.AppSettings
import com.example.data.local.NoteRepository
import com.example.data.local.SettingsRepository
import com.example.data.model.BlockType
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.model.NoteBlock
import com.example.util.BackupManager
import com.example.util.PdfExporter
import com.example.util.ReminderReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.UUID

sealed class Screen {
    object Dashboard : Screen()
    object Calendar : Screen()
    data class Editor(val noteId: String? = null, val initialCategory: String? = null) : Screen()
    object Settings : Screen()
    object Achievements : Screen()
}

class MainViewModel(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository
) : ViewModel() {

    // Screen navigation
    private val _screenStack = mutableListOf<Screen>(Screen.Dashboard)
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Dashboard)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // App Lock
    private val _isAppUnlocked = MutableStateFlow(true)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    // Settings
    val settings: StateFlow<AppSettings> = settingsRepository.settingsFlow.stateIn(
        viewModelScope,
        SharingStarted.Eagerly,
        AppSettings()
    )

    // Notes and Categories
    val allNotes: StateFlow<List<Note>> = noteRepository.notes
    val categories: StateFlow<List<Category>> = noteRepository.categories

    // Dashboard State
    val searchQuery = MutableStateFlow("")
    val selectedCategory = MutableStateFlow<String?>(null) // null = All
    val isGridView = MutableStateFlow(true)
    val scopeTab = MutableStateFlow(0) // 0 = All, 1 = Notes
    val selectedDateFilter = MutableStateFlow<Long?>(null)
    val selectedNoteIds = MutableStateFlow<Set<String>>(emptySet())
    val isMultiSelectActive = MutableStateFlow(false)

    // Status snackbar / alert messages
    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // AI loading state
    private val _isAiLoading = MutableStateFlow(false)
    val isAiLoading: StateFlow<Boolean> = _isAiLoading.asStateFlow()

    private var smartSearchJob: Job? = null
    private val _rankedNoteIds = MutableStateFlow<List<String>?>(null)

    private data class FilterParams(
        val query: String,
        val categoryId: String?,
        val tab: Int,
        val rankedIds: List<String>?
    )

    private val filterParams = combine(
        searchQuery,
        selectedCategory,
        scopeTab,
        _rankedNoteIds
    ) { q, cat, tab, ranked ->
        FilterParams(q, cat, tab, ranked)
    }

    // Filtered and ranked notes list
    val filteredNotes: StateFlow<List<Note>> = combine(
        allNotes,
        filterParams,
        settings
    ) { notes, filter, appSettings ->
        var list = notes

        // Scope tab filter (e.g. pinned / notes)
        if (filter.tab == 1) {
            list = list.filter { it.blocks.any { b -> b.content.isNotBlank() } }
        }

        // Category filter
        if (!filter.categoryId.isNullOrBlank()) {
            list = list.filter { it.categoryId == filter.categoryId }
        }

        // Search query filtering & ranking
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            // 1. Local search ranking
            val scored = list.mapNotNull { note ->
                val title = note.title.lowercase()
                val body = note.blocks.joinToString(" ") { it.content }.lowercase()
                val tags = note.tags.joinToString(" ").lowercase()

                var score = 0
                if (title == q) score += 100
                else if (title.startsWith(q)) score += 60
                else if (title.contains(q)) score += 40

                if (body.contains(q)) score += 20
                if (tags.contains(q)) score += 15

                if (score > 0) Pair(note, score) else null
            }.sortedByDescending { it.second }.map { it.first }

            // If Gemini smart search ranked IDs are available, use them
            if (filter.rankedIds != null && filter.rankedIds.isNotEmpty()) {
                val idOrderMap = filter.rankedIds.withIndex().associate { it.value to it.index }
                list = scored.sortedBy { idOrderMap[it.id] ?: Int.MAX_VALUE }
            } else {
                list = scored
            }
        } else {
            // Standard sorting
            list = when (appSettings.sortOrder) {
                "custom" -> list
                "modified_asc" -> list.sortedBy { it.updatedAt }
                "created_desc" -> list.sortedByDescending { it.createdAt }
                "title_asc" -> list.sortedBy { it.title.lowercase() }
                else -> list.sortedByDescending { it.updatedAt }
            }
        }

        // Pinned notes always on top unless explicit search is active
        if (filter.query.isBlank()) {
            list = list.sortedByDescending { it.isPinned }
        }

        list
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            val initialSettings = settingsRepository.settingsFlow.first()
            if (initialSettings.appLockEnabled) {
                _isAppUnlocked.value = false
            }
        }
    }

    fun setAppUnlocked(unlocked: Boolean) {
        _isAppUnlocked.value = unlocked
    }

    fun navigateTo(screen: Screen) {
        _screenStack.add(screen)
        _currentScreen.value = screen
    }

    fun navigateBack(): Boolean {
        if (_screenStack.size > 1) {
            _screenStack.removeAt(_screenStack.lastIndex)
            _currentScreen.value = _screenStack.last()
            return true
        }
        return false
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }

    fun showMessage(msg: String) {
        _statusMessage.value = msg
    }

    // Search Query Change + Debounced Smart Search
    fun onSearchQueryChanged(newQuery: String) {
        searchQuery.value = newQuery
        _rankedNoteIds.value = null

        smartSearchJob?.cancel()
        if (newQuery.isNotBlank() && settings.value.geminiEnabled && settings.value.smartSearchEnabled) {
            smartSearchJob = viewModelScope.launch {
                delay(800) // Debounce
                val candidates = allNotes.value.filter { note ->
                    val q = newQuery.lowercase()
                    note.title.lowercase().contains(q) ||
                            note.blocks.any { it.content.lowercase().contains(q) }
                }.take(10)

                if (candidates.size > 1) {
                    val result = aiRepository.reRankCandidates(newQuery, candidates)
                    result.onSuccess { ranked ->
                        _rankedNoteIds.value = ranked
                    }
                }
            }
        }
    }

    // Multi-select actions
    fun toggleNoteSelection(noteId: String) {
        val current = selectedNoteIds.value.toMutableSet()
        if (current.contains(noteId)) {
            current.remove(noteId)
        } else {
            current.add(noteId)
        }
        selectedNoteIds.value = current
        isMultiSelectActive.value = current.isNotEmpty()
    }

    fun clearSelection() {
        selectedNoteIds.value = emptySet()
        isMultiSelectActive.value = false
    }

    fun deleteSelectedNotes() {
        viewModelScope.launch {
            val toDelete = selectedNoteIds.value
            for (id in toDelete) {
                noteRepository.deleteNote(id)
            }
            clearSelection()
            showMessage("Deleted ${toDelete.size} note(s)")
        }
    }

    // Single Note CRUD
    fun saveNote(note: Note, commitHistory: Boolean = false) {
        viewModelScope.launch {
            noteRepository.saveNote(note, commitHistory)
            if (commitHistory) {
                showMessage("Note saved")
            }
        }
    }

    fun deleteNote(noteId: String) {
        viewModelScope.launch {
            noteRepository.deleteNote(noteId)
            showMessage("Note deleted")
        }
    }

    fun duplicateNote(note: Note) {
        viewModelScope.launch {
            noteRepository.duplicateNote(note)
            showMessage("Note duplicated")
        }
    }

    fun moveNotePosition(noteId: String, delta: Int) {
        viewModelScope.launch {
            if (settings.value.sortOrder != "custom") {
                settingsRepository.updateSortOrder("custom")
            }
            noteRepository.moveNote(noteId, delta)
            showMessage("Note position updated")
        }
    }

    fun reorderNotes(fromIndex: Int, toIndex: Int) {
        viewModelScope.launch {
            if (settings.value.sortOrder != "custom") {
                settingsRepository.updateSortOrder("custom")
            }
            noteRepository.reorderNotes(fromIndex, toIndex)
        }
    }

    fun splitNoteByCharacters(noteId: String, splitCharCount: Int) {
        viewModelScope.launch {
            val result = noteRepository.splitNoteByCharCount(noteId, splitCharCount)
            if (result != null) {
                val (orig, created) = result
                val firstCount = orig.blocks.sumOf { it.content.length }
                val secondCount = created.blocks.sumOf { it.content.length }
                showMessage("Note split! Kept $firstCount chars, created new note with $secondCount chars.")
            } else {
                showMessage("Could not split note: invalid character position.")
            }
        }
    }

    fun togglePinNote(note: Note) {
        viewModelScope.launch {
            noteRepository.saveNote(note.copy(isPinned = !note.isPinned))
        }
    }

    fun addCategory(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                val cat = noteRepository.addCategory(name)
                selectedCategory.value = cat.id
                showMessage("Category added: ${cat.name}")
            }
        }
    }

    fun deleteCategory(categoryId: String) {
        viewModelScope.launch {
            noteRepository.deleteCategory(categoryId)
            if (selectedCategory.value == categoryId) {
                selectedCategory.value = null
            }
            showMessage("Category deleted")
        }
    }

    // Import Image from SAF URI into internal media directory
    suspend fun importImage(uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val fileName = "img_${UUID.randomUUID()}.jpg"
            val destFile = File(noteRepository.mediaDir, fileName)

            context.contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(destFile).use { output ->
                    input.copyTo(output)
                }
            }
            "media/$fileName"
        } catch (e: Exception) {
            null
        }
    }

    // Reminders
    fun scheduleReminder(note: Note, epochMillis: Long) {
        viewModelScope.launch {
            val updated = note.copy(
                reminderEnabled = true,
                reminderAtEpochMillis = epochMillis
            )
            noteRepository.saveNote(updated)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java).apply {
                putExtra(ReminderReceiver.EXTRA_NOTE_ID, note.id)
                putExtra(ReminderReceiver.EXTRA_NOTE_TITLE, note.title.ifBlank { "Note Reminder" })
                putExtra(ReminderReceiver.EXTRA_NOTE_CONTENT, note.blocks.firstOrNull()?.content ?: "")
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                note.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
                } else {
                    alarmManager.set(AlarmManager.RTC_WAKEUP, epochMillis, pendingIntent)
                }
            }
            showMessage("Reminder set")
        }
    }

    fun cancelReminder(note: Note) {
        viewModelScope.launch {
            val updated = note.copy(
                reminderEnabled = false,
                reminderAtEpochMillis = null
            )
            noteRepository.saveNote(updated)

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
            val intent = Intent(context, ReminderReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                note.id.hashCode(),
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager?.cancel(pendingIntent)
            showMessage("Reminder cancelled")
        }
    }

    // PDF Export
    fun exportNoteToPdf(note: Note, outputStream: OutputStream) {
        viewModelScope.launch {
            val catName = categories.value.find { it.id == note.categoryId }?.name
            val result = PdfExporter.exportNoteToPdf(context, note, catName, outputStream)
            result.onSuccess {
                showMessage("PDF exported successfully")
            }.onFailure {
                showMessage("PDF export failed: ${it.localizedMessage}")
            }
        }
    }

    // Backup & Restore
    fun exportBackup(outputStream: OutputStream) {
        viewModelScope.launch {
            val result = BackupManager.createBackupZip(context, noteRepository, outputStream)
            result.onSuccess {
                showMessage("Backup created ($it items archived)")
            }.onFailure {
                showMessage("Backup failed: ${it.localizedMessage}")
            }
        }
    }

    fun restoreBackup(inputStream: InputStream, isReplace: Boolean) {
        viewModelScope.launch {
            val result = BackupManager.restoreBackupZip(context, noteRepository, inputStream, isReplace)
            result.onSuccess {
                showMessage("Restore completed ($it notes restored)")
            }.onFailure {
                showMessage("Restore failed: ${it.localizedMessage}")
            }
        }
    }

    // AI Operations
    fun executeAiInstruction(
        instruction: String,
        text: String,
        onResult: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.executeNaturalLanguageInstruction(instruction, text)
            _isAiLoading.value = false
            result.onSuccess { transformed ->
                onResult(transformed)
            }.onFailure { err ->
                val errorMsg = err.localizedMessage ?: "AI processing failed"
                onError(errorMsg)
            }
        }
    }

    fun summarizeNote(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.summarize(text)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("AI Summarize: ${it.message}") }
        }
    }

    fun rewriteNote(text: String, style: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.rewrite(text, style)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("AI Rewrite: ${it.message}") }
        }
    }

    fun fixGrammar(text: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.fixGrammar(text)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("Grammar check: ${it.message}") }
        }
    }

    fun translateNote(text: String, targetLanguage: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.translate(text, targetLanguage)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("Translation: ${it.message}") }
        }
    }

    fun autoTagNote(title: String, content: String, onResult: (List<String>) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val result = aiRepository.autoTag(title, content)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("Auto-tagging: ${it.message}") }
        }
    }

    fun autoCategorizeNote(title: String, content: String, onResult: (String) -> Unit) {
        viewModelScope.launch {
            _isAiLoading.value = true
            val catNames = categories.value.map { it.name }
            val result = aiRepository.autoCategorize(title, content, catNames)
            _isAiLoading.value = false
            result.onSuccess { onResult(it) }
                .onFailure { showMessage("Auto-categorize: ${it.message}") }
        }
    }

    // Settings modifiers
    fun updateTheme(theme: String) = viewModelScope.launch { settingsRepository.updateTheme(theme) }
    fun updateFont(font: String) = viewModelScope.launch { settingsRepository.updateFont(font) }
    fun updateDefaultTint(tint: String) = viewModelScope.launch { settingsRepository.updateDefaultTint(tint) }
    fun updateSortOrder(sort: String) = viewModelScope.launch { settingsRepository.updateSortOrder(sort) }
    fun updateDateFormat(format: String) = viewModelScope.launch { settingsRepository.updateDateFormat(format) }
    fun updateDisplayUpdatedDate(show: Boolean) = viewModelScope.launch { settingsRepository.updateDisplayUpdatedDate(show) }
    fun updateStartDayOfWeek(day: String) = viewModelScope.launch { settingsRepository.updateStartDayOfWeek(day) }
    fun updateGeminiEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateGeminiEnabled(enabled) }
    fun updateGeminiApiKey(key: String) = viewModelScope.launch { settingsRepository.updateGeminiApiKey(key) }
    fun updateGeminiModel(model: String) = viewModelScope.launch { settingsRepository.updateGeminiModel(model) }
    fun updateAutoTagAndCategorize(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAutoTagAndCategorize(enabled) }
    fun updateSmartSearchEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateSmartSearchEnabled(enabled) }
    fun updateClipboardDetection(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateClipboardDetection(enabled) }
    fun updateAppLockEnabled(enabled: Boolean) = viewModelScope.launch { settingsRepository.updateAppLockEnabled(enabled) }
    fun updateSkipSplash(skip: Boolean) = viewModelScope.launch { settingsRepository.updateSkipSplash(skip) }
    fun updatePremiumDemo(demo: Boolean) = viewModelScope.launch { settingsRepository.updatePremiumDemo(demo) }
}

class MainViewModelFactory(
    private val context: Context,
    private val noteRepository: NoteRepository,
    private val settingsRepository: SettingsRepository,
    private val aiRepository: AiRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(context, noteRepository, settingsRepository, aiRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
