package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable
import org.abma.offlinelai_kmp.domain.model.Attachment

/**
 * Result from attachment picker containing file path and mime type
 */
data class AttachmentPickerResult(
    val path: String,
    val mimeType: String,
    val fileName: String
)

/**
 * Types of files the attachment picker should allow
 */
enum class AttachmentPickerType {
    IMAGES,
    PDFS,
    IMAGES_AND_PDFS
}

/**
 * Composable that provides attachment picking functionality for images and PDFs.
 */
@Composable
expect fun rememberAttachmentPicker(
    type: AttachmentPickerType = AttachmentPickerType.IMAGES_AND_PDFS,
    onAttachmentPicked: (AttachmentPickerResult?) -> Unit
): () -> Unit
