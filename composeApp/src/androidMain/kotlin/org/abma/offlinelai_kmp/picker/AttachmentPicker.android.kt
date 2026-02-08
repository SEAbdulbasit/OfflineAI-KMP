package org.abma.offlinelai_kmp.picker

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
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
import java.io.FileOutputStream

@Composable
actual fun rememberAttachmentPicker(
    type: AttachmentPickerType,
    onAttachmentPicked: (AttachmentPickerResult?) -> Unit
): () -> Unit {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val mimeTypes = when (type) {
        AttachmentPickerType.IMAGES -> arrayOf("image/*")
        AttachmentPickerType.PDFS -> arrayOf("application/pdf")
        AttachmentPickerType.IMAGES_AND_PDFS -> arrayOf("image/*", "application/pdf")
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                val result = withContext(Dispatchers.IO) {
                    copyAttachmentToAppStorage(context, uri)
                }
                onAttachmentPicked(result)
            }
        } else {
            onAttachmentPicked(null)
        }
    }

    return remember(launcher, mimeTypes) {
        { launcher.launch(mimeTypes) }
    }
}

private fun copyAttachmentToAppStorage(context: Context, uri: Uri): AttachmentPickerResult? {
    return try {
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val fileName = getFileName(context, uri) ?: "attachment_${System.currentTimeMillis()}"

        val attachmentsDir = File(context.filesDir, "attachments")
        if (!attachmentsDir.exists()) {
            attachmentsDir.mkdirs()
        }

        val uniqueFileName = "${System.currentTimeMillis()}_$fileName"
        val destFile = File(attachmentsDir, uniqueFileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        AttachmentPickerResult(
            path = destFile.absolutePath,
            mimeType = mimeType,
            fileName = fileName
        )
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun getFileName(context: Context, uri: Uri): String? {
    var name: String? = null
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
        if (cursor.moveToFirst() && nameIndex >= 0) {
            name = cursor.getString(nameIndex)
        }
    }
    return name
}
