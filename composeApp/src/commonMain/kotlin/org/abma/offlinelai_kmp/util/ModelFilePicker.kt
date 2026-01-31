package org.abma.offlinelai_kmp.util

/**
 * Platform-specific file picker interface.
 * On iOS: Opens document picker to select files from Files app
 * On Android: Opens file picker or SAF
 */
expect class ModelFilePicker() {
    /**
     * List model files (.bin, .task) available in app's local storage
     */
    fun listAvailableModels(): List<ModelFileInfo>

    /**
     * Get the app's documents/files directory path
     */
    fun getAppStoragePath(): String

    /**
     * Check if a model file exists at the given path
     */
    fun modelExists(fileName: String): Boolean

    /**
     * Get the full path for a model file
     */
    fun getModelPath(fileName: String): String

    /**
     * Save model file bytes to the app's storage
     * Returns the saved file path if successful, null otherwise
     * Note: For large files, use copyModelFile instead
     */
    fun saveModelFile(fileName: String, bytes: ByteArray): String?

    /**
     * Copy a model file from a source path to app storage
     * This is better for large files as it doesn't load everything into memory
     * Returns the destination path if successful, null otherwise
     */
    fun copyModelFile(sourcePath: String, fileName: String): String?
}

data class ModelFileInfo(
    val name: String,
    val path: String,
    val sizeBytes: Long
)
