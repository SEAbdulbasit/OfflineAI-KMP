package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private const val TOOL_CALL_START = "<<"
private const val TOOL_CALL_END = ">>"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}


fun buildSystemPrompt(specs: List<ToolSpec>): String = buildString {
    if (specs.isEmpty()) return ""

    appendLine("You are a helpful AI assistant with access to tools.")
    appendLine("When you need to use a tool, output it in this EXACT format:")
    appendLine("<<{\"tool\":\"tool_name\",\"arguments\":{\"param\":\"value\"}}>>")
    appendLine()
    appendLine("Available tools:")

    specs.forEach { spec ->
        appendLine("• ${spec.name}: ${spec.description}")

        val params = extractParamDetails(spec.parametersSchemaJson)
        if (params.isNotEmpty()) {
            params.forEach { (paramName, paramDesc) ->
                appendLine("  - $paramName: $paramDesc")
            }
        }
    }

    appendLine()
    appendLine("Examples:")
    appendLine("User: What's the weather in Paris?")
    appendLine("Assistant: <<{\"tool\":\"get_weather\",\"arguments\":{\"location\":\"Paris\"}}>>")
    appendLine()
    appendLine("User: What time is it?")
    appendLine("Assistant: <<{\"tool\":\"get_current_time\",\"arguments\":{}}}>>")
    appendLine()
    appendLine("User: Open google.com")
    appendLine("Assistant: <<{\"tool\":\"open_url\",\"arguments\":{\"url\":\"google.com\"}}>>")
    appendLine()
    appendLine("IMPORTANT:")
    appendLine("- If you need a tool, output ONLY the tool call in << >> format")
    appendLine("- If you don't need a tool, respond naturally")
    appendLine("- Always extract parameters from the user's message")
    appendLine("- Use proper JSON format inside << >>")
}

private fun extractParamDetails(schemaJson: String): Map<String, String> {
    return try {
        val json = Json { ignoreUnknownKeys = true }
        val schema = json.parseToJsonElement(schemaJson).jsonObject
        val properties = schema["properties"]?.jsonObject ?: return emptyMap()

        properties.mapNotNull { (paramName, paramObj) ->
            val desc = paramObj.jsonObject["description"]?.toString()?.trim('"')
            if (desc != null) paramName to desc else null
        }.toMap()
    } catch (e: Exception) {
        extractParamNames(schemaJson).associateWith { "Parameter" }
    }
}

private fun extractParamNames(schemaJson: String): List<String> {
    return try {
        val regex = """"(\w+)":\s*\{""".toRegex()
        val matches = regex.findAll(schemaJson)
        matches.mapNotNull { match ->
            val name = match.groupValues.getOrNull(1)
            if (name in listOf("type", "properties", "required", "description", "object")) null
            else name
        }.toList()
    } catch (e: Exception) {
        emptyList()
    }
}

fun extractToolCall(text: String): ToolCall? {
    val start = text.indexOf(TOOL_CALL_START)
    val end = text.indexOf(TOOL_CALL_END, start + TOOL_CALL_START.length)

    val jsonPayload = if (start != -1 && end != -1 && end > start) {
        text.substring(start + TOOL_CALL_START.length, end).trim()
    } else {
        val jsonMatch = text.let { input ->
            val codeBlockPattern = "```json\\s*\\n([\\s\\S]*?)\\n```".toRegex()
            val match = codeBlockPattern.find(input)
            if (match != null) {
                match.groupValues[1].trim()
            } else {
                val jsonObjectPattern = "\\{[\\s\\S]*?\"tool\"[\\s\\S]*?\\}".toRegex()
                jsonObjectPattern.find(input)?.value?.trim()
            }
        }
        jsonMatch ?: return null
    }

    if (jsonPayload.isBlank() || !jsonPayload.startsWith("{")) return null

    return try {
        val element = json.parseToJsonElement(jsonPayload)
        val obj = element.jsonObject

        val toolName = obj["tool"]?.jsonPrimitive?.content ?: return null
        val args = obj["arguments"]?.jsonObject ?: JsonObject(emptyMap())

        ToolCall(tool = toolName, arguments = args)
    } catch (e: Exception) {
        println("❌ Failed to parse tool call: ${e.message}")
        println("   JSON payload was: $jsonPayload")
        null
    }
}

fun stripToolCallBlock(text: String): String {
    val start = text.indexOf(TOOL_CALL_START)
    val end = text.indexOf(TOOL_CALL_END, start + TOOL_CALL_START.length)
    if (start != -1 && end != -1 && end > start) {
        return (text.substring(0, start) + text.substring(end + TOOL_CALL_END.length)).trim()
    }

    val codeBlockPattern = "```json\\s*\\n[\\s\\S]*?\\n```".toRegex()
    var result = text.replace(codeBlockPattern, "")

    val jsonObjectPattern = "\\{[\\s\\S]*?\"tool\"[\\s\\S]*?\\}".toRegex()
    result = result.replace(jsonObjectPattern, "")

    return result.trim()
}

fun buildToolResultPrompt(toolCall: ToolCall, toolResult: ToolResult): String {
    return "Result: ${toolResult.result}"
}

fun formatPromptWithHistoryAndToolResult(
    messages: List<Pair<String, Boolean>>,
    toolCall: ToolCall,
    toolResultPrompt: String
): String = buildString {
    messages.takeLast(1).forEach { (content, isFromUser) ->
        if (isFromUser) {
            append("<start_of_turn>user\n$content<end_of_turn>\n")
        } else {
            append("<start_of_turn>model\n$content<end_of_turn>\n")
        }
    }
    append("<start_of_turn>user\n$toolResultPrompt<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
