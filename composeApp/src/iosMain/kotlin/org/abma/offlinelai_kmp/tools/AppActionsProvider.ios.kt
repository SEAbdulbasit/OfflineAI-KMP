package org.abma.offlinelai_kmp.tools

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import platform.Foundation.*
import platform.UIKit.*
import kotlin.coroutines.resume

actual object AppActionsProvider {

    actual suspend fun openUrl(url: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val validUrl = if (!url.startsWith("http://") && !url.startsWith("https://")) {
                "https://$url"
            } else url

            val nsUrl = NSURL.URLWithString(validUrl) ?: return@withContext false

            // Use modern openURL:options:completionHandler: API
            suspendCancellableCoroutine { continuation ->
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            println("Failed to open URL: ${e.message}")
            false
        }
    }

    actual suspend fun openDialer(phoneNumber: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val cleanNumber = phoneNumber.replace(Regex("[^0-9+]"), "")
            val nsUrl = NSURL.URLWithString("tel:$cleanNumber") ?: return@withContext false

            suspendCancellableCoroutine { continuation ->
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            println("Failed to open dialer: ${e.message}")
            false
        }
    }

    actual suspend fun openEmail(to: String, subject: String, body: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val encodedSubject = encodeUrlComponent(subject)
            val encodedBody = encodeUrlComponent(body)
            val urlString = buildString {
                append("mailto:$to")
                val params = mutableListOf<String>()
                if (subject.isNotEmpty()) params.add("subject=$encodedSubject")
                if (body.isNotEmpty()) params.add("body=$encodedBody")
                if (params.isNotEmpty()) {
                    append("?")
                    append(params.joinToString("&"))
                }
            }
            val nsUrl = NSURL.URLWithString(urlString) ?: return@withContext false

            suspendCancellableCoroutine { continuation ->
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            println("Failed to open email: ${e.message}")
            false
        }
    }

    actual suspend fun openMaps(query: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val encodedQuery = encodeUrlComponent(query)
            val nsUrl = NSURL.URLWithString("maps://?q=$encodedQuery") ?: return@withContext false

            suspendCancellableCoroutine { continuation ->
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            println("Failed to open maps: ${e.message}")
            false
        }
    }

    @Suppress("DEPRECATION")
    actual suspend fun shareText(text: String, title: String): Boolean = withContext(Dispatchers.Main) {
        try {
            val activityItems = listOf(text)
            val activityVC = UIActivityViewController(
                activityItems = activityItems,
                applicationActivities = null
            )

            // Get the top view controller to present from
            val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
            var topVC = rootVC
            while (topVC?.presentedViewController != null) {
                topVC = topVC.presentedViewController
            }

            topVC?.presentViewController(activityVC, animated = true, completion = null)
            true
        } catch (e: Exception) {
            println("Failed to share: ${e.message}")
            false
        }
    }

    actual suspend fun copyToClipboard(text: String): Boolean = withContext(Dispatchers.Main) {
        try {
            UIPasteboard.generalPasteboard.string = text
            true
        } catch (e: Exception) {
            println("Failed to copy to clipboard: ${e.message}")
            false
        }
    }

    actual suspend fun openAppSettings(): Boolean = withContext(Dispatchers.Main) {
        try {
            val nsUrl = NSURL.URLWithString(UIApplicationOpenSettingsURLString) ?: return@withContext false

            suspendCancellableCoroutine { continuation ->
                UIApplication.sharedApplication.openURL(
                    nsUrl,
                    options = emptyMap<Any?, Any>(),
                    completionHandler = { success ->
                        continuation.resume(success)
                    }
                )
            }
        } catch (e: Exception) {
            println("Failed to open settings: ${e.message}")
            false
        }
    }

    private fun encodeUrlComponent(value: String): String {
        // Simple URL encoding for common characters
        return value
            .replace("%", "%25")
            .replace(" ", "%20")
            .replace("&", "%26")
            .replace("=", "%3D")
            .replace("?", "%3F")
            .replace("#", "%23")
            .replace("\n", "%0A")
    }
}
