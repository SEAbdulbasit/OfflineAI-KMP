package org.abma.offlinelai_kmp.inference

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Model Path Resolver (expect/actual)
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Handles finding the model file across different locations.
 *
 * WHY IS THIS NEEDED?
 * Model files can be in various places:
 * - App's internal storage (after user import)
 * - App's external storage
 * - Downloads folder
 * - /data/local/tmp/llm (developer ADB push)
 *
 * This resolver checks all common locations so the user doesn't
 * need to specify the exact path.
 *
 * PLATFORM DIFFERENCES:
 * - Android: Multiple storage locations, needs Context
 * - iOS: Different directory structure (Documents, Bundle)
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
expect object ModelPathResolver {
    /**
     * Find the model file by checking common locations.
     *
     * @param modelPath Filename or partial path (e.g., "gemma-2b-it-gpu-int4.bin")
     * @return Absolute path if found, null if not found anywhere
     */
    fun resolve(modelPath: String): String?

    /**
     * Get list of paths that will be searched.
     * Useful for error messages showing where we looked.
     */
    fun getSearchPaths(modelPath: String): List<String>

    /**
     * Check if a file exists at the given path.
     */
    fun fileExists(path: String): Boolean

    /**
     * Get the app's documents/files directory for storing models.
     */
    fun getDocumentsDirectory(): String
}
