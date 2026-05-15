package org.abma.offlinelai_kmp.picker

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "FilePicker"

/**
 * Check if we have permission to access all files on external storage.
 * Required for Android 11+ to read files directly from Downloads, etc.
 */
fun hasAllFilesAccessPermission(): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        Environment.isExternalStorageManager()
    } else {
        true // On Android 10 and below, READ_EXTERNAL_STORAGE is sufficient
    }
}

@Composable
actual fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        Log.d(TAG, "File picker returned URI: $uri")
        if (uri != null) {
            scope.launch {
                val path = withContext(Dispatchers.IO) {
                    resolveFilePathOrCopy(context, uri)
                }
                Log.d(TAG, "Resolved file path: $path")
                onFilePicked(path)
            }
        } else {
            Log.d(TAG, "File picker returned null URI")
            onFilePicked(null)
        }
    }

    return remember(launcher) {
        { launcher.launch(arrayOf("application/octet-stream", "*/*")) }
    }
}

@Composable
actual fun rememberFilePickerWithStatus(onStatusChange: (FilePickerStatus) -> Unit): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        Log.d(TAG, "File picker returned URI: $uri")
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    try {
                        // Check if we have MANAGE_EXTERNAL_STORAGE permission
                        val hasAllFilesAccess = hasAllFilesAccessPermission()
                        Log.d(TAG, "Has all files access permission: $hasAllFilesAccess")

                        if (hasAllFilesAccess) {
                            // Try to get the actual file path without copying
                            val directPath = getActualFilePath(context, uri)
                            if (directPath != null && File(directPath).exists() && File(directPath).canRead()) {
                                Log.d(TAG, "Using direct file path (no copy): $directPath")
                                // Take persistable permission for future access
                                try {
                                    context.contentResolver.takePersistableUriPermission(
                                        uri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                    )
                                } catch (e: Exception) {
                                    Log.w(TAG, "Could not take persistable permission: ${e.message}")
                                }
                                return@withContext FilePickerStatus.Success(directPath)
                            }
                        }

                        // No direct access - need to copy the file
                        Log.d(TAG, "Direct path not available or no permission, copying file...")

                        // Signal that we're copying (this takes time for large files)
                        withContext(Dispatchers.Main) {
                            onStatusChange(FilePickerStatus.Copying)
                        }

                        val copiedPath = copyFileToAppStorage(context, uri)
                        if (copiedPath != null) {
                            FilePickerStatus.Success(copiedPath)
                        } else {
                            // Check if it's a space issue
                            val availableSpace = context.filesDir.freeSpace / (1024 * 1024) // MB
                            if (availableSpace < 2000) { // Less than 2GB
                                FilePickerStatus.Error(
                                    "Not enough storage space (${availableSpace}MB available).\n\n" +
                                    "Options:\n" +
                                    "1. Free up space on your device\n" +
                                    "2. Grant 'All files access' permission in Settings > Apps > OfflineAI > Permissions"
                                )
                            } else {
                                FilePickerStatus.Error("Failed to copy file")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error resolving file path", e)
                        when {
                            e.message?.contains("ENOSPC") == true ->
                                FilePickerStatus.Error("Not enough storage space to copy the model file")
                            e.message?.contains("Permission denied") == true ->
                                FilePickerStatus.Error(
                                    "Permission denied.\n\n" +
                                    "Please grant 'All files access' in Settings > Apps > OfflineAI"
                                )
                            else -> FilePickerStatus.Error(e.message ?: "Unknown error")
                        }
                    }
                }
                Log.d(TAG, "File picker result: $result")
                onStatusChange(result)
            }
        } else {
            Log.d(TAG, "File picker cancelled")
            onStatusChange(FilePickerStatus.Idle)
        }
    }

    return remember(launcher) {
        { launcher.launch(arrayOf("application/octet-stream", "*/*")) }
    }
}

/**
 * Try to get the actual file path from a content URI without copying.
 * This works for files in external storage (Downloads, etc.)
 */
private fun getActualFilePath(context: Context, uri: Uri): String? {
    Log.d(TAG, "Attempting to resolve actual path for URI: $uri")

    // Handle file:// URIs directly
    if (uri.scheme == "file") {
        return uri.path
    }

    // Handle content:// URIs
    if (uri.scheme == "content") {
        // Check if it's an external storage document
        if (isExternalStorageDocument(uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                val path = "${Environment.getExternalStorageDirectory().absolutePath}/${split[1]}"
                Log.d(TAG, "Resolved external storage path: $path")
                if (File(path).exists()) {
                    return path
                }
            }

            // Handle non-primary volumes (SD card, etc.)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                // On Android 10+, try to use the Downloads directory
                if (split[1].startsWith("Download/") || type == "primary") {
                    val downloadPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/${split[1].removePrefix("Download/")}"
                    Log.d(TAG, "Trying downloads path: $downloadPath")
                    if (File(downloadPath).exists()) {
                        return downloadPath
                    }
                }
            }
        }

        // Check if it's a downloads document
        if (isDownloadsDocument(uri)) {
            try {
                val id = DocumentsContract.getDocumentId(uri)
                // Handle raw: prefix on some devices
                if (id.startsWith("raw:")) {
                    return id.removePrefix("raw:")
                }
                // Handle msf: prefix (media store file)
                if (id.startsWith("msf:")) {
                    // Can't resolve directly, need to copy
                    return null
                }
                // Try standard Downloads path
                val fileName = getFileName(context, uri)
                if (fileName != null) {
                    val downloadPath = "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath}/$fileName"
                    Log.d(TAG, "Trying downloads path with filename: $downloadPath")
                    if (File(downloadPath).exists()) {
                        return downloadPath
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error resolving downloads document: ${e.message}")
            }
        }
    }

    return null
}

/**
 * Resolve file path, preferring direct access over copying.
 * Only uses direct path if we have MANAGE_EXTERNAL_STORAGE permission.
 */
private fun resolveFilePathOrCopy(context: Context, uri: Uri): String? {
    // Only try direct path if we have all files access permission
    if (hasAllFilesAccessPermission()) {
        val directPath = getActualFilePath(context, uri)
        if (directPath != null && File(directPath).exists() && File(directPath).canRead()) {
            Log.d(TAG, "Using direct path: $directPath")
            // Take persistable permission
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (e: Exception) {
                Log.w(TAG, "Could not take persistable permission: ${e.message}")
            }
            return directPath
        }
    }
    
    // Fall back to copying
    Log.d(TAG, "Direct path not available or no permission, copying file...")
    return copyFileToAppStorage(context, uri)
}

private fun isExternalStorageDocument(uri: Uri): Boolean {
    return "com.android.externalstorage.documents" == uri.authority
}

private fun isDownloadsDocument(uri: Uri): Boolean {
    return "com.android.providers.downloads.documents" == uri.authority
}

private fun copyFileToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val fileName = getFileName(context, uri) ?: "model_${System.currentTimeMillis()}.bin"
        Log.d(TAG, "Copying file: $fileName from URI: $uri")

        val destFile = File(context.filesDir, fileName)
        Log.d(TAG, "Destination path: ${destFile.absolutePath}")

        context.contentResolver.openInputStream(uri)?.use { input ->
            destFile.outputStream().use { output ->
                val bytesCopied = input.copyTo(output)
                Log.d(TAG, "Copied $bytesCopied bytes to ${destFile.absolutePath}")
            }
        }

        if (destFile.exists()) {
            Log.d(TAG, "File copy successful, size: ${destFile.length()} bytes")
            destFile.absolutePath
        } else {
            Log.e(TAG, "File copy failed - destination file doesn't exist")
            null
        }
    } catch (e: Exception) {
        Log.e(TAG, "Error copying file: ${e.message}", e)
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
