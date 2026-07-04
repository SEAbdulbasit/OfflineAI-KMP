package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.*

private const val TOOL_CALL_START = "<<"
private const val TOOL_CALL_END = ">>"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}

/**
 * Build a concise system prompt for tool calling.
 * Optimized for smaller models like Gemma 3n.
 */
fun buildSystemPrompt(specs: List<ToolSpec>): String {
    if (specs.isEmpty()) return ""

    return buildString {
        appendLine("You have access to these tools:")
        appendLine()

        specs.forEach { spec ->
            val params = extractParamDetails(spec.parametersSchemaJson)
            if (params.isEmpty()) {
                appendLine("- ${spec.name}: ${spec.description}")
            } else {
                appendLine("- ${spec.name}: ${spec.description}")
                val paramList = params.entries.joinToString(", ") { (name, desc) -> "$name ($desc)" }
                appendLine("  params: $paramList")
            }
        }

        appendLine()
        appendLine("To use a tool, reply with ONLY:")
        appendLine("<<{\"tool\":\"TOOL_NAME\",\"arguments\":{...}}>>")
        appendLine()
        appendLine("Examples:")
        appendLine("Q: What time is it?")
        appendLine("A: <<{\"tool\":\"get_current_time\",\"arguments\":{}}>>")
        appendLine()
        appendLine("Q: Open youtube")
        appendLine("A: <<{\"tool\":\"open_url\",\"arguments\":{\"url\":\"youtube.com\"}}>>")
        appendLine()
        appendLine("Q: Call 555-1234")
        appendLine("A: <<{\"tool\":\"open_dialer\",\"arguments\":{\"phone_number\":\"555-1234\"}}>>")
        appendLine()
        appendLine("Q: Email john@example.com about the meeting")
        appendLine("A: <<{\"tool\":\"open_email\",\"arguments\":{\"to\":\"john@example.com\",\"subject\":\"Meeting\",\"body\":\"\"}}>>")
        appendLine()
        appendLine("Q: Find coffee shops near me")
        appendLine("A: <<{\"tool\":\"open_maps\",\"arguments\":{\"query\":\"coffee shops near me\"}}>>")
        appendLine()
        appendLine("Q: Turn on flashlight")
        appendLine("A: <<{\"tool\":\"toggle_torch\",\"arguments\":{\"enable\":true}}>>")
        appendLine()
        appendLine("If no tool needed, answer normally.")
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
    println("🔍 Extracting tool call from: ${text.take(200)}${if (text.length > 200) "..." else ""}")

    // Try Gemma native function call format first
    extractGemmaFunctionCall(text)?.let {
        println("✅ Found Gemma function call: ${it.tool}")
        return it
    }

    // Try << >> format
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
            parseToolCallJson(p)?.let {
                println("✅ Found << >> format tool call: ${it.tool}")
                return it
            }
        }
    }

    // Try to find raw JSON with "tool" field anywhere in the text
    val toolJsonPatterns = listOf(
        """\{\s*"tool"\s*:\s*"([^"]+)"\s*,\s*"arguments"\s*:\s*(\{[^}]*\})?\s*\}""".toRegex(),
        """\{\s*"tool"\s*:\s*"([^"]+)"[^}]*\}""".toRegex(),
        """tool["\s:]+([a-z_]+)""".toRegex(RegexOption.IGNORE_CASE)
    )

    for (pattern in toolJsonPatterns) {
        pattern.find(text)?.let { match ->
            val fullMatch = match.value

            // If it looks like JSON, try to parse it
            if (fullMatch.contains("{")) {
                val jsonToTry = if (fullMatch.count { it == '{' } > fullMatch.count { it == '}' }) {
                    fullMatch + "}"
                } else {
                    fullMatch
                }
                parseToolCallJson(jsonToTry)?.let {
                    println("✅ Found JSON pattern tool call: ${it.tool}")
                    return it
                }
            } else {
                // Simple tool name match - try to create a basic tool call
                val toolName = match.groupValues.getOrNull(1) ?: continue
                if (toolName.isNotBlank() && toolName.contains("_")) {
                    println("✅ Found simple tool reference: $toolName")
                    return ToolCall(tool = toolName, arguments = JsonObject(emptyMap()))
                }
            }
        }
    }

    println("❌ No tool call detected in response")
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
        if (started && char == '\n' && braceCount > 0) {
            return sb.toString() + "}".repeat(braceCount)
        }
    }
    return if (started) sb.toString() else null
}

private fun parseToolCallJson(payload: String): ToolCall? {
    if (payload.isBlank() || !payload.contains("tool")) return null

    return try {
        var cleaned = payload.trim()
        // Balance any unclosed braces
        val openCount = cleaned.count { it == '{' }
        val closeCount = cleaned.count { it == '}' }
        if (openCount > closeCount) {
            cleaned += "}".repeat(openCount - closeCount)
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
