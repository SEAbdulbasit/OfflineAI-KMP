package org.abma.offlinelai_kmp.picker

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import org.abma.offlinelai_kmp.inference.AndroidContextProvider
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        if (uri != null) {
            val path = copyFileToAppStorage(context, uri)
            onFilePicked(path)
        } else {
            onFilePicked(null)
        }
    }

    return remember(launcher) {
        { launcher.launch(arrayOf("application/octet-stream", "*/*")) }
    }
}

private fun copyFileToAppStorage(context: Context, uri: Uri): String? {
    return try {
        val fileName = getFileName(context, uri) ?: "model_${System.currentTimeMillis()}.bin"
        val destFile = File(context.filesDir, fileName)

        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(destFile).use { output ->
                input.copyTo(output)
            }
        }

        destFile.absolutePath
    } catch (e: Exception) {
        e.printStackTrace()
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
