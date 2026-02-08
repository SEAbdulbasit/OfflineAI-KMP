package org.abma.offlinelai_kmp.tools

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.abma.offlinelai_kmp.inference.AndroidContextProvider

actual object AppActionsProvider {

    private val context: Context
        get() = AndroidContextProvider.applicationContext

    actual suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(validUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            println("Failed to open URL: ${e.message}")
            false
        }
    }

    actual suspend fun openDialer(phoneNumber: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phoneNumber")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            println("Failed to open dialer: ${e.message}")
            false
        }
    }

    actual suspend fun openEmail(to: String, subject: String, body: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
                if (subject.isNotEmpty()) putExtra(Intent.EXTRA_SUBJECT, subject)
                if (body.isNotEmpty()) putExtra(Intent.EXTRA_TEXT, body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            println("Failed to open email: ${e.message}")
            false
        }
    }

    actual suspend fun openMaps(query: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(query)}")).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            println("Failed to open maps: ${e.message}")
            false
        }
    }

    actual suspend fun shareText(text: String, title: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
                if (title.isNotEmpty()) putExtra(Intent.EXTRA_TITLE, title)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val chooser = Intent.createChooser(intent, title.ifEmpty { "Share via" }).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(chooser)
            true
        } catch (e: Exception) {
            println("Failed to share: ${e.message}")
            false
        }
    }

    actual suspend fun copyToClipboard(text: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Copied Text", text)
            clipboard.setPrimaryClip(clip)
            true
        } catch (e: Exception) {
            println("Failed to copy to clipboard: ${e.message}")
            false
        }
    }

    actual suspend fun openAppSettings(): Boolean = withContext(Dispatchers.Main) {
        try {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            println("Failed to open settings: ${e.message}")
            false
        }
    }

    actual suspend fun toggleTorch(enable: Boolean): Boolean = withContext(Dispatchers.Main) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
                val cameraId = cameraManager.cameraIdList.firstOrNull() ?: return@withContext false
                cameraManager.setTorchMode(cameraId, enable)
                true
            } else {
                println("Torch control requires Android M or higher")
                false
            }
        } catch (e: Exception) {
            println("Failed to toggle torch: ${e.message}")
            false
        }
    }
}
