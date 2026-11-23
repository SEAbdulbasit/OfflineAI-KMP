package org.abma.offlinelai_kmp.inference

import org.abma.offlinelai_kmp.domain.model.ChatMessage
import org.abma.offlinelai_kmp.domain.model.PersonaPreset
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PromptBuilderTest {

    @Test
    fun personaPromptForResolvesBuiltInAndCustomPresets() {
        assertEquals(DEFAULT_SYSTEM_PROMPT, PromptBuilder.personaPromptFor(PersonaPreset.DEFAULT))
        assertEquals(CODING_ASSISTANT_PROMPT, PromptBuilder.personaPromptFor(PersonaPreset.CODE_ARCHITECT))
        assertEquals(CREATIVE_WRITER_PROMPT, PromptBuilder.personaPromptFor(PersonaPreset.CREATIVE_SYNTH))
        assertEquals(CONCISE_ASSISTANT_PROMPT, PromptBuilder.personaPromptFor(PersonaPreset.TACTICAL_BRIEF))

        val customDirective = "Always reply in JSON format."
        assertEquals(customDirective, PromptBuilder.personaPromptFor(PersonaPreset.CUSTOM, customDirective))
        // Fallback when custom prompt is blank
        assertEquals(DEFAULT_SYSTEM_PROMPT, PromptBuilder.personaPromptFor(PersonaPreset.CUSTOM, "   "))
    }

    @Test
    fun buildCombinedSystemPromptIncludesPersonaPinnedMemoriesAndTools() {
        val pinned = listOf(
            ChatMessage(id = "1", content = "My name is Alex.", isFromUser = true, isPinned = true),
            ChatMessage(id = "2", content = "Project uses Kotlin 2.1.", isFromUser = false, isPinned = true)
        )
        val combined = PromptBuilder.buildCombinedSystemPrompt(
            preset = PersonaPreset.CODE_ARCHITECT,
            customPrompt = "",
            toolInstructions = "Use <<{\"tool\":\"get_current_time\"}>>",
            pinnedMessages = pinned
        )

        assertContains(combined, "expert Kotlin and Android developer assistant")
        assertContains(combined, "Pinned Context Memories (keep these facts in mind):")
        assertContains(combined, "- [User]: My name is Alex.")
        assertContains(combined, "- [Assistant]: Project uses Kotlin 2.1.")
        assertContains(combined, "Use <<{\"tool\":\"get_current_time\"}>>")
    }

    @Test
    fun buildPromptFormatsGemmaTurnsAndOptionalSystemTurn() {
        val history = listOf(
            ChatMessage.user("Hi there"),
            ChatMessage.ai("Hello! How can I help?")
        )

        val withSystem = PromptBuilder.buildPrompt(
            systemPrompt = "Be concise.",
            messages = history,
            currentMessage = "Explain KMP.",
            includeSystemTurn = true
        )

        assertContains(withSystem, "<start_of_turn>user\nSystem Instructions: Be concise.<end_of_turn>")
        assertContains(withSystem, "<start_of_turn>model\nI understand. I'll follow these instructions.<end_of_turn>")
        assertContains(withSystem, "<start_of_turn>user\nHi there<end_of_turn>")
        assertContains(withSystem, "<start_of_turn>model\nHello! How can I help?<end_of_turn>")
        assertContains(withSystem, "<start_of_turn>user\nExplain KMP.<end_of_turn>\n<start_of_turn>model\n")

        val withoutSystem = PromptBuilder.buildPrompt(
            systemPrompt = "Be concise.",
            messages = history,
            currentMessage = "Explain KMP.",
            includeSystemTurn = false
        )
        assertFalse(withoutSystem.contains("System Instructions:"))
        assertContains(withoutSystem, "<start_of_turn>user\nHi there<end_of_turn>")
    }

    @Test
    fun estimateTokensAndContextUsageCalculateExpectedBounds() {
        assertEquals(1, PromptBuilder.estimateTokens(""))
        assertEquals(2, PromptBuilder.estimateTokens("abcd"))
        assertEquals(3, PromptBuilder.estimateTokens("abcdefgh"))

        val usage = PromptBuilder.getContextUsage(
            systemPrompt = "System prompt",
            messages = listOf(ChatMessage.user("Hello"), ChatMessage.ai("World")),
            currentMessage = "Next question"
        )
        assertTrue(usage > 0f && usage < 1f)
        assertTrue(PromptBuilder.wouldExceedContext(7400, 200, maxTokens = 7500))
        assertFalse(PromptBuilder.wouldExceedContext(7000, 200, maxTokens = 7500))
    }

    @Test
    fun contextTrimmerKeepRecentDropsOldestMessagesWhenExceedingBudget() {
        // Each 40-char message = (40/4)+1 = 11 tokens + 20 turn tokens = 31 tokens
        val messages = (1..6).map { index ->
            ChatMessage(
                id = "msg-$index",
                content = "Message-$index-" + "x".repeat(30),
                isFromUser = index % 2 == 1
            )
        }

        // Budget of 100 tokens allows 3 messages (31 * 3 = 93 < 100, 4th would be 124)
        val trimmed = ContextTrimmer.trim(
            messages = messages,
            maxTokens = 100,
            strategy = TrimmingStrategy.KEEP_RECENT
        )

        assertEquals(listOf("msg-4", "msg-5", "msg-6"), trimmed.map { it.id })
    }

    @Test
    fun contextTrimmerKeepFirstAndRecentPreservesInitialExchangePlusNewest() {
        val messages = (1..6).map { index ->
            ChatMessage(
                id = "msg-$index",
                content = "Message-$index-" + "x".repeat(30),
                isFromUser = index % 2 == 1
            )
        }

        // First 2 messages take 62 tokens; budget of 130 leaves 68 tokens -> fits 2 most recent messages (62 < 68)
        val trimmed = ContextTrimmer.trim(
            messages = messages,
            maxTokens = 130,
            strategy = TrimmingStrategy.KEEP_FIRST_AND_RECENT
        )

        assertEquals(listOf("msg-1", "msg-2", "msg-5", "msg-6"), trimmed.map { it.id })
    }
}
