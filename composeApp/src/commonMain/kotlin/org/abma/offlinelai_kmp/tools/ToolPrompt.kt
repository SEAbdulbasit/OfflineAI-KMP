package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject

private const val TOOL_CALL_START = "<<tool_call>>"
private const val TOOL_CALL_END = "<</tool_call>>"

private val json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
}

fun buildToolInstruction(specs: List<ToolSpec>): String {
    if (specs.isEmpty()) return ""

    val toolList = specs.joinToString("\n") { spec ->
        "- name: ${spec.name}\n  description: ${spec.description}\n  parameters: ${spec.parametersSchemaJson}"
    }

    return buildString {
        append("You have access to tools that you can use to help answer questions.\n")
        append("When you need to use a tool, respond ONLY with a JSON block between special tags.\n")
        append("Do not include any other text when calling a tool.\n\n")
        append("Format for tool calls:\n")
        append("$TOOL_CALL_START\n")
        append("{\"tool\":\"tool_name\",\"arguments\":{\"param\":\"value\"}}\n")
        append("$TOOL_CALL_END\n\n")
        append("Example - to get weather for a city:\n")
        append("$TOOL_CALL_START\n")
        append("{\"tool\":\"get_weather\",\"arguments\":{\"location\":\"Tokyo\"}}\n")
        append("$TOOL_CALL_END\n\n")
        append("Available tools:\n")
        append(toolList)
        append("\n\nAfter receiving tool results, provide a helpful response to the user.")
    }
}

fun buildToolAwarePrompt(userPrompt: String, specs: List<ToolSpec>): String {
    val instruction = buildToolInstruction(specs)
    return if (instruction.isNotBlank()) {
        "$instruction\n\nUser question: $userPrompt"
    } else {
        userPrompt
    }
}

fun extractToolCall(text: String): ToolCall? {
    val start = text.indexOf(TOOL_CALL_START)
    val end = text.indexOf(TOOL_CALL_END)
    if (start == -1 || end == -1 || end <= start) return null

    val jsonPayload = text.substring(start + TOOL_CALL_START.length, end).trim()
    if (jsonPayload.isBlank()) return null

    return try {
        val element = json.parseToJsonElement(jsonPayload)
        val obj = element.jsonObject
        val toolName = obj["tool"]?.toString()?.trim('"') ?: return null
        val args = obj["arguments"] as? JsonObject ?: JsonObject(emptyMap())
        ToolCall(tool = toolName, arguments = args)
    } catch (e: Exception) {
        println("Failed to parse tool call: ${e.message}")
        null
    }
}

fun stripToolCallBlock(text: String): String {
    val start = text.indexOf(TOOL_CALL_START)
    val end = text.indexOf(TOOL_CALL_END)
    if (start == -1 || end == -1 || end <= start) return text
    return (text.substring(0, start) + text.substring(end + TOOL_CALL_END.length)).trim()
}

fun buildToolResultPrompt(toolCall: ToolCall, toolResult: ToolResult): String {
    return "Tool result for ${toolCall.tool}: ${toolResult.result}\n\nNow provide a helpful response based on this information."
}

fun formatPromptWithHistoryAndToolResult(
    messages: List<Pair<String, Boolean>>,
    toolCall: ToolCall,
    toolResultPrompt: String
): String = buildString {
    messages.forEach { (content, isFromUser) ->
        if (isFromUser) {
            append("<start_of_turn>user\n$content<end_of_turn>\n")
        } else {
            append("<start_of_turn>model\n$content<end_of_turn>\n")
        }
    }
    append("<start_of_turn>model\n")
    append("$TOOL_CALL_START\n")
    append("{\"tool\":\"${toolCall.tool}\",\"arguments\":${toolCall.arguments}}\n")
    append("$TOOL_CALL_END\n")
    append("<end_of_turn>\n")
    append("<start_of_turn>user\n$toolResultPrompt<end_of_turn>\n")
    append("<start_of_turn>model\n")
}
