package org.abma.offlinelai_kmp.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.model.ModelInfo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLoadModel: (String, ModelConfig) -> Unit,
    currentModelPath: String = ""
) {
    var modelPath by remember { mutableStateOf(currentModelPath.ifBlank { "/storage/emulated/0/Download/gemma-2b-it-gpu-int4.bin" }) }
    var maxTokens by remember { mutableIntStateOf(1024) }
    var temperature by remember { mutableFloatStateOf(0.8f) }
    var topK by remember { mutableIntStateOf(40) }
    var selectedModel by remember { mutableStateOf<ModelInfo?>(ModelInfo.GEMMA_2B_IT_GPU) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Text("←", style = MaterialTheme.typography.titleLarge)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Model Selection Section
            Text(
                text = "Model",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Available Models
            ModelInfo.availableModels.forEach { model ->
                ModelCard(
                    model = model,
                    isSelected = selectedModel == model,
                    onClick = {
                        selectedModel = model
                        modelPath = model.fileName
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Custom Model Path
            Text(
                text = "Custom Model Path",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = modelPath,
                onValueChange = {
                    modelPath = it
                    selectedModel = null
                },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Model file path") },
                placeholder = { Text("/path/to/model.bin") },
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Generation Settings
            Text(
                text = "Generation Settings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Max Tokens
            SettingSlider(
                title = "Max Tokens",
                value = maxTokens.toFloat(),
                onValueChange = { maxTokens = it.toInt() },
                valueRange = 128f..4096f,
                displayValue = maxTokens.toString()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Temperature
            SettingSlider(
                title = "Temperature",
                value = temperature,
                onValueChange = { temperature = it },
                valueRange = 0f..2f,
                displayValue = temperature.toString().take(4)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Top K
            SettingSlider(
                title = "Top K",
                value = topK.toFloat(),
                onValueChange = { topK = it.toInt() },
                valueRange = 1f..100f,
                displayValue = topK.toString()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Load Model Button
            Button(
                onClick = {
                    val config = ModelConfig(
                        modelPath = modelPath,
                        maxTokens = maxTokens,
                        temperature = temperature,
                        topK = topK
                    )
                    onLoadModel(modelPath, config)
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = modelPath.isNotBlank()
            ) {
                Text("Load Model")
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ℹ️",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Column {
                        Text(
                            text = "Model Setup Instructions",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "1. Download a Gemma model (.bin or .task file)\n" +
                                   "2. Place it in your device's storage\n" +
                                   "3. Enter the full path or select from available models\n" +
                                   "4. Tap 'Load Model' to start",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelCard(
    model: ModelInfo,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = model.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
                Text(
                    text = "Size: ~${model.sizeInMB} MB",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    }
                )
            }
            if (isSelected) {
                Text(
                    text = "✓",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun SettingSlider(
    title: String,
    value: Float,
    onValueChange: (Float) -> Unit,
    valueRange: ClosedFloatingPointRange<Float>,
    displayValue: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = displayValue,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange
        )
    }
}
