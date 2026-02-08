package org.abma.offlinelai_kmp.inference

import java.io.File

actual object ModelPathResolver {

    actual fun resolve(modelPath: String): String? {
        if (modelPath.startsWith("/") && fileExists(modelPath)) {
            return modelPath
        }
        return getSearchPaths(modelPath).firstOrNull { fileExists(it) }
    }

    actual fun getSearchPaths(modelPath: String): List<String> {
        if (modelPath.startsWith("/")) {
            return listOf(modelPath)
        }

        val context = AndroidContextProvider.applicationContext
        return listOf(
            "/data/local/tmp/llm/$modelPath",
            "${context.getExternalFilesDir(null)?.absolutePath}/$modelPath",
            "${context.filesDir.absolutePath}/$modelPath",
            "${context.cacheDir.absolutePath}/$modelPath",
            "/storage/emulated/0/Download/$modelPath",
            modelPath
        )
    }

    actual fun fileExists(path: String): Boolean = File(path).exists()

    actual fun getDocumentsDirectory(): String {
        return AndroidContextProvider.applicationContext.filesDir.absolutePath
    }
}
