package org.abma.offlinelai_kmp.util

import android.os.Environment
import java.io.File

/**
 * Android implementation of ModelFilePicker.
 * Provides access to model files in various storage locations.
 */
actual class ModelFilePicker {

    actual fun listAvailableModels(): List<ModelFileInfo> {
        val modelFiles = mutableListOf<ModelFileInfo>()

        // Check multiple locations
        val searchDirs = listOf(
            // App's files directory
            getAppStoragePath(),
            // Downloads folder
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).absolutePath,
            // Common LLM location
            "/data/local/tmp/llm",
            // External storage Documents
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).absolutePath
        )

        for (dir in searchDirs) {
            val directory = File(dir)
            if (directory.exists() && directory.isDirectory) {
                directory.listFiles()?.filter { file ->
                    file.isFile && (file.name.endsWith(".bin") || file.name.endsWith(".task"))
                }?.forEach { file ->
                    modelFiles.add(
                        ModelFileInfo(
                            name = file.name,
                            path = file.absolutePath,
                            sizeBytes = file.length()
                        )
                    )
                }
            }
        }

        return modelFiles.distinctBy { it.path }
    }

    actual fun getAppStoragePath(): String {
        // This will be set by the Android app context
        return AndroidStorageProvider.getFilesDir() ?: "/data/local/tmp"
    }

    actual fun modelExists(fileName: String): Boolean {
        // Check in multiple locations
        val possiblePaths = listOf(
            "${getAppStoragePath()}/$fileName",
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName",
            "/data/local/tmp/llm/$fileName",
            fileName // Direct path
        )

        return possiblePaths.any { File(it).exists() }
    }

    actual fun getModelPath(fileName: String): String {
        // Check in multiple locations and return the first existing one
        val possiblePaths = listOf(
            "${getAppStoragePath()}/$fileName",
            "${Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)}/$fileName",
            "/data/local/tmp/llm/$fileName",
            fileName
        )

        return possiblePaths.firstOrNull { File(it).exists() } ?: possiblePaths.first()
    }

    actual fun saveModelFile(fileName: String, bytes: ByteArray): String? {
        return try {
            val filePath = "${getAppStoragePath()}/$fileName"
            val file = File(filePath)
            file.parentFile?.mkdirs()
            file.writeBytes(bytes)
            filePath
        } catch (e: Exception) {
            null
        }
    }

    actual fun copyModelFile(sourcePath: String, fileName: String): String? {
        return try {
            val destinationPath = "${getAppStoragePath()}/$fileName"
            val sourceFile = File(sourcePath)
            val destFile = File(destinationPath)

            destFile.parentFile?.mkdirs()
            sourceFile.copyTo(destFile, overwrite = true)

            destinationPath
        } catch (e: Exception) {
            null
        }
    }
}

/**
 * Provider for Android storage paths - must be initialized from MainActivity
 */
object AndroidStorageProvider {
    private var filesDir: String? = null
    private var cacheDir: String? = null

    fun init(filesDir: File, cacheDir: File) {
        this.filesDir = filesDir.absolutePath
        this.cacheDir = cacheDir.absolutePath
    }

    fun getFilesDir(): String? = filesDir
    fun getCacheDir(): String? = cacheDir
}
