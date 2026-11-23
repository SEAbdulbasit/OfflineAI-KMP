package org.abma.offlinelai_kmp.demo

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import kotlin.test.*

class DemoControllerTest {
    @Test fun successfulRunPublishesValidatedOutputAndReleasesEngine() = runBlocking<Unit> {
        var released = false
        val controller = DemoController(this, { true }, { released = true }) { flowOf("", "{\"items\":[]}") }
        controller.run()
        withTimeout(3000) { controller.state.first { !it.running } }
        assertTrue(released)
        assertEquals(emptyList(), controller.state.value.items)
        assertNotNull(controller.state.value.timing?.firstChunkMs)
    }
    @Test fun stopWaitsForCleanupAndReleasesEngine() = runBlocking<Unit> {
        val started = CompletableDeferred<Unit>()
        var released = false
        var cleaned = false
        val controller = DemoController(this, { true }, { released = true }) {
            flow { try { started.complete(Unit); awaitCancellation() } finally { cleaned = true } }
        }
        controller.run()
        withTimeout(3000) { started.await() }
        controller.cancel()
        withTimeout(3000) { controller.state.first { !it.running } }
        assertTrue(cleaned)
        assertTrue(released)
        assertNull(controller.state.value.items)
        assertNotNull(controller.state.value.error)
    }
    @Test fun cancellationBeforeBodyStartsStillReleasesEngine() = runBlocking<Unit> {
        var released = false
        val cancelledScope = CoroutineScope(Job().apply { cancel() } + Dispatchers.Default)
        val controller = DemoController(cancelledScope, { true }, { released = true }) { error("Must not generate") }
        controller.run()
        withTimeout(3000) { controller.state.first { !it.running } }
        assertTrue(released)
    }
    @Test fun unavailableEngineDoesNotGenerate() {
        val scope = CoroutineScope(Job())
        val controller = DemoController(scope, { false }, { error("Not acquired") }) { error("Must not generate") }
        controller.run()
        assertFalse(controller.state.value.running)
        assertNotNull(controller.state.value.error)
        scope.cancel()
    }
}
