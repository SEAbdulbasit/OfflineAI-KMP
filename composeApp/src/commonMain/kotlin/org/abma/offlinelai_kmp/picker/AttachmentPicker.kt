package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable

data class AttachmentPickerResult(
    val path: String,
    val mimeType: String,
    val fileName: String
)

enum class AttachmentPickerType {
    IMAGES,
    PDFS,
    IMAGES_AND_PDFS
}

@Composable
expect fun rememberAttachmentPicker(
    type: AttachmentPickerType = AttachmentPickerType.IMAGES_AND_PDFS,
    onAttachmentPicked: (AttachmentPickerResult?) -> Unit
): () -> Unit
