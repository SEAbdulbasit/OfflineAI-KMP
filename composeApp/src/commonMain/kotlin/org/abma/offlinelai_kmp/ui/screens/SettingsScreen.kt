package org.abma.offlinelai_kmp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.abma.offlinelai_kmp.domain.model.ModelConfig
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import org.abma.offlinelai_kmp.picker.rememberFilePicker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onLoadModel: (String, ModelConfig) -> Unit,
    onRemoveModel: (String) -> Unit = {},
    loadedModels: List<LoadedModel> = emptyList(),
    currentModelPath: String? = null
) {
    val filePicker = rememberFilePicker { path ->
        if (path != null) onLoadModel(path, ModelConfig())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        LazyColumn(Modifier.padding(pad).fillMaxSize().padding(16.dp)) {
            item {
                Button(onClick = { filePicker() }, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Import Model")
                }
                Spacer(Modifier.height(24.dp))
                Text("Models", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
            }

            items(loadedModels) { model ->
                ListItem(
                    headlineContent = { Text(model.name) },
                    supportingContent = { Text(model.path) },
                    trailingContent = {
                        IconButton(onClick = { onRemoveModel(model.path) }) {
                            Icon(Icons.Default.Delete, "Remove")
                        }
                    },
                    colors = if (model.path == currentModelPath) ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.primaryContainer) else ListItemDefaults.colors()
                )
            }
        }
    }
}
