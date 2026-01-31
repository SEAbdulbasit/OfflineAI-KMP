package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.picker.rememberFilePicker
import org.abma.offlinelai_kmp.ui.theme.*
import org.jetbrains.compose.ui.tooling.preview.Preview
import kotlin.time.Clock

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLoadModel: (String, ModelConfig) -> Unit,
    onRemoveModel: (String) -> Unit = {},
    loadedModels: List<LoadedModel> = emptyList(),
    currentModelPath: String? = null
) {
    var maxTokens by remember { mutableIntStateOf(2048) }
    var temperature by remember { mutableFloatStateOf(0.7f) }
    var topP by remember { mutableFloatStateOf(0.9f) }
    var showAdvancedSettings by remember { mutableStateOf(false) }
    var pendingModelPath by remember { mutableStateOf<String?>(null) }

    val extendedColors = MaterialTheme.extendedColors

    // File picker for importing new models
    val launchFilePicker = rememberFilePicker { path ->
        if (path != null) {
            pendingModelPath = path
        }
    }

    Scaffold(
        topBar = {
            SettingsTopBar(
                onNavigateBack = onNavigateBack,
                headerBackground = extendedColors.headerBackground,
                headerBorder = extendedColors.headerBorder
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Import Model Section
            SectionHeader(
                title = "Import Model",
                icon = Icons.Default.Add
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Browse Files Button (Dashed border style)
            ImportModelButton(
                onClick = { launchFilePicker() }
            )

            // Show pending model path if selected
            pendingModelPath?.let { path ->
                Spacer(modifier = Modifier.height(16.dp))
                PendingModelCard(
                    path = path,
                    onLoad = {
                        val config = ModelConfig(
                            maxTokens = maxTokens,
                            temperature = temperature,
                            topK = 40
                        )
                        onLoadModel(path, config)
                        pendingModelPath = null
                        onNavigateBack()
                    },
                    onCancel = { pendingModelPath = null }
                )
            }

            // Previously Loaded Models Section
            if (loadedModels.isNotEmpty()) {
                Spacer(modifier = Modifier.height(32.dp))

                SectionHeader(
                    title = "Previously Loaded Models",
                    icon = Icons.Default.AutoAwesome
                )

                Spacer(modifier = Modifier.height(16.dp))

                loadedModels.forEachIndexed { index, model ->
                    val isActive = currentModelPath == model.path
                    ModelCard(
                        model = model,
                        isActive = isActive,
                        onLoad = {
                            onLoadModel(model.path, model.config)
                            onNavigateBack()
                        },
                        onRemove = { onRemoveModel(model.path) }
                    )
                    if (index < loadedModels.lastIndex) {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Advanced Settings Section
            AdvancedSettingsSection(
                expanded = showAdvancedSettings,
                onToggle = { showAdvancedSettings = !showAdvancedSettings },
                temperature = temperature,
                onTemperatureChange = { temperature = it },
                maxTokens = maxTokens,
                onMaxTokensChange = { maxTokens = it },
                topP = topP,
                onTopPChange = { topP = it }
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Info Card
            InfoCard()

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SettingsTopBar(
    onNavigateBack: () -> Unit,
    headerBackground: Color,
    headerBorder: Color
) {
    Surface(
        color = headerBackground.copy(alpha = 0.8f),
        tonalElevation = 0.dp
    ) {
        Column {
            TopAppBar(
                title = {
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
            // Border divider
            Surface(
                modifier = Modifier.fillMaxWidth().height(1.dp),
                color = headerBorder
            ) {}
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PrimaryBlue,
            modifier = Modifier.size(20.dp)
        )
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            fontSize = 17.sp
        )
    }
}

@Composable
private fun ImportModelButton(
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f),
        border = BorderStroke(
            width = 2.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                modifier = Modifier.size(40.dp)
            )
            Text(
                text = "Browse Files to Import Model",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PendingModelCard(
    path: String,
    onLoad: () -> Unit,
    onCancel: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selected file:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = path.substringAfterLast("/"),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                IconButton(onClick = onCancel) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onLoad,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryBlue
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Load This Model",
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
    val extendedColors = MaterialTheme.extendedColors

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isActive) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = PrimaryBlue.copy(alpha = 0.15f),
                        spotColor = PrimaryBlue.copy(alpha = 0.15f)
                    )
                } else Modifier
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.bubbleAi
        ),
        border = if (isActive) {
            BorderStroke(2.dp, PrimaryBlue)
        } else {
            BorderStroke(1.dp, extendedColors.bubbleAiBorder)
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Model Icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .shadow(
                        elevation = if (isActive) 8.dp else 0.dp,
                        shape = RoundedCornerShape(16.dp),
                        ambientColor = GradientIndigo.copy(alpha = 0.3f)
                    )
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isActive) {
                            Brush.linearGradient(listOf(GradientIndigo, GradientPurple))
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color(0xFF76A1F8),
                                    Color(0xFF76A1F8)
                                )
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Model Info
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = model.name.take(15) + if (model.name.length > 15) "..." else "",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) {
                            MaterialTheme.colorScheme.onSurface
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = PrimaryBlue,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // On-device badge
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Bolt,
                                contentDescription = null,
                                modifier = Modifier.size(11.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "ON-DEVICE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.5.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(
                    onClick = onLoad,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Load",
                        tint = if (isActive) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier.size(28.dp)
                    )
                }
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = if (isActive) {
                            MaterialTheme.colorScheme.error
                        } else {
                            MaterialTheme.colorScheme.error.copy(alpha = 0.4f)
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AdvancedSettingsSection(
    expanded: Boolean,
    onToggle: () -> Unit,
    temperature: Float,
    onTemperatureChange: (Float) -> Unit,
    maxTokens: Int,
    onMaxTokensChange: (Int) -> Unit,
    topP: Float,
    onTopPChange: (Float) -> Unit
) {
    val extendedColors = MaterialTheme.extendedColors

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = extendedColors.bubbleAi
        ),
        border = BorderStroke(1.dp, extendedColors.bubbleAiBorder)
    ) {
        Column {
            // Header (clickable)
            Surface(
                onClick = onToggle,
                color = Color.Transparent
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Advanced Settings",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customize generation parameters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 12.sp
                            )
                        }
                    }
                    Icon(
                        imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Expandable content
            AnimatedVisibility(
                visible = expanded,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically()
            ) {
                Column {
                    HorizontalDivider(
                        color = extendedColors.bubbleAiBorder,
                        thickness = 1.dp
                    )
                    Column(
                        modifier = Modifier.padding(24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Temperature slider
                        SettingSlider(
                            title = "Temperature",
                            description = "Controls randomness: lower is more focused, higher is more creative.",
                            value = temperature,
                            onValueChange = onTemperatureChange,
                            valueRange = 0f..1f,
                            displayValue = ((temperature * 10).toInt() / 10.0).toString()
                        )

                        // Max Tokens slider
                        SettingSlider(
                            title = "Max Tokens",
                            description = "The maximum number of tokens to generate in a single response.",
                            value = maxTokens.toFloat(),
                            onValueChange = { onMaxTokensChange(it.toInt()) },
                            valueRange = 256f..4096f,
                            displayValue = maxTokens.toString(),
                            steps = 15
                        )

                        // Top-p slider
                        SettingSlider(
                            title = "Top-p (Nucleus Sampling)",
                            description = null,
                            value = topP,
                            onValueChange = onTopPChange,
                            valueRange = 0f..1f,
                            displayValue = ((topP * 100).toInt() / 100.0).toString()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    description: String?,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String,
    steps: Int = 0
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            steps = steps,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = PrimaryBlue,
                activeTrackColor = PrimaryBlue,
                inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        )

        if (description != null) {
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 11.sp,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun InfoCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrimaryBlue.copy(alpha = 0.05f)
        ),
        border = BorderStroke(1.dp, PrimaryBlue.copy(alpha = 0.2f))
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = PrimaryBlue,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "How to add model files",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 15.sp
                )
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoStep(
                        number = "1",
                        text = "Download a Gemma model ("
                    )
                    InfoStep(number = "2", text = "Tap 'Browse Files' above")
                    InfoStep(number = "3", text = "Select the model from your device storage")
                    InfoStep(number = "4", text = "The model will be imported and ready to use")
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Supported: Gemma 2B/7B, GPU/CPU variants",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Normal
                )
            }
        }
    }
}

@Composable
private fun InfoStep(
    number: String,
    text: String
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$number.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp
        )
        Row {
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp
            )
        }
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

@Preview
@Composable
fun SettingsScreenPreviewWithModels() {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val models = listOf(
        LoadedModel(
            name = "gemma-3n-E2B-it",
            path = "/path/to/gemma-3n-E2B-it.bin",
            config = ModelConfig(),
            loadedAt = currentTime
        ),
        LoadedModel(
            name = "gemma-2b-it-gpu-int4",
            path = "/path/to/gemma-2b-it-gpu-int4.bin",
            config = ModelConfig(),
            loadedAt = currentTime - 86400000
        ),
        LoadedModel(
            name = "llama-3-8b-instruct",
            path = "/path/to/llama-3-8b-instruct.bin",
            config = ModelConfig(),
            loadedAt = currentTime - 172800000
        )
    )
    PreviewSettingsScreenContent(
        loadedModels = models,
        currentModelPath = models.first().path
    )
}

@Preview
@Composable
fun SettingsScreenPreviewWithModelsDark() {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val models = listOf(
        LoadedModel(
            name = "gemma-3n-E2B-it",
            path = "/path/to/gemma-3n-E2B-it.bin",
            config = ModelConfig(),
            loadedAt = currentTime
        ),
        LoadedModel(
            name = "gemma-2b-it-gpu-int4",
            path = "/path/to/gemma-2b-it-gpu-int4.bin",
            config = ModelConfig(),
            loadedAt = currentTime - 86400000
        )
    )
    PreviewSettingsScreenContent(
        loadedModels = models,
        currentModelPath = models.first().path,
        isDarkTheme = true
    )
}

@Preview
@Composable
fun SettingsScreenPreviewSingleModel() {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val models = listOf(
        LoadedModel(
            name = "gemma-2b-it-gpu-int4",
            path = "/path/to/gemma-2b-it-gpu-int4.bin",
            config = ModelConfig(
                maxTokens = 2048,
                temperature = 0.7f,
                topK = 40
            ),
            loadedAt = currentTime
        )
    )
    PreviewSettingsScreenContent(
        loadedModels = models,
        currentModelPath = null
    )
}

@Preview
@Composable
fun SettingsScreenPreviewMultipleModelsNoActive() {
    val currentTime = Clock.System.now().toEpochMilliseconds()
    val models = listOf(
        LoadedModel(
            name = "gemma-3n-E2B-it",
            path = "/path/to/gemma-3n-E2B-it.bin",
            config = ModelConfig(),
            loadedAt = currentTime
        ),
        LoadedModel(
            name = "gemma-2b-it-gpu-int4",
            path = "/path/to/gemma-2b-it-gpu-int4.bin",
            config = ModelConfig(),
            loadedAt = currentTime - 86400000
        )
    )
    PreviewSettingsScreenContent(
        loadedModels = models,
        currentModelPath = null
    )
}

