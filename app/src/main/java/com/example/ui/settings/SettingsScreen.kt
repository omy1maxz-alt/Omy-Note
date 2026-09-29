package com.example.ui.settings

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FontDownload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainViewModel
import com.example.Screen
import com.example.ui.components.NoteTintRow
import com.example.util.DateFormats
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val settings by viewModel.settings.collectAsState()

    var showApiKeyDialog by remember { mutableStateOf(false) }
    var apiKeyInput by remember { mutableStateOf(settings.geminiApiKey) }
    var isApiKeyVisible by remember { mutableStateOf(false) }

    var showModelDialog by remember { mutableStateOf(false) }
    var modelInput by remember { mutableStateOf(settings.geminiModel) }

    var showDateFormatDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showTintDialog by remember { mutableStateOf(false) }
    var showFontDialog by remember { mutableStateOf(false) }
    var showStartDayDialog by remember { mutableStateOf(false) }
    var showRestoreConfirmDialog by remember { mutableStateOf(false) }

    // SAF Backup create document
    val backupLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/zip")
    ) { uri ->
        if (uri != null) {
            coroutineScope.launch {
                context.contentResolver.openOutputStream(uri)?.use { os: OutputStream ->
                    viewModel.exportBackup(os)
                }
            }
        }
    }

    // SAF Restore open document
    var selectedRestoreUri by remember { mutableStateOf<android.net.Uri?>(null) }
    val restoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            selectedRestoreUri = uri
            showRestoreConfirmDialog = true
        }
    }

    // Device credential lock launcher
    val lockLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            viewModel.updateAppLockEnabled(!settings.appLockEnabled)
            viewModel.showMessage(if (!settings.appLockEnabled) "App lock enabled" else "App lock disabled")
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // SECTION: General
            item {
                SettingsSectionHeader("General")
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "System Default",
                    onClick = { viewModel.showMessage("App language is set to device default.") }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Notifications,
                    title = "Notifications & Reminders",
                    subtitle = "Manage note alarm alerts and channels",
                    onClick = {
                        val intent = Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(android.provider.Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        try {
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            viewModel.showMessage("Reminders use system AlarmManager.")
                        }
                    }
                )
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.Lock,
                    title = "Application Lock",
                    subtitle = "Require device credential (PIN/pattern/biometric) on app launch",
                    checked = settings.appLockEnabled,
                    onCheckedChange = { enabled ->
                        val km = context.getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
                        if (km != null && km.isDeviceSecure) {
                            val intent = km.createConfirmDeviceCredentialIntent("Unlock AI Notes", "Confirm your identity")
                            if (intent != null) {
                                lockLauncher.launch(intent)
                            } else {
                                viewModel.updateAppLockEnabled(enabled)
                            }
                        } else {
                            viewModel.showMessage("No device security (PIN/password) is set on this device.")
                        }
                    }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.EmojiEvents,
                    title = "Achievements",
                    subtitle = "View your local productivity badges",
                    onClick = { viewModel.navigateTo(Screen.Achievements) }
                )
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.DateRange,
                    title = "Skip Splash Screen",
                    subtitle = "Launch directly into dashboard",
                    checked = settings.skipSplash,
                    onCheckedChange = { viewModel.updateSkipSplash(it) }
                )
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.Star,
                    title = "VIP / Demo Features",
                    subtitle = "Local development flag (No Play Billing required)",
                    checked = settings.premiumDemo,
                    onCheckedChange = { viewModel.updatePremiumDemo(it) }
                )
            }

            // SECTION: Editing Preference
            item {
                SettingsSectionHeader("Editing Preference")
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.ContentPaste,
                    title = "Clipboard Detection",
                    subtitle = "Detect copied content and simplify pasting",
                    checked = settings.clipboardDetection,
                    onCheckedChange = { viewModel.updateClipboardDetection(it) }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.DarkMode,
                    title = "Theme",
                    subtitle = settings.theme.replaceFirstChar { it.uppercase() },
                    onClick = { showThemeDialog = true }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.ColorLens,
                    title = "Default Note Tint",
                    subtitle = settings.defaultTint.replaceFirstChar { it.uppercase() },
                    onClick = { showTintDialog = true }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.FontDownload,
                    title = "Default Font",
                    subtitle = when (settings.font) {
                        "serif" -> "Serif (Editorial)"
                        "mono" -> "Monospace (Code)"
                        else -> "Sans-Serif (Modern)"
                    },
                    onClick = { showFontDialog = true }
                )
            }

            // SECTION: Display & Date
            item {
                SettingsSectionHeader("Display & Date")
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.CalendarToday,
                    title = "Date Format",
                    subtitle = "${settings.dateFormat} (Preview: ${DateFormats.formatNoteTimestamp(System.currentTimeMillis(), settings.dateFormat)})",
                    onClick = { showDateFormatDialog = true }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.DateRange,
                    title = "Start Day of Week",
                    subtitle = settings.startDayOfWeek,
                    onClick = { showStartDayDialog = true }
                )
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.DateRange,
                    title = "Display updated date/time",
                    subtitle = "Show the date on notes in the list screen",
                    checked = settings.displayUpdatedDate,
                    onCheckedChange = { viewModel.updateDisplayUpdatedDate(it) }
                )
            }

            // SECTION: Data Management
            item {
                SettingsSectionHeader("Data Management")
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Download,
                    title = "Back up notes",
                    subtitle = "Save all notes, media, and handwriting to a ZIP file via SAF",
                    onClick = {
                        val fileName = "AINotes_Backup_${System.currentTimeMillis()}.zip"
                        backupLauncher.launch(fileName)
                    }
                )
            }

            item {
                SettingsActionRow(
                    icon = Icons.Default.Upload,
                    title = "Restore notes",
                    subtitle = "Load notes from a backup ZIP file (Merge or Replace)",
                    onClick = {
                        restoreLauncher.launch("application/zip")
                    }
                )
            }

            // SECTION: Google Gemini AI
            item {
                SettingsSectionHeader("Google Gemini AI")
            }

            item {
                SettingsSwitchRow(
                    icon = Icons.Default.Psychology,
                    title = "Enable Gemini Features",
                    subtitle = "Master switch for AI summarization, proofreading, and smart search",
                    checked = settings.geminiEnabled,
                    onCheckedChange = { viewModel.updateGeminiEnabled(it) }
                )
            }

            if (settings.geminiEnabled) {
                item {
                    val keyDisplay = if (settings.geminiApiKey.isNotBlank()) "••••••••••••••••" else "Not set (Tap to configure)"
                    SettingsActionRow(
                        icon = Icons.Default.Key,
                        title = "Gemini API Key",
                        subtitle = keyDisplay,
                        onClick = {
                            apiKeyInput = settings.geminiApiKey
                            showApiKeyDialog = true
                        }
                    )
                }

                item {
                    SettingsActionRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Selected Model",
                        subtitle = settings.geminiModel,
                        onClick = {
                            modelInput = settings.geminiModel
                            showModelDialog = true
                        }
                    )
                }

                item {
                    SettingsSwitchRow(
                        icon = Icons.Default.AutoAwesome,
                        title = "Auto-Tag & Auto-Categorize",
                        subtitle = "Provide smart hashtags and suggested categories",
                        checked = settings.autoTagAndCategorize,
                        onCheckedChange = { viewModel.updateAutoTagAndCategorize(it) }
                    )
                }

                item {
                    SettingsSwitchRow(
                        icon = Icons.Default.Search,
                        title = "Gemini Smart Search",
                        subtitle = "Semantically re-rank local search candidate matches",
                        checked = settings.smartSearchEnabled,
                        onCheckedChange = { viewModel.updateSmartSearchEnabled(it) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Gemini API Key Dialog
    if (showApiKeyDialog) {
        AlertDialog(
            onDismissRequest = { showApiKeyDialog = false },
            title = { Text("Gemini API Key") },
            text = {
                Column {
                    Text(
                        text = "Enter your Google Gemini API key. It is stored privately in DataStore on your device and never exported or logged.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = apiKeyInput,
                        onValueChange = { apiKeyInput = it },
                        label = { Text("API Key") },
                        singleLine = true,
                        visualTransformation = if (isApiKeyVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isApiKeyVisible = !isApiKeyVisible }) {
                                Icon(
                                    imageVector = if (isApiKeyVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = "Toggle key visibility"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateGeminiApiKey(apiKeyInput)
                    showApiKeyDialog = false
                    viewModel.showMessage("API Key saved locally")
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showApiKeyDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Gemini Model Selection Dialog
    if (showModelDialog) {
        val modelOptions = listOf(
            Triple("gemini-3.5-flash", "Gemini 3.5 Flash", "Recommended • High intelligence, fast generation & smart search"),
            Triple("gemini-3.5-flash-lite", "Gemini 3.5 Flash Lite", "Ultra-low latency & quick formatting"),
            Triple("gemini-3.8-flash", "Gemini 3.8 Flash", "Advanced multi-step reasoning & agent tasks"),
            Triple("gemini-3.1-pro-preview", "Gemini 3.1 Pro", "Deep analysis, creative writing & math"),
            Triple("gemini-2.5-flash", "Gemini 2.5 Flash", "Fast, balanced general note assistant"),
            Triple("gemini-2.5-pro", "Gemini 2.5 Pro", "Extended context & synthesis"),
            Triple("gemini-flash-latest", "Gemini Flash (Latest)", "Always tracks the latest stable Flash release")
        )
        var customModelInput by remember { mutableStateOf(settings.geminiModel) }

        AlertDialog(
            onDismissRequest = { showModelDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select Gemini Model", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(modelOptions) { (mId, mTitle, mDesc) ->
                        val isSelected = settings.geminiModel == mId
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color.Transparent,
                            border = if (isSelected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    modelInput = mId
                                    customModelInput = mId
                                    viewModel.updateGeminiModel(mId)
                                    showModelDialog = false
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = {
                                        modelInput = mId
                                        customModelInput = mId
                                        viewModel.updateGeminiModel(mId)
                                        showModelDialog = false
                                    }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(mTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                    Text(mId, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                    Text(mDesc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("Or type custom model identifier:", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = customModelInput,
                                onValueChange = { customModelInput = it },
                                placeholder = { Text("e.g. gemini-3.5-flash", fontSize = 12.sp) },
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            Button(
                                onClick = {
                                    if (customModelInput.isNotBlank()) {
                                        viewModel.updateGeminiModel(customModelInput.trim())
                                        showModelDialog = false
                                    }
                                },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Apply")
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showModelDialog = false }) { Text("Close") }
            }
        )
    }

    // Theme Selection Dialog
    if (showThemeDialog) {
        AlertDialog(
            onDismissRequest = { showThemeDialog = false },
            title = { Text("Application Theme") },
            text = {
                Column {
                    listOf("system" to "Follow System", "light" to "Light", "dark" to "Dark").forEach { (tId, tLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateTheme(tId)
                                    showThemeDialog = false
                                }
                        ) {
                            Text(
                                text = tLabel,
                                fontWeight = if (settings.theme == tId) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showThemeDialog = false }) { Text("Close") }
            }
        )
    }

    // Default Note Tint Dialog
    if (showTintDialog) {
        AlertDialog(
            onDismissRequest = { showTintDialog = false },
            title = { Text("Default Note Tint") },
            text = {
                NoteTintRow(
                    selectedTint = settings.defaultTint,
                    onTintSelected = {
                        viewModel.updateDefaultTint(it)
                        showTintDialog = false
                    }
                )
            },
            confirmButton = {
                TextButton(onClick = { showTintDialog = false }) { Text("Close") }
            }
        )
    }

    // Font Style Dialog
    if (showFontDialog) {
        AlertDialog(
            onDismissRequest = { showFontDialog = false },
            title = { Text("Default Font Style") },
            text = {
                Column {
                    listOf("sans" to "Sans-Serif (Modern)", "serif" to "Serif (Editorial)", "mono" to "Monospace (Code)").forEach { (fId, fLabel) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateFont(fId)
                                    showFontDialog = false
                                }
                        ) {
                            Text(
                                text = fLabel,
                                fontWeight = if (settings.font == fId) FontWeight.Bold else FontWeight.Normal
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

    // Date Format Dialog
    if (showDateFormatDialog) {
        AlertDialog(
            onDismissRequest = { showDateFormatDialog = false },
            title = { Text("Date Format") },
            text = {
                Column {
                    listOf("dd/MM/yyyy", "MM/dd/yyyy", "yyyy-MM-dd").forEach { fmt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateDateFormat(fmt)
                                    showDateFormatDialog = false
                                }
                        ) {
                            Text(
                                text = "$fmt (${DateFormats.formatNoteTimestamp(System.currentTimeMillis(), fmt)})",
                                fontWeight = if (settings.dateFormat == fmt) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDateFormatDialog = false }) { Text("Close") }
            }
        )
    }

    // Start Day Dialog
    if (showStartDayDialog) {
        AlertDialog(
            onDismissRequest = { showStartDayDialog = false },
            title = { Text("Start Day of Week") },
            text = {
                Column {
                    listOf("Monday", "Sunday").forEach { day ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp)
                                .clickable {
                                    viewModel.updateStartDayOfWeek(day)
                                    showStartDayDialog = false
                                }
                        ) {
                            Text(
                                text = day,
                                fontWeight = if (settings.startDayOfWeek == day) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showStartDayDialog = false }) { Text("Close") }
            }
        )
    }

    // Restore Confirmation Dialog (Merge or Replace)
    if (showRestoreConfirmDialog && selectedRestoreUri != null) {
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = false },
            title = { Text("Restore Notes Backup") },
            text = {
                Text("How would you like to restore the backup file?\n\n• Merge: Add backup notes to existing notes without deleting existing ones.\n• Replace: Completely replace all current notes with backup notes.")
            },
            confirmButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    selectedRestoreUri?.let { uri ->
                        coroutineScope.launch {
                            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                                viewModel.restoreBackup(stream, isReplace = false)
                            }
                        }
                    }
                }) {
                    Text("Merge")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showRestoreConfirmDialog = false
                    selectedRestoreUri?.let { uri ->
                        coroutineScope.launch {
                            context.contentResolver.openInputStream(uri)?.use { stream: InputStream ->
                                viewModel.restoreBackup(stream, isReplace = true)
                            }
                        }
                    }
                }) {
                    Text("Replace", color = Color.Red)
                }
            }
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 6.dp)
    )
}

@Composable
fun SettingsActionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
    Divider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}

@Composable
fun SettingsSwitchRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
    Divider(modifier = Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
}
