package org.abma.offlinelai_kmp.platform

import android.content.ClipData
import androidx.compose.ui.platform.ClipEntry

actual fun String.toClipEntry(): ClipEntry =
    ClipEntry(ClipData.newPlainText(this, this))
