package org.abma.offlinelai_kmp.domain.model

import kotlin.random.Random
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

/**
 * Types of attachments that can be added to messages
 */
enum class AttachmentType {
    IMAGE,
    PDF,
    DOCUMENT
}

/**
 * Represents an attachment (image, PDF, etc.) that can be added to a message
 */
@OptIn(ExperimentalTime::class)
data class Attachment(
    val id: String = generateId(),
    val type: AttachmentType,
    val filePath: String,
    val fileName: String,
    val mimeType: String,
    val fileSize: Long = 0L,
    val thumbnailPath: String? = null
) {
    companion object {
        @OptIn(ExperimentalTime::class)
        private fun generateId() = "att-${Clock.System.now().toEpochMilliseconds()}-${Random.nextInt(10000)}"

        fun fromPath(path: String, mimeType: String): Attachment {
            val fileName = path.substringAfterLast("/")
            val type = when {
                mimeType.startsWith("image/") -> AttachmentType.IMAGE
                mimeType == "application/pdf" -> AttachmentType.PDF
                else -> AttachmentType.DOCUMENT
            }
            return Attachment(
                type = type,
                filePath = path,
                fileName = fileName,
                mimeType = mimeType
            )
        }

        fun image(path: String, fileName: String = path.substringAfterLast("/")): Attachment {
            return Attachment(
                type = AttachmentType.IMAGE,
                filePath = path,
                fileName = fileName,
                mimeType = "image/*"
            )
        }

        fun pdf(path: String, fileName: String = path.substringAfterLast("/")): Attachment {
            return Attachment(
                type = AttachmentType.PDF,
                filePath = path,
                fileName = fileName,
                mimeType = "application/pdf"
            )
        }
    }
}
