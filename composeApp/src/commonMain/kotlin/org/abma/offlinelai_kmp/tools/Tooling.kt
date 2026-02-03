package org.abma.offlinelai_kmp.tools

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.contentOrNull
import org.abma.offlinelai_kmp.domain.repository.LoadedModel
import kotlin.random.Random
import kotlin.time.Clock

/**
 * Defines a tool available to the model.
 */
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

/*
"What's the weather in New York?"
"What time is it?"
"What device am I using?"
"What models do I have loaded?"
*/

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

class GetDeviceInfoTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "get_device_info",
        description = "Get platform, OS version, and device model.",
        parametersSchemaJson = "{\"type\":\"object\",\"properties\":{},\"required\":[]}"
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val result = buildJsonObject {
            put("platform", JsonPrimitive(PlatformInfoProvider.getPlatformName()))
            put("osVersion", JsonPrimitive(PlatformInfoProvider.getOsVersion()))
            put("deviceModel", JsonPrimitive(PlatformInfoProvider.getDeviceModel()))
        }.toString()
        return ToolResult(spec.name, result)
    }
}

class ListLoadedModelsTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "list_loaded_models",
        description = "List locally loaded models and the currently selected model.",
        parametersSchemaJson = "{\"type\":\"object\",\"properties\":{},\"required\":[]}"
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val models = context.loadedModels.joinToString("\n") { model ->
            val isCurrent = model.path == context.currentModelPath
            val marker = if (isCurrent) "(current)" else ""
            "- ${model.name} $marker"
        }.ifEmpty { "No models loaded." }
        return ToolResult(spec.name, models)
    }
}

/**
 * Weather tool that simulates getting weather for a location.
 * In a real app, this would call a weather API.
 */
class GetWeatherTool : ToolHandler {
    override val spec: ToolSpec = ToolSpec(
        name = "get_weather",
        description = "Get the current weather for a specified city or location. Returns temperature, conditions, humidity, and wind speed.",
        parametersSchemaJson = """{"type":"object","properties":{"location":{"type":"string","description":"The city name or location (e.g., 'New York', 'London', 'Tokyo')"}},"required":["location"]}"""
    )

    override suspend fun execute(arguments: JsonObject, context: ToolContext): ToolResult {
        val location = arguments["location"]?.jsonPrimitive?.contentOrNull
            ?: return ToolResult(spec.name, "Error: location parameter is required")

        // Simulate weather data - in a real app, call a weather API
        val weatherData = generateMockWeather(location)
        return ToolResult(spec.name, weatherData)
    }

    private fun generateMockWeather(location: String): String {
        // Generate realistic mock weather based on location hash for consistency
        val seed = location.lowercase().hashCode()
        val random = Random(seed)

        val conditions = listOf(
            "Sunny", "Partly Cloudy", "Cloudy", "Light Rain",
            "Heavy Rain", "Thunderstorm", "Snow", "Foggy", "Windy", "Clear"
        )
        val condition = conditions[random.nextInt(conditions.size)]

        // Temperature varies by condition
        val baseTemp = when (condition) {
            "Snow" -> random.nextInt(-5, 5)
            "Heavy Rain", "Thunderstorm" -> random.nextInt(10, 20)
            "Sunny", "Clear" -> random.nextInt(20, 35)
            else -> random.nextInt(10, 28)
        }

        val humidity = when (condition) {
            "Sunny", "Clear" -> random.nextInt(30, 50)
            "Heavy Rain", "Thunderstorm", "Snow" -> random.nextInt(70, 95)
            else -> random.nextInt(45, 75)
        }

        val windSpeed = when (condition) {
            "Windy", "Thunderstorm" -> random.nextInt(25, 50)
            "Heavy Rain" -> random.nextInt(15, 30)
            else -> random.nextInt(5, 20)
        }

        return buildJsonObject {
            put("location", JsonPrimitive(location.replaceFirstChar { it.uppercaseChar() }))
            put("temperature_celsius", JsonPrimitive(baseTemp))
            put("temperature_fahrenheit", JsonPrimitive((baseTemp * 9 / 5) + 32))
            put("condition", JsonPrimitive(condition))
            put("humidity_percent", JsonPrimitive(humidity))
            put("wind_speed_kmh", JsonPrimitive(windSpeed))
            put("description", JsonPrimitive("Currently $condition in ${location.replaceFirstChar { it.uppercaseChar() }} with a temperature of ${baseTemp}°C (${(baseTemp * 9 / 5) + 32}°F). Humidity is $humidity% with winds at $windSpeed km/h."))
        }.toString()
    }
}

/**
 * Creates the default tool registry with all available tools.
 */
fun createDefaultToolRegistry(): ToolRegistry {
    return ToolRegistry(
        listOf(
            GetCurrentTimeTool(),
            GetDeviceInfoTool(),
            ListLoadedModelsTool(),
            GetWeatherTool()
        )
    )
}
