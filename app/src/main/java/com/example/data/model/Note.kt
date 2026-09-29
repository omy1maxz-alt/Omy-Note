package com.example.data.model

import java.util.UUID

data class PointData(
    val x: Float = 0f,
    val y: Float = 0f
)

data class HandwritingStroke(
    val color: Long = 0xFF000000,
    val width: Float = 4f,
    val points: List<PointData> = emptyList()
)

enum class BlockType {
    TEXT,
    CHECKLIST,
    BULLET,
    IMAGE,
    HANDWRITING
}

data class NoteBlock(
    val id: String = UUID.randomUUID().toString(),
    val type: BlockType = BlockType.TEXT,
    val content: String = "",
    val checked: Boolean = false,
    val imagePath: String? = null,
    val imageAlignment: String = "center", // "left", "center", "right", "full"
    val imageScale: Float = 1.0f,
    val strokes: List<HandwritingStroke> = emptyList()
)

data class NoteHistoryVersion(
    val timestamp: Long = System.currentTimeMillis(),
    val title: String = "",
    val summaryText: String = "",
    val blocksJson: String? = null
)

data class Note(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val blocks: List<NoteBlock> = listOf(NoteBlock()),
    val categoryId: String? = null,
    val tags: List<String> = emptyList(),
    val tint: String = "cream",
    val font: String = "sans",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
    val reminderEnabled: Boolean = false,
    val reminderAtEpochMillis: Long? = null,
    val isPinned: Boolean = false,
    val history: List<NoteHistoryVersion> = emptyList()
)

data class Category(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val icon: String = "folder"
)

data class NotesDataPayload(
    val notes: List<Note> = emptyList(),
    val categories: List<Category> = emptyList(),
    val exportedAt: Long = System.currentTimeMillis()
)
