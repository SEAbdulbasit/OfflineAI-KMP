package org.abma.offlinelai_kmp.tools

import android.content.Intent
import android.net.Uri
import org.abma.offlinelai_kmp.inference.AndroidContextProvider

actual object AppActionsProvider {
    actual suspend fun openUrl(url: String): Boolean {
        return try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            AndroidContextProvider.applicationContext.startActivity(intent)
            true
        } catch (e: Exception) { false }
    }
}
