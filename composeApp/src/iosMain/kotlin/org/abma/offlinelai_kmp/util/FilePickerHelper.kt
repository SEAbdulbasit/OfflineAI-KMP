package org.abma.offlinelai_kmp.util

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.*

/**
 * iOS implementation of ModelFilePicker.
 * Provides access to model files in the app's Documents directory.
 */
@OptIn(ExperimentalForeignApi::class)
actual class ModelFilePicker {

    actual fun listAvailableModels(): List<ModelFileInfo> {
        val documentsDir = getAppStoragePath()
        val fileManager = NSFileManager.defaultManager

        val contents = fileManager.contentsOfDirectoryAtPath(documentsDir, null)

        return (contents as? List<*>)?.mapNotNull { it as? String }
            ?.filter { it.endsWith(".bin") || it.endsWith(".task") }
            ?.map { fileName ->
                val filePath = "$documentsDir/$fileName"
                val attributes = fileManager.attributesOfItemAtPath(filePath, null)
                val size = (attributes?.get(NSFileSize) as? NSNumber)?.longValue ?: 0L
                ModelFileInfo(
                    name = fileName,
                    path = filePath,
                    sizeBytes = size
                )
            }
            ?: emptyList()
    }

    actual fun getAppStoragePath(): String {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: ""
    }

    actual fun modelExists(fileName: String): Boolean {
        val filePath = getModelPath(fileName)
        return NSFileManager.defaultManager.fileExistsAtPath(filePath)
    }

    actual fun getModelPath(fileName: String): String {
        return "${getAppStoragePath()}/$fileName"
    }

    actual fun saveModelFile(fileName: String, bytes: ByteArray): String? {
        val documentsDir = getAppStoragePath()
        val filePath = "$documentsDir/$fileName"

        return try {
            val data = bytes.usePinned { pinned ->
                NSData.dataWithBytes(pinned.addressOf(0), bytes.size.toULong())
            }
            val success = data.writeToFile(filePath, atomically = true)
            if (success) filePath else null
        } catch (e: Exception) {
            null
        }
    }

    actual fun copyModelFile(sourcePath: String, fileName: String): String? {
        val documentsDir = getAppStoragePath()
        val destinationPath = "$documentsDir/$fileName"
        val fileManager = NSFileManager.defaultManager

        return try {
            // Remove existing file if present
            if (fileManager.fileExistsAtPath(destinationPath)) {
                fileManager.removeItemAtPath(destinationPath, null)
            }

            // Handle file:// URL prefix if present
            val cleanSourcePath = if (sourcePath.startsWith("file://")) {
                sourcePath.removePrefix("file://")
            } else {
                sourcePath
            }

            // Try to copy the file
            val success = fileManager.copyItemAtPath(cleanSourcePath, destinationPath, null)
            if (success) destinationPath else null
        } catch (e: Exception) {
            println("Error copying file: ${e.message}")
            null
        }
    }
}


/**
 * Additional iOS-specific file utilities
 */
@OptIn(ExperimentalForeignApi::class)
object IOSFileUtils {

    /**
     * Copy a file from a source URL to the app's Documents directory.
     * Used when importing files via document picker.
     */
    fun copyFileToAppDocuments(sourceUrl: NSURL, fileName: String): String? {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: return null

        val destinationPath = "$documentsDir/$fileName"
        val destinationUrl = NSURL.fileURLWithPath(destinationPath)

        val fileManager = NSFileManager.defaultManager

        // Remove existing file if present
        if (fileManager.fileExistsAtPath(destinationPath)) {
            fileManager.removeItemAtPath(destinationPath, null)
        }

        // Start accessing security-scoped resource
        val accessing = sourceUrl.startAccessingSecurityScopedResource()

        return try {
            val success = fileManager.copyItemAtURL(sourceUrl, destinationUrl, null)
            if (success) destinationPath else null
        } catch (e: Exception) {
            null
        } finally {
            if (accessing) {
                sourceUrl.stopAccessingSecurityScopedResource()
            }
        }
    }

    /**
     * Delete a model file
     */
    fun deleteModelFile(fileName: String): Boolean {
        val documentsDir = NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory,
            NSUserDomainMask,
            true
        ).firstOrNull() as? String ?: return false

        val filePath = "$documentsDir/$fileName"
        return NSFileManager.defaultManager.removeItemAtPath(filePath, null)
    }

    /**
     * Get file size in a human-readable format
     */
    fun getFileSizeString(bytes: Long): String {
        return when {
            bytes >= 1_000_000_000 -> "${bytes / 1_000_000_000} GB"
            bytes >= 1_000_000 -> "${bytes / 1_000_000} MB"
            bytes >= 1_000 -> "${bytes / 1_000} KB"
            else -> "$bytes B"
        }
    }
}
