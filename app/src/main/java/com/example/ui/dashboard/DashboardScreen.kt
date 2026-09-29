package com.example.ui.dashboard

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ViewAgenda
import androidx.compose.material.icons.outlined.Description
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt
import com.example.MainViewModel
import com.example.Screen
import com.example.data.model.BlockType
import com.example.data.model.Category
import com.example.data.model.Note
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.FocusModeBg
import com.example.ui.theme.VipOrangeGradientEnd
import com.example.ui.theme.VipOrangeGradientStart
import com.example.ui.theme.getFontFamily
import com.example.ui.theme.getNoteColor
import com.example.util.BitmapLoader
import com.example.util.DateFormats
import com.example.util.NoteTextStats
import com.example.util.TextStats
import java.io.File
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    snackbarHostState: SnackbarHostState,
    onOpenDrawer: () -> Unit,
    onExportBackupClick: () -> Unit
) {
    val context = LocalContext.current
    val notes by viewModel.filteredNotes.collectAsState()
    val categories by viewModel.categories.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val isGridView by viewModel.isGridView.collectAsState()
    val scopeTab by viewModel.scopeTab.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedNoteIds by viewModel.selectedNoteIds.collectAsState()
    val isMultiSelect by viewModel.isMultiSelectActive.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    var isSearchExpanded by remember { mutableStateOf(false) }
    var showOverflowMenu by remember { mutableStateOf(false) }
    var showSortDialog by remember { mutableStateOf(false) }
    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var showManageCategoriesDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var noteToSplit by remember { mutableStateOf<Note?>(null) }

    val todayDayNumber = remember { Calendar.getInstance().get(Calendar.DAY_OF_MONTH).toString() }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(FocusModeBg)) {
                // Main Top Bar
                TopAppBar(
                    title = {
                        if (isSearchExpanded) {
                            TextField(
                                value = searchQuery,
                                onValueChange = { viewModel.onSearchQueryChanged(it) },
                                placeholder = {
                                    Text(
                                        if (settings.geminiEnabled && settings.smartSearchEnabled)
                                            "Gemini Smart Search..."
                                        else
                                            "Search notes...",
                                        color = Color.White.copy(alpha = 0.7f),
                                        fontSize = 15.sp
                                    )
                                },
                                singleLine = true,
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White,
                                    cursorColor = Color.White,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )
                        } else {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "AI Notes",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 20.sp
                                )

                                // Orange gradient VIP Pill
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(VipOrangeGradientStart, VipOrangeGradientEnd)
                                            )
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "VIP",
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "VIP",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    },
                    navigationIcon = {
                        if (isSearchExpanded) {
                            IconButton(onClick = {
                                isSearchExpanded = false
                                viewModel.onSearchQueryChanged("")
                            }) {
                                Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.White)
                            }
                        } else {
                            IconButton(onClick = onOpenDrawer) {
                                Box {
                                    Icon(Icons.Default.Menu, contentDescription = "Menu drawer", tint = Color.White)
                                    // Badge dot
                                    Box(
                                        modifier = Modifier
                                            .size(7.dp)
                                            .align(Alignment.TopEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFFFF9800))
                                    )
                                }
                            }
                        }
                    },
                    actions = {
                        if (!isSearchExpanded) {
                            // Search icon
                            IconButton(onClick = { isSearchExpanded = true }) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = Color.White)
                            }

                            // Calendar icon with today's number
                            IconButton(onClick = {
                                viewModel.showMessage("Filter: Showing today's notes")
                            }) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        Icons.Default.CalendarToday,
                                        contentDescription = "Calendar",
                                        tint = Color.White
                                    )
                                    Text(
                                        text = todayDayNumber,
                                        color = FocusModeBg,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // Overflow menu
                            IconButton(onClick = { showOverflowMenu = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "More options", tint = Color.White)
                            }

                            DropdownMenu(
                                expanded = showOverflowMenu,
                                onDismissRequest = { showOverflowMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Sort notes") },
                                    leadingIcon = { Icon(Icons.Default.Label, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        showSortDialog = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(if (isGridView) "Switch to List View" else "Switch to Grid View") },
                                    leadingIcon = {
                                        Icon(
                                            if (isGridView) Icons.Default.ViewAgenda else Icons.Default.GridView,
                                            contentDescription = null
                                        )
                                    },
                                    onClick = {
                                        showOverflowMenu = false
                                        viewModel.isGridView.value = !isGridView
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Backup to ZIP") },
                                    leadingIcon = { Icon(Icons.Default.ContentCopy, contentDescription = null) },
                                    onClick = {
                                        showOverflowMenu = false
                                        onExportBackupClick()
                                    }
                                )
                            }
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = FocusModeBg
                    )
                )

                // Scope Tabs: All | Notes
                TabRow(
                    selectedTabIndex = scopeTab,
                    containerColor = FocusModeBg,
                    contentColor = Color(0xFFFF9800),
                    indicator = { tabPositions ->
                        TabRowDefaults.Indicator(
                            Modifier.tabIndicatorOffset(tabPositions[scopeTab]),
                            color = Color(0xFFFF9800),
                            height = 3.dp
                        )
                    }
                ) {
                    Tab(
                        selected = scopeTab == 0,
                        onClick = { viewModel.scopeTab.value = 0 },
                        text = {
                            Text(
                                text = "All",
                                color = if (scopeTab == 0) Color(0xFFFF9800) else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (scopeTab == 0) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                    Tab(
                        selected = scopeTab == 1,
                        onClick = { viewModel.scopeTab.value = 1 },
                        text = {
                            Text(
                                text = "Notes",
                                color = if (scopeTab == 1) Color(0xFFFF9800) else Color.White.copy(alpha = 0.7f),
                                fontWeight = if (scopeTab == 1) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }

                // Horizontal Category Chips
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().background(FocusModeBg.copy(alpha = 0.95f))
                ) {
                    item {
                        CategoryChip(
                            label = "All",
                            isSelected = selectedCategory == null,
                            onClick = { viewModel.selectedCategory.value = null }
                        )
                    }

                    items(categories) { cat ->
                        CategoryChip(
                            label = cat.name,
                            isSelected = selectedCategory == cat.id,
                            onClick = { viewModel.selectedCategory.value = cat.id }
                        )
                    }

                    // Trailing manage / add category button
                    item {
                        IconButton(
                            onClick = { showManageCategoriesDialog = true },
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add category",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            Column {
                // Multi-select action banner
                AnimatedVisibility(visible = isMultiSelect) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedNoteIds.size} selected",
                                style = MaterialTheme.typography.titleSmall
                            )
                            Row {
                                TextButton(onClick = { viewModel.deleteSelectedNotes() }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Delete", color = Color.Red)
                                }
                                IconButton(onClick = { viewModel.clearSelection() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cancel")
                                }
                            }
                        }
                    }
                }

                // Standard Bottom Bar
                NavigationBar {
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.Editor(initialCategory = selectedCategory)) },
                        icon = { Icon(Icons.Default.Edit, contentDescription = "New Note") },
                        label = { Text("New Note") }
                    )
                    NavigationBarItem(
                        selected = true,
                        onClick = { /* Already on notes */ },
                        icon = { Icon(Icons.Outlined.Description, contentDescription = "Notes") },
                        label = { Text("Notes") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.showMessage("Inbox is clear.") },
                        icon = { Icon(Icons.Default.Inbox, contentDescription = "Inbox") },
                        label = { Text("Inbox") }
                    )
                    NavigationBarItem(
                        selected = false,
                        onClick = { viewModel.navigateTo(Screen.Settings) },
                        icon = { Icon(Icons.Default.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.navigateTo(Screen.Editor(initialCategory = selectedCategory)) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add new note")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (notes.isEmpty()) {
                // Empty state illustration
                Column(
                    modifier = Modifier.fillMaxSize().padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Description,
                        contentDescription = "Empty notes",
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(72.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotBlank()) "No notes match your search" else "No notes yet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tap the + button below to create your first note.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            } else if (isGridView) {
                // Masonry 2-Column Balanced Layout using Row of 2 LazyColumns
                val col1Notes = remember(notes) { notes.filterIndexed { index, _ -> index % 2 == 0 } }
                val col2Notes = remember(notes) { notes.filterIndexed { index, _ -> index % 2 == 1 } }

                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(col1Notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isSelected = selectedNoteIds.contains(note.id),
                                showUpdatedDate = settings.displayUpdatedDate,
                                dateFormat = settings.dateFormat,
                                onClick = {
                                    if (isMultiSelect) {
                                        viewModel.toggleNoteSelection(note.id)
                                    } else {
                                        viewModel.navigateTo(Screen.Editor(note.id))
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleNoteSelection(note.id)
                                },
                                onSplitClick = {
                                    noteToSplit = note
                                },
                                onMove = { delta ->
                                    viewModel.moveNotePosition(note.id, delta)
                                }
                            )
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(col2Notes, key = { it.id }) { note ->
                            NoteCard(
                                note = note,
                                isSelected = selectedNoteIds.contains(note.id),
                                showUpdatedDate = settings.displayUpdatedDate,
                                dateFormat = settings.dateFormat,
                                onClick = {
                                    if (isMultiSelect) {
                                        viewModel.toggleNoteSelection(note.id)
                                    } else {
                                        viewModel.navigateTo(Screen.Editor(note.id))
                                    }
                                },
                                onLongClick = {
                                    viewModel.toggleNoteSelection(note.id)
                                },
                                onSplitClick = {
                                    noteToSplit = note
                                },
                                onMove = { delta ->
                                    viewModel.moveNotePosition(note.id, delta)
                                }
                            )
                        }
                    }
                }
            } else {
                // List Mode with Detailed Stats
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(top = 10.dp, bottom = 80.dp, start = 12.dp, end = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(notes, key = { it.id }) { note ->
                        NoteListRow(
                            note = note,
                            isSelected = selectedNoteIds.contains(note.id),
                            showUpdatedDate = settings.displayUpdatedDate,
                            dateFormat = settings.dateFormat,
                            onClick = {
                                if (isMultiSelect) {
                                    viewModel.toggleNoteSelection(note.id)
                                } else {
                                    viewModel.navigateTo(Screen.Editor(note.id))
                                }
                            },
                            onLongClick = { viewModel.toggleNoteSelection(note.id) },
                            onDuplicate = { viewModel.duplicateNote(note) },
                            onDelete = { viewModel.deleteNote(note.id) },
                            onExportPdf = {
                                viewModel.showMessage("Use Editor to choose PDF destination")
                            },
                            onSplitClick = {
                                noteToSplit = note
                            },
                            onMove = { delta ->
                                viewModel.moveNotePosition(note.id, delta)
                            }
                        )
                    }
                }
            }

            // AI loading indicator overlay
            if (isAiLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Text("Gemini AI is processing...")
                        }
                    }
                }
            }
        }
    }

    // Sort Order Selection Dialog
    if (showSortDialog) {
        AlertDialog(
            onDismissRequest = { showSortDialog = false },
            title = { Text("Sort notes by") },
            text = {
                Column {
                    listOf(
                        "custom" to "Custom position (Hold & drag to arrange)",
                        "modified_desc" to "Last modified, newest first",
                        "modified_asc" to "Last modified, oldest first",
                        "created_desc" to "Creation date, newest first",
                        "title_asc" to "Title (A to Z)"
                    ).forEach { (id, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (settings.sortOrder == id) MaterialTheme.colorScheme.primaryContainer
                                    else Color.Transparent
                                )
                                .padding(12.dp)
                                .clickable {
                                    viewModel.updateSortOrder(id)
                                    showSortDialog = false
                                }
                        ) {
                            Text(
                                text = label,
                                fontWeight = if (settings.sortOrder == id) FontWeight.Bold else FontWeight.Normal,
                                color = if (settings.sortOrder == id) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showSortDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    // Manage Categories Dialog
    if (showManageCategoriesDialog) {
        AlertDialog(
            onDismissRequest = { showManageCategoriesDialog = false },
            title = { Text("Categories") },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyColumn(modifier = Modifier.height(180.dp)) {
                        items(categories) { cat ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(cat.name, style = MaterialTheme.typography.bodyLarge)
                                IconButton(onClick = { viewModel.deleteCategory(cat.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Gray)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("New Category") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newCategoryName.isNotBlank()) {
                        viewModel.addCategory(newCategoryName)
                        newCategoryName = ""
                    }
                    showManageCategoriesDialog = false
                }) {
                    Text("Add & Close")
                }
            },
            dismissButton = {
                TextButton(onClick = { showManageCategoriesDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Split Note by Character Count Dialog
    noteToSplit?.let { note ->
        val fullText = remember(note) {
            val joined = note.blocks.joinToString("\n") { it.content }
            if (joined.isNotBlank()) joined else note.title
        }
        val totalChars = fullText.length
        var splitCharInput by remember(note) {
            val initial = if (totalChars > 100) "100" else (totalChars / 2).coerceAtLeast(1).toString()
            mutableStateOf(initial)
        }

        val parsedSplit = splitCharInput.toIntOrNull() ?: 0
        val isValid = parsedSplit in 1 until totalChars
        val remainingChars = (totalChars - parsedSplit).coerceAtLeast(0)

        AlertDialog(
            onDismissRequest = { noteToSplit = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CallSplit,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Split Note by Characters")
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Separate text of \"${note.title.ifBlank { "Untitled Note" }}\" into two notes.",
                        style = MaterialTheme.typography.bodyMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "Total Note Length: $totalChars characters",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            if (isValid) {
                                Text(
                                    text = "• Leave in original note: first $parsedSplit characters",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "• Split to new note: remaining $remainingChars characters",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF2E7D32)
                                )
                            } else {
                                Text(
                                    text = "Please input a count between 1 and ${totalChars - 1} characters.",
                                    fontSize = 12.sp,
                                    color = Color.Red
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = splitCharInput,
                        onValueChange = { input ->
                            if (input.all { it.isDigit() }) {
                                splitCharInput = input
                            }
                        },
                        label = { Text("Characters to leave in original note") },
                        placeholder = { Text("e.g. 100") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Preset quick selection chips
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(50, 100, 150, totalChars / 2).distinct().filter { it in 1 until totalChars }.forEach { count ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { splitCharInput = count.toString() }
                            ) {
                                Text(
                                    text = "$count chars",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (isValid) {
                            viewModel.splitNoteByCharacters(note.id, parsedSplit)
                            noteToSplit = null
                        }
                    },
                    enabled = isValid
                ) {
                    Text("Split Note")
                }
            },
            dismissButton = {
                TextButton(onClick = { noteToSplit = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun CategoryChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (isSelected) Color(0xFFFF9800) else Color.White.copy(alpha = 0.2f),
        modifier = Modifier.clip(RoundedCornerShape(16.dp)).clickable { onClick() }
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.White.copy(alpha = 0.9f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteCard(
    note: Note,
    isSelected: Boolean,
    showUpdatedDate: Boolean,
    dateFormat: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onSplitClick: () -> Unit,
    onMove: (delta: Int) -> Unit
) {
    val context = LocalContext.current
    val tintColor = getNoteColor(note.tint)
    val fontFamily = getFontFamily(note.font)

    var isDragging by remember { mutableStateOf(false) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    val imageBlock = note.blocks.firstOrNull { it.type == BlockType.IMAGE && !it.imagePath.isNullOrBlank() }
    val loadedBitmap by remember(imageBlock?.imagePath) {
        val path = imageBlock?.imagePath
        val bmp = if (!path.isNullOrBlank()) {
            val file = File(context.filesDir, path)
            BitmapLoader.loadScaledBitmap(file, 400, 250)
        } else null
        mutableStateOf(bmp)
    }

    val previewText = remember(note.blocks) {
        val sb = StringBuilder()
        for (block in note.blocks) {
            if (block.content.isNotBlank()) {
                val needed = 200 - sb.length
                if (needed <= 0) break
                sb.append(block.content.take(needed)).append(" ")
            }
        }
        sb.toString().trim()
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = tintColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 10.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, accumulatedDragY.roundToInt()) }
            .scale(if (isDragging) 1.04f else 1f)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = if (isDragging) 2.5.dp else if (isSelected) 2.5.dp else 0.dp,
                color = if (isDragging) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header: Pinned indicator & Drag Handle + Quick Move controls
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pinned", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                // Drag on hold & Quick move buttons to adjust position
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(
                        onClick = { onMove(-1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowUpward,
                            contentDescription = "Move note up",
                            tint = Color.DarkGray.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    IconButton(
                        onClick = { onMove(1) },
                        modifier = Modifier.size(26.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowDownward,
                            contentDescription = "Move note down",
                            tint = Color.DarkGray.copy(alpha = 0.8f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                    // Drag handle zone: Hold to drag and adjust position
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .pointerInput(note.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        isDragging = true
                                        accumulatedDragY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDragY += dragAmount.y
                                        if (accumulatedDragY > 50f) {
                                            onMove(1)
                                            accumulatedDragY = 0f
                                        } else if (accumulatedDragY < -50f) {
                                            onMove(-1)
                                            accumulatedDragY = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        accumulatedDragY = 0f
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        accumulatedDragY = 0f
                                    }
                                )
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Hold to drag and reorder",
                                tint = if (isDragging) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isDragging) "Moving" else "Hold drag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDragging) MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Clickable Note Body: Tapping here opens the Note Editor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(vertical = 2.dp)
            ) {
                // Optional 16:9 image thumbnail
                if (loadedBitmap != null) {
                    Image(
                        bitmap = loadedBitmap!!.asImageBitmap(),
                        contentDescription = "Note image preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(110.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .padding(bottom = 8.dp)
                    )
                }

                // Title
                if (note.title.isNotBlank()) {
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        fontFamily = fontFamily,
                        color = Color.Black,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                }

                // 3-6 lines preview
                if (previewText.isNotBlank()) {
                    Text(
                        text = previewText,
                        fontSize = 13.sp,
                        fontFamily = fontFamily,
                        color = Color.DarkGray,
                        maxLines = 5,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }

                // Tags chips
                if (note.tags.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        note.tags.take(3).forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.08f)
                            ) {
                                Text(
                                    text = "#$tag",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color.DarkGray,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Bottom action row: Date/Time caption + Dedicated Box Button to Split Note
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showUpdatedDate) {
                    Text(
                        text = DateFormats.formatNoteTimestamp(note.updatedAt, dateFormat),
                        fontSize = 11.sp,
                        color = Color.Gray,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // Dedicated Box button to split note by char count (DOES NOT OPEN NOTE)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E5B5B), // High-contrast dark teal box button
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .testTag("split_char_box_btn_${note.id}")
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onSplitClick() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CallSplit,
                            contentDescription = "Split text based on characters",
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Split [char]",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteListRow(
    note: Note,
    isSelected: Boolean,
    showUpdatedDate: Boolean,
    dateFormat: String,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onExportPdf: () -> Unit,
    onSplitClick: () -> Unit,
    onMove: (delta: Int) -> Unit
) {
    val tintColor = getNoteColor(note.tint)
    var isDragging by remember { mutableStateOf(false) }
    var accumulatedDragY by remember { mutableFloatStateOf(0f) }

    val previewText = remember(note.blocks) {
        val sb = StringBuilder()
        for (block in note.blocks) {
            if (block.content.isNotBlank()) {
                val needed = 120 - sb.length
                if (needed <= 0) break
                sb.append(block.content.take(needed)).append(" ")
            }
        }
        sb.toString().trim()
    }

    val stats: NoteTextStats = remember(note) {
        // Calculate stats by aggregating block text stats without huge string concatenation
        var chars = note.title.length
        var charsNoSpaces = note.title.count { !it.isWhitespace() }
        var words = 0
        var sentences = 0
        var paragraphs = 0

        for (b in note.blocks) {
            chars += b.content.length
        }

        // Fast sample-based or aggregate calculate
        val titleStats = TextStats.calculate(note.title)
        words += titleStats.words
        sentences += titleStats.sentences

        for (b in note.blocks) {
            if (b.content.isNotBlank()) {
                val bStats = TextStats.calculate(b.content)
                charsNoSpaces += bStats.charactersNoSpaces
                words += bStats.words
                sentences += bStats.sentences
                paragraphs += bStats.paragraphs
            }
        }

        NoteTextStats(
            characters = chars,
            charactersNoSpaces = charsNoSpaces,
            words = words,
            sentences = sentences.coerceAtLeast(if (words > 0) 1 else 0),
            paragraphs = paragraphs.coerceAtLeast(if (words > 0) 1 else 0)
        )
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = tintColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDragging) 10.dp else 2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .offset { IntOffset(0, accumulatedDragY.roundToInt()) }
            .scale(if (isDragging) 1.03f else 1f)
            .clip(RoundedCornerShape(14.dp))
            .border(
                width = if (isDragging) 2.5.dp else if (isSelected) 2.dp else 0.dp,
                color = if (isDragging) MaterialTheme.colorScheme.primary else if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = RoundedCornerShape(14.dp)
            )
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header: Pinned indicator & Reorder Controls / Drag Handle
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (note.isPinned) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            tint = Color.DarkGray,
                            modifier = Modifier.size(16.dp).padding(end = 4.dp)
                        )
                        Text("Pinned", fontSize = 11.sp, color = Color.DarkGray, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    IconButton(onClick = { onMove(-1) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.ArrowUpward, contentDescription = "Move up", tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                    }
                    IconButton(onClick = { onMove(1) }, modifier = Modifier.size(26.dp)) {
                        Icon(Icons.Default.ArrowDownward, contentDescription = "Move down", tint = Color.DarkGray, modifier = Modifier.size(15.dp))
                    }
                    // Drag handle zone
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isDragging) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.08f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .pointerInput(note.id) {
                                detectDragGesturesAfterLongPress(
                                    onDragStart = {
                                        isDragging = true
                                        accumulatedDragY = 0f
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        accumulatedDragY += dragAmount.y
                                        if (accumulatedDragY > 50f) {
                                            onMove(1)
                                            accumulatedDragY = 0f
                                        } else if (accumulatedDragY < -50f) {
                                            onMove(-1)
                                            accumulatedDragY = 0f
                                        }
                                    },
                                    onDragEnd = {
                                        isDragging = false
                                        accumulatedDragY = 0f
                                    },
                                    onDragCancel = {
                                        isDragging = false
                                        accumulatedDragY = 0f
                                    }
                                )
                            }
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DragHandle,
                                contentDescription = "Hold to drag and reorder",
                                tint = if (isDragging) MaterialTheme.colorScheme.primary else Color.DarkGray,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = if (isDragging) "Moving" else "Hold drag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isDragging) MaterialTheme.colorScheme.primary else Color.DarkGray
                            )
                        }
                    }
                }
            }

            // Clickable Note Body: Tapping here opens Note Editor
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .combinedClickable(
                        onClick = onClick,
                        onLongClick = onLongClick
                    )
                    .padding(vertical = 4.dp)
            ) {
                Text(
                    text = note.title.ifBlank { "Untitled Note" },
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))
                if (previewText.isNotBlank()) {
                    Text(
                        text = previewText,
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // TextStats metadata banner: Chars, Words, Sentences, Paragraphs
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${stats.characters} chars", fontSize = 11.sp, color = Color.Gray)
                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                    Text("${stats.words} words", fontSize = 11.sp, color = Color.Gray)
                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                    Text("${stats.sentences} sent", fontSize = 11.sp, color = Color.Gray)
                    Text("•", fontSize = 11.sp, color = Color.LightGray)
                    Text("${stats.paragraphs} para", fontSize = 11.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Action row: Date, Dedicated Box Button to Split, Duplicate, Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (showUpdatedDate) {
                    Text(
                        text = DateFormats.formatNoteTimestamp(note.updatedAt, dateFormat),
                        fontSize = 11.sp,
                        color = Color.Gray
                    )
                } else {
                    Spacer(modifier = Modifier.width(1.dp))
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Dedicated Box button to split text based on char count (DOES NOT OPEN NOTE)
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF1E5B5B), // High-contrast dark teal box button
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.35f)),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .testTag("split_char_box_btn_list_${note.id}")
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onSplitClick() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CallSplit,
                                contentDescription = "Split text based on characters",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Split [char]",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    IconButton(onClick = onDuplicate, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Duplicate", tint = Color.DarkGray, modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}
