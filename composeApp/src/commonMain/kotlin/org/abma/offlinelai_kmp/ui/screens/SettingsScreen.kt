package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.picker.FilePickerStatus
import org.abma.offlinelai_kmp.picker.rememberFilePickerWithStatus
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
    var pendingModelPath by remember { mutableStateOf<String?>(null) }
    var isCopyingFile by remember { mutableStateOf(false) }
    var copyError by remember { mutableStateOf<String?>(null) }

    val extendedColors = MaterialTheme.extendedColors

    // File picker with status reporting for importing new models
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

    Scaffold(
        modifier = Modifier.pointerInput(Unit) {
            detectTapGestures(onTap = {
                focusManager.clearFocus()
            })
        },
        topBar = {
            SettingsTopBar(
                onNavigateBack = onNavigateBack,
                headerBackground = extendedColors.headerBackground,
                headerBorder = extendedColors.headerBorder
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0)
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Import Model Section
            SectionHeader(
                title = "Import Model",
                icon = Icons.Default.Add
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Browse Files Button (Dashed border style)
            // Disable button while copying
            ImportModelButton(
                onClick = { launchFilePicker() },
                enabled = !isCopyingFile
            )

            // Show copying progress indicator
            if (isCopyingFile) {
                Spacer(modifier = Modifier.height(16.dp))
                CopyingProgressCard()
            }

            // Show error if copy failed
            copyError?.let { error ->
                Spacer(modifier = Modifier.height(16.dp))
                ErrorCard(
                    message = error,
                    onDismiss = { copyError = null }
                )
            }

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
    val isDark = LocalIsDarkTheme.current
    val extendedColors = MaterialTheme.extendedColors

    Surface(
        color = Color.Black,
        tonalElevation = 0.dp,
        modifier = Modifier.statusBarsPadding()
    ) {
        Column {
            TopAppBar(
                title = {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.padding(start = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFF1A1A1A),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF22C55E))
                                )
                                Text(
                                    text = "Offline",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFFA1A1A1)
                                )
                            }
                        }

                        Icon(
                            imageVector = Icons.Default.SensorsOff,
                            contentDescription = "Offline",
                            tint = Color(0xFF3B82F6),
                            modifier = Modifier.size(22.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                ),
                windowInsets = WindowInsets(0, 0, 0, 0)
            )
            HorizontalDivider(
                color = Color.White.copy(alpha = 0.1f),
                thickness = 1.dp
            )
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
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(bottom = 12.dp)
    ) {
        Surface(
            color = Color(0xFF1E3A8A).copy(alpha = 0.4f),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.size(32.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color(0xFF3B82F6),
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
private fun ImportModelButton(
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFF0A0A0A),
        border = BorderStroke(
            width = 1.dp,
            color = Color.White.copy(alpha = 0.15f)
        ),
        enabled = enabled
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = Color(0xFF525252),
                modifier = Modifier.size(48.dp)
            )
            Text(
                text = "Browse Files to Import Model",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA1A1A1)
            )
        }
    }
}

@Composable
private fun CopyingProgressCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(48.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 4.dp
            )
            Text(
                text = "Copying model file...",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This may take a few minutes for large models",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorCard(
    message: String,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Error,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = "Failed to import model",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.7f)
                    )
                }
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Dismiss",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
            }
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
                    containerColor = MaterialTheme.colorScheme.primary
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
    Surface(
        onClick = onLoad,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0A0A0A),
        border = BorderStroke(
            width = if (isActive) 2.dp else 1.dp,
            color = if (isActive) Color(0xFF2563EB) else Color.White.copy(alpha = 0.1f)
        )
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
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                        )
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
                        text = model.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActive) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(4.dp),
                    color = Color(0xFF262626)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Bolt,
                            contentDescription = null,
                            modifier = Modifier.size(10.dp),
                            tint = Color(0xFFA1A1A1)
                        )
                        Text(
                            text = "ON-DEVICE",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFA1A1A1)
                        )
                    }
                }
            }

            // Action buttons
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onLoad, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Load",
                        tint = Color(0xFF525252),
                        modifier = Modifier.size(24.dp)
                    )
                }
                IconButton(onClick = onRemove, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = Color(0xFFEF4444).copy(alpha = 0.8f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoCard() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF0A0A0A),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.Info,
                contentDescription = null,
                tint = Color(0xFF3B82F6),
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = "How to add model files",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(12.dp))

                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    InfoStep(number = "1", text = "Download a Gemma model (")
                    InfoStep(number = "2", text = "Tap 'Browse Files' above")
                    InfoStep(number = "3", text = "Select the model from your device storage")
                    InfoStep(number = "4", text = "The model will be imported and ready to use")
                }

                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Supported: Gemma 2B/7B, GPU/CPU variants",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF525252)
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
            color = Color(0xFFA1A1A1),
            fontWeight = FontWeight.Medium
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFA1A1A1),
            fontWeight = FontWeight.Medium
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
