package org.abma.offlinelai_kmp.platform

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.platform.ClipEntry

@OptIn(ExperimentalComposeUiApi::class)
actual fun String.toClipEntry(): ClipEntry =
    ClipEntry.withPlainText(this)
