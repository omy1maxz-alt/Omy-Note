package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HealthStatus
import com.example.data.ModelFilter
import com.example.data.ModelHealth
import com.example.data.SortOption
import com.example.ui.theme.*
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KieMonitorScreen(viewModel: KieMonitorViewModel = viewModel()) {
    val context = LocalContext.current
    val models by viewModel.modelsState.collectAsState()
    val isRefreshing by viewModel.isRefreshing.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedSort by viewModel.selectedSort.collectAsState()
    val cookieCount by viewModel.cookieCount.collectAsState()
    val savedCookieText by viewModel.savedCookieText.collectAsState()
    val isAutoRefreshEnabled by viewModel.isAutoRefreshEnabled.collectAsState()
    val autoRefreshSeconds by viewModel.autoRefreshSeconds.collectAsState()
    val selectedModelForDetails by viewModel.selectedModelForDetails.collectAsState()

    var showCookieDialog by remember { mutableStateOf(false) }
    var showAddModelDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showManageListDialog by remember { mutableStateOf(false) }
    var showClearAllConfirmDialog by remember { mutableStateOf(false) }

    // Filter and sort the models
    val filteredModels = remember(models, searchQuery, selectedFilter, selectedSort) {
        var list = models

        if (searchQuery.isNotBlank()) {
            val q = searchQuery.trim().lowercase(Locale.ROOT)
            list = list.filter {
                it.modelId.lowercase(Locale.ROOT).contains(q) ||
                it.modelName.lowercase(Locale.ROOT).contains(q) ||
                it.provider.lowercase(Locale.ROOT).contains(q)
            }
        }

        list = when (selectedFilter) {
            ModelFilter.ALL -> list
            ModelFilter.GOOGLE -> list.filter { it.provider.equals("Google", ignoreCase = true) }
            ModelFilter.OPENAI -> list.filter { it.provider.equals("OpenAI", ignoreCase = true) }
            ModelFilter.ANTHROPIC -> list.filter { it.provider.equals("Anthropic", ignoreCase = true) }
            ModelFilter.DEEPSEEK -> list.filter { it.provider.equals("DeepSeek", ignoreCase = true) }
            ModelFilter.ISSUES_ONLY -> list.filter { it.status != HealthStatus.OPERATIONAL }
        }

        when (selectedSort) {
            SortOption.DEFAULT -> list
            SortOption.SUCCESS_RATE_ASC -> list.sortedBy { it.successRate }
            SortOption.SUCCESS_RATE_DESC -> list.sortedByDescending { it.successRate }
            SortOption.LATENCY_ASC -> list.sortedBy { if (it.latencyMs <= 0) 99999 else it.latencyMs }
            SortOption.NAME_ASC -> list.sortedBy { it.modelName }
        }
    }

    val summary = remember(models, cookieCount) { viewModel.getSummary() }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Scaffold(
        containerColor = BgDarkest,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BgDark,
                    titleContentColor = TextPrimary
                ),
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isRefreshing) AccentCyan.copy(alpha = pulseAlpha)
                                    else if (summary.outageCount > 0) StatusOutage.copy(alpha = pulseAlpha)
                                    else StatusOperational.copy(alpha = pulseAlpha)
                                )
                        )
                        Column {
                            Text(
                                text = "KIE Status Monitor",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = if (summary.lastRefreshTime.isNotEmpty()) "Real-time API availability & latency • ${summary.lastRefreshTime}" else "Real-time API availability & latency",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = if (cookieCount > 0) StatusOperationalBg else SurfaceDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (cookieCount > 0) StatusOperational.copy(alpha = 0.5f) else SurfaceBorder
                        ),
                        modifier = Modifier
                            .testTag("cookie_button")
                            .clickable { showCookieDialog = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = if (cookieCount > 0) Icons.Default.Key else Icons.Outlined.KeyOff,
                                contentDescription = "Cookie Status",
                                tint = if (cookieCount > 0) StatusOperationalText else TextSecondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (cookieCount > 0) "$cookieCount Cookies" else "Cookie",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (cookieCount > 0) StatusOperationalText else TextSecondary
                            )
                        }
                    }

                    IconButton(
                        onClick = { showManageListDialog = true },
                        modifier = Modifier.testTag("manage_list_button")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.PlaylistRemove,
                            contentDescription = "Manage / Remove from list",
                            tint = if (models.isEmpty()) TextTertiary else TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { showSettingsDialog = true },
                        modifier = Modifier.testTag("settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings",
                            tint = TextSecondary
                        )
                    }

                    IconButton(
                        onClick = { viewModel.refreshStatus() },
                        enabled = !isRefreshing && models.isNotEmpty(),
                        modifier = Modifier.testTag("refresh_button")
                    ) {
                        if (isRefreshing) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = PrimaryIndigoLight,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh",
                                tint = if (models.isEmpty()) TextTertiary else PrimaryIndigoLight
                            )
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddModelDialog = true },
                containerColor = PrimaryIndigo,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_model_fab")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Add custom model"
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(4.dp))
            }

            if (models.isNotEmpty()) {
                item {
                    StatusSummaryDashboard(
                        summary = summary,
                        isRefreshing = isRefreshing,
                        onCopyReport = {
                            val report = viewModel.generateStatusReport()
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("KIE Status Report", report)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Status report copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            if (models.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            placeholder = { Text("Filter by model name or provider...", fontSize = 13.sp, color = TextTertiary) },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary, modifier = Modifier.size(18.dp))
                            },
                            trailingIcon = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (searchQuery.isNotEmpty()) {
                                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextSecondary, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                    Box {
                                        IconButton(
                                            onClick = { showSortMenu = true },
                                            modifier = Modifier.testTag("sort_button")
                                        ) {
                                            Icon(Icons.Default.Sort, contentDescription = "Sort Options", tint = AccentSky, modifier = Modifier.size(20.dp))
                                        }
                                        DropdownMenu(
                                            expanded = showSortMenu,
                                            onDismissRequest = { showSortMenu = false },
                                            modifier = Modifier.background(SurfaceDark)
                                        ) {
                                            DropdownMenuItem(
                                                text = { Text("Default Order", color = TextPrimary) },
                                                onClick = { viewModel.setSort(SortOption.DEFAULT); showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Success Rate (Highest First)", color = TextPrimary) },
                                                onClick = { viewModel.setSort(SortOption.SUCCESS_RATE_DESC); showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Success Rate (Lowest / Issues First)", color = TextPrimary) },
                                                onClick = { viewModel.setSort(SortOption.SUCCESS_RATE_ASC); showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Latency (Lowest First)", color = TextPrimary) },
                                                onClick = { viewModel.setSort(SortOption.LATENCY_ASC); showSortMenu = false }
                                            )
                                            DropdownMenuItem(
                                                text = { Text("Model Name (A-Z)", color = TextPrimary) },
                                                onClick = { viewModel.setSort(SortOption.NAME_ASC); showSortMenu = false }
                                            )
                                        }
                                    }
                                }
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = BgDark,
                                unfocusedContainerColor = BgDark,
                                focusedBorderColor = PrimaryIndigoLight,
                                unfocusedBorderColor = SurfaceBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp)
                                .testTag("search_field")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            ModelFilterChip(
                                label = "All (${models.size})",
                                selected = selectedFilter == ModelFilter.ALL,
                                onClick = { viewModel.setFilter(ModelFilter.ALL) }
                            )
                            ModelFilterChip(
                                label = "Google",
                                selected = selectedFilter == ModelFilter.GOOGLE,
                                onClick = { viewModel.setFilter(ModelFilter.GOOGLE) }
                            )
                            ModelFilterChip(
                                label = "OpenAI",
                                selected = selectedFilter == ModelFilter.OPENAI,
                                onClick = { viewModel.setFilter(ModelFilter.OPENAI) }
                            )
                            ModelFilterChip(
                                label = "Anthropic",
                                selected = selectedFilter == ModelFilter.ANTHROPIC,
                                onClick = { viewModel.setFilter(ModelFilter.ANTHROPIC) }
                            )
                            ModelFilterChip(
                                label = "DeepSeek",
                                selected = selectedFilter == ModelFilter.DEEPSEEK,
                                onClick = { viewModel.setFilter(ModelFilter.DEEPSEEK) }
                            )
                            val issueCount = models.count { it.status != HealthStatus.OPERATIONAL }
                            ModelFilterChip(
                                label = "Issues ($issueCount)",
                                selected = selectedFilter == ModelFilter.ISSUES_ONLY,
                                highlightColor = if (issueCount > 0) StatusOutage else null,
                                onClick = { viewModel.setFilter(ModelFilter.ISSUES_ONLY) }
                            )
                        }
                    }
                }
            }

            if (models.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "MONITORED ENDPOINTS (${filteredModels.size})",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextTertiary,
                            letterSpacing = 1.sp
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "Manage List",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = AccentCyan,
                                modifier = Modifier
                                    .clickable { showManageListDialog = true }
                                    .padding(4.dp)
                            )
                        }
                    }
                }
            }

            if (models.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = BgDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                        modifier = Modifier.fillMaxWidth().padding(top = 20.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PlaylistRemove,
                                contentDescription = "List empty",
                                tint = StatusDegraded,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No Endpoints Monitored",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "All models were removed from the monitoring list. You can restore the default models or add custom ones.",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                lineHeight = 18.sp
                            )
                            Row(
                                modifier = Modifier.padding(top = 6.dp),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {
                                        viewModel.resetToDefaultModels()
                                        Toast.makeText(context, "Default models restored", Toast.LENGTH_SHORT).show()
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Icon(Icons.Default.RestartAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Restore Defaults")
                                }

                                OutlinedButton(
                                    onClick = { showAddModelDialog = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Add Model")
                                }
                            }
                        }
                    }
                }
            } else if (filteredModels.isEmpty() && isRefreshing) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            CircularProgressIndicator(color = PrimaryIndigoLight)
                            Text("Querying model endpoints...", color = TextSecondary, fontSize = 13.sp)
                        }
                    }
                }
            } else if (filteredModels.isEmpty()) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = BgDark),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.SearchOff,
                                contentDescription = "No models",
                                tint = TextSecondary,
                                modifier = Modifier.size(36.dp)
                            )
                            Text("No matching models found", color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Try adjusting your search query or filter", color = TextSecondary, fontSize = 12.sp)
                        }
                    }
                }
            } else {
                items(filteredModels, key = { it.modelId }) { model ->
                    ModelStatusCard(
                        model = model,
                        onClick = { viewModel.selectModelForDetails(model) },
                        onQuickPing = { viewModel.refreshSingleModel(model.modelId) },
                        onRemove = {
                            viewModel.removeModel(model.modelId)
                            Toast.makeText(context, "Removed ${model.modelName}", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (showCookieDialog) {
        CookieAuthDialog(
            currentCookie = savedCookieText,
            activeCount = cookieCount,
            onDismiss = { showCookieDialog = false },
            onSave = { rawCookie ->
                viewModel.setCookieText(rawCookie)
                showCookieDialog = false
                Toast.makeText(context, "Cookie saved and connected", Toast.LENGTH_SHORT).show()
            },
            onClear = {
                viewModel.clearCookie()
                showCookieDialog = false
                Toast.makeText(context, "Cookies cleared", Toast.LENGTH_SHORT).show()
            }
        )
    }

    if (showAddModelDialog) {
        AddCustomModelDialog(
            onDismiss = { showAddModelDialog = false },
            onAdd = { newModelId ->
                val added = viewModel.addCustomModel(newModelId)
                if (added) {
                    Toast.makeText(context, "Added $newModelId to monitor", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Model already in list or invalid name", Toast.LENGTH_SHORT).show()
                }
                showAddModelDialog = false
            }
        )
    }

    if (showManageListDialog) {
        ManageEndpointsDialog(
            models = models,
            onDismiss = { showManageListDialog = false },
            onRemoveModel = { id ->
                viewModel.removeModel(id)
                Toast.makeText(context, "Removed endpoint", Toast.LENGTH_SHORT).show()
            },
            onClearAll = {
                showManageListDialog = false
                showClearAllConfirmDialog = true
            },
            onResetDefaults = {
                viewModel.resetToDefaultModels()
                showManageListDialog = false
                Toast.makeText(context, "Reset to default models", Toast.LENGTH_SHORT).show()
            },
            onAddClick = {
                showManageListDialog = false
                showAddModelDialog = true
            }
        )
    }

    if (showClearAllConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearAllConfirmDialog = false },
            containerColor = SurfaceDark,
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.WarningAmber, contentDescription = null, tint = StatusOutage)
                    Text("Clear All Endpoints?", color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(
                    "Are you sure you want to remove all monitored endpoints from the list? You can restore the default models at any time.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearAllModels()
                        showClearAllConfirmDialog = false
                        Toast.makeText(context, "All models removed", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusOutage)
                ) {
                    Text("Clear All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearAllConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    if (showSettingsDialog) {
        SettingsDialog(
            autoRefreshEnabled = isAutoRefreshEnabled,
            refreshIntervalSeconds = autoRefreshSeconds,
            onToggleAutoRefresh = { viewModel.toggleAutoRefresh(it) },
            onChangeInterval = { viewModel.setAutoRefreshInterval(it) },
            onOpenCookie = {
                showSettingsDialog = false
                showCookieDialog = true
            },
            onOpenManageList = {
                showSettingsDialog = false
                showManageListDialog = true
            },
            onDismiss = { showSettingsDialog = false }
        )
    }

    selectedModelForDetails?.let { model ->
        ModelDetailsDialog(
            model = model,
            onDismiss = { viewModel.selectModelForDetails(null) },
            onPing = { viewModel.refreshSingleModel(model.modelId) },
            onDelete = {
                viewModel.removeModel(model.modelId)
                viewModel.selectModelForDetails(null)
                Toast.makeText(context, "Removed ${model.modelName} from list", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
fun StatusSummaryDashboard(
    summary: com.example.data.SystemStatusSummary,
    isRefreshing: Boolean,
    onCopyReport: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = BgDark),
        border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Analytics,
                        contentDescription = "Status overview",
                        tint = AccentCyan,
                        modifier = Modifier.size(18.dp)
                    )
                    Text(
                        text = "NETWORK TELEMETRY OVERVIEW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        letterSpacing = 0.5.sp
                    )
                }

                IconButton(
                    onClick = onCopyReport,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy status report",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatBox(
                    modifier = Modifier.weight(1f),
                    title = "OPERATIONAL",
                    value = "${summary.operationalCount}/${summary.totalModels}",
                    subValue = "${String.format(Locale.US, "%.1f", summary.averageSuccessRate)}% avg",
                    color = StatusOperational,
                    bgColor = StatusOperationalBg
                )

                StatBox(
                    modifier = Modifier.weight(1f),
                    title = "INCIDENTS",
                    value = "${summary.degradedCount + summary.outageCount}",
                    subValue = if (summary.outageCount > 0) "${summary.outageCount} Outage" else "Normal",
                    color = if (summary.outageCount > 0) StatusOutage else if (summary.degradedCount > 0) StatusDegraded else StatusOperational,
                    bgColor = if (summary.outageCount > 0) StatusOutageBg else if (summary.degradedCount > 0) StatusDegradedBg else StatusOperationalBg
                )

                StatBox(
                    modifier = Modifier.weight(1f),
                    title = "AVG LATENCY",
                    value = if (summary.averageLatencyMs > 0) "${summary.averageLatencyMs}ms" else "--",
                    subValue = "Response",
                    color = AccentSky,
                    bgColor = SurfaceDark
                )
            }

            val operationalRatio = if (summary.totalModels > 0) summary.operationalCount.toFloat() / summary.totalModels else 0f
            val degradedRatio = if (summary.totalModels > 0) summary.degradedCount.toFloat() / summary.totalModels else 0f
            val outageRatio = if (summary.totalModels > 0) summary.outageCount.toFloat() / summary.totalModels else 0f

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .background(SurfaceDark)
                ) {
                    if (operationalRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(operationalRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(StatusOperational)
                        )
                    }
                    if (degradedRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(degradedRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(StatusDegraded)
                        )
                    }
                    if (outageRatio > 0) {
                        Box(
                            modifier = Modifier
                                .weight(outageRatio.coerceAtLeast(0.01f))
                                .fillMaxHeight()
                                .background(StatusOutage)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${summary.operationalCount} Operational",
                        fontSize = 10.sp,
                        color = StatusOperationalText
                    )
                    if (summary.degradedCount > 0) {
                        Text(
                            text = "${summary.degradedCount} Degraded",
                            fontSize = 10.sp,
                            color = StatusDegradedText
                        )
                    }
                    if (summary.outageCount > 0) {
                        Text(
                            text = "${summary.outageCount} Outage",
                            fontSize = 10.sp,
                            color = StatusOutageText
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatBox(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    subValue: String,
    color: Color,
    bgColor: Color
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = SurfaceDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.3f)),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = title,
                fontSize = 9.sp,
                fontWeight = FontWeight.Bold,
                color = TextSecondary,
                letterSpacing = 0.5.sp
            )
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                fontFamily = FontFamily.Monospace
            )
            Text(
                text = subValue,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}

@Composable
fun ModelFilterChip(
    label: String,
    selected: Boolean,
    highlightColor: Color? = null,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (selected) (highlightColor ?: PrimaryIndigoLight).copy(alpha = 0.2f) else BgDark,
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (selected) (highlightColor ?: PrimaryIndigoLight) else SurfaceBorder
        ),
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
            color = if (selected) (highlightColor ?: PrimaryIndigoLight) else TextSecondary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun ModelStatusCard(
    model: ModelHealth,
    onClick: () -> Unit,
    onQuickPing: () -> Unit,
    onRemove: () -> Unit
) {
    val (statusColor, statusBg, statusText) = when (model.status) {
        HealthStatus.OPERATIONAL -> Triple(StatusOperational, StatusOperationalBg, "Operational")
        HealthStatus.DEGRADED -> Triple(StatusDegraded, StatusDegradedBg, "Degraded")
        HealthStatus.OUTAGE -> Triple(StatusOutage, StatusOutageBg, "Outage")
    }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = BgDark),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (model.status == HealthStatus.OUTAGE) StatusOutage.copy(alpha = 0.5f) else SurfaceBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )

                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = model.modelName.uppercase(),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            if (model.isCustom) {
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = PrimaryIndigoDark,
                                    modifier = Modifier.padding(vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "CUSTOM",
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${model.provider} • ${model.modelId}",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextTertiary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", model.successRate),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = statusColor
                    )
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg,
                        modifier = Modifier.padding(top = 2.dp)
                    ) {
                        Text(
                            text = statusText,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Latency",
                            tint = if (model.latencyMs > 0 && model.latencyMs < 500) StatusOperationalText else if (model.latencyMs >= 500) StatusDegradedText else TextTertiary,
                            modifier = Modifier.size(13.dp)
                        )
                        Text(
                            text = if (model.latencyMs > 0) "${model.latencyMs}ms" else "--",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextSecondary
                        )
                    }

                    Text(
                        text = "• ${model.lastUpdated}",
                        fontSize = 10.sp,
                        color = TextTertiary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (model.historyPoints.isNotEmpty()) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(2.dp),
                            verticalAlignment = Alignment.Bottom,
                            modifier = Modifier.height(14.dp).padding(end = 4.dp)
                        ) {
                            model.historyPoints.takeLast(6).forEach { pt ->
                                val ptColor = when {
                                    pt >= 90.0 -> StatusOperational
                                    pt >= 50.0 -> StatusDegraded
                                    else -> StatusOutage
                                }
                                Box(
                                    modifier = Modifier
                                        .width(3.dp)
                                        .height(14.dp)
                                        .clip(RoundedCornerShape(1.dp))
                                        .background(ptColor.copy(alpha = 0.7f))
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onQuickPing,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Test Endpoint",
                            tint = AccentCyan,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = onRemove,
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Close,
                            contentDescription = "Remove from list",
                            tint = TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            if (model.errorMessage != null && model.status == HealthStatus.OUTAGE) {
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = StatusOutageBg.copy(alpha = 0.5f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, StatusOutage.copy(alpha = 0.3f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Error: ${model.errorMessage}",
                        fontSize = 10.sp,
                        color = StatusOutageText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ManageEndpointsDialog(
    models: List<ModelHealth>,
    onDismiss: () -> Unit,
    onRemoveModel: (String) -> Unit,
    onClearAll: () -> Unit,
    onResetDefaults: () -> Unit,
    onAddClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Outlined.PlaylistRemove, contentDescription = null, tint = AccentCyan)
                Text("Manage Endpoints", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Total ${models.size} models currently monitored. Tap the trash icon to remove any model.",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onResetDefaults,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AccentCyan)
                    ) {
                        Text("Reset Defaults", fontSize = 11.sp)
                    }

                    OutlinedButton(
                        onClick = onClearAll,
                        enabled = models.isNotEmpty(),
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOutage)
                    ) {
                        Text("Remove All", fontSize = 11.sp)
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                if (models.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No models in monitor list", color = TextTertiary, fontSize = 12.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 260.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(models, key = { it.modelId }) { m ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = BgDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 10.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = m.modelName,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "${m.provider} • ${m.modelId}",
                                            fontSize = 10.sp,
                                            fontFamily = FontFamily.Monospace,
                                            color = TextTertiary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    IconButton(
                                        onClick = { onRemoveModel(m.modelId) },
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.DeleteOutline,
                                            contentDescription = "Remove ${m.modelId}",
                                            tint = StatusOutage,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onAddClick,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CookieAuthDialog(
    currentCookie: String,
    activeCount: Int,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit,
    onClear: () -> Unit
) {
    var cookieInput by remember { mutableStateOf(currentCookie) }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Key, contentDescription = null, tint = AccentCyan)
                Text("Cookie Authentication", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Paste cookie.txt content or raw cookie header:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = cookieInput,
                    onValueChange = { cookieInput = it },
                    placeholder = {
                        Text(
                            "Paste cookie.txt content or raw cookie header",
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextTertiary
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace,
                        color = TextPrimary
                    ),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDarkest,
                        unfocusedContainerColor = BgDarkest,
                        focusedBorderColor = PrimaryIndigoLight,
                        unfocusedBorderColor = SurfaceBorder
                    )
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clipText = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                            if (clipText.isNotEmpty()) {
                                cookieInput = clipText
                                Toast.makeText(context, "Pasted from clipboard", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Clipboard is empty", Toast.LENGTH_SHORT).show()
                            }
                        }
                    ) {
                        Icon(Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Paste Clipboard", fontSize = 11.sp)
                    }

                    if (activeCount > 0) {
                        TextButton(
                            onClick = onClear,
                            colors = ButtonDefaults.textButtonColors(contentColor = StatusOutage)
                        ) {
                            Text("Clear", fontSize = 11.sp)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(cookieInput) },
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                modifier = Modifier.testTag("save_cookie_button")
            ) {
                Text("Save & Connect")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun AddCustomModelDialog(
    onDismiss: () -> Unit,
    onAdd: (String) -> Unit
) {
    var modelIdInput by remember { mutableStateOf("") }

    val popularSuggestions = listOf(
        "claude-3-7-sonnet",
        "gemini-2.5-pro",
        "gpt-4o",
        "o3-mini",
        "qwen-2.5-72b",
        "mistral-large-2411"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.AddCircleOutline, contentDescription = null, tint = PrimaryIndigoLight)
                Text("Add Model to Monitor", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Enter the model identifier used on KIE API:",
                    fontSize = 12.sp,
                    color = TextSecondary
                )

                OutlinedTextField(
                    value = modelIdInput,
                    onValueChange = { modelIdInput = it },
                    placeholder = { Text("e.g. claude-3-7-sonnet", fontSize = 12.sp, color = TextTertiary) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = BgDarkest,
                        unfocusedContainerColor = BgDarkest,
                        focusedBorderColor = PrimaryIndigoLight,
                        unfocusedBorderColor = SurfaceBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Popular choices:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextTertiary
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularSuggestions.forEach { suggestion ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = BgDark,
                            border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { modelIdInput = suggestion }
                        ) {
                            Text(
                                text = "+ $suggestion",
                                fontSize = 10.sp,
                                fontFamily = FontFamily.Monospace,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onAdd(modelIdInput) },
                enabled = modelIdInput.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Add Model")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        }
    )
}

@Composable
fun SettingsDialog(
    autoRefreshEnabled: Boolean,
    refreshIntervalSeconds: Int,
    onToggleAutoRefresh: (Boolean) -> Unit,
    onChangeInterval: (Int) -> Unit,
    onOpenCookie: () -> Unit,
    onOpenManageList: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Tune, contentDescription = null, tint = AccentCyan)
                Text("Monitor Settings", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Auto-Refresh", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Text("Continuously poll endpoints", color = TextSecondary, fontSize = 11.sp)
                    }
                    Switch(
                        checked = autoRefreshEnabled,
                        onCheckedChange = onToggleAutoRefresh,
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryIndigo
                        )
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("Refresh Frequency", color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(15, 30, 45, 60, 120).forEach { sec ->
                            val selected = refreshIntervalSeconds == sec
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) PrimaryIndigo else BgDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) PrimaryIndigoLight else SurfaceBorder
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onChangeInterval(sec) }
                            ) {
                                Box(
                                    modifier = Modifier.padding(vertical = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${sec}s",
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }

                Divider(color = SurfaceBorder, thickness = 1.dp)

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BgDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenManageList)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Outlined.PlaylistRemove, contentDescription = null, tint = AccentCyan, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Manage / Remove Models", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Remove or reset monitored endpoints", color = TextSecondary, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                    }
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BgDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .clickable(onClick = onOpenCookie)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Key, contentDescription = null, tint = AccentSky, modifier = Modifier.size(18.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Cookie Authentication", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Text("Manage session headers", color = TextSecondary, fontSize = 10.sp)
                        }
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextTertiary)
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
            ) {
                Text("Done")
            }
        }
    )
}

@Composable
fun ModelDetailsDialog(
    model: ModelHealth,
    onDismiss: () -> Unit,
    onPing: () -> Unit,
    onDelete: () -> Unit
) {
    val (statusColor, statusBg, statusText) = when (model.status) {
        HealthStatus.OPERATIONAL -> Triple(StatusOperational, StatusOperationalBg, "Operational")
        HealthStatus.DEGRADED -> Triple(StatusDegraded, StatusDegradedBg, "Degraded")
        HealthStatus.OUTAGE -> Triple(StatusOutage, StatusOutageBg, "Outage")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceDark,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(statusColor)
                )
                Column {
                    Text(model.modelName.uppercase(), color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    Text(model.modelId, color = TextTertiary, fontSize = 11.sp, fontFamily = FontFamily.Monospace)
                }
            }
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = BgDark,
                    border = androidx.compose.foundation.BorderStroke(1.dp, SurfaceBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DetailRow(label = "Status", value = statusText, color = statusColor)
                        DetailRow(label = "Success Rate", value = String.format(Locale.US, "%.1f%%", model.successRate), color = statusColor)
                        DetailRow(label = "Provider", value = model.provider, color = TextPrimary)
                        DetailRow(label = "Latency", value = "${model.latencyMs}ms", color = AccentSky)
                        DetailRow(label = "Last Check", value = model.lastUpdated, color = TextSecondary)
                        DetailRow(label = "Type", value = if (model.isCustom) "Custom User Endpoint" else "Standard Catalog", color = TextSecondary)
                    }
                }

                if (model.errorMessage != null) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = StatusOutageBg.copy(alpha = 0.5f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, StatusOutage.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text("Diagnostic Information", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = StatusOutageText)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(model.errorMessage, fontSize = 11.sp, fontFamily = FontFamily.Monospace, color = StatusOutageText)
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = onPing,
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ping")
                }

                OutlinedButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusOutage)
                ) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Remove")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = TextSecondary)
            }
        }
    )
}

@Composable
fun DetailRow(label: String, value: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = color)
    }
}
