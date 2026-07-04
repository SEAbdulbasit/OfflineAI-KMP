package org.abma.offlinelai_kmp.inference

import platform.Foundation.*

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

        val documentsDir = getDocumentsDirectory()
        val cachesDir = NSSearchPathForDirectoriesInDomains(
            NSCachesDirectory, NSUserDomainMask, true
        ).firstOrNull() as? String ?: ""
        val homeDir = NSHomeDirectory()

        val fileName = modelPath.substringBeforeLast('.', modelPath)
        val fileExtension = modelPath.substringAfterLast('.', "").ifEmpty { null }
        val bundlePath = NSBundle.mainBundle.pathForResource(fileName, ofType = fileExtension) ?: ""

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
