package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import kotlin.time.Clock

data class ToolSpec(
    val name: String,
    val description: String,
    val parametersSchemaJson: String
)

data class ToolCall(
    val tool: String,
    val arguments: JsonObject
)

data class ToolResult(
    val tool: String,
    val result: String
)

data class ToolContext(
    val loadedModels: List<LoadedModel>,
    val currentModelPath: String?
)

interface ToolHandler {
    val spec: ToolSpec
    suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult
}


class ToolRegistry(private val handlers: List<ToolHandler>) {
    fun specs(): List<ToolSpec> = handlers.map { it.spec }

    suspend fun execute(call: ToolCall, context: ToolContext): ToolResult {
        val handler = handlers.firstOrNull { it.spec.name == call.tool }
            ?: return ToolResult(call.tool, "Unknown tool: ${call.tool}")
        return handler.execute(call.arguments, context)
    }
}

class GetCurrentTimeTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "get_current_time",
        description = "Get the current device time in ISO-8601 format.",
        parametersSchemaJson = "{\"type\":\"object\",\"properties\":{},\"required\":[]}"
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val now = Clock.System.now().toString()
        return ToolResult(spec.name, now)
    }
}

class OpenUrlTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "open_url",
        description = "Open a URL in the device's web browser. Use this when the user wants to visit a website.",
        parametersSchemaJson = """{"type":"object","properties":{"url":{"type":"string","description":"The URL to open (e.g., 'google.com', 'https://example.com')"}},"required":["url"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val url = arguments["url"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: url parameter is required")

        val success = AppActionsProvider.openUrl(url)
        return ToolResult(
            spec.name,
            if (success) "The URL $url has been opened in the device browser."
            else "Unable to open $url. The URL might be invalid or the browser might not be available."
        )
    }
}

class OpenDialerTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "open_dialer",
        description = "Open the phone dialer with a phone number. Use this when the user wants to call someone.",
        parametersSchemaJson = """{"type":"object","properties":{"phone_number":{"type":"string","description":"The phone number to dial"}},"required":["phone_number"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val phoneNumber = arguments["phone_number"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: phone_number parameter is required")

        val success = AppActionsProvider.openDialer(phoneNumber)
        return ToolResult(spec.name, if (success) "Opened dialer with number $phoneNumber" else "Failed to open dialer")
    }
}

class OpenEmailTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "open_email",
        description = "Open email composer to send an email. Use this when the user wants to send an email.",
        parametersSchemaJson = """{"type":"object","properties":{"to":{"type":"string","description":"Recipient email address"},"subject":{"type":"string","description":"Email subject (optional)"},"body":{"type":"string","description":"Email body text (optional)"}},"required":["to"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val to = arguments["to"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: 'to' parameter is required")
        val subject = arguments["subject"]?.jsonPrimitive?.contentOrNull ?: ""
        val body = arguments["body"]?.jsonPrimitive?.contentOrNull ?: ""

        val success = AppActionsProvider.openEmail(to, subject, body)
        return ToolResult(spec.name, if (success) "Opened email composer for $to" else "Failed to open email")
    }
}

class OpenMapsTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "open_maps",
        description = "Open maps application to search for a location or place. Use this when the user wants directions or to find a place.",
        parametersSchemaJson = """{"type":"object","properties":{"query":{"type":"string","description":"The location or place to search for (e.g., 'coffee shops near me', 'Times Square New York')"}},"required":["query"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val query = arguments["query"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: query parameter is required")

        val success = AppActionsProvider.openMaps(query)
        return ToolResult(spec.name, if (success) "Opened maps searching for: $query" else "Failed to open maps")
    }
}

class ShareTextTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "share_text",
        description = "Share text content using the system share sheet. Use this when the user wants to share something.",
        parametersSchemaJson = """{"type":"object","properties":{"text":{"type":"string","description":"The text content to share"},"title":{"type":"string","description":"Optional title for the share dialog"}},"required":["text"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val text = arguments["text"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: text parameter is required")
        val title = arguments["title"]?.jsonPrimitive?.contentOrNull ?: ""

        val success = AppActionsProvider.shareText(text, title)
        return ToolResult(spec.name, if (success) "Opened share dialog" else "Failed to share")
    }
}

class CopyToClipboardTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "copy_to_clipboard",
        description = "Copy text to the device clipboard. Use this when the user wants to copy something.",
        parametersSchemaJson = """{"type":"object","properties":{"text":{"type":"string","description":"The text to copy to clipboard"}},"required":["text"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val text = arguments["text"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: text parameter is required")

        val success = AppActionsProvider.copyToClipboard(text)
        return ToolResult(spec.name, if (success) "Text copied to clipboard" else "Failed to copy to clipboard")
    }
}

class OpenAppSettingsTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "open_app_settings",
        description = "Open this app's settings page in system settings. Use this when the user wants to change app permissions.",
        parametersSchemaJson = """{"type":"object","properties":{},"required":[]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val success = AppActionsProvider.openAppSettings()
        return ToolResult(spec.name, if (success) "Opened app settings" else "Failed to open settings")
    }
}

fun createDefaultToolRegistry(): ToolRegistry {
    return ToolRegistry(
        listOf(
            GetCurrentTimeTool(),
            OpenUrlTool(),
            OpenDialerTool(),
            OpenEmailTool(),
            OpenMapsTool(),
            ShareTextTool(),
            CopyToClipboardTool(),
            OpenAppSettingsTool()
        )
    )
}
