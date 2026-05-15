package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable

/**
 * Result class for file picker operations.
 * - [FilePickerStatus.Idle] - no operation in progress
 * - [FilePickerStatus.Copying] - file is being copied to app storage
 * - [FilePickerStatus.Success] - file copy completed with path
 * - [FilePickerStatus.Error] - file copy failed with message
 */
sealed class FilePickerStatus {
    data object Idle : FilePickerStatus()
    data object Copying : FilePickerStatus()
    data class Success(val path: String) : FilePickerStatus()
    data class Error(val message: String) : FilePickerStatus()
}

@Composable
expect fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit

/**
 * Enhanced file picker that reports status (copying, success, error).
 */
@Composable
expect fun rememberFilePickerWithStatus(onStatusChange: (FilePickerStatus) -> Unit): () -> Unit
