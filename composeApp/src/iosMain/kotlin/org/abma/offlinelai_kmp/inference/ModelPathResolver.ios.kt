package org.abma.offlinelai_kmp.inference

import platform.Foundation.*

actual object ModelPathResolver {

    actual fun resolve(modelPath: String): String? {
        return getSearchPaths(modelPath).firstOrNull { fileExists(it) }
    }

    actual fun getSearchPaths(modelPath: String): List<String> {
        val documentsDir = getDocumentsDirectory()
        val cachesDir = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: ""
        val homeDir = NSHomeDirectory()

        val bundlePath = NSBundle.mainBundle.pathForResource(
            modelPath.removeSuffix(".bin").removeSuffix(".task"),
            ofType = if (modelPath.endsWith(".bin")) "bin" else "task"
        ) ?: ""

        return listOfNotNull(
            "$documentsDir/$modelPath",
            "$documentsDir/models/$modelPath",
            "$cachesDir/$modelPath",
            bundlePath.ifEmpty { null },
            modelPath,
            "$homeDir/Documents/$modelPath"
        )
    }

    actual fun fileExists(path: String): Boolean {
        return NSFileManager.defaultManager.fileExistsAtPath(path)
    }

    actual fun getDocumentsDirectory(): String {
        return NSSearchPathForDirectoriesInDomains(
            NSDocumentDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: ""
    }
}
