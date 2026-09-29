package com.example.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.data.model.BlockType
import com.example.util.TextStats

data class BadgeItem(
    val id: String,
    val title: String,
    val description: String,
    val iconEmoji: String,
    val current: Int,
    val target: Int
) {
    val isUnlocked: Boolean get() = current >= target
    val progress: Float get() = (current.toFloat() / target).coerceIn(0f, 1f)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AchievementsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val notes by viewModel.allNotes.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val totalNotes = notes.size
    val totalWords = remember(notes) {
        notes.sumOf { note ->
            val text = note.title + " " + note.blocks.joinToString(" ") { it.content }
            TextStats.calculate(text).words
        }
    }
    val handwritingNotesCount = remember(notes) {
        notes.count { it.blocks.any { b -> b.type == BlockType.HANDWRITING && b.strokes.isNotEmpty() } }
    }
    val checklistItemsCount = remember(notes) {
        notes.sumOf { it.blocks.count { b -> b.type == BlockType.CHECKLIST && b.checked } }
    }
    val categoriesCount = categories.size

    val badges = listOf(
        BadgeItem(
            id = "first_note",
            title = "First Note",
            description = "Create your very first note",
            iconEmoji = "✍️",
            current = totalNotes,
            target = 1
        ),
        BadgeItem(
            id = "wordsmith",
            title = "Wordsmith",
            description = "Write 500 words across all your notes",
            iconEmoji = "📚",
            current = totalWords,
            target = 500
        ),
        BadgeItem(
            id = "doodle_master",
            title = "Doodle Artist",
            description = "Draw hand-drawn strokes or sketches in a note",
            iconEmoji = "🎨",
            current = handwritingNotesCount,
            target = 1
        ),
        BadgeItem(
            id = "task_crusher",
            title = "Task Crusher",
            description = "Complete 5 checklist items",
            iconEmoji = "✅",
            current = checklistItemsCount,
            target = 5
        ),
        BadgeItem(
            id = "master_organizer",
            title = "Master Organizer",
            description = "Organize notes across 3 categories",
            iconEmoji = "📁",
            current = categoriesCount,
            target = 3
        ),
        BadgeItem(
            id = "knowledge_base",
            title = "Knowledge Base",
            description = "Accumulate 10 or more notes locally",
            iconEmoji = "🧠",
            current = totalNotes,
            target = 10
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Achievements",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                val unlockedCount = badges.count { it.isUnlocked }
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Badges Unlocked: $unlockedCount / ${badges.size}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { unlockedCount.toFloat() / badges.size },
                            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp))
                        )
                    }
                }
            }

            items(badges) { badge ->
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.isUnlocked) MaterialTheme.colorScheme.surface
                        else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ),
                    elevation = CardDefaults.cardElevation(defaultElevation = if (badge.isUnlocked) 2.dp else 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(CircleShape)
                                .background(
                                    if (badge.isUnlocked) MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else Color.LightGray.copy(alpha = 0.3f)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.iconEmoji, fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = badge.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                if (badge.isUnlocked) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Unlocked",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Locked",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge.description,
                                fontSize = 13.sp,
                                color = Color.Gray
                            )

                            if (!badge.isUnlocked) {
                                Spacer(modifier = Modifier.height(6.dp))
                                LinearProgressIndicator(
                                    progress = { badge.progress },
                                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp))
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${badge.current} / ${badge.target}",
                                    fontSize = 11.sp,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
