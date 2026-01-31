package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable

/**
 * Composable that provides file picking functionality.
 */
@Composable
expect fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit
