package org.abma.offlinelai_kmp.inference

/**
 * Platform-specific model file utilities.
 */
expect object ModelPathResolver {
    /**
     * Resolve a model filename to its full path.
     * Searches platform-specific locations.
     * @return The full path if found, null otherwise
     */
    fun resolve(modelPath: String): String?

    /**
     * Get the list of search paths for the given filename.
     */
    fun getSearchPaths(modelPath: String): List<String>

    /**
     * Check if a file exists at the given path.
     */
    fun fileExists(path: String): Boolean

    /**
     * Get the app's documents directory path.
     */
    fun getDocumentsDirectory(): String
}
