package org.abma.offlinelai_kmp.picker

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.*
import platform.UIKit.*
import platform.UniformTypeIdentifiers.UTTypeData
import platform.UniformTypeIdentifiers.UTTypeItem
import platform.darwin.NSObject

@Composable
actual fun rememberFilePicker(onFilePicked: (String?) -> Unit): () -> Unit {
    return remember {
        { showDocumentPicker(onFilePicked) }
    }
}

// Keep a strong reference to prevent delegate from being deallocated
private var currentDelegate: DocumentPickerDelegate? = null

private fun showDocumentPicker(onFilePicked: (String?) -> Unit) {
    val documentTypes = listOf(UTTypeData, UTTypeItem)

    val picker = UIDocumentPickerViewController(
        forOpeningContentTypes = documentTypes,
        asCopy = true  // iOS will copy the file to a temp location
    )

    // Create delegate and keep strong reference
    val delegate = DocumentPickerDelegate { path ->
        // Callback from main queue - do NOT release delegate here
        // as it may still be used by UIKit
        onFilePicked(path)
        // Release delegate asynchronously after a short delay
        NSOperationQueue.mainQueue.addOperationWithBlock {
            currentDelegate = null
        }
    }
    currentDelegate = delegate

    picker.delegate = delegate
    picker.allowsMultipleSelection = false
    picker.modalPresentationStyle = UIModalPresentationPageSheet

    // Get the top view controller safely
    val rootViewController = getRootViewController()

    // Find the topmost presented view controller
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

private class DocumentPickerDelegate(
    private val onFilePicked: (String?) -> Unit
) : NSObject(), UIDocumentPickerDelegateProtocol {

    override fun documentPicker(
        controller: UIDocumentPickerViewController,
        didPickDocumentsAtURLs: List<*>
    ) {
        try {
            val url = didPickDocumentsAtURLs.firstOrNull() as? NSURL
            if (url != null) {
                val backgroundQueue = NSOperationQueue()
                backgroundQueue.qualityOfService = NSQualityOfServiceUserInitiated

                backgroundQueue.addOperationWithBlock {
                    val destPath = copyToDocuments(url)

                    NSOperationQueue.mainQueue.addOperationWithBlock {
                        try {
                            onFilePicked(destPath)
                        } catch (e: Exception) {
                            NSLog("Error in file picker callback: ${e.message}")
                            onFilePicked(null)
                        }
                    }
                }
            } else {
                NSOperationQueue.mainQueue.addOperationWithBlock {
                    onFilePicked(null)
                }
            }
        } catch (e: Exception) {
            NSLog("Error processing picked document: ${e.message}")
            NSOperationQueue.mainQueue.addOperationWithBlock {
                onFilePicked(null)
            }
        }
    }

    override fun documentPickerWasCancelled(controller: UIDocumentPickerViewController) {
        NSOperationQueue.mainQueue.addOperationWithBlock {
            try {
                onFilePicked(null)
            } catch (e: Exception) {
                NSLog("Error in file picker cancel callback: ${e.message}")
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    private fun copyToDocuments(sourceUrl: NSURL): String? {
        return try {
            val fileManager = NSFileManager.defaultManager
            val documentsDir = NSSearchPathForDirectoriesInDomains(
                NSDocumentDirectory,
                NSUserDomainMask,
                true
            ).firstOrNull() as? String ?: return null

            val fileName = sourceUrl.lastPathComponent
                ?: "model_${NSDate().timeIntervalSince1970.toLong()}.bin"
            val destPath = "$documentsDir/$fileName"

            if (fileManager.fileExistsAtPath(destPath)) {
                fileManager.removeItemAtPath(destPath, null)
            }

            val sourcePath = sourceUrl.path ?: return null

            if (!fileManager.fileExistsAtPath(sourcePath)) {
                NSLog("Source file does not exist: $sourcePath")
                return null
            }

            val success = fileManager.copyItemAtPath(sourcePath, destPath, null)

            if (success) {
                NSLog("File copied successfully to: $destPath")
                fileManager.removeItemAtPath(sourcePath, null)
                destPath
            } else {
                NSLog("Failed to copy file to Documents")
                sourcePath
            }
        } catch (e: Exception) {
            NSLog("Error copying file: ${e.message}")
            null
        }
    }
}
