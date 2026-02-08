package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable

@Composable
expect fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit
