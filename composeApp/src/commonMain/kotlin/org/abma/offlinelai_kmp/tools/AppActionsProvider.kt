package org.abma.offlinelai_kmp.tools

expect object AppActionsProvider {
    suspend fun openUrl(url: String): Boolean
}
