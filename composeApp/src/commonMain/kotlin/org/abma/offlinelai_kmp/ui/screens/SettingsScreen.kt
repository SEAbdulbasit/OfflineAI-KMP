package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Bolt
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.inference.PromptBuilder
import org.abma.offlinelai_kmp.inference.TrimmingStrategy
import org.abma.offlinelai_kmp.picker.FilePickerStatus
import org.abma.offlinelai_kmp.picker.rememberFilePickerWithStatus
import org.abma.offlinelai_kmp.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.math.roundToInt
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLoadModel: (String, ModelConfig) -> Unit,
    onRemoveModel: (String) -> Unit = {},
    loadedModels: List<LoadedModel> = emptyList(),
    currentModelPath: String? = null,
    activeConfig: ModelConfig = ModelConfig(),
    onUpdateModelConfig: (ModelConfig) -> Unit = {},
    trimmingStrategy: TrimmingStrategy = TrimmingStrategy.KEEP_RECENT,
    onUpdateTrimmingStrategy: (TrimmingStrategy) -> Unit = {},
    platformName: String = "KMP",
    deviceModel: String = "On-Device Neural Engine",
    osVersion: String = "",
    maxContextTokens: Int = PromptBuilder.MAX_CONTEXT_TOKENS,
    estimatedContextTokens: Int = 0
) {
    var maxTokens by remember(activeConfig.maxTokens) { mutableIntStateOf(activeConfig.maxTokens) }
    var temperature by remember(activeConfig.temperature) { mutableFloatStateOf(activeConfig.temperature) }
    var topK by remember(activeConfig.topK) { mutableIntStateOf(activeConfig.topK) }
    var selectedStrategy by remember(trimmingStrategy) { mutableStateOf(trimmingStrategy) }

    var pendingModelPath by remember { mutableStateOf<String?>(null) }
    var isCopyingFile by remember { mutableStateOf(false) }
    var copyError by remember { mutableStateOf<String?>(null) }

    val publishConfigChange: (Int, Float, Int) -> Unit = { newMaxTokens, newTemp, newTopK ->
        maxTokens = newMaxTokens
        temperature = newTemp
        topK = newTopK
        onUpdateModelConfig(
            activeConfig.copy(
                maxTokens = newMaxTokens,
                temperature = newTemp,
                topK = newTopK
            )
        )
    }

    val launchFilePicker = rememberFilePickerWithStatus { status ->
        when (status) {
            is FilePickerStatus.Idle -> {
                isCopyingFile = false
            }
            is FilePickerStatus.Copying -> {
                isCopyingFile = true
                copyError = null
            }
            is FilePickerStatus.Success -> {
                isCopyingFile = false
                pendingModelPath = status.path
                copyError = null
            }
            is FilePickerStatus.Error -> {
                isCopyingFile = false
                copyError = status.message
            }
        }
    }

    val focusManager = LocalFocusManager.current
    val colors = MaterialTheme.extendedColors

    RelayAtmosphere(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectTapGestures(onTap = { focusManager.clearFocus() })
            }
    ) {
        Scaffold(
            topBar = {
                SettingsTopBar(onNavigateBack = onNavigateBack)
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0, 0, 0, 0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(4.dp))

                // ═══ SECTION 1: DEVICE & NEURAL HARDWARE PROFILE ═══
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RelaySectionHeading(
                        eyebrow = "01 / HARDWARE & NEURAL RUNTIME",
                        title = "Device & Neural Hardware Profile",
                        icon = Icons.Default.Memory
                    )
                    HardwareProfileCard(
                        platformName = platformName,
                        deviceModel = deviceModel,
                        osVersion = osVersion,
                        currentModelPath = currentModelPath,
                        estimatedContextTokens = estimatedContextTokens,
                        maxContextTokens = maxContextTokens
                    )
                }

                // ═══ SECTION 2: RUNTIME INFERENCE TUNING ═══
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RelaySectionHeading(
                        eyebrow = "02 / SAMPLING & CONTEXT BUDGET",
                        title = "Runtime Inference Tuning",
                        icon = Icons.Default.Tune
                    )
                    InferenceTuningCard(
                        temperature = temperature,
                        topK = topK,
                        maxTokens = maxTokens,
                        trimmingStrategy = selectedStrategy,
                        onTemperatureChange = { newTemp ->
                            val rounded = (newTemp * 20f).roundToInt() / 20f
                            publishConfigChange(maxTokens, rounded, topK)
                        },
                        onTopKChange = { newTopK ->
                            publishConfigChange(maxTokens, temperature, newTopK)
                        },
                        onMaxTokensChange = { newMaxTokens ->
                            publishConfigChange(newMaxTokens, temperature, topK)
                        },
                        onTrimmingStrategyChange = { strategy ->
                            selectedStrategy = strategy
                            onUpdateTrimmingStrategy(strategy)
                        },
                        onResetDefaults = {
                            publishConfigChange(2048, 0.7f, 40)
                            selectedStrategy = TrimmingStrategy.KEEP_RECENT
                            onUpdateTrimmingStrategy(TrimmingStrategy.KEEP_RECENT)
                        }
                    )
                }

                // ═══ SECTION 3: ACTIVE & INSTALLED MODELS ═══
                if (loadedModels.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        RelaySectionHeading(
                            eyebrow = "03 / INSTALLED MODELS (${loadedModels.size})",
                            title = "On-Device Model Registry",
                            icon = Icons.Default.AutoAwesome
                        )
                        loadedModels.forEach { model ->
                            val isActive = currentModelPath == model.path
                            ModelCard(
                                model = model,
                                isActive = isActive,
                                onLoad = {
                                    val configToUse = ModelConfig(
                                        maxTokens = maxTokens,
                                        temperature = temperature,
                                        topK = topK
                                    )
                                    onLoadModel(model.path, configToUse)
                                    onNavigateBack()
                                },
                                onRemove = { onRemoveModel(model.path) }
                            )
                        }
                    }
                }

                // ═══ SECTION 4: IMPORT LOCAL MODEL ═══
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RelaySectionHeading(
                        eyebrow = if (loadedModels.isNotEmpty()) "04 / MODEL STORAGE" else "03 / MODEL STORAGE",
                        title = "Import Local Model",
                        icon = Icons.Default.FolderOpen
                    )

                    ImportModelButton(
                        onClick = { launchFilePicker() },
                        enabled = !isCopyingFile
                    )

                    if (isCopyingFile) {
                        CopyingProgressCard()
                    }

                    copyError?.let { error ->
                        ErrorCard(
                            message = error,
                            onDismiss = { copyError = null }
                        )
                    }

                    pendingModelPath?.let { path ->
                        PendingModelCard(
                            path = path,
                            config = ModelConfig(
                                maxTokens = maxTokens,
                                temperature = temperature,
                                topK = topK
                            ),
                            onLoad = {
                                val config = ModelConfig(
                                    maxTokens = maxTokens,
                                    temperature = temperature,
                                    topK = topK
                                )
                                onLoadModel(path, config)
                                pendingModelPath = null
                                onNavigateBack()
                            },
                            onCancel = { pendingModelPath = null }
                        )
                    }
                }

                // ═══ SECTION 5: CURATED MODEL CATALOG ═══
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    RelaySectionHeading(
                        eyebrow = if (loadedModels.isNotEmpty()) "05 / MODEL CATALOG & SETUP" else "04 / MODEL CATALOG & SETUP",
                        title = "Curated Gemma Models",
                        icon = Icons.Outlined.Info
                    )
                    InfoCard()
                }

                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val isDark = LocalIsDarkTheme.current
    val onToggleTheme = LocalThemeToggle.current

    Surface(
        color = colors.glassBackdrop,
        tonalElevation = 0.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(58.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Relay translucent back button pill
                    Surface(
                        onClick = onNavigateBack,
                        shape = RoundedCornerShape(RelayRadius.pill),
                        color = colors.subtleFill,
                        border = BorderStroke(1.dp, colors.borderDefault),
                        modifier = Modifier.height(32.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = colors.textPrimary,
                                modifier = Modifier.size(15.dp)
                            )
                            Text(
                                text = "Chat",
                                style = MaterialTheme.typography.labelMedium,
                                color = colors.textPrimary
                            )
                        }
                    }

                    Column {
                        RelayMonoCaps(
                            text = "SENTIENT OS · ENGINE",
                            color = colors.textMuted,
                            fontSize = 9.5.sp
                        )
                        Text(
                            text = "Runtime & Models",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = colors.textPrimary
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Night / Day Appearance Switcher Pill
                    Surface(
                        onClick = onToggleTheme,
                        shape = RoundedCornerShape(RelayRadius.pill),
                        color = colors.subtleFill,
                        border = BorderStroke(1.dp, colors.borderSubtle),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Toggle theme",
                                tint = if (isDark) colors.accentGold else colors.accentAmber,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = if (isDark) "Night" else "Day",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.textSecondary
                            )
                        }
                    }

                    // Air-Gapped Status Badge
                    Surface(
                        shape = RoundedCornerShape(RelayRadius.pill),
                        color = colors.accentEmerald.copy(alpha = 0.10f),
                        border = BorderStroke(1.dp, colors.accentEmerald.copy(alpha = 0.28f)),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            RelayHealthDot(color = colors.accentEmerald)
                            Text(
                                text = "AIR-GAPPED",
                                style = MaterialTheme.typography.labelSmall,
                                color = colors.accentEmerald
                            )
                        }
                    }
                }
            }

            HorizontalDivider(
                color = colors.borderSubtle,
                thickness = 1.dp
            )
        }
    }
}

@Composable
private fun RelaySectionHeading(
    eyebrow: String,
    title: String,
    icon: ImageVector
) {
    val colors = MaterialTheme.extendedColors
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        RelayIconBadge(
            icon = icon,
            tint = colors.accentBlue,
            size = 32.dp,
            iconSize = 16.dp
        )
        Column {
            RelayMonoCaps(
                text = eyebrow,
                color = colors.textMuted,
                fontSize = 9.5.sp
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun HardwareProfileCard(
    platformName: String,
    deviceModel: String,
    osVersion: String,
    currentModelPath: String?,
    estimatedContextTokens: Int,
    maxContextTokens: Int
) {
    val colors = MaterialTheme.extendedColors
    val activeModelName = currentModelPath
        ?.substringAfterLast("/")
        ?.substringBeforeLast(".")
        ?: "No active model"
    val contextRatio = (estimatedContextTokens.toFloat() / maxContextTokens.coerceAtLeast(1).toFloat())
        .coerceIn(0f, 1f)

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    RelayIconBadge(
                        icon = Icons.Default.DeveloperBoard,
                        tint = colors.accentCyan,
                        size = 38.dp,
                        iconSize = 18.dp
                    )
                    Column {
                        Text(
                            text = deviceModel.ifBlank { "Local Neural Engine" },
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = buildString {
                                append(platformName.ifBlank { "KMP" })
                                if (osVersion.isNotBlank()) append(" · $osVersion")
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textSecondary
                        )
                    }
                }

                RelayBadge(
                    text = "LiteRT-LM",
                    icon = Icons.Outlined.Bolt,
                    tint = colors.accentCyan,
                    prominent = true,
                    monospace = true
                )
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            // Telemetry grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                HardwareMetricTile(
                    label = "ACTIVE ENGINE",
                    value = activeModelName,
                    accent = if (currentModelPath != null) colors.accentEmerald else colors.textMuted,
                    modifier = Modifier.weight(1f)
                )
                HardwareMetricTile(
                    label = "ACCELERATOR",
                    value = "GPU / Metal / CPU",
                    accent = colors.accentBlue,
                    modifier = Modifier.weight(1f)
                )
            }

            // Context window capacity bar
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RelayMonoCaps(
                        text = "CONTEXT WINDOW CAPACITY",
                        color = colors.textMuted,
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = "$estimatedContextTokens / $maxContextTokens tok",
                        style = MaterialTheme.typography.labelSmall,
                        color = colors.textSecondary
                    )
                }
                RelayGlowProgressBar(
                    progress = contextRatio.coerceAtLeast(0.04f),
                    modifier = Modifier.fillMaxWidth(),
                    height = 5.dp
                )
            }
        }
    }
}

@Composable
private fun HardwareMetricTile(
    label: String,
    value: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(RelayRadius.sm),
        color = colors.pitch,
        border = BorderStroke(1.dp, colors.borderSubtle)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            RelayMonoCaps(text = label, color = colors.textMuted, fontSize = 9.sp)
            Text(
                text = value,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontFamily = FontFamily.Monospace
                ),
                color = accent,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun InferenceTuningCard(
    temperature: Float,
    topK: Int,
    maxTokens: Int,
    trimmingStrategy: TrimmingStrategy,
    onTemperatureChange: (Float) -> Unit,
    onTopKChange: (Int) -> Unit,
    onMaxTokensChange: (Int) -> Unit,
    onTrimmingStrategyChange: (TrimmingStrategy) -> Unit,
    onResetDefaults: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val tempFormatted = ((temperature * 100).roundToInt() / 100.0).toString()

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(18.dp)) {
            // Header row with reset button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sampling & Generation Parameters",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textSecondary
                )
                Surface(
                    onClick = onResetDefaults,
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = colors.subtleFill,
                    border = BorderStroke(1.dp, colors.borderSubtle)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset defaults",
                            tint = colors.textMuted,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "Reset",
                            style = MaterialTheme.typography.labelSmall,
                            color = colors.textSecondary
                        )
                    }
                }
            }

            // 1. Temperature Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Temperature",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = when {
                                temperature <= 0.3f -> "Deterministic & analytical"
                                temperature <= 0.75f -> "Balanced accuracy & fluency"
                                else -> "High creativity & divergent synthesis"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                    RelayBadge(
                        text = "T = $tempFormatted",
                        tint = colors.accentBlue,
                        prominent = true,
                        monospace = true
                    )
                }

                Slider(
                    value = temperature,
                    onValueChange = onTemperatureChange,
                    valueRange = 0.0f..1.0f,
                    steps = 19,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.textPrimary,
                        activeTrackColor = colors.accentBlue,
                        inactiveTrackColor = colors.surfaceElevated
                    )
                )
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            // 2. Top-K Slider
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Top-K Sampling Pool",
                            style = MaterialTheme.typography.titleSmall,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Limits candidate tokens considered at each step",
                            style = MaterialTheme.typography.bodySmall,
                            color = colors.textMuted
                        )
                    }
                    RelayBadge(
                        text = "K = $topK",
                        tint = colors.accentAmethyst,
                        prominent = true,
                        monospace = true
                    )
                }

                Slider(
                    value = topK.toFloat(),
                    onValueChange = { onTopKChange(it.roundToInt().coerceIn(1, 100)) },
                    valueRange = 1f..100f,
                    colors = SliderDefaults.colors(
                        thumbColor = colors.textPrimary,
                        activeTrackColor = colors.accentAmethyst,
                        inactiveTrackColor = colors.surfaceElevated
                    )
                )
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            // 3. Max Response Tokens Pills
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Max Response Budget",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary
                    )
                    RelayBadge(
                        text = "$maxTokens tokens",
                        tint = colors.workbenchAccent,
                        prominent = true,
                        monospace = true
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(512, 1024, 2048, 4096).forEach { tokenOption ->
                        val selected = maxTokens == tokenOption
                        Surface(
                            onClick = { onMaxTokensChange(tokenOption) },
                            modifier = Modifier.weight(1f).height(34.dp),
                            shape = RoundedCornerShape(RelayRadius.sm),
                            color = if (selected) colors.buttonFace else colors.pitch,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.borderStrong else colors.borderSubtle
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "$tokenOption",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                    ),
                                    color = if (selected) colors.buttonInk else colors.textSecondary
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            // 4. Context Trimming Strategy
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Context Window Trimming Policy",
                    style = MaterialTheme.typography.titleSmall,
                    color = colors.textPrimary
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val options = listOf(
                        TrimmingStrategy.KEEP_RECENT to "Keep Recent",
                        TrimmingStrategy.KEEP_FIRST_AND_RECENT to "Anchor First + Recent"
                    )
                    options.forEach { (strategy, label) ->
                        val selected = trimmingStrategy == strategy
                        Surface(
                            onClick = { onTrimmingStrategyChange(strategy) },
                            modifier = Modifier.weight(1f).height(36.dp),
                            shape = RoundedCornerShape(RelayRadius.sm),
                            color = if (selected) colors.activeFill else colors.pitch,
                            border = BorderStroke(
                                1.dp,
                                if (selected) colors.accentCyan else colors.borderSubtle
                            )
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (selected) colors.textPrimary else colors.textSecondary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }
                Text(
                    text = when (trimmingStrategy) {
                        TrimmingStrategy.KEEP_RECENT ->
                            "Drops oldest turns first when approaching 7,500 tokens. Optimal for rolling multi-turn chat."
                        TrimmingStrategy.KEEP_FIRST_AND_RECENT ->
                            "Preserves the opening exchange plus the latest turns so initial task constraints never roll off."
                        TrimmingStrategy.SUMMARIZE_OLD ->
                            "Compacts older turns to preserve context headroom."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun ImportModelButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    val colors = MaterialTheme.extendedColors

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg,
        strokeColor = colors.borderDefault,
        onClick = if (enabled) onClick else null
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.weight(1f)
            ) {
                RelayIconBadge(
                    icon = Icons.Default.FolderOpen,
                    tint = colors.accentBlue,
                    size = 44.dp,
                    iconSize = 22.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Import .litertlm, .task, or .bin Model",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary
                    )
                    Text(
                        text = "Runs 100% locally from private device storage",
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }

            Surface(
                shape = RoundedCornerShape(RelayRadius.pill),
                color = colors.buttonFace
            ) {
                Text(
                    text = "Browse",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = colors.buttonInk,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun CopyingProgressCard() {
    val colors = MaterialTheme.extendedColors
    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg,
        strokeColor = colors.accentBlue.copy(alpha = 0.45f)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RelayPrismOrb(
                mode = RelayOrbMode.PROCESSING,
                size = 56.dp
            )
            Text(
                text = "Staging model in local storage…",
                style = MaterialTheme.typography.titleSmall,
                color = colors.textPrimary
            )
            Text(
                text = "Large 1.4GB–4GB model files may take a moment to verify",
                style = MaterialTheme.typography.bodySmall,
                color = colors.textSecondary
            )
            NotchThinkingUnderlight(
                active = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg,
        strokeColor = colors.accentCoral.copy(alpha = 0.45f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RelayIconBadge(
                    icon = Icons.Default.ErrorOutline,
                    tint = colors.accentCoral,
                    size = 36.dp,
                    iconSize = 18.dp
                )
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(
                        text = "Model Import Failed",
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.accentCoral
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = colors.textSecondary
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = colors.textMuted
                )
            }
        }
    }
}

@Composable
private fun PendingModelCard(
    path: String,
    config: ModelConfig,
    onLoad: () -> Unit,
    onCancel: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val tempStr = ((config.temperature * 100).roundToInt() / 100.0).toString()

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg,
        strokeColor = colors.accentEmerald.copy(alpha = 0.5f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    RelayMonoCaps(
                        text = "READY TO INITIALIZE",
                        color = colors.accentEmerald,
                        fontSize = 9.5.sp
                    )
                    Text(
                        text = path.substringAfterLast("/"),
                        style = MaterialTheme.typography.titleSmall,
                        color = colors.textPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    RelayBadge(
                        text = "${config.maxTokens} ctx · T=$tempStr · TopK=${config.topK}",
                        monospace = true
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = colors.textMuted
                    )
                }
            }

            Button(
                onClick = onLoad,
                modifier = Modifier.fillMaxWidth().height(42.dp),
                shape = RoundedCornerShape(RelayRadius.sm),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colors.buttonFace,
                    contentColor = colors.buttonInk
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Initialize Engine with Model",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: LoadedModel,
    isActive: Boolean,
    onLoad: () -> Unit,
    onRemove: () -> Unit
) {
    val colors = MaterialTheme.extendedColors
    val tempStr = ((model.config.temperature * 100).roundToInt() / 100.0).toString()

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg,
        strokeColor = if (isActive) colors.accentEmerald.copy(alpha = 0.6f) else colors.borderSubtle,
        onClick = onLoad
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            RelayIconBadge(
                icon = Icons.Default.AutoAwesome,
                tint = if (isActive) colors.accentEmerald else colors.accentBlue,
                size = 40.dp,
                iconSize = 20.dp
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isActive) {
                        RelayBadge(
                            text = "ACTIVE",
                            tint = colors.accentEmerald,
                            prominent = true,
                            monospace = true
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    RelayBadge(
                        text = "ON-DEVICE",
                        icon = Icons.Outlined.Bolt,
                        tint = colors.workbenchAccent,
                        monospace = true
                    )
                    RelayBadge(
                        text = "${model.config.maxTokens} ctx · T=$tempStr · K=${model.config.topK}",
                        monospace = true
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Surface(
                    onClick = onLoad,
                    shape = RoundedCornerShape(RelayRadius.pill),
                    color = if (isActive) colors.subtleFill else colors.buttonFace,
                    border = BorderStroke(1.dp, if (isActive) colors.borderDefault else Color.Transparent)
                ) {
                    Text(
                        text = if (isActive) "Reload" else "Load",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = if (isActive) colors.textPrimary else colors.buttonInk,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }

                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Remove model",
                        tint = colors.accentCoral.copy(alpha = 0.85f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    val colors = MaterialTheme.extendedColors
    val uriHandler = LocalUriHandler.current

    RelaySurfaceCard(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = RelayRadius.lg
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                RelayMonoCaps(
                    text = "PROVISIONING STEPS",
                    color = colors.textMuted,
                    fontSize = 9.5.sp
                )
                InfoStep(number = "01", text = "Download a compatible LiteRT-LM (.litertlm / .task / .bin) model below")
                InfoStep(number = "02", text = "Adjust Temperature, Top-K, and Max Response tokens above")
                InfoStep(number = "03", text = "Tap 'Browse' in Import Local Model and select the file from storage")
                InfoStep(number = "04", text = "Verify air-gapped inference in Chat or Offline Workbench")
            }

            HorizontalDivider(color = colors.borderSubtle, thickness = 1.dp)

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                RelayMonoCaps(
                    text = "CURATED HUGGING FACE RELEASES",
                    color = colors.textMuted,
                    fontSize = 9.5.sp
                )

                DownloadLink(
                    label = "Gemma 3n E2B IT · LiteRT Preview",
                    badge = "RECOMMENDED · ~1.8 GB",
                    url = "https://huggingface.co/google/gemma-3n-E2B-it-litert-preview",
                    onClick = { uriHandler.openUri(it) }
                )
                DownloadLink(
                    label = "Gemma 3n E4B IT · LiteRT Preview",
                    badge = "HIGH CAPACITY · ~2.9 GB",
                    url = "https://huggingface.co/google/gemma-3n-E4B-it-litert-preview",
                    onClick = { uriHandler.openUri(it) }
                )
                DownloadLink(
                    label = "Gemma 2 2B IT · INT4 GPU",
                    badge = "COMPACT · ~1.4 GB",
                    url = "https://huggingface.co/google/gemma-2-2b-it",
                    onClick = { uriHandler.openUri(it) }
                )
                DownloadLink(
                    label = "Browse Full Gemma 3n Collection",
                    badge = "CATALOG",
                    url = "https://huggingface.co/collections/google/gemma-3n-67e32e29f05a47e895a8beac",
                    onClick = { uriHandler.openUri(it) }
                )
            }

            Text(
                text = "Supported formats: LiteRT-LM (.litertlm, .task, .bin), TFLite (.tflite)",
                style = MaterialTheme.typography.labelSmall,
                color = colors.textMuted
            )
        }
    }
}

@Composable
private fun DownloadLink(
    label: String,
    badge: String,
    url: String,
    onClick: (String) -> Unit
) {
    val colors = MaterialTheme.extendedColors
    Surface(
        onClick = { onClick(url) },
        shape = RoundedCornerShape(RelayRadius.sm),
        color = colors.pitch,
        border = BorderStroke(1.dp, colors.borderSubtle),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = colors.textPrimary
                )
                RelayMonoCaps(
                    text = badge,
                    color = colors.accentBlue,
                    fontSize = 9.sp
                )
            }
            Icon(
                imageVector = Icons.Default.OpenInNew,
                contentDescription = "Open link",
                tint = colors.textSecondary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

@Composable
private fun InfoStep(
    number: String,
    text: String
) {
    val colors = MaterialTheme.extendedColors
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(6.dp),
            color = colors.pitch,
            border = BorderStroke(1.dp, colors.borderSubtle)
        ) {
            Text(
                text = number,
                style = MaterialTheme.typography.labelSmall,
                color = colors.accentCyan,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = colors.textSecondary
        )
    }
}

@Composable
private fun PreviewSettingsScreenContent(
    loadedModels: List<LoadedModel> = emptyList(),
    currentModelPath: String? = null,
    isDarkTheme: Boolean = false
) {
    GemmaTheme(darkTheme = isDarkTheme) {
        SettingsScreen(
            onNavigateBack = {},
            onLoadModel = { _, _ -> },
            onRemoveModel = {},
            loadedModels = loadedModels,
            currentModelPath = currentModelPath
        )
    }
}

@Preview
@Composable
fun SettingsScreenPreviewEmpty() {
    PreviewSettingsScreenContent()
}

@Preview
@Composable
fun SettingsScreenPreviewEmptyDark() {
    PreviewSettingsScreenContent(isDarkTheme = true)
}

@OptIn(kotlin.time.ExperimentalTime::class)
@Preview
@Composable
fun SettingsScreenPreviewWithModels() {
    val now = Clock.System.now().toEpochMilliseconds()
    val sampleModels = listOf(
        LoadedModel(
            name = "gemma-3n-E2B-it-int4",
            path = "/storage/emulated/0/Download/gemma-3n-E2B-it-int4.litertlm",
            config = ModelConfig(maxTokens = 2048, temperature = 0.7f, topK = 40),
            loadedAt = now - 3600_000
        ),
        LoadedModel(
            name = "gemma-2b-it-gpu-int4",
            path = "/storage/emulated/0/Download/gemma-2b-it-gpu-int4.bin",
            config = ModelConfig(maxTokens = 1024, temperature = 0.8f, topK = 50),
            loadedAt = now - 86400_000
        )
    )
    PreviewSettingsScreenContent(
        loadedModels = sampleModels,
        currentModelPath = sampleModels.first().path,
        isDarkTheme = true
    )
}
