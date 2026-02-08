package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.*

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
    if (start != -1) {
        val end = text.indexOf(TOOL_CALL_END, start + TOOL_CALL_START.length)
        val payload = if (end != -1) {
            text.substring(start + TOOL_CALL_START.length, end).trim()
        } else {
            val afterStart = text.substring(start + TOOL_CALL_START.length)
            val jsonStart = afterStart.indexOf('{')
            if (jsonStart != -1) {
                extractJsonObject(afterStart.substring(jsonStart))
            } else null
        }

        payload?.let { p ->
            parseToolCallJson(p)?.let { return it }
        }
    }

    val toolJsonPattern = """\{\s*"tool"\s*:\s*"([^"]+)"\s*,\s*"arguments"\s*:\s*(\{[^}]*\})?\s*\}?""".toRegex()
    toolJsonPattern.find(text)?.let { match ->
        val fullMatch = match.value
        val jsonToTry = if (fullMatch.count { it == '{' } > fullMatch.count { it == '}' }) {
            fullMatch + "}"
        } else {
            fullMatch
        }
        parseToolCallJson(jsonToTry)?.let { return it }
    }

    return null
}

private fun extractJsonObject(text: String): String? {
    var braceCount = 0
    var started = false
    val sb = StringBuilder()

    for (char in text) {
        if (char == '{') {
            braceCount++
            started = true
        }
        if (started) {
            sb.append(char)
        }
        if (char == '}') {
            braceCount--
            if (braceCount == 0 && started) {
                return sb.toString()
            }
        }
        // Stop if we hit a newline after starting (incomplete JSON)
        if (started && char == '\n' && braceCount > 0) {
            return sb.toString() + "}" // Try to complete it
        }
    }
    return if (started) sb.toString() else null
}

private fun parseToolCallJson(payload: String): ToolCall? {
    if (payload.isBlank() || !payload.contains("tool")) return null

    return try {
        var cleaned = payload.trim()
        if (!cleaned.endsWith("}")) {
            cleaned = cleaned + "}"
        }
        if (cleaned.contains("\"arguments\":{}") || cleaned.contains("\"arguments\": {}")) {
        } else if (cleaned.contains("\"arguments\":{") && !cleaned.contains("\"arguments\":{}")) {
            val argsStart = cleaned.indexOf("\"arguments\":{")
            val afterArgs = cleaned.substring(argsStart)
            if (afterArgs.count { it == '{' } > afterArgs.count { it == '}' }) {
                cleaned = cleaned.trimEnd('}') + "}}"
            }
        }

        val obj = json.parseToJsonElement(cleaned).jsonObject
        val toolName = obj["tool"]?.jsonPrimitive?.content ?: return null
        val args = obj["arguments"]?.jsonObject ?: JsonObject(emptyMap())
        ToolCall(tool = toolName, arguments = args)
    } catch (e: Exception) {
        println("Failed to parse tool call JSON: ${e.message}")
        println("Payload was: $payload")
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
        } catch (_: Exception) {
        }
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
