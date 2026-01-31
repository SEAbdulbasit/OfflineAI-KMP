package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.*
import platform.darwin.NSObject

@Composable
actual fun rememberAttachmentPicker(
    type: AttachmentPickerType,
    onAttachmentPicked: (AttachmentPickerResult?) -> Unit
): () -> Unit {
    return remember {
        { showAttachmentPicker(type, onAttachmentPicked) }
    }
}

// Keep a strong reference to prevent delegate from being deallocated
private var currentAttachmentDelegate: AttachmentPickerDelegate? = null

private fun showAttachmentPicker(
    type: AttachmentPickerType,
    onAttachmentPicked: (AttachmentPickerResult?) -> Unit
) {
    val contentTypes = when (type) {
        AttachmentPickerType.IMAGES -> listOf(UTTypeImage)
        AttachmentPickerType.PDFS -> listOf(UTTypePDF)
        AttachmentPickerType.IMAGES_AND_PDFS -> listOf(UTTypeImage, UTTypePDF)
    }

    val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = contentTypes,
        asCopy = true
    )

    val delegate = AttachmentPickerDelegate(onAttachmentPicked)
    currentAttachmentDelegate = delegate

    picker.delegate = delegate
    picker.allowsMultipleSelection = false
    picker.modalPresentationStyle = UIModalPresentationPageSheet

    val rootViewController = getRootViewController()
    var topController = rootViewController
    while (topController?.presentedViewController != null) {
        topController = topController.presentedViewController
    }

    topController?.presentViewController(picker, animated = true, completion = null)
}

private fun getRootViewController(): UIViewController? {
    @Suppress("DEPRECATION")
    return UIApplication.sharedApplication.keyWindow?.rootViewController
}

private class AttachmentPickerDelegate(
    private val onAttachmentPicked: (AttachmentPickerResult?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        try {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url != null) {
                // Move file processing to background queue
                val backgroundQueue = NSOperationQueue()
                backgroundQueue.qualityOfService = NSQualityOfServiceUserInitiated

                backgroundQueue.addOperationWithBlock {
                    val result = processPickedAttachment(url)

                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        try {
                            onAttachmentPicked(result)
                        } catch (e: Exception) {
                            NSLog("Error in attachment picker callback: ${e.message}")
                            onAttachmentPicked(null)
                        }
                        // Release delegate
                        currentAttachmentDelegate = null
                    }
                }
            } else {
                NSOperationQueue.mainQueue.addOperationWithBlock {
                    onAttachmentPicked(null)
                    currentAttachmentDelegate = null
                }
            }
        } catch (e: Exception) {
            NSLog("Error processing picked attachment: ${e.message}")
            NSOperationQueue.mainQueue.addOperationWithBlock {
                onAttachmentPicked(null)
                currentAttachmentDelegate = null
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        NSOperationQueue.mainQueue.addOperationWithBlock {
            try {
                onAttachmentPicked(null)
            } catch (e: Exception) {
                NSLog("Error in attachment picker cancel callback: ${e.message}")
            }
            currentAttachmentDelegate = null
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun processPickedAttachment(sourceUrl: NSURL): AttachmentPickerResult? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val documentsDir = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return null

            // Create attachments directory
            val attachmentsDir = "$documentsDir/attachments"
            if (!fileManager.fileExistsAtPath(attachmentsDir)) {
                fileManager.createDirectoryAtPath(attachmentsDir, true, null, null)
            }

            val originalFileName = sourceUrl.lastPathComponent
                ?: "attachment_${NSDate().timeIntervalSince1970.toLong()}"
            val uniqueFileName = "${NSDate().timeIntervalSince1970.toLong()}_$originalFileName"
            val destPath = "$attachmentsDir/$uniqueFileName"

            // Remove existing file if present
            if (fileManager.fileExistsAtPath(destPath)) {
                fileManager.removeItemAtPath(destPath, null)
            }

            val sourcePath = sourceUrl.path ?: return null

            if (!fileManager.fileExistsAtPath(sourcePath)) {
                NSLog("Source attachment does not exist: $sourcePath")
                return null
            }

            val success = fileManager.copyItemAtPath(sourcePath, destPath, null)

            if (success) {
                NSLog("Attachment copied successfully to: $destPath")
                // Clean up temp file
                fileManager.removeItemAtPath(sourcePath, null)

                // Determine mime type from extension
                val mimeType = getMimeType(originalFileName)

                AttachmentPickerResult(
                    path = destPath,
                    mimeType = mimeType,
                    fileName = originalFileName
                )
            } else {
                NSLog("Failed to copy attachment")
                null
            }
        } catch (e: Exception) {
            NSLog("Error copying attachment: ${e.message}")
            null
        }
    }

    private fun getMimeType(fileName: String): String {
        val extension = fileName.substringAfterLast(".", "").lowercase()
        return when (extension) {
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "gif" -> "image/gif"
            "webp" -> "image/webp"
            "heic", "heif" -> "image/heic"
            "pdf" -> "application/pdf"
            else -> "application/octet-stream"
        }
    }
}
