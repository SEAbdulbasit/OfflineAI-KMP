package org.abma.offlinelai_kmp.tools

expect object AppActionsProvider {
    suspend fun openUrl(url: String): Boolean
    suspend fun openDialer(phoneNumber: String): Boolean
    suspend fun openEmail(to: String, subject: String, body: String): Boolean
    suspend fun openMaps(query: String): Boolean
    suspend fun shareText(text: String, title: String): Boolean
    suspend fun copyToClipboard(text: String): Boolean
    suspend fun openAppSettings(): Boolean
    suspend fun toggleTorch(enable: Boolean): Boolean
}
