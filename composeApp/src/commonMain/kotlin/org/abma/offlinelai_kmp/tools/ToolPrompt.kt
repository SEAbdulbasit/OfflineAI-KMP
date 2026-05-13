package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.*

fun buildSystemPrompt(specs: List<ToolSpec>): String = buildString {
    appendLine("Tools available: ${specs.joinToString { it.name }}")
    appendLine("Format: <<{\"tool\":\"name\",\"arguments\":{}}>>")
}

fun extractToolCall(text: String): ToolCall? {
    val start = text.indexOf("<<")
    val end = text.indexOf(">>")
    if (start == -1 || end == -1) return null
    return try {
        val json = Json.parseToJsonElement(text.substring(start + 2, end)).jsonObject
        ToolCall(json["tool"]?.jsonPrimitive?.content ?: "", json["arguments"]?.jsonObject ?: JsonObject(emptyMap()))
    } catch (e: Exception) { null }
}
