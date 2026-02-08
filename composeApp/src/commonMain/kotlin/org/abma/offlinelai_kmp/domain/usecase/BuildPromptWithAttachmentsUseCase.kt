package org.abma.offlinelai_kmp.domain.usecase

import org.abma.offlinelai_kmp.domain.model.Attachment
import org.abma.offlinelai_kmp.domain.model.AttachmentType

class BuildPromptWithAttachmentsUseCase {
    operator fun invoke(text: String, attachments: List<Attachment>): String {
        if (attachments.isEmpty()) return text

        val attachmentDescriptions = attachments.joinToString("\n") { attachment ->
            when (attachment.type) {
                AttachmentType.IMAGE -> "[User attached an image: ${attachment.fileName}]"
                AttachmentType.PDF -> "[User attached a PDF document: ${attachment.fileName}]"
                AttachmentType.DOCUMENT -> "[User attached a document: ${attachment.fileName}]"
            }
        }

        return if (text.isNotEmpty()) {
            "$attachmentDescriptions\n\n$text"
        } else {
            attachmentDescriptions
        }
    }
}
