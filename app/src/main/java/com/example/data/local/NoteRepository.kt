package com.example.data.local

import android.content.Context
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.data.model.NoteBlock
import com.example.data.model.NoteHistoryVersion
import com.example.data.model.NotesDataPayload
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets
import java.util.UUID

class NoteRepository(
    private val context: Context,
    private val scope: CoroutineScope
) {
    private val mutex = Mutex()
    private val notesDir = File(context.filesDir, "notes").apply { mkdirs() }
    val mediaDir: File = File(context.filesDir, "media").apply { mkdirs() }
    private val notesFile = File(notesDir, "notes.json")
    private val tempFile = File(notesDir, "notes.json.tmp")

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()
    private val adapter = moshi.adapter(NotesDataPayload::class.java)

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes.asStateFlow()

    private val _categories = MutableStateFlow<List<Category>>(defaultCategories())
    val categories: StateFlow<List<Category>> = _categories.asStateFlow()

    init {
        scope.launch(Dispatchers.IO) {
            loadFromDisk()
        }
    }

    private fun defaultCategories(): List<Category> = listOf(
        Category(id = "cat_home", name = "Home", icon = "home"),
        Category(id = "cat_work", name = "Work", icon = "work"),
        Category(id = "cat_ideas", name = "Ideas", icon = "lightbulb"),
        Category(id = "cat_personal", name = "Personal", icon = "person")
    )

    private suspend fun loadFromDisk() {
        mutex.withLock {
            try {
                val targetFile = if (notesFile.exists() && notesFile.length() > 0) {
                    notesFile
                } else if (tempFile.exists() && tempFile.length() > 0) {
                    tempFile
                } else null

                if (targetFile != null) {
                    val json = targetFile.readText(StandardCharsets.UTF_8)
                    val payload = adapter.fromJson(json)
                    if (payload != null) {
                        _notes.value = payload.notes
                        if (payload.categories.isNotEmpty()) {
                            _categories.value = payload.categories
                        }
                        return
                    }
                }
            } catch (e: Exception) {
                // If parsing fails, fall back to initial sample or empty
            }

            // Create initial welcome note if empty
            if (_notes.value.isEmpty()) {
                val welcome = Note(
                    id = UUID.randomUUID().toString(),
                    title = "Welcome to AI Notes ✍️",
                    blocks = listOf(
                        NoteBlock(
                            content = "AI Notes is an offline-first, private notebook with rich blocks, handwriting, and optional Google Gemini AI assistance."
                        ),
                        NoteBlock(
                            content = "• All notes are stored locally on your device in secure JSON format.\n• Tap the '+' button to write a new note.\n• Switch to List mode to see word & character statistics.\n• Export to PDF or backup to ZIP anytime from Settings."
                        )
                    ),
                    categoryId = "cat_personal",
                    tags = listOf("welcome", "guide"),
                    tint = "cream"
                )
                _notes.value = listOf(welcome)
                persistLocked()
            }
        }
    }

    suspend fun saveNote(note: Note, commitHistory: Boolean = false) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val currentList = _notes.value.toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == note.id }

            val updatedHistory = if (commitHistory) {
                val textPreview = buildString {
                    for (b in note.blocks) {
                        if (b.content.isNotBlank()) {
                            val needed = 80 - length
                            if (needed <= 0) break
                            append(b.content.take(needed)).append(' ')
                        }
                    }
                }.trim()
                val newVersion = NoteHistoryVersion(
                    timestamp = System.currentTimeMillis(),
                    title = note.title,
                    summaryText = textPreview.ifBlank { "Snapshot" }
                )
                (listOf(newVersion) + note.history).take(20)
            } else {
                note.history
            }

            val finalNote = note.copy(
                updatedAt = System.currentTimeMillis(),
                history = updatedHistory
            )

            if (existingIndex >= 0) {
                currentList[existingIndex] = finalNote
            } else {
                currentList.add(0, finalNote)
            }
            _notes.value = currentList
            persistLocked()
        }
    }

    suspend fun deleteNote(noteId: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            _notes.value = _notes.value.filterNot { it.id == noteId }
            persistLocked()
        }
    }

    suspend fun duplicateNote(note: Note): Note = withContext(Dispatchers.IO) {
        mutex.withLock {
            val duplicated = note.copy(
                id = UUID.randomUUID().toString(),
                title = if (note.title.isNotBlank()) "${note.title} (Copy)" else "Copy",
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis(),
                history = emptyList()
            )
            val currentList = _notes.value.toMutableList()
            currentList.add(0, duplicated)
            _notes.value = currentList
            persistLocked()
            duplicated
        }
    }

    suspend fun addCategory(name: String): Category = withContext(Dispatchers.IO) {
        mutex.withLock {
            val newCat = Category(
                id = UUID.randomUUID().toString(),
                name = name.trim(),
                icon = "label"
            )
            _categories.value = _categories.value + newCat
            persistLocked()
            newCat
        }
    }

    suspend fun deleteCategory(categoryId: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            _categories.value = _categories.value.filterNot { it.id == categoryId }
            // Reset categoryId on notes that had this category
            _notes.value = _notes.value.map {
                if (it.categoryId == categoryId) it.copy(categoryId = null) else it
            }
            persistLocked()
        }
    }

    suspend fun replaceData(newNotes: List<Note>, newCategories: List<Category>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            _notes.value = newNotes
            if (newCategories.isNotEmpty()) {
                _categories.value = newCategories
            }
            persistLocked()
        }
    }

    suspend fun mergeData(incomingNotes: List<Note>, incomingCategories: List<Category>) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val existingIds = _notes.value.map { it.id }.toSet()
            val mergedNotes = _notes.value + incomingNotes.filterNot { it.id in existingIds }

            val existingCatNames = _categories.value.map { it.name.lowercase() }.toSet()
            val mergedCategories = _categories.value + incomingCategories.filterNot { it.name.lowercase() in existingCatNames }

            _notes.value = mergedNotes
            _categories.value = mergedCategories
            persistLocked()
        }
    }

    suspend fun reorderNotes(fromIndex: Int, toIndex: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _notes.value.toMutableList()
            if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
                val item = current.removeAt(fromIndex)
                current.add(toIndex, item)
                _notes.value = current
                persistLocked()
            }
        }
    }

    suspend fun moveNote(noteId: String, delta: Int) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _notes.value.toMutableList()
            val index = current.indexOfFirst { it.id == noteId }
            if (index >= 0) {
                val targetIndex = (index + delta).coerceIn(0, current.lastIndex)
                if (index != targetIndex) {
                    val item = current.removeAt(index)
                    current.add(targetIndex, item)
                    _notes.value = current
                    persistLocked()
                }
            }
        }
    }

    suspend fun splitNoteByCharCount(noteId: String, splitCharCount: Int): Pair<Note, Note>? = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = _notes.value.toMutableList()
            val index = current.indexOfFirst { it.id == noteId }
            if (index < 0) return@withContext null

            val originalNote = current[index]
            val allText = if (originalNote.blocks.isNotEmpty()) {
                val joined = originalNote.blocks.joinToString("\n") { it.content }
                if (joined.isNotBlank()) joined else originalNote.title
            } else {
                originalNote.title
            }

            if (splitCharCount <= 0 || splitCharCount >= allText.length) {
                return@withContext null
            }

            val keepText = allText.take(splitCharCount)
            val splitOffText = allText.drop(splitCharCount)

            val updatedOriginal = originalNote.copy(
                blocks = listOf(NoteBlock(type = com.example.data.model.BlockType.TEXT, content = keepText)),
                updatedAt = System.currentTimeMillis()
            )

            val newTitle = if (originalNote.title.isNotBlank()) "${originalNote.title} (Part 2)" else "Note (Part 2)"
            val newNote = Note(
                id = UUID.randomUUID().toString(),
                title = newTitle,
                blocks = listOf(NoteBlock(type = com.example.data.model.BlockType.TEXT, content = splitOffText)),
                categoryId = originalNote.categoryId,
                tags = originalNote.tags,
                tint = originalNote.tint,
                font = originalNote.font,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )

            current[index] = updatedOriginal
            current.add(index + 1, newNote)
            _notes.value = current
            persistLocked()
            Pair(updatedOriginal, newNote)
        }
    }

    fun getNotesJsonString(): String {
        val payload = NotesDataPayload(
            notes = _notes.value,
            categories = _categories.value
        )
        return adapter.toJson(payload)
    }

    private fun persistLocked() {
        try {
            val payload = NotesDataPayload(
                notes = _notes.value,
                categories = _categories.value
            )
            val json = adapter.toJson(payload)

            // Atomic persistence: write to tempFile first
            FileOutputStream(tempFile).use { fos ->
                OutputStreamWriter(fos, StandardCharsets.UTF_8).use { writer ->
                    writer.write(json)
                    writer.flush()
                }
                fos.fd.sync()
            }

            // Safely rename temp to primary
            if (tempFile.exists()) {
                if (notesFile.exists()) {
                    notesFile.delete()
                }
                tempFile.renameTo(notesFile)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
