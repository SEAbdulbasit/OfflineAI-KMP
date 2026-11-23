package org.abma.offlinelai_kmp.tools

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlin.test.*

class ToggleTorchToolTest {
    private val context = ToolContext(emptyList(), null)
    private fun args(json: String) = Json.parseToJsonElement(json).jsonObject

    @Test fun modelOnAndOffCallsReachPlatformWithExactBoolean() = runBlocking<Unit> {
        val received = mutableListOf<Boolean>()
        val registry = ToolRegistry(listOf(ToggleTorchTool { received += it; true }))
        for (enabled in listOf(true, false)) {
            val call = assertNotNull(extractToolCall("<<{\"tool\":\"toggle_torch\",\"arguments\":{\"enable\":$enabled}}>>"))
            val result = registry.execute(call, context)
            assertContains(result.result, if (enabled) "ON" else "OFF")
            assertContains(result.result, "accepted")
        }
        assertEquals(listOf(true, false), received)
    }

    @Test fun malformedArgumentsNeverReachHardware() = runBlocking<Unit> {
        var calls = 0
        val tool = ToggleTorchTool { calls++; true }
        listOf("{}", "{\"enable\":null}", "{\"enable\":\"true\"}", "{\"enable\":\"false\"}",
            "{\"enable\":1}", "{\"enable\":[]}", "{\"enable\":{}}", "{\"enable\":true,\"other\":false}").forEach {
            assertContains(tool.execute(args(it), context).result, "No action was taken")
        }
        assertEquals(0, calls)
    }

    @Test fun unavailableOrThrowingPlatformDoesNotReportSuccess() = runBlocking<Unit> {
        assertContains(ToggleTorchTool { false }.execute(args("{\"enable\":true}"), context).result, "Unable")
        assertContains(ToggleTorchTool { error("unavailable") }.execute(args("{\"enable\":false}"), context).result, "Unable")
    }

    @Test fun cancellationIsNotConvertedIntoHardwareFailure() = runBlocking<Unit> {
        assertFailsWith<CancellationException> {
            ToggleTorchTool { throw CancellationException("stop") }.execute(args("{\"enable\":true}"), context)
        }
    }

    @Test fun promptIncludesBothDirectionsOnlyWhenRegistered() {
        val prompt = buildSystemPrompt(listOf(ToggleTorchTool { true }.spec))
        assertContains(prompt, "Turn on flashlight")
        assertContains(prompt, "Turn off flashlight")
        assertFalse(prompt.contains("open_url"))
        assertFalse(buildSystemPrompt(listOf(GetCurrentTimeTool().spec)).contains("toggle_torch"))
    }
}
