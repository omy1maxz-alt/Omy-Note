package com.example.ui.editor

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material.icons.filled.FormatAlignRight
import androidx.compose.material.icons.filled.FormatListBulleted
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.VerticalAlignBottom
import androidx.compose.material.icons.filled.VerticalAlignTop
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.example.util.SampleImageGenerator
import com.example.util.SampleImagePreset
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.withStyle
import com.example.MainViewModel
import com.example.data.model.BlockType
import com.example.data.model.Category
import com.example.data.model.HandwritingStroke
import com.example.data.model.Note
import com.example.data.model.NoteBlock
import com.example.ui.components.EmojiDialog
import com.example.ui.components.HandwritingBlockView
import com.example.ui.components.NoteTintRow
import com.example.ui.theme.FocusModeBg
import com.example.ui.theme.getFontFamily
import com.example.ui.theme.getNoteColor
import com.example.util.BitmapLoader
import com.example.util.DateFormats
import com.example.util.TextStats
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Locale
import java.util.UUID

@Composable
fun ExpressiveAiIcon(
    modifier: Modifier = Modifier,
    size: Dp = 26.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(size * 0.32f))
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF673AB7), // Deep Purple
                        Color(0xFF3F51B5), // Indigo
                        Color(0xFF00BCD4)  // Vibrant Cyan
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size * 0.76f)) {
            val w = this.size.width
            val h = this.size.height

            // Star Sparkle ✦ in top-right corner
            val sparkleX = w * 0.84f
            val sparkleY = h * 0.16f
            val sRadius = w * 0.12f
            val sparklePath = Path().apply {
                moveTo(sparkleX, sparkleY - sRadius)
                quadraticBezierTo(sparkleX, sparkleY, sparkleX + sRadius, sparkleY)
                quadraticBezierTo(sparkleX, sparkleY, sparkleX, sparkleY + sRadius)
                quadraticBezierTo(sparkleX, sparkleY, sparkleX - sRadius, sparkleY)
                quadraticBezierTo(sparkleX, sparkleY, sparkleX, sparkleY - sRadius)
            }
            drawPath(sparklePath, color = Color(0xFFFFD54F))

            // AI Face Screen visor
            val faceLeft = w * 0.12f
            val faceTop = h * 0.28f
            val faceWidth = w * 0.72f
            val faceHeight = h * 0.58f
            val cornerRadius = CornerRadius(faceWidth * 0.26f, faceWidth * 0.26f)

            drawRoundRect(
                color = Color.White.copy(alpha = 0.25f),
                topLeft = Offset(faceLeft, faceTop),
                size = Size(faceWidth, faceHeight),
                cornerRadius = cornerRadius
            )

            // Expressive Happy / Smart Eyes (cute curved arches `^ ^`)
            val strokeWidth = w * 0.09f
            val leftEyeX = w * 0.33f
            val rightEyeX = w * 0.63f
            val eyeY = h * 0.51f
            val eyeWidth = w * 0.12f

            // Left eye curve ^
            val leftEyePath = Path().apply {
                moveTo(leftEyeX - eyeWidth / 2, eyeY + strokeWidth * 0.35f)
                quadraticBezierTo(leftEyeX, eyeY - eyeWidth * 0.52f, leftEyeX + eyeWidth / 2, eyeY + strokeWidth * 0.35f)
            }
            drawPath(
                leftEyePath,
                color = Color.White,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Right eye curve ^
            val rightEyePath = Path().apply {
                moveTo(rightEyeX - eyeWidth / 2, eyeY + strokeWidth * 0.35f)
                quadraticBezierTo(rightEyeX, eyeY - eyeWidth * 0.52f, rightEyeX + eyeWidth / 2, eyeY + strokeWidth * 0.35f)
            }
            drawPath(
                rightEyePath,
                color = Color.White,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )

            // Expressive smiling mouth `‿`
            val mouthCenterX = w * 0.48f
            val mouthY = h * 0.69f
            val mouthWidth = w * 0.18f
            val mouthPath = Path().apply {
                moveTo(mouthCenterX - mouthWidth / 2, mouthY)
                quadraticBezierTo(mouthCenterX, mouthY + mouthWidth * 0.5f, mouthCenterX + mouthWidth / 2, mouthY)
            }
            drawPath(
                mouthPath,
                color = Color(0xFFFFD54F),
                style = Stroke(width = strokeWidth * 0.85f, cap = StrokeCap.Round)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteEditorScreen(
    noteId: String?,
    initialCategory: String?,
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val allNotes by viewModel.allNotes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()

    // Find existing note or prepare new note
    val existingNote = remember(noteId, allNotes) {
        if (!noteId.isNullOrBlank()) allNotes.find { it.id == noteId } else null
    }

    // Helper to chunk large text into virtualized blocks for zero-lag rendering
    fun chunkLargeTextToBlocks(text: String, targetChunkSize: Int = 4000): List<NoteBlock> {
        if (text.length <= targetChunkSize) {
            return listOf(NoteBlock(type = BlockType.TEXT, content = text))
        }

        val result = mutableListOf<NoteBlock>()
        var currentIndex = 0
        val totalLen = text.length

        while (currentIndex < totalLen) {
            val remaining = totalLen - currentIndex
            if (remaining <= targetChunkSize) {
                result.add(NoteBlock(type = BlockType.TEXT, content = text.substring(currentIndex)))
                break
            }

            val searchLimit = (currentIndex + targetChunkSize + 1000).coerceAtMost(totalLen)
            val chunkSub = text.substring(currentIndex, searchLimit)

            var splitOffset = chunkSub.lastIndexOf("\n\n", targetChunkSize)
            if (splitOffset == -1 || splitOffset < targetChunkSize / 2) {
                splitOffset = chunkSub.lastIndexOf("\n", targetChunkSize)
            }
            if (splitOffset == -1 || splitOffset < targetChunkSize / 2) {
                splitOffset = chunkSub.lastIndexOf(" ", targetChunkSize)
            }
            if (splitOffset == -1) {
                splitOffset = targetChunkSize
            } else {
                if (splitOffset < chunkSub.length && chunkSub[splitOffset] == '\n') {
                    splitOffset++
                }
            }

            val chunkContent = text.substring(currentIndex, currentIndex + splitOffset)
            if (chunkContent.isNotEmpty()) {
                result.add(NoteBlock(type = BlockType.TEXT, content = chunkContent))
            }
            currentIndex += splitOffset
        }

        return if (result.isNotEmpty()) result else listOf(NoteBlock(type = BlockType.TEXT, content = ""))
    }

    var title by remember { mutableStateOf(existingNote?.title ?: "") }
    var tint by remember { mutableStateOf(existingNote?.tint ?: settings.defaultTint) }
    var font by remember { mutableStateOf(existingNote?.font ?: settings.font) }
    var categoryId by remember { mutableStateOf(existingNote?.categoryId ?: initialCategory) }
    var tags by remember { mutableStateOf(existingNote?.tags ?: emptyList()) }
    var isPinned by remember { mutableStateOf(existingNote?.isPinned ?: false) }
    val blocks = remember {
        mutableStateListOf<NoteBlock>().apply {
            if (existingNote != null && existingNote.blocks.isNotEmpty()) {
                // If any text block is large (>6,000 chars), automatically chunk for 120 FPS LazyColumn virtualization
                existingNote.blocks.forEach { b ->
                    if (b.type == BlockType.TEXT && b.content.length > 6000) {
                        addAll(chunkLargeTextToBlocks(b.content))
                    } else {
                        add(b)
                    }
                }
            } else {
                add(NoteBlock(type = BlockType.TEXT, content = ""))
            }
        }
    }

    var isDirty by remember { mutableStateOf(false) }
    var isFocusMode by remember { mutableStateOf(false) }
    var isFormattedView by remember { mutableStateOf(false) }

    // Undo / Redo Stacks (snapshots of title + blocks)
    data class EditorSnapshot(val title: String, val blocks: List<NoteBlock>)
    val undoStack = remember { mutableStateListOf<EditorSnapshot>() }
    val redoStack = remember { mutableStateListOf<EditorSnapshot>() }
    var lastSnapshotTime by remember { mutableLongStateOf(0L) }

    fun captureSnapshot(force: Boolean = false) {
        val now = System.currentTimeMillis()
        if (force || now - lastSnapshotTime > 1500L || undoStack.isEmpty()) {
            undoStack.add(EditorSnapshot(title, blocks.map { it.copy() }))
            if (undoStack.size > 20) undoStack.removeAt(0)
            redoStack.clear()
            lastSnapshotTime = now
        }
        isDirty = true
    }

    fun undo() {
        if (undoStack.isNotEmpty()) {
            val last = undoStack.removeAt(undoStack.lastIndex)
            redoStack.add(EditorSnapshot(title, blocks.map { it.copy() }))
            title = last.title
            blocks.clear()
            blocks.addAll(last.blocks)
        }
    }

    fun redo() {
        if (redoStack.isNotEmpty()) {
            val next = redoStack.removeAt(redoStack.lastIndex)
            undoStack.add(EditorSnapshot(title, blocks.map { it.copy() }))
            title = next.title
            blocks.clear()
            blocks.addAll(next.blocks)
        }
    }

    // Live character count (zero string allocations via derivedStateOf)
    val totalCharCount by remember {
        derivedStateOf { title.length + blocks.sumOf { it.content.length } }
    }

    // Selection tracking across blocks
    var activeSelectedText by remember { mutableStateOf("") }
    var activeSelectedBlockIndex by remember { mutableStateOf(-1) }
    var activeSelectionRange by remember { mutableStateOf<TextRange?>(null) }

    // In-note find and replace
    var showFindReplace by remember { mutableStateOf(false) }
    var findQuery by remember { mutableStateOf("") }
    var replaceWith by remember { mutableStateOf("") }
    var currentMatchIndex by remember { mutableIntStateOf(0) }
    val lazyListState = rememberLazyListState()

    data class NoteSearchMatch(
        val blockIndex: Int, // -1 for title, 0..N for blocks
        val startIndex: Int,
        val endIndex: Int
    )

    val searchMatches by remember(findQuery, title, blocks) {
        derivedStateOf {
            if (findQuery.isBlank()) {
                emptyList<NoteSearchMatch>()
            } else {
                val list = mutableListOf<NoteSearchMatch>()
                // Search in title
                var idx = 0
                while (idx < title.length) {
                    val found = title.indexOf(findQuery, idx, ignoreCase = true)
                    if (found == -1) break
                    list.add(NoteSearchMatch(blockIndex = -1, startIndex = found, endIndex = found + findQuery.length))
                    idx = found + findQuery.length.coerceAtLeast(1)
                }
                // Search in text/checklist/bullet blocks
                blocks.forEachIndexed { bIndex, block ->
                    if (block.type == BlockType.TEXT || block.type == BlockType.CHECKLIST || block.type == BlockType.BULLET) {
                        var bIdx = 0
                        while (bIdx < block.content.length) {
                            val found = block.content.indexOf(findQuery, bIdx, ignoreCase = true)
                            if (found == -1) break
                            list.add(NoteSearchMatch(blockIndex = bIndex, startIndex = found, endIndex = found + findQuery.length))
                            bIdx = found + findQuery.length.coerceAtLeast(1)
                        }
                    }
                }
                list
            }
        }
    }

    val safeMatchIndex = if (searchMatches.isNotEmpty()) {
        currentMatchIndex.coerceIn(0, searchMatches.lastIndex)
    } else {
        0
    }

    fun scrollToMatch(matchIndex: Int) {
        if (searchMatches.isNotEmpty() && matchIndex in searchMatches.indices) {
            val match = searchMatches[matchIndex]
            if (match.blockIndex >= 0) {
                coroutineScope.launch {
                    lazyListState.animateScrollToItem(match.blockIndex)
                }
                activeSelectedBlockIndex = match.blockIndex
                activeSelectionRange = TextRange(match.startIndex, match.endIndex)
            } else {
                activeSelectedBlockIndex = -1
                activeSelectionRange = TextRange(match.startIndex, match.endIndex)
            }
        }
    }

    fun findNextMatch() {
        if (searchMatches.isNotEmpty()) {
            val nextIdx = (safeMatchIndex + 1) % searchMatches.size
            currentMatchIndex = nextIdx
            scrollToMatch(nextIdx)
        }
    }

    fun findPrevMatch() {
        if (searchMatches.isNotEmpty()) {
            val prevIdx = if (safeMatchIndex - 1 < 0) searchMatches.lastIndex else safeMatchIndex - 1
            currentMatchIndex = prevIdx
            scrollToMatch(prevIdx)
        }
    }

    fun replaceCurrentMatch() {
        if (searchMatches.isNotEmpty() && safeMatchIndex in searchMatches.indices) {
            captureSnapshot(force = true)
            val match = searchMatches[safeMatchIndex]
            if (match.blockIndex == -1) {
                title = title.substring(0, match.startIndex) + replaceWith + title.substring(match.endIndex)
            } else if (match.blockIndex in blocks.indices) {
                val block = blocks[match.blockIndex]
                val newContent = block.content.substring(0, match.startIndex) + replaceWith + block.content.substring(match.endIndex)
                blocks[match.blockIndex] = block.copy(content = newContent)
            }
            viewModel.showMessage("Replaced match")
        }
    }

    fun replaceAllMatches() {
        if (findQuery.isNotBlank() && searchMatches.isNotEmpty()) {
            captureSnapshot(force = true)
            val count = searchMatches.size
            title = title.replace(findQuery, replaceWith, ignoreCase = true)
            for (i in blocks.indices) {
                if (blocks[i].content.isNotBlank()) {
                    blocks[i] = blocks[i].copy(
                        content = blocks[i].content.replace(findQuery, replaceWith, ignoreCase = true)
                    )
                }
            }
            viewModel.showMessage("Replaced $count occurrences")
        }
    }

    // Dialogs
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showColorDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showEmojiDialog by remember { mutableStateOf(false) }
    var showHistoryDialog by remember { mutableStateOf(false) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    var placeImageTargetIndex by remember { mutableStateOf<Int?>(null) }
    var activeImageSettingsIndex by remember { mutableStateOf<Int?>(null) }
    var showAiResultDialog by remember { mutableStateOf<Pair<String, String>?>(null) } // Title to Result

    // Keyboard & Clipboard
    val keyboardController = LocalSoftwareKeyboardController.current
    val clipboardManager = LocalClipboardManager.current
    var showAiInstructionBox by remember { mutableStateOf(false) }
    var showAiDisabledDialog by remember { mutableStateOf(false) }
    var showAiResultPreview by remember { mutableStateOf(false) }
    var aiInstructionText by remember { mutableStateOf("") }
    var aiTargetIsSelection by remember { mutableStateOf(false) }
    var aiTransformedResult by remember { mutableStateOf("") }
    var aiErrorMessage by remember { mutableStateOf<String?>(null) }
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    fun insertImageAtCurrentPosition(imagePath: String, caption: String = "") {
        captureSnapshot(force = true)
        val newBlock = NoteBlock(type = BlockType.IMAGE, imagePath = imagePath, content = caption)
        if (activeSelectedBlockIndex in blocks.indices && blocks[activeSelectedBlockIndex].type == BlockType.TEXT) {
            val currentBlock = blocks[activeSelectedBlockIndex]
            val range = activeSelectionRange
            val splitPos = range?.start ?: currentBlock.content.length
            if (splitPos > 0 && splitPos < currentBlock.content.length) {
                // Split text block in half and put image right between them!
                val topText = currentBlock.content.substring(0, splitPos).trimEnd()
                val bottomText = currentBlock.content.substring(splitPos).trimStart()
                blocks[activeSelectedBlockIndex] = currentBlock.copy(content = topText)
                blocks.add(activeSelectedBlockIndex + 1, newBlock)
                if (bottomText.isNotBlank()) {
                    blocks.add(activeSelectedBlockIndex + 2, NoteBlock(type = BlockType.TEXT, content = bottomText))
                }
                viewModel.showMessage("Placed image between text paragraphs")
                return
            } else {
                blocks.add(activeSelectedBlockIndex + 1, newBlock)
                viewModel.showMessage("Inserted image below current text")
                return
            }
        }
        blocks.add(newBlock)
        viewModel.showMessage("Added image to note")
    }

    // SAF Image Picker launcher
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                val relPath = viewModel.importImage(uri)
                if (relPath != null) {
                    insertImageAtCurrentPosition(relPath)
                }
            }
        }
    }

    // SAF PDF Save launcher
    val pdfSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    val noteToExport = Note(
                        id = existingNote?.id ?: UUID.randomUUID().toString(),
                        title = title,
                        blocks = blocks.toList(),
                        categoryId = categoryId,
                        tint = tint,
                        font = font
                    )
                    viewModel.exportNoteToPdf(noteToExport, os)
                }
            }
        }
    }

    // SAF Text File (.txt, .md, .log, .csv) Importer (Lag-free chunking)
    val textFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    val content = context.contentResolver.openInputStream(uri)?.use { stream ->
                        stream.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
                    }
                    if (!content.isNullOrBlank()) {
                        val chunks = chunkLargeTextToBlocks(content)
                        withContext(Dispatchers.Main) {
                            captureSnapshot(force = true)
                            if (blocks.size == 1 && blocks[0].content.isBlank()) {
                                blocks.clear()
                            }
                            blocks.addAll(chunks)
                            viewModel.showMessage("Imported text file (${content.length} chars, lag-free)")
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.showMessage("Could not import file: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    // SAF Plain Text (.txt) Save launcher
    val txtSaveLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch(Dispatchers.IO) {
                try {
                    context.contentResolver.openOutputStream(uri)?.use { os ->
                        val writer = os.bufferedWriter(StandardCharsets.UTF_8)
                        if (title.isNotBlank()) {
                            writer.write("# $title\n\n")
                        }
                        for (b in blocks) {
                            if (b.content.isNotBlank()) {
                                writer.write(b.content)
                                writer.write("\n\n")
                            }
                        }
                        writer.flush()
                    }
                    withContext(Dispatchers.Main) {
                        viewModel.showMessage("Text file exported successfully")
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        viewModel.showMessage("Export failed: ${e.localizedMessage}")
                    }
                }
            }
        }
    }

    // Speech Recognizer setup
    var isDictating by remember { mutableStateOf(false) }
    var speechRecognizer by remember { mutableStateOf<SpeechRecognizer?>(null) }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startSpeechRecognition(context, onResult = { spokenText ->
                captureSnapshot()
                val lastTextBlock = blocks.indexOfLast { it.type == BlockType.TEXT }
                if (lastTextBlock >= 0) {
                    val current = blocks[lastTextBlock].content
                    blocks[lastTextBlock] = blocks[lastTextBlock].copy(
                        content = if (current.isBlank()) spokenText else "$current $spokenText"
                    )
                } else {
                    blocks.add(NoteBlock(type = BlockType.TEXT, content = spokenText))
                }
            }, onStatusChange = { isDictating = it })
        } else {
            viewModel.showMessage("Microphone permission denied for voice dictation.")
        }
    }

    // Save note helper
    fun saveCurrentNote(commitHistory: Boolean = false, showFeedback: Boolean = true) {
        val currentId = existingNote?.id ?: noteId ?: UUID.randomUUID().toString()
        val noteToSave = Note(
            id = currentId,
            title = title.trim(),
            blocks = blocks.toList(),
            categoryId = categoryId,
            tags = tags,
            tint = tint,
            font = font,
            isPinned = isPinned,
            createdAt = existingNote?.createdAt ?: System.currentTimeMillis(),
            updatedAt = System.currentTimeMillis(),
            history = existingNote?.history ?: emptyList()
        )
        viewModel.saveNote(noteToSave, commitHistory = commitHistory)
        isDirty = false
        if (showFeedback) {
            viewModel.showMessage("Note saved ✓")
        }
    }

    // Autosave on back navigation
    BackHandler {
        if (isFormattedView) {
            isFormattedView = false
        } else {
            if (isDirty) {
                saveCurrentNote(commitHistory = false)
            }
            onBack()
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            if (isDirty) {
                saveCurrentNote(commitHistory = false)
            }
            speechRecognizer?.destroy()
        }
    }

    // Colors & typography
    val backgroundColor = if (isFocusMode) FocusModeBg else getNoteColor(tint)
    val textColor = if (isFocusMode) Color.White else Color.Black
    val activeFontFamily = if (isFocusMode) FontFamily.Serif else getFontFamily(font)

    Scaffold(
        containerColor = backgroundColor,
        topBar = {
            if (isFocusMode) {
                // Focus Mode Condensed Top Bar
                TopAppBar(
                    title = {
                        Text(
                            text = "$totalCharCount chars",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { isFocusMode = false }) {
                            Icon(Icons.Default.FullscreenExit, contentDescription = "Exit Focus Mode", tint = Color.White)
                        }
                    },
                    actions = {
                        IconButton(onClick = { undo() }, enabled = undoStack.isNotEmpty() && !isFormattedView) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (undoStack.isNotEmpty() && !isFormattedView) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                        IconButton(onClick = { redo() }, enabled = redoStack.isNotEmpty() && !isFormattedView) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (redoStack.isNotEmpty() && !isFormattedView) Color.White else Color.White.copy(alpha = 0.3f)
                            )
                        }
                        // Rectangular Formatted View Toggle Button (Focus Mode)
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (isFormattedView) Color.White.copy(alpha = 0.9f) else Color.White.copy(alpha = 0.15f),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isFormattedView) Color.White else Color.White.copy(alpha = 0.35f)
                            ),
                            modifier = Modifier
                                .height(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { isFormattedView = !isFormattedView }
                                .testTag("formatted_view_toggle_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFormattedView) Icons.Default.EditNote else Icons.Default.MenuBook,
                                    contentDescription = if (isFormattedView) "Return to Edit View" else "Switch to Formatted View",
                                    tint = if (isFormattedView) FocusModeBg else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isFormattedView) "Edit" else "View",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFormattedView) FocusModeBg else Color.White
                                )
                            }
                        }
                        IconButton(onClick = { showFindReplace = !showFindReplace }) {
                            Icon(Icons.Default.Search, contentDescription = "Find and replace", tint = Color.White)
                        }
                        // Expressive AI Button in Focus Mode
                        IconButton(onClick = {
                            if (!settings.geminiEnabled || settings.geminiApiKey.isBlank()) {
                                showAiDisabledDialog = true
                            } else {
                                aiTargetIsSelection = activeSelectedText.isNotBlank()
                                aiErrorMessage = null
                                showAiInstructionBox = true
                            }
                        }) {
                            ExpressiveAiIcon(size = 26.dp)
                        }
                        IconButton(onClick = {
                            keyboardController?.hide()
                            saveCurrentNote(commitHistory = true, showFeedback = true)
                            onBack()
                        }) {
                            Icon(Icons.Default.Check, contentDescription = "Save", tint = Color.White)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FocusModeBg)
                )
            } else {
                // Standard Note Editor Top Bar
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "$totalCharCount chars",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.DarkGray
                            )
                            if (isDirty) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFF9800))
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            if (isDirty) saveCurrentNote(commitHistory = false)
                            onBack()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.DarkGray)
                        }
                    },
                    actions = {
                        // Undo
                        IconButton(
                            onClick = { undo() },
                            enabled = undoStack.isNotEmpty() && !isFormattedView,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Undo,
                                contentDescription = "Undo",
                                tint = if (undoStack.isNotEmpty() && !isFormattedView) Color.Black else Color.LightGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        // Redo
                        IconButton(
                            onClick = { redo() },
                            enabled = redoStack.isNotEmpty() && !isFormattedView,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.Redo,
                                contentDescription = "Redo",
                                tint = if (redoStack.isNotEmpty() && !isFormattedView) Color.Black else Color.LightGray.copy(alpha = 0.5f),
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        // High-contrast, Uncluttered, Prominent Save Button
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .height(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    keyboardController?.hide()
                                    saveCurrentNote(commitHistory = true, showFeedback = true)
                                    onBack()
                                }
                                .testTag("save_note_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Save Note",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Save",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Overflow Menu for secondary options
                        IconButton(
                            onClick = { showOverflowMenu = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                Icons.Default.MoreVert,
                                contentDescription = "More options",
                                tint = Color.DarkGray,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        DropdownMenu(
                            expanded = showOverflowMenu,
                            onDismissRequest = { showOverflowMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(if (showFindReplace) "Hide Find & Replace" else "Find & Replace") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showOverflowMenu = false
                                    showFindReplace = !showFindReplace
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isFormattedView) "Switch to Edit View" else "Formatted Reading View") },
                                leadingIcon = { Icon(if (isFormattedView) Icons.Default.EditNote else Icons.Default.MenuBook, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                                onClick = {
                                    showOverflowMenu = false
                                    isFormattedView = !isFormattedView
                                }
                            )
                            HorizontalDivider()
                            DropdownMenuItem(
                                text = { Text("Focus / Fullscreen Mode") },
                                leadingIcon = { Icon(Icons.Default.Fullscreen, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    isFocusMode = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (isPinned) "Unpin Note" else "Pin Note") },
                                leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) },
                                onClick = {
                                    isPinned = !isPinned
                                    isDirty = true
                                    showOverflowMenu = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Note Color") },
                                leadingIcon = { Icon(Icons.Default.ColorLens, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showColorDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Font Style") },
                                leadingIcon = { Icon(Icons.Default.FontDownload, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showFontDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export to PDF") },
                                leadingIcon = { Icon(Icons.Default.Share, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    val safeName = (title.ifBlank { "Note" }).replace("[^a-zA-Z0-9]".toRegex(), "_")
                                    pdfSaveLauncher.launch("$safeName.pdf")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Import Text File (.txt, .md)") },
                                leadingIcon = { Icon(Icons.Default.FileOpen, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    textFilePickerLauncher.launch("text/*")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Export as Text (.txt)") },
                                leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    val safeName = (title.ifBlank { "Note" }).replace("[^a-zA-Z0-9]".toRegex(), "_")
                                    txtSaveLauncher.launch("$safeName.txt")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Optimize for Speed") },
                                leadingIcon = { Icon(Icons.Default.Speed, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    val newBlocks = mutableListOf<NoteBlock>()
                                    for (b in blocks) {
                                        if (b.type == BlockType.TEXT && b.content.length > 4000) {
                                            newBlocks.addAll(chunkLargeTextToBlocks(b.content, 3500))
                                        } else {
                                            newBlocks.add(b)
                                        }
                                    }
                                    captureSnapshot(force = true)
                                    blocks.clear()
                                    blocks.addAll(newBlocks)
                                    viewModel.showMessage("Note optimized for high performance (${blocks.size} blocks)")
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Version History") },
                                leadingIcon = { Icon(Icons.Default.History, contentDescription = null) },
                                onClick = {
                                    showOverflowMenu = false
                                    showHistoryDialog = true
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Delete Note") },
                                leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                                onClick = {
                                    showOverflowMenu = false
                                    if (existingNote != null) {
                                        viewModel.deleteNote(existingNote.id)
                                    }
                                    onBack()
                                }
                            )

                            // Gemini AI Features
                            if (settings.geminiEnabled) {
                                Surface(color = MaterialTheme.colorScheme.surfaceVariant) {
                                    Text(
                                        text = "GEMINI AI",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
                                    )
                                }
                                DropdownMenuItem(
                                    text = { Text("Summarize") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        val fullText = title + "\n" + blocks.joinToString("\n") { it.content }
                                        viewModel.summarizeNote(fullText) { summary ->
                                            showAiResultDialog = "AI Summary" to summary
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rewrite (Clear & Polished)") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        val fullText = blocks.joinToString("\n") { it.content }
                                        viewModel.rewriteNote(fullText, "clear, structured, and modern") { rewritten ->
                                            showAiResultDialog = "Rewritten Content" to rewritten
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Fix Grammar & Spelling") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        val fullText = blocks.joinToString("\n") { it.content }
                                        viewModel.fixGrammar(fullText) { corrected ->
                                            showAiResultDialog = "Grammar Proofread" to corrected
                                        }
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Auto-Tag Note") },
                                    leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800)) },
                                    onClick = {
                                        showOverflowMenu = false
                                        val fullText = blocks.joinToString("\n") { it.content }
                                        viewModel.autoTagNote(title, fullText) { generatedTags ->
                                            captureSnapshot()
                                            tags = (tags + generatedTags).distinct()
                                            viewModel.showMessage("Added ${generatedTags.size} AI tags")
                                        }
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
                )
            }
        },
        bottomBar = {
            if (!isFocusMode && !isFormattedView) {
                // Floating Bottom Formatting Toolbar
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.95f),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                        .imePadding()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Aa text formatting
                        IconButton(onClick = { showFontDialog = true }) {
                            Text("Aa", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                        }

                        // Checklist block
                        IconButton(onClick = {
                            captureSnapshot()
                            blocks.add(NoteBlock(type = BlockType.CHECKLIST, content = ""))
                        }) {
                            Icon(Icons.Default.CheckBox, contentDescription = "Checklist", tint = Color.DarkGray)
                        }

                        // Dictation Microphone
                        IconButton(onClick = {
                            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
                                startSpeechRecognition(context, onResult = { spokenText ->
                                    captureSnapshot()
                                    val lastTextBlock = blocks.indexOfLast { it.type == BlockType.TEXT }
                                    if (lastTextBlock >= 0) {
                                        val current = blocks[lastTextBlock].content
                                        blocks[lastTextBlock] = blocks[lastTextBlock].copy(
                                            content = if (current.isBlank()) spokenText else "$current $spokenText"
                                        )
                                    } else {
                                        blocks.add(NoteBlock(type = BlockType.TEXT, content = spokenText))
                                    }
                                }, onStatusChange = { isDictating = it })
                            } else {
                                audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                            }
                        }) {
                            Icon(
                                Icons.Default.Mic,
                                contentDescription = "Dictate note",
                                tint = if (isDictating) Color.Red else Color.DarkGray
                            )
                        }

                        // Pen (Handwriting stroke block)
                        IconButton(onClick = {
                            captureSnapshot()
                            blocks.add(NoteBlock(type = BlockType.HANDWRITING))
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Handwriting", tint = Color.DarkGray)
                        }

                        // Image Source Picker (Sample Gallery or Device Picker)
                        IconButton(onClick = { showImageSourceDialog = true }) {
                            Icon(Icons.Default.Image, contentDescription = "Insert Image", tint = Color.DarkGray)
                        }

                        // Expressive AI Assistant Button (NOT an emoji button!)
                        IconButton(
                            onClick = {
                                if (!settings.geminiEnabled || settings.geminiApiKey.isBlank()) {
                                    showAiDisabledDialog = true
                                } else {
                                    aiTargetIsSelection = activeSelectedText.isNotBlank()
                                    aiErrorMessage = null
                                    showAiInstructionBox = true
                                }
                            },
                            modifier = Modifier.testTag("ai_assistant_toolbar_button")
                        ) {
                            ExpressiveAiIcon(size = 30.dp)
                        }

                        // Emoji dialog
                        IconButton(onClick = { showEmojiDialog = true }) {
                            Icon(Icons.Default.Mood, contentDescription = "Insert Emoji", tint = Color.DarkGray)
                        }

                        // Background color
                        IconButton(onClick = { showColorDialog = true }) {
                            Icon(Icons.Default.ColorLens, contentDescription = "Note Color", tint = Color.DarkGray)
                        }

                        // Bullet list
                        IconButton(onClick = {
                            captureSnapshot()
                            blocks.add(NoteBlock(type = BlockType.BULLET, content = ""))
                        }) {
                            Icon(Icons.Default.FormatListBulleted, contentDescription = "Bullet List", tint = Color.DarkGray)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        if (isFormattedView) {
            FormattedNoteView(
                title = title,
                blocks = blocks,
                categoryName = categories.find { it.id == categoryId }?.name,
                tags = tags,
                timestamp = existingNote?.updatedAt ?: System.currentTimeMillis(),
                fontFamily = activeFontFamily,
                textColor = textColor,
                dateFormat = settings.dateFormat,
                onExitFormattedView = { isFormattedView = false },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp)
            ) {
            // Find and Replace Banner
            AnimatedVisibility(visible = showFindReplace) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                    tonalElevation = 6.dp,
                    shadowElevation = 6.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        // Find Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            OutlinedTextField(
                                value = findQuery,
                                onValueChange = { 
                                    findQuery = it
                                    currentMatchIndex = 0
                                },
                                placeholder = { Text("Find in note...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (findQuery.isNotEmpty()) {
                                        IconButton(onClick = { findQuery = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear search", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                    focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            // Match Counter Indicator
                            if (findQuery.isNotBlank()) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (searchMatches.isNotEmpty()) MaterialTheme.colorScheme.primaryContainer else Color.Black.copy(alpha = 0.08f)
                                ) {
                                    Text(
                                        text = if (searchMatches.isNotEmpty()) "${safeMatchIndex + 1}/${searchMatches.size}" else "0/0",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (searchMatches.isNotEmpty()) MaterialTheme.colorScheme.primary else Color.Gray,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Find Previous Button (↑)
                            IconButton(
                                onClick = { findPrevMatch() },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowUp,
                                    contentDescription = "Find Previous",
                                    tint = if (searchMatches.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Find Next Button (↓)
                            IconButton(
                                onClick = { findNextMatch() },
                                enabled = searchMatches.isNotEmpty(),
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Find Next",
                                    tint = if (searchMatches.isNotEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else Color.LightGray,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            // Close Search Button
                            IconButton(
                                onClick = { showFindReplace = false },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close search",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        // Replace Row
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedTextField(
                                value = replaceWith,
                                onValueChange = { replaceWith = it },
                                placeholder = { Text("Replace with...", fontSize = 13.sp) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.FindReplace,
                                        contentDescription = "Replace",
                                        tint = MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                trailingIcon = {
                                    if (replaceWith.isNotEmpty()) {
                                        IconButton(onClick = { replaceWith = "" }, modifier = Modifier.size(24.dp)) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear replace", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                },
                                singleLine = true,
                                textStyle = TextStyle(fontSize = 13.sp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White.copy(alpha = 0.9f),
                                    focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            )

                            // Replace Current Match Button
                            Button(
                                onClick = { replaceCurrentMatch() },
                                enabled = searchMatches.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.secondary,
                                    contentColor = Color.White
                                )
                            ) {
                                Text("Replace", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            // Replace All Matches Button
                            OutlinedButton(
                                onClick = { replaceAllMatches() },
                                enabled = searchMatches.isNotEmpty(),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Text("All", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Note Meta Row (Date + Category Chip)
            if (!isFocusMode) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val timestamp = existingNote?.updatedAt ?: System.currentTimeMillis()
                    Text(
                        text = DateFormats.formatNoteTimestamp(timestamp, settings.dateFormat),
                        fontSize = 12.sp,
                        color = Color.DarkGray.copy(alpha = 0.8f)
                    )

                    // Category Chip Dropdown
                    val currentCat = categories.find { it.id == categoryId }
                    Box {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.Black.copy(alpha = 0.08f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { showCategoryMenu = true }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = currentCat?.name ?: "No Category",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Change category",
                                    tint = Color.DarkGray,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("None") },
                                onClick = {
                                    categoryId = null
                                    isDirty = true
                                    showCategoryMenu = false
                                }
                            )
                            categories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.name) },
                                    onClick = {
                                        categoryId = cat.id
                                        isDirty = true
                                        showCategoryMenu = false
                                    }
                                )
                            }

                            // AI Auto-categorize option
                            if (settings.geminiEnabled) {
                                DropdownMenuItem(
                                    text = { Text("✨ AI Auto-Categorize") },
                                    onClick = {
                                        showCategoryMenu = false
                                        val fullText = blocks.joinToString(" ") { it.content }
                                        viewModel.autoCategorizeNote(title, fullText) { suggestedName ->
                                            val matchedCat = categories.find { it.name.equals(suggestedName, ignoreCase = true) }
                                            if (matchedCat != null) {
                                                categoryId = matchedCat.id
                                            } else {
                                                viewModel.addCategory(suggestedName)
                                            }
                                            isDirty = true
                                            viewModel.showMessage("Category set to '$suggestedName'")
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Note Title TextField
            TextField(
                value = title,
                onValueChange = {
                    title = it
                    captureSnapshot()
                },
                placeholder = {
                    Text(
                        text = "Title",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = activeFontFamily,
                        color = textColor.copy(alpha = 0.4f)
                    )
                },
                textStyle = TextStyle(
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = activeFontFamily,
                    color = textColor
                ),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )

            // Tags display
            if (tags.isNotEmpty() && !isFocusMode) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.Black.copy(alpha = 0.08f)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Rich Blocks List
            LazyColumn(
                state = lazyListState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 4.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(blocks, key = { _, block -> block.id }) { index, block ->
                    when (block.type) {
                        BlockType.TEXT -> {
                            var tfv by remember(block.id) {
                                mutableStateOf(TextFieldValue(block.content))
                            }
                            if (tfv.text != block.content) {
                                tfv = tfv.copy(text = block.content)
                            }

                            LaunchedEffect(activeSelectedBlockIndex, activeSelectionRange) {
                                if (activeSelectedBlockIndex == index && activeSelectionRange != null) {
                                    val sel = activeSelectionRange!!
                                    if (sel.max <= tfv.text.length && tfv.selection != sel) {
                                        tfv = tfv.copy(selection = sel)
                                    }
                                }
                            }

                            TextField(
                                value = tfv,
                                onValueChange = { newTfv ->
                                    tfv = newTfv
                                    if (newTfv.text != block.content) {
                                        // If large paste (>6,000 chars), automatically chunk into virtualized blocks
                                        if (newTfv.text.length > 6000 && newTfv.text.length - block.content.length > 200) {
                                            val chunks = chunkLargeTextToBlocks(newTfv.text)
                                            captureSnapshot(force = true)
                                            blocks.removeAt(index)
                                            blocks.addAll(index, chunks)
                                        } else {
                                            blocks[index] = block.copy(content = newTfv.text)
                                            captureSnapshot(force = false)
                                        }
                                    }
                                    if (!newTfv.selection.collapsed) {
                                        val minPos = newTfv.selection.min.coerceIn(0, newTfv.text.length)
                                        val maxPos = newTfv.selection.max.coerceIn(0, newTfv.text.length)
                                        if (minPos < maxPos) {
                                            activeSelectedText = newTfv.text.substring(minPos, maxPos)
                                            activeSelectedBlockIndex = index
                                            activeSelectionRange = TextRange(minPos, maxPos)
                                        }
                                    } else {
                                        if (activeSelectedBlockIndex == index) {
                                            activeSelectedText = ""
                                            activeSelectionRange = null
                                        }
                                    }
                                },
                                placeholder = {
                                    if (index == 0) {
                                        Text(
                                            text = "Note here",
                                            fontSize = 16.sp,
                                            fontFamily = activeFontFamily,
                                            color = textColor.copy(alpha = 0.4f)
                                        )
                                    }
                                },
                                textStyle = TextStyle(
                                    fontSize = 16.sp,
                                    fontFamily = activeFontFamily,
                                    color = textColor,
                                    lineHeight = 24.sp
                                ),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        BlockType.CHECKLIST -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        captureSnapshot()
                                        blocks[index] = block.copy(checked = !block.checked)
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = if (block.checked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                                        contentDescription = "Check",
                                        tint = if (block.checked) MaterialTheme.colorScheme.primary else Color.Gray
                                    )
                                }

                                TextField(
                                    value = block.content,
                                    onValueChange = {
                                        blocks[index] = block.copy(content = it)
                                        captureSnapshot()
                                    },
                                    placeholder = { Text("To-do item", color = textColor.copy(alpha = 0.4f)) },
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontFamily = activeFontFamily,
                                        color = if (block.checked) Color.Gray else textColor,
                                        textDecoration = if (block.checked) TextDecoration.LineThrough else TextDecoration.None
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        captureSnapshot()
                                        blocks.removeAt(index)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete item", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        BlockType.BULLET -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "•",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isFocusMode) Color.White else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(start = 8.dp, end = 4.dp)
                                )

                                TextField(
                                    value = block.content,
                                    onValueChange = {
                                        blocks[index] = block.copy(content = it)
                                        captureSnapshot()
                                    },
                                    placeholder = { Text("Bullet item", color = textColor.copy(alpha = 0.4f)) },
                                    textStyle = TextStyle(
                                        fontSize = 15.sp,
                                        fontFamily = activeFontFamily,
                                        color = textColor
                                    ),
                                    colors = TextFieldDefaults.colors(
                                        focusedContainerColor = Color.Transparent,
                                        unfocusedContainerColor = Color.Transparent,
                                        focusedIndicatorColor = Color.Transparent,
                                        unfocusedIndicatorColor = Color.Transparent
                                    ),
                                    modifier = Modifier.weight(1f)
                                )

                                IconButton(
                                    onClick = {
                                        captureSnapshot()
                                        blocks.removeAt(index)
                                    },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Delete item", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }

                        BlockType.IMAGE -> {
                            val imgFile = block.imagePath?.let { File(context.filesDir, it) }
                            val bitmap = remember(block.imagePath) {
                                if (imgFile != null) BitmapLoader.loadScaledBitmap(imgFile, 800, 600) else null
                            }
                            var resizeScale by remember(block.id, block.imageScale) { mutableFloatStateOf(block.imageScale) }

                            val alignment = when (block.imageAlignment) {
                                "left" -> Alignment.Start
                                "right" -> Alignment.End
                                else -> Alignment.CenterHorizontally
                            }

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalAlignment = alignment
                            ) {
                                // Clean Image Container: 2-Finger Pinch-to-Resize while 1-finger scrolls freely!
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(resizeScale.coerceIn(0.2f, 1.0f))
                                        .clip(RoundedCornerShape(10.dp))
                                        .pointerInput(block.id) {
                                            awaitEachGesture {
                                                do {
                                                    val event = awaitPointerEvent()
                                                    if (event.changes.size >= 2) {
                                                        val p1 = event.changes[0]
                                                        val p2 = event.changes[1]
                                                        val prevDist = (p1.previousPosition - p2.previousPosition).getDistance()
                                                        val currDist = (p1.position - p2.position).getDistance()
                                                        if (prevDist > 0f && currDist > 0f) {
                                                            val zoom = currDist / prevDist
                                                            if (kotlin.math.abs(zoom - 1f) > 0.002f) {
                                                                p1.consume()
                                                                p2.consume()
                                                                val newScale = (resizeScale * zoom).coerceIn(0.2f, 1.0f)
                                                                if (kotlin.math.abs(newScale - resizeScale) > 0.002f) {
                                                                    resizeScale = newScale
                                                                    blocks[index] = block.copy(imageScale = newScale)
                                                                    isDirty = true
                                                                }
                                                            }
                                                        }
                                                    }
                                                } while (event.changes.any { it.pressed })
                                            }
                                        }
                                ) {
                                    if (bitmap != null) {
                                        Image(
                                            bitmap = bitmap.asImageBitmap(),
                                            contentDescription = "Note image (Pinch with two fingers to resize)",
                                            contentScale = ContentScale.FillWidth,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                        )
                                    } else {
                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color.Black.copy(alpha = 0.05f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(140.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text("🖼️ [Image file]", color = Color.Gray, fontSize = 13.sp)
                                            }
                                        }
                                    }

                                    // Top-left compact Move / Reorder Badges
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Black.copy(alpha = 0.65f),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            if (index > 0) {
                                                IconButton(
                                                    onClick = {
                                                        captureSnapshot()
                                                        val item = blocks.removeAt(index)
                                                        blocks.add(index - 1, item)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowUp, contentDescription = "Move Up", tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            if (index < blocks.lastIndex) {
                                                IconButton(
                                                    onClick = {
                                                        captureSnapshot()
                                                        val item = blocks.removeAt(index)
                                                        blocks.add(index + 1, item)
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Move Down", tint = Color.White, modifier = Modifier.size(18.dp))
                                                }
                                            }
                                            IconButton(
                                                onClick = { placeImageTargetIndex = index },
                                                modifier = Modifier.size(28.dp)
                                            ) {
                                                Icon(Icons.Default.Place, contentDescription = "Place Between Paragraphs", tint = Color(0xFFFFD54F), modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }

                                    // Top-right Discreet Delete Button
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.65f))
                                            .clickable {
                                                captureSnapshot()
                                                blocks.removeAt(index)
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Delete image",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }

                                    // Bottom-right Resize & Preset Button (Pinch hint & tap for quick size / alignment dialog)
                                    Surface(
                                        shape = RoundedCornerShape(14.dp),
                                        color = Color.Black.copy(alpha = 0.72f),
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .padding(6.dp)
                                            .clip(RoundedCornerShape(14.dp))
                                            .clickable { activeImageSettingsIndex = index }
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Text(
                                                text = "${(resizeScale * 100).toInt()}% 🤏",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Icon(
                                                imageVector = Icons.Default.OpenInFull,
                                                contentDescription = "Resize or adjust image",
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                        }
                                    }
                                }

                                // Subtle borderless caption line under the picture
                                if (block.content.isNotBlank() || !isFormattedView) {
                                    BasicTextField(
                                        value = block.content,
                                        onValueChange = { newCaption ->
                                            blocks[index] = block.copy(content = newCaption)
                                            captureSnapshot(force = false)
                                        },
                                        textStyle = TextStyle(
                                            fontSize = 12.sp,
                                            fontFamily = activeFontFamily,
                                            fontStyle = FontStyle.Italic,
                                            color = textColor.copy(alpha = 0.65f),
                                            textAlign = TextAlign.Center
                                        ),
                                        decorationBox = { innerTextField ->
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth(resizeScale.coerceIn(0.2f, 1.0f))
                                                    .padding(top = 4.dp),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (block.content.isBlank()) {
                                                    Text(
                                                        text = "Add caption...",
                                                        fontSize = 12.sp,
                                                        fontStyle = FontStyle.Italic,
                                                        color = textColor.copy(alpha = 0.35f),
                                                        textAlign = TextAlign.Center
                                                    )
                                                }
                                                innerTextField()
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth(resizeScale.coerceIn(0.2f, 1.0f))
                                            .padding(horizontal = 4.dp)
                                    )
                                }
                            }
                        }

                        BlockType.HANDWRITING -> {
                            Box(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                                HandwritingBlockView(
                                    strokes = block.strokes,
                                    onStrokesChanged = { updatedStrokes ->
                                        blocks[index] = block.copy(strokes = updatedStrokes)
                                        isDirty = true
                                    }
                                )
                                IconButton(
                                    onClick = {
                                        captureSnapshot()
                                        blocks.removeAt(index)
                                    },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(28.dp)
                                ) {
                                    Icon(Icons.Default.Close, contentDescription = "Remove canvas", tint = Color.Gray, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }

                // Bottom padding inside scroll list (tap to focus keyboard and write)
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clickable {
                                val lastIdx = blocks.indexOfLast { it.type == BlockType.TEXT }
                                if (lastIdx >= 0) {
                                    activeSelectedBlockIndex = lastIdx
                                } else {
                                    captureSnapshot()
                                    blocks.add(NoteBlock(type = BlockType.TEXT, content = ""))
                                    activeSelectedBlockIndex = blocks.lastIndex
                                }
                                keyboardController?.show()
                            }
                    )
                }
            }
        }
    }
}

    // Place Image Between Text Paragraphs Dialog
    if (placeImageTargetIndex != null) {
        val imgIdx = placeImageTargetIndex!!
        val textBlocks = remember(blocks, imgIdx) {
            blocks.mapIndexedNotNull { idx, b ->
                if (b.type == BlockType.TEXT && b.content.isNotBlank()) idx to b.content else null
            }
        }
        AlertDialog(
            onDismissRequest = { placeImageTargetIndex = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Default.Place, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Text("Place Image Between Text", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth().height(320.dp)
                ) {
                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    captureSnapshot()
                                    val item = blocks.removeAt(imgIdx)
                                    blocks.add(0, item)
                                    placeImageTargetIndex = null
                                    viewModel.showMessage("Moved image to top before all text")
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.VerticalAlignTop, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Place at very top (before all text)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    items(textBlocks) { (bIdx, content) ->
                        val snippet = if (content.length > 50) content.take(50) + "..." else content
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    captureSnapshot()
                                    val item = blocks.removeAt(imgIdx)
                                    val targetPos = if (imgIdx < bIdx) bIdx else bIdx + 1
                                    blocks.add(targetPos.coerceIn(0, blocks.size), item)
                                    placeImageTargetIndex = null
                                    viewModel.showMessage("Placed image after paragraph")
                                }
                                .padding(10.dp)
                        ) {
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    Icon(Icons.Default.SwapVert, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(16.dp))
                                    Text("Place directly below this paragraph:", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                                Text("\"$snippet\"", fontSize = 12.sp, color = Color.DarkGray)
                            }
                        }
                    }

                    item {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    captureSnapshot()
                                    val item = blocks.removeAt(imgIdx)
                                    blocks.add(blocks.size, item)
                                    placeImageTargetIndex = null
                                    viewModel.showMessage("Moved image to bottom after all text")
                                }
                                .padding(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Icon(Icons.Default.VerticalAlignBottom, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Text("Place at very bottom (after all text)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { placeImageTargetIndex = null }) { Text("Close") }
            }
        )
    }

    // Image Source & Sample Preset Dialog
    if (showImageSourceDialog) {
        Dialog(onDismissRequest = { showImageSourceDialog = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Image,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Insert Image",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        IconButton(
                            onClick = { showImageSourceDialog = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Preset Sample Photos (Instant for Preview / Emulator):",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // 5 Presets list
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        SampleImageGenerator.PRESETS.forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        showImageSourceDialog = false
                                        coroutineScope.launch {
                                            val relPath = SampleImageGenerator.generateAndSaveSampleImage(context, preset.id)
                                            insertImageAtCurrentPosition(relPath, preset.title)
                                        }
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(preset.previewEmoji, fontSize = 22.sp)
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = preset.title,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = preset.subtitle,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Icon(
                                        imageVector = Icons.Default.ArrowDownward,
                                        contentDescription = "Insert",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                    Spacer(modifier = Modifier.height(14.dp))

                    // Device Storage Picker Option
                    OutlinedButton(
                        onClick = {
                            showImageSourceDialog = false
                            imagePickerLauncher.launch("image/*")
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FileOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pick from Device Storage / Gallery")
                    }
                }
            }
        }
    }

    // Image Sizing & Alignment Dialog
    val imageTarget = activeImageSettingsIndex?.let { if (it in blocks.indices && blocks[it].type == BlockType.IMAGE) blocks[it] else null }
    if (activeImageSettingsIndex != null && imageTarget != null) {
        val targetIdx = activeImageSettingsIndex!!
        AlertDialog(
            onDismissRequest = { activeImageSettingsIndex = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Image Size & Position", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text(
                        text = "Tip: You can pinch with two fingers directly on the picture to resize dynamically.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Quick Width Presets
                    Text("Image Width:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(0.25f to "25%", 0.50f to "50%", 0.75f to "75%", 1.0f to "100%").forEach { (scale, label) ->
                            val isSelected = kotlin.math.abs(imageTarget.imageScale - scale) < 0.08f
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        captureSnapshot()
                                        blocks[targetIdx] = imageTarget.copy(imageScale = scale)
                                    }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Alignment
                    Text("Alignment:", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf("left" to "Left", "center" to "Center", "right" to "Right").forEach { (alignVal, label) ->
                            val isSelected = imageTarget.imageAlignment == alignVal || (alignVal == "center" && imageTarget.imageAlignment !in listOf("left", "right"))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                border = BorderStroke(1.dp, if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        captureSnapshot()
                                        blocks[targetIdx] = imageTarget.copy(imageAlignment = alignVal)
                                    }
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = label,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // Delete & Move options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = {
                                captureSnapshot()
                                blocks.removeAt(targetIdx)
                                activeImageSettingsIndex = null
                            },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Remove")
                        }

                        Button(
                            onClick = { activeImageSettingsIndex = null },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Done")
                        }
                    }
                }
            },
            confirmButton = {}
        )
    }

    // Color Tint Picker Dialog
    if (showColorDialog) {
        AlertDialog(
            onDismissRequest = { showColorDialog = false },
            title = { Text("Note Background Color") },
            text = {
                NoteTintRow(
                    selectedTint = tint,
                    onTintSelected = {
                        tint = it
                        isDirty = true
                        showColorDialog = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showColorDialog = false }) { Text("Close") }
            }
        )
    }

    // Font Style Picker Dialog
    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("Note Font Style") },
            text = {
                Column {
                    listOf("sans" to "Sans-Serif (Modern)", "serif" to "Serif (Editorial)", "mono" to "Monospace (Code)").forEach { (fId, fLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (font == fId) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .padding(12.dp)
                                .clickable {
                                    font = fId
                                    isDirty = true
                                    showFontDialog = false
                                }
                        ) {
                            Text(
                                text = fLabel,
                                fontFamily = getFontFamily(fId),
                                fontWeight = if (font == fId) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFontDialog = false }) { Text("Close") }
            }
        )
    }

    // Emoji Insertion Dialog
    if (showEmojiDialog) {
        EmojiDialog(
            onDismissRequest = { showEmojiDialog = false },
            onEmojiSelected = { emoji ->
                captureSnapshot()
                val lastIdx = blocks.indexOfLast { it.type == BlockType.TEXT }
                if (lastIdx >= 0) {
                    blocks[lastIdx] = blocks[lastIdx].copy(content = blocks[lastIdx].content + " " + emoji)
                } else {
                    blocks.add(NoteBlock(type = BlockType.TEXT, content = emoji))
                }
            }
        )
    }

    // Version History Dialog
    if (showHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showHistoryDialog = false },
            title = { Text("Saved Snapshots") },
            text = {
                val history = existingNote?.history ?: emptyList()
                if (history.isEmpty()) {
                    Text("No snapshots saved yet. Tap the save checkmark to commit a version.")
                } else {
                    LazyColumn(modifier = Modifier.height(200.dp)) {
                        itemsIndexed(history) { idx, version ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        title = version.title
                                        isDirty = true
                                        showHistoryDialog = false
                                        viewModel.showMessage("Restored snapshot from ${DateFormats.formatNoteTimestamp(version.timestamp)}")
                                    }
                            ) {
                                Column(modifier = Modifier.padding(10.dp)) {
                                    Text(
                                        text = DateFormats.formatNoteTimestamp(version.timestamp),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = version.summaryText,
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        maxLines = 2
                                    )
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showHistoryDialog = false }) { Text("Close") }
            }
        )
    }

    // Natural-Language AI Assistant Floating Instruction Box Dialog
    if (showAiInstructionBox) {
        val entireNoteText = remember(title, blocks) {
            val contentStr = blocks.joinToString("\n") { it.content }.trim()
            if (title.isNotBlank()) "$title\n$contentStr" else contentStr
        }
        val targetText = if (aiTargetIsSelection && activeSelectedText.isNotBlank()) {
            activeSelectedText
        } else {
            entireNoteText
        }

        val suggestions = listOf(
            "correct my writing",
            "fix the grammar",
            "make this sound more natural",
            "translate this into Indonesian",
            "translate this into Korean",
            "make this shorter",
            "make this more professional",
            "summarize this",
            "rewrite this romantically",
            "turn this into lyrics",
            "continue writing this",
            "make this easier to understand",
            "remove unnecessary words",
            "explain this"
        )

        Dialog(onDismissRequest = { if (!isAiLoading) showAiInstructionBox = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth()
                ) {
                    // Header Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExpressiveAiIcon(size = 32.dp)
                            Column {
                                Text(
                                    text = "AI Assistant",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Natural language instructions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(
                            onClick = { showAiInstructionBox = false },
                            enabled = !isAiLoading,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Target Selector
                    Text(
                        text = "TARGET TEXT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    if (activeSelectedText.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = aiTargetIsSelection,
                                onClick = { aiTargetIsSelection = true },
                                label = { Text("Selected (${activeSelectedText.length} chars)", fontSize = 12.sp) },
                                leadingIcon = {
                                    if (aiTargetIsSelection) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                            FilterChip(
                                selected = !aiTargetIsSelection,
                                onClick = { aiTargetIsSelection = false },
                                label = { Text("Entire Note (${entireNoteText.length} chars)", fontSize = 12.sp) },
                                leadingIcon = {
                                    if (!aiTargetIsSelection) {
                                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                                    }
                                }
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Target: Entire Note (${entireNoteText.length} chars)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    // Target Snippet Preview
                    if (targetText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "\"${targetText.take(90).replace("\n", " ")}${if (targetText.length > 90) "..." else ""}\"",
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Instruction Input
                    Text(
                        text = "WHAT WOULD YOU LIKE GEMINI TO DO?",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = aiInstructionText,
                        onValueChange = {
                            aiInstructionText = it
                            aiErrorMessage = null
                        },
                        placeholder = {
                            Text(
                                "e.g. 'correct my writing', 'fix grammar', 'make shorter', 'translate to Indonesian'...",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        },
                        enabled = !isAiLoading,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(95.dp)
                            .testTag("ai_instruction_input"),
                        maxLines = 3
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Natural-language suggestion chips
                    Text(
                        text = "Quick suggestions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(suggestions) { suggestion ->
                            SuggestionChip(
                                onClick = {
                                    aiInstructionText = suggestion
                                    aiErrorMessage = null
                                },
                                label = { Text(suggestion, fontSize = 12.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                                )
                            )
                        }
                    }

                    // Error Message Banner (if any)
                    if (aiErrorMessage != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    Icons.Default.WarningAmber,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = aiErrorMessage ?: "Error",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Action Buttons Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAiInstructionBox = false },
                            enabled = !isAiLoading
                        ) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val textToSend = if (targetText.isNotBlank()) targetText else title
                                if (textToSend.isBlank()) {
                                    aiErrorMessage = "Please type some text in the note before using AI."
                                    return@Button
                                }
                                if (aiInstructionText.isBlank()) {
                                    aiErrorMessage = "Please enter an instruction for the AI."
                                    return@Button
                                }
                                aiErrorMessage = null
                                viewModel.executeAiInstruction(
                                    instruction = aiInstructionText,
                                    text = textToSend,
                                    onResult = { result ->
                                        aiTransformedResult = result
                                        showAiInstructionBox = false
                                        showAiResultPreview = true
                                    },
                                    onError = { error ->
                                        aiErrorMessage = error
                                    }
                                )
                            },
                            enabled = !isAiLoading && aiInstructionText.isNotBlank(),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("submit_ai_instruction_button")
                        ) {
                            if (isAiLoading) {
                                CircularProgressIndicator(
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Thinking...")
                            } else {
                                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Submit")
                            }
                        }
                    }
                }
            }
        }
    }

    // AI Result Preview & Review Dialog
    if (showAiResultPreview) {
        Dialog(onDismissRequest = { showAiResultPreview = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                shadowElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(18.dp)
                        .fillMaxWidth()
                ) {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ExpressiveAiIcon(size = 30.dp)
                            Column {
                                Text(
                                    text = "AI Result Preview",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Instruction: \"$aiInstructionText\"",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        IconButton(
                            onClick = { showAiResultPreview = false },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close", modifier = Modifier.size(18.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Target Badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (aiTargetIsSelection) "Target: Selected text" else "Target: Entire note",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transformed text card
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    ) {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp)
                        ) {
                            item {
                                Text(
                                    text = aiTransformedResult,
                                    style = MaterialTheme.typography.bodyMedium,
                                    lineHeight = 22.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Secondary Actions: Copy & Insert Below
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                clipboardManager.setText(AnnotatedString(aiTransformedResult))
                                viewModel.showMessage("Copied to clipboard")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Copy")
                        }

                        OutlinedButton(
                            onClick = {
                                captureSnapshot()
                                val insertIdx = if (aiTargetIsSelection && activeSelectedBlockIndex in blocks.indices) {
                                    activeSelectedBlockIndex + 1
                                } else {
                                    blocks.size
                                }
                                blocks.add(insertIdx, NoteBlock(type = BlockType.TEXT, content = aiTransformedResult))
                                isDirty = true
                                showAiResultPreview = false
                                viewModel.showMessage("Inserted AI result into note (Undo available)")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1.3f)
                        ) {
                            Text("Insert Below")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Primary Action: Apply / Replace & Cancel
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(
                            onClick = { showAiResultPreview = false }
                        ) {
                            Text("Cancel / Discard", color = MaterialTheme.colorScheme.error)
                        }

                        Button(
                            onClick = {
                                captureSnapshot()
                                if (aiTargetIsSelection && activeSelectedBlockIndex in blocks.indices && activeSelectionRange != null) {
                                    val block = blocks[activeSelectedBlockIndex]
                                    val range = activeSelectionRange!!
                                    val start = range.min.coerceIn(0, block.content.length)
                                    val end = range.max.coerceIn(0, block.content.length)
                                    val newContent = block.content.substring(0, start) + aiTransformedResult + block.content.substring(end)
                                    blocks[activeSelectedBlockIndex] = block.copy(content = newContent)
                                    activeSelectedText = ""
                                    activeSelectionRange = null
                                } else {
                                    if (blocks.isNotEmpty()) {
                                        blocks[0] = blocks[0].copy(type = BlockType.TEXT, content = aiTransformedResult)
                                        while (blocks.size > 1) {
                                            blocks.removeAt(1)
                                        }
                                    } else {
                                        blocks.add(NoteBlock(type = BlockType.TEXT, content = aiTransformedResult))
                                    }
                                }
                                isDirty = true
                                showAiResultPreview = false
                                viewModel.showMessage("AI changes applied (Undo available)")
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("apply_ai_result_button")
                        ) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (aiTargetIsSelection) "Replace Selection" else "Apply to Note")
                        }
                    }
                }
            }
        }
    }

    // AI Disabled / Offline Dialog
    if (showAiDisabledDialog) {
        AlertDialog(
            onDismissRequest = { showAiDisabledDialog = false },
            icon = {
                Icon(
                    Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = { Text("Gemini AI Unavailable") },
            text = {
                Text("Gemini AI features are currently disabled or an API key has not been configured.\n\nTo use AI writing assistance, please enable Gemini and provide your Gemini API key in the app Settings.\n\nYour existing note and normal editing are unaffected.")
            },
            confirmButton = {
                Button(
                    onClick = { showAiDisabledDialog = false },
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Got It")
                }
            }
        )
    }

    // AI Result Dialog
    showAiResultDialog?.let { (dialogTitle, aiText) ->
        AlertDialog(
            onDismissRequest = { showAiResultDialog = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFFFF9800))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(dialogTitle)
                }
            },
            text = {
                LazyColumn(modifier = Modifier.height(240.dp)) {
                    item {
                        Text(text = aiText, style = MaterialTheme.typography.bodyMedium, lineHeight = 22.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        captureSnapshot()
                        blocks.add(NoteBlock(type = BlockType.TEXT, content = "\n--- $dialogTitle ---\n$aiText"))
                        showAiResultDialog = null
                        viewModel.showMessage("Appended AI result to note")
                    }
                ) {
                    Text("Insert into Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAiResultDialog = null }) {
                    Text("Dismiss")
                }
            }
        )
    }
}

// Android SpeechRecognizer helper function
private fun startSpeechRecognition(
    context: Context,
    onResult: (String) -> Unit,
    onStatusChange: (Boolean) -> Unit
) {
    if (!SpeechRecognizer.isRecognitionAvailable(context)) return

    val recognizer = SpeechRecognizer.createSpeechRecognizer(context)
    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
    }

    recognizer.setRecognitionListener(object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) { onStatusChange(true) }
        override fun onBeginningOfSpeech() {}
        override fun onRmsChanged(rmsdB: Float) {}
        override fun onBufferReceived(buffer: ByteArray?) {}
        override fun onEndOfSpeech() { onStatusChange(false) }
        override fun onError(error: Int) {
            onStatusChange(false)
            recognizer.destroy()
        }
        override fun onResults(results: Bundle?) {
            onStatusChange(false)
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            if (!matches.isNullOrEmpty()) {
                onResult(matches[0])
            }
            recognizer.destroy()
        }
        override fun onPartialResults(partialResults: Bundle?) {}
        override fun onEvent(eventType: Int, params: Bundle?) {}
    })
    recognizer.startListening(intent)
}

/**
 * Formatted Note reading item types for 120 FPS LazyColumn virtualization
 */
sealed class FormattedNoteItem {
    data class Heading(val level: Int, val text: String) : FormattedNoteItem()
    data class Paragraph(val text: String) : FormattedNoteItem()
    data class Bullet(val text: String) : FormattedNoteItem()
    data class Numbered(val num: String, val text: String) : FormattedNoteItem()
    data class Checklist(val isChecked: Boolean, val text: String) : FormattedNoteItem()
    data class Blockquote(val text: String) : FormattedNoteItem()
    data class CodeBlock(val code: String) : FormattedNoteItem()
    object Divider : FormattedNoteItem()
    data class ImageItem(
        val imagePath: String?,
        val caption: String = "",
        val imageScale: Float = 1.0f,
        val imageAlignment: String = "center"
    ) : FormattedNoteItem()
    data class HandwritingItem(val strokes: List<HandwritingStroke>) : FormattedNoteItem()
    data class Fallback(val text: String) : FormattedNoteItem()
}

/**
 * Clean, formatted reading view for the note that preserves content,
 * rendering headings, lists, checklists, blockquotes, code, images, and handwriting.
 * Virtualized line-by-line in LazyColumn for instantaneous zero-lag scrolling on 100k+ word notes.
 */
@Composable
fun FormattedNoteView(
    title: String,
    blocks: List<NoteBlock>,
    categoryName: String?,
    tags: List<String>,
    timestamp: Long,
    fontFamily: FontFamily,
    textColor: Color,
    dateFormat: String,
    onExitFormattedView: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val formattedItems = remember(blocks) {
        val items = mutableListOf<FormattedNoteItem>()
        for (block in blocks) {
            when (block.type) {
                BlockType.TEXT -> {
                    val lines = block.content.lines()
                    var i = 0
                    while (i < lines.size) {
                        val line = lines[i]
                        val trimmed = line.trim()
                        if (trimmed.startsWith("```")) {
                            val codeLines = mutableListOf<String>()
                            i++
                            while (i < lines.size && !lines[i].trim().startsWith("```")) {
                                codeLines.add(lines[i])
                                i++
                            }
                            items.add(FormattedNoteItem.CodeBlock(codeLines.joinToString("\n")))
                        } else if (trimmed.startsWith("#### ")) {
                            items.add(FormattedNoteItem.Heading(4, trimmed.removePrefix("#### ")))
                        } else if (trimmed.startsWith("### ")) {
                            items.add(FormattedNoteItem.Heading(3, trimmed.removePrefix("### ")))
                        } else if (trimmed.startsWith("## ")) {
                            items.add(FormattedNoteItem.Heading(2, trimmed.removePrefix("## ")))
                        } else if (trimmed.startsWith("# ")) {
                            items.add(FormattedNoteItem.Heading(1, trimmed.removePrefix("# ")))
                        } else if (trimmed == "---" || trimmed == "***" || trimmed == "___") {
                            items.add(FormattedNoteItem.Divider)
                        } else if (trimmed.startsWith("> ")) {
                            items.add(FormattedNoteItem.Blockquote(trimmed.removePrefix("> ")))
                        } else if (trimmed.matches(Regex("""^\d+\.\s+.*"""))) {
                            items.add(FormattedNoteItem.Numbered(trimmed.substringBefore(". ") + ".", trimmed.substringAfter(". ")))
                        } else if (trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("• ")) {
                            items.add(FormattedNoteItem.Bullet(trimmed.substring(2)))
                        } else if (trimmed.startsWith("[ ] ") || trimmed.startsWith("[x] ", ignoreCase = true)) {
                            items.add(FormattedNoteItem.Checklist(trimmed.startsWith("[x] ", ignoreCase = true), trimmed.substring(4)))
                        } else if (trimmed.isNotBlank()) {
                            items.add(FormattedNoteItem.Paragraph(line))
                        }
                        i++
                    }
                }
                BlockType.CHECKLIST -> items.add(FormattedNoteItem.Checklist(block.checked, block.content))
                BlockType.BULLET -> items.add(FormattedNoteItem.Bullet(block.content))
                BlockType.IMAGE -> items.add(
                    FormattedNoteItem.ImageItem(
                        imagePath = block.imagePath,
                        caption = block.content,
                        imageScale = block.imageScale,
                        imageAlignment = block.imageAlignment
                    )
                )
                BlockType.HANDWRITING -> items.add(FormattedNoteItem.HandwritingItem(block.strokes))
                else -> items.add(FormattedNoteItem.Fallback(block.content))
            }
        }
        items
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 4.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Formatted Mode Active Indicator Banner
        item {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Formatted Reading View (${formattedItems.size} items)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    TextButton(
                        onClick = onExitFormattedView,
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Edit Note", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Title
        if (title.isNotBlank()) {
            item {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontFamily = fontFamily,
                        color = textColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Meta info row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = DateFormats.formatNoteTimestamp(timestamp, dateFormat),
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.6f)
                )

                if (categoryName != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = categoryName,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }

                if (tags.isNotEmpty()) {
                    tags.forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color.Black.copy(alpha = 0.06f)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 11.sp,
                                color = textColor.copy(alpha = 0.7f),
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }
            HorizontalDivider(
                color = textColor.copy(alpha = 0.12f),
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        // Fully Virtualized Formatted Blocks & Paragraph Items
        items(formattedItems) { item ->
            when (item) {
                is FormattedNoteItem.Heading -> {
                    val size = when (item.level) {
                        1 -> 24.sp
                        2 -> 20.sp
                        3 -> 17.sp
                        else -> 15.sp
                    }
                    Text(
                        text = renderInlineMarkdown(item.text, textColor, fontFamily),
                        style = TextStyle(
                            fontSize = size,
                            fontWeight = FontWeight.Bold,
                            fontFamily = fontFamily,
                            color = textColor
                        ),
                        modifier = Modifier.padding(top = (item.level * 2).dp)
                    )
                }

                is FormattedNoteItem.Paragraph -> {
                    Text(
                        text = renderInlineMarkdown(item.text, textColor, fontFamily),
                        style = TextStyle(
                            fontSize = 16.sp,
                            fontFamily = fontFamily,
                            color = textColor,
                            lineHeight = 24.sp
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                is FormattedNoteItem.Bullet -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "•",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                        Text(
                            text = renderInlineMarkdown(item.text, textColor, fontFamily),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = fontFamily,
                                color = textColor,
                                lineHeight = 24.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is FormattedNoteItem.Numbered -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.Top,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = item.num,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            fontFamily = fontFamily,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = renderInlineMarkdown(item.text, textColor, fontFamily),
                            style = TextStyle(
                                fontSize = 16.sp,
                                fontFamily = fontFamily,
                                color = textColor,
                                lineHeight = 24.sp
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is FormattedNoteItem.Checklist -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = if (item.isChecked) Icons.Default.CheckBox else Icons.Default.CheckBoxOutlineBlank,
                            contentDescription = null,
                            tint = if (item.isChecked) MaterialTheme.colorScheme.primary else textColor.copy(alpha = 0.5f),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = renderInlineMarkdown(item.text, textColor, fontFamily),
                            style = TextStyle(
                                fontSize = 15.sp,
                                fontFamily = fontFamily,
                                color = if (item.isChecked) textColor.copy(alpha = 0.5f) else textColor,
                                textDecoration = if (item.isChecked) TextDecoration.LineThrough else TextDecoration.None
                            ),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                is FormattedNoteItem.Blockquote -> {
                    Surface(
                        shape = RoundedCornerShape(topEnd = 8.dp, bottomEnd = 8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Box(
                                modifier = Modifier
                                    .width(4.dp)
                                    .height(36.dp)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                            Text(
                                text = renderInlineMarkdown(item.text, textColor, fontFamily),
                                style = TextStyle(
                                    fontSize = 15.sp,
                                    fontStyle = FontStyle.Italic,
                                    fontFamily = fontFamily,
                                    color = textColor.copy(alpha = 0.85f),
                                    lineHeight = 22.sp
                                ),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                is FormattedNoteItem.CodeBlock -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF212121),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = item.code,
                            style = TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp,
                                color = Color(0xFF80DEEA),
                                lineHeight = 19.sp
                            ),
                            modifier = Modifier.padding(12.dp)
                        )
                    }
                }

                is FormattedNoteItem.Divider -> {
                    HorizontalDivider(
                        color = textColor.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                is FormattedNoteItem.ImageItem -> {
                    val imgFile = item.imagePath?.let { File(context.filesDir, it) }
                    val bitmap = remember(item.imagePath) {
                        if (imgFile != null) BitmapLoader.loadScaledBitmap(imgFile, 800, 600) else null
                    }
                    val alignment = when (item.imageAlignment) {
                        "left" -> Alignment.Start
                        "right" -> Alignment.End
                        else -> Alignment.CenterHorizontally
                    }
                    if (bitmap != null) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalAlignment = alignment
                        ) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Note Image",
                                contentScale = ContentScale.FillWidth,
                                modifier = Modifier
                                    .fillMaxWidth(item.imageScale.coerceIn(0.2f, 1.0f))
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            if (item.caption.isNotBlank()) {
                                Text(
                                    text = item.caption,
                                    style = TextStyle(
                                        fontSize = 12.sp,
                                        fontFamily = fontFamily,
                                        fontStyle = FontStyle.Italic,
                                        color = textColor.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth(item.imageScale.coerceIn(0.2f, 1.0f))
                                        .padding(top = 4.dp)
                                )
                            }
                        }
                    }
                }

                is FormattedNoteItem.HandwritingItem -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.White.copy(alpha = 0.75f))
                            .padding(4.dp)
                    ) {
                        HandwritingBlockView(
                            strokes = item.strokes,
                            onStrokesChanged = { /* Read only */ }
                        )
                    }
                }

                is FormattedNoteItem.Fallback -> {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.05f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = item.text.ifBlank { "[Note Block]" },
                            style = TextStyle(fontSize = 14.sp, fontFamily = fontFamily, color = textColor),
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }
            }
        }

        // Bottom reading spacer
        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}

/**
 * Parses inline markdown: bold (**text** or __text__), italic (*text* or _text_),
 * strikethrough (~~text~~), and inline code (`code`).
 */
fun renderInlineMarkdown(
    text: String,
    baseColor: Color,
    fontFamily: FontFamily
): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length
        while (i < len) {
            // Bold with **
            if (i + 1 < len && text[i] == '*' && text[i + 1] == '*') {
                val end = text.indexOf("**", i + 2)
                if (end != -1) {
                    val content = text.substring(i + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(content)
                    }
                    i = end + 2
                    continue
                }
            }
            // Bold with __
            if (i + 1 < len && text[i] == '_' && text[i + 1] == '_') {
                val end = text.indexOf("__", i + 2)
                if (end != -1) {
                    val content = text.substring(i + 2, end)
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(content)
                    }
                    i = end + 2
                    continue
                }
            }
            // Strikethrough with ~~
            if (i + 1 < len && text[i] == '~' && text[i + 1] == '~') {
                val end = text.indexOf("~~", i + 2)
                if (end != -1) {
                    val content = text.substring(i + 2, end)
                    withStyle(SpanStyle(textDecoration = TextDecoration.LineThrough)) {
                        append(content)
                    }
                    i = end + 2
                    continue
                }
            }
            // Inline code `code`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1) {
                    val content = text.substring(i + 1, end)
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = Color.Black.copy(alpha = 0.08f),
                            fontWeight = FontWeight.Medium
                        )
                    ) {
                        append(" $content ")
                    }
                    i = end + 1
                    continue
                }
            }
            // Italic with *
            if (text[i] == '*' && (i + 1 >= len || text[i + 1] != '*')) {
                val end = text.indexOf('*', i + 1)
                if (end != -1) {
                    val content = text.substring(i + 1, end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                    i = end + 1
                    continue
                }
            }
            // Italic with _
            if (text[i] == '_' && (i + 1 >= len || text[i + 1] != '_')) {
                val end = text.indexOf('_', i + 1)
                if (end != -1) {
                    val content = text.substring(i + 1, end)
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(content)
                    }
                    i = end + 1
                    continue
                }
            }

            append(text[i])
            i++
        }
    }
}
