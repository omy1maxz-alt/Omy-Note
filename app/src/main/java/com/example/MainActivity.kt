package com.example

import android.app.KeyguardManager
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.editor.NoteEditorScreen
import com.example.ui.settings.AchievementsScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.FocusModeBg
import com.example.ui.theme.MyApplicationTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    private val viewModel: MainViewModel by viewModels {
        MainViewModelFactory(
            context = applicationContext,
            noteRepository = appContainer.noteRepository,
            settingsRepository = appContainer.settingsRepository,
            aiRepository = appContainer.aiRepository
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        appContainer = AppContainer(applicationContext)

        // Handle open note from reminder notification
        val openNoteId = intent.getStringExtra("OPEN_NOTE_ID")
        if (!openNoteId.isNullOrBlank()) {
            viewModel.navigateTo(Screen.Editor(noteId = openNoteId))
        }

        enableEdgeToEdge()

        setContent {
            val settings by viewModel.settings.collectAsState()

            val isDark = when (settings.theme) {
                "dark" -> true
                "light" -> false
                else -> isSystemInDarkTheme()
            }

            MyApplicationTheme(darkTheme = isDark) {
                MainApp(
                    viewModel = viewModel,
                    onPromptDeviceUnlock = { promptUnlock() }
                )
            }
        }
    }

    private fun promptUnlock() {
        val km = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
        if (km != null && km.isDeviceSecure) {
            val intent = km.createConfirmDeviceCredentialIntent("AI Notes Locked", "Enter your credentials to unlock")
            if (intent != null) {
                unlockLauncher.launch(intent)
            } else {
                viewModel.setAppUnlocked(true)
            }
        } else {
            viewModel.setAppUnlocked(true)
        }
    }

    private val unlockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            viewModel.setAppUnlocked(true)
        }
    }
}

@Composable
fun MainApp(
    viewModel: MainViewModel,
    onPromptDeviceUnlock: () -> Unit
) {
    val currentScreen by viewModel.currentScreen.collectAsState()
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsState()
    val statusMessage by viewModel.statusMessage.collectAsState()
    val categories by viewModel.categories.collectAsState()

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // Show status snackbar whenever message is updated
    LaunchedEffect(statusMessage) {
        statusMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearStatusMessage()
        }
    }

    // App Lock Screen overlay if locked
    if (!isAppUnlocked) {
        LaunchedEffect(Unit) {
            onPromptDeviceUnlock()
        }

        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "App Locked",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "AI Notes is Locked",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Unlock with your device PIN, pattern, or fingerprint.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(28.dp))

                Button(
                    onClick = onPromptDeviceUnlock,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Unlock")
                }
            }
        }
        return
    }

    // Main App Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        gesturesEnabled = currentScreen is Screen.Dashboard,
        drawerContent = {
            ModalDrawerSheet {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(FocusModeBg)
                        .padding(24.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI Notes",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = Color(0xFFFF9800),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Private • Offline-First • Fast",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items
                NavigationDrawerItem(
                    label = { Text("All Notes") },
                    selected = currentScreen is Screen.Dashboard && viewModel.selectedCategory.value == null,
                    icon = { Icon(Icons.Default.Description, contentDescription = null) },
                    onClick = {
                        viewModel.selectedCategory.value = null
                        viewModel.navigateTo(Screen.Dashboard)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                // Categories list in drawer
                categories.forEach { cat ->
                    NavigationDrawerItem(
                        label = { Text(cat.name) },
                        selected = currentScreen is Screen.Dashboard && viewModel.selectedCategory.value == cat.id,
                        icon = { Icon(Icons.Default.Folder, contentDescription = null) },
                        onClick = {
                            viewModel.selectedCategory.value = cat.id
                            viewModel.navigateTo(Screen.Dashboard)
                            coroutineScope.launch { drawerState.close() }
                        },
                        modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                NavigationDrawerItem(
                    label = { Text("Achievements") },
                    selected = currentScreen is Screen.Achievements,
                    icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                    onClick = {
                        viewModel.navigateTo(Screen.Achievements)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )

                NavigationDrawerItem(
                    label = { Text("Settings") },
                    selected = currentScreen is Screen.Settings,
                    icon = { Icon(Icons.Default.Settings, contentDescription = null) },
                    onClick = {
                        viewModel.navigateTo(Screen.Settings)
                        coroutineScope.launch { drawerState.close() }
                    },
                    modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
                )
            }
        }
    ) {
        when (val screen = currentScreen) {
            is Screen.Dashboard -> {
                DashboardScreen(
                    viewModel = viewModel,
                    snackbarHostState = snackbarHostState,
                    onOpenDrawer = { coroutineScope.launch { drawerState.open() } },
                    onExportBackupClick = { viewModel.navigateTo(Screen.Settings) }
                )
            }

            is Screen.Editor -> {
                NoteEditorScreen(
                    noteId = screen.noteId,
                    initialCategory = screen.initialCategory,
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }

            is Screen.Achievements -> {
                AchievementsScreen(
                    viewModel = viewModel,
                    onBack = { viewModel.navigateBack() }
                )
            }
        }
    }
}
