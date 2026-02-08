package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TOOL_CALL_START = "<<"
private const val TOOL_CALL_END = ">>"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun buildSystemPrompt(specs: List<ToolSpec>): String {
    if (specs.isEmpty()) return ""

    return buildString {
        appendLine("You are a helpful AI assistant with access to tools.")
        appendLine("When you need to use a tool, output it in this EXACT format:")
        appendLine("<<{\"tool\":\"tool_name\",\"arguments\":{\"param\":\"value\"}}>>")
        appendLine()
        appendLine("Available tools:")

        specs.forEach { spec ->
            appendLine("• ${spec.name}: ${spec.description}")
            extractParamDetails(spec.parametersSchemaJson).forEach { (name, desc) ->
                appendLine("  - $name: $desc")
            }
        }

        appendLine()
        appendLine("Examples:")
        appendLine("User: What time is it?")
        appendLine("Assistant: <<{\"tool\":\"get_current_time\",\"arguments\":{}}>>")
        appendLine()
        appendLine("User: Open google.com")
        appendLine("Assistant: <<{\"tool\":\"open_url\",\"arguments\":{\"url\":\"https://google.com\"}}>>")
        appendLine()
        appendLine("IMPORTANT:")
        appendLine("- If you need a tool, output ONLY the tool call in << >> format")
        appendLine("- If you don't need a tool, respond naturally")
    }
}

private fun extractParamDetails(schemaJson: String): Map<String, String> {
    return try {
        val schema = json.parseToJsonElement(schemaJson).jsonObject
        val properties = schema["properties"]?.jsonObject ?: return emptyMap()
        properties.mapNotNull { (name, obj) ->
            val desc = obj.jsonObject["description"]?.jsonPrimitive?.content
            if (desc != null) name to desc else null
        }.toMap()
    } catch (_: Exception) {
        emptyMap()
    }
}

fun extractToolCall(text: String): ToolCall? {
    extractGemmaFunctionCall(text)?.let { return it }

    val start = text.indexOf(TOOL_CALL_START)
    val end = text.indexOf(TOOL_CALL_END, start + TOOL_CALL_START.length)

    if (start == -1 || end == -1 || end <= start) return null

    val payload = text.substring(start + TOOL_CALL_START.length, end).trim()
    if (!payload.startsWith("{")) return null

    return try {
        val obj = json.parseToJsonElement(payload).jsonObject
        val toolName = obj["tool"]?.jsonPrimitive?.content ?: return null
        val args = obj["arguments"]?.jsonObject ?: JsonObject(emptyMap())
        ToolCall(tool = toolName, arguments = args)
    } catch (_: Exception) {
        null
    }
}

private fun extractGemmaFunctionCall(text: String): ToolCall? {
    val startTag = "<start_function_call>"
    val startIdx = text.indexOf(startTag)
    if (startIdx == -1) return null

    val endTag = "<end_function_call>"
    val endIdx = text.indexOf(endTag, startIdx)
    val content = if (endIdx != -1) {
        text.substring(startIdx + startTag.length, endIdx).trim()
    } else {
        text.substring(startIdx + startTag.length).substringBefore('\n').trim()
    }

    // Try JSON format first
    if (content.startsWith("{")) {
        try {
            val obj = json.parseToJsonElement(content).jsonObject
            val toolName = obj["name"]?.jsonPrimitive?.content
                ?: obj["tool"]?.jsonPrimitive?.content ?: return null
            val args = obj["arguments"]?.jsonObject ?: JsonObject(emptyMap())
            return ToolCall(tool = toolName, arguments = args)
        } catch (_: Exception) { }
    }

    // Parse attribute format: call="tool_name" arg1="value1"
    val callMatch = """call="([^"]+)"""".toRegex().find(content) ?: return null
    val toolName = callMatch.groupValues[1]

    val args = mutableMapOf<String, JsonPrimitive>()
    """(\w+)="([^"]*)"""".toRegex().findAll(content).forEach { match ->
        val argName = match.groupValues[1]
        val argValue = match.groupValues[2]
        if (argName != "call") {
            args[argName] = JsonPrimitive(argValue)
        }
    }

    return ToolCall(tool = toolName, arguments = JsonObject(args))
}

fun stripToolCallBlock(text: String): String {
    var result = text

    // Remove Gemma format
    result = result.replace("""<start_function_call>[\s\S]*?(<end_function_call>|$)""".toRegex(), "")

    // Remove << >> format
    val start = result.indexOf(TOOL_CALL_START)
    val end = result.indexOf(TOOL_CALL_END, maxOf(0, start))
    if (start != -1 && end != -1 && end > start) {
        result = result.substring(0, start) + result.substring(end + TOOL_CALL_END.length)
    }

    return result.trim()
}
