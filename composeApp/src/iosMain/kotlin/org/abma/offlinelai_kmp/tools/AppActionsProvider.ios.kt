package org.abma.offlinelai_kmp.tools

import platform.Foundation.*
import platform.UIKit.*

actual object AppActionsProvider {
    actual suspend fun openUrl(url: String): Boolean {
        val nsUrl = NSURL.URLWithString(url) ?: return false
        UIApplication.sharedApplication.openURL(nsUrl)
        return true
    }
}
