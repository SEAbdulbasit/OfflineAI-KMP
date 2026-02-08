package org.abma.offlinelai_kmp.inference

expect object ModelPathResolver {
    fun resolve(modelPath: String): String?
    fun getSearchPaths(modelPath: String): List<String>
    fun fileExists(path: String): Boolean
    fun getDocumentsDirectory(): String
}
