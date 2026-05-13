package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import kotlin.time.Clock

data class ToolSpec(val name: String, val description: String, val parametersSchemaJson: String)
data class ToolCall(val tool: String, val arguments: JsonObject)
data class ToolResult(val tool: String, val result: String)
data class ToolContext(val loadedModels: List<LoadedModel>, val currentModelPath: String?)

interface ToolHandler {
    val spec: ToolSpec
    suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult
}

class ToolRegistry(private val handlers: List<ToolHandler>) {
    fun specs(): List<ToolSpec> = handlers.map { it.spec }
    suspend fun execute(call: ToolCall, context: ToolContext): ToolResult {
        val handler = handlers.firstOrNull { it.spec.name == call.tool } ?: return ToolResult(call.tool, "Unknown tool")
        return handler.execute(call.arguments, context)
    }
}

class GetCurrentTimeTool : ToolHandler {
    override val spec = ToolSpec("get_current_time", "Get current time", "{}")
    override suspend fun execute(arguments: JsonObject, context: ToolContext) = ToolResult(spec.name, Clock.System.now().toString())
}

class OpenUrlTool : ToolHandler {
    override val spec = ToolSpec("open_url", "Open a URL", "{\"url\": \"string\"}")
    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val url = arguments["url"]?.jsonPrimitive?.contentOrNull ?: return ToolResult(spec.name, "Error: url required")
        AppActionsProvider.openUrl(url)
        return ToolResult(spec.name, "Opened $url")
    }
}

fun createDefaultToolRegistry() = ToolRegistry(listOf(GetCurrentTimeTool(), OpenUrlTool()))
