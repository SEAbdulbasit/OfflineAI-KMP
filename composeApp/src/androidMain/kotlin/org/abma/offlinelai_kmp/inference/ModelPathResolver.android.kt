package org.abma.offlinelai_kmp.inference

import android.os.Environment
import java.io.File

/**
 * ═══════════════════════════════════════════════════════════════════════════════
 * WORKSHOP: Android Model Path Resolution
 * ═══════════════════════════════════════════════════════════════════════════════
 *
 * Finds model files across Android storage locations.
 *
 * WORKSHOP TIP: During development, use:
 * ```bash
 * adb push gemma-2b-it-gpu-int4.bin /data/local/tmp/llm/
 * ```
 * This avoids copying large files through the app.
 *
 * For production, users would import from Downloads or app storage.
 *
 * ═══════════════════════════════════════════════════════════════════════════════
 */
actual object ModelPathResolver {

    /**
     * Try to find the model file.
     *
     * First checks if it's an absolute path that exists.
     * Then searches common Android locations.
     */
    actual fun resolve(modelPath: String): String? {
        // If already absolute and exists, use as-is
        if (modelPath.startsWith("/") && fileExists(modelPath)) {
            return modelPath
        }
        // Otherwise search known locations
        return getSearchPaths(modelPath).firstOrNull { fileExists(it) }
    }

    /**
     * Get all paths we'll search for the model.
     *
     * WORKSHOP: Search order matters!
     * We prioritize:
     * 1. Developer location (/data/local/tmp/llm/) - easiest for testing
     * 2. Downloads folder - where user downloads models
     * 3. External files - where user might import
     * 4. Internal files - app's private storage
     */
    actual fun getSearchPaths(modelPath: String): List<String> {
        if (modelPath.startsWith("/")) {
            return listOf(modelPath)
        }

        val context = AndroidContextProvider.applicationContext
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)

        return listOf(
            // ═══ DEVELOPER: ADB push location ═══
            // adb push model.bin /data/local/tmp/llm/
            "/data/local/tmp/llm/$modelPath",

            // ═══ DOWNLOADS: Where users typically download models ═══
            // This is the most common location for model files
            "${downloadsDir.absolutePath}/$modelPath",

            // ═══ APP EXTERNAL: User-visible app storage ═══
            // Usually: /storage/emulated/0/Android/data/<package>/files/
            "${context.getExternalFilesDir(null)?.absolutePath}/$modelPath",

            // ═══ APP INTERNAL: Private app storage ═══
            // Not visible to user, protected from other apps
            "${context.filesDir.absolutePath}/$modelPath",

            // ═══ CACHE: Temporary storage ═══
            "${context.cacheDir.absolutePath}/$modelPath",


            // ═══ FALLBACK: Raw path as-is ═══
            modelPath
        )
    }

    actual fun fileExists(path: String): Boolean = File(path).exists()

    actual fun getDocumentsDirectory(): String {
        return AndroidContextProvider.applicationContext.filesDir.absolutePath
    }
}
