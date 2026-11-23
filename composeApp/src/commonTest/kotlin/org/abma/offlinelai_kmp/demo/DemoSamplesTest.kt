package org.abma.offlinelai_kmp.demo

import kotlin.test.*

class DemoSamplesTest {
    @Test fun missingDeadlineStaysNull() {
        val items = DemoSamples.parseNotes("""{"items":[{"task":"update the slides","owner":"Sara","deadline":null,"evidence":"Sara will update the slides."}]}""", DemoSamples.notes)
        assertEquals("Sara", items.single().owner)
        assertNull(items.single().deadline)
    }
    @Test fun inventedDeadlineIsRejected() {
        assertFails { DemoSamples.parseNotes("""{"items":[{"task":"update the slides","owner":"Sara","deadline":"Monday","evidence":"Sara will update the slides."}]}""", DemoSamples.notes) }
    }
    @Test fun deadlineFromAnotherPersonsCommitmentIsRejected() {
        // Friday exists in the notes, but not in Sara's evidence sentence.
        assertFails { DemoSamples.parseNotes("""{"items":[{"task":"update the slides","owner":"Sara","deadline":"Friday","evidence":"Sara will update the slides."}]}""", DemoSamples.notes) }
    }
    @Test fun literalEvidenceValidationDoesNotEstablishACommitment() {
        // This documents a limitation: exact source matching does not understand negation.
        // The prompt asks the model to omit discussions; the parser cannot enforce that.
        val items = DemoSamples.parseNotes("""{"items":[{"task":"adding a dark theme","owner":null,"deadline":null,"evidence":"We discussed adding a dark theme, but made no commitment."}]}""", DemoSamples.notes)
        assertEquals("adding a dark theme", items.single().task)
        assertEquals("We discussed adding a dark theme, but made no commitment.", items.single().evidence)
    }
    @Test fun fabricatedEvidenceIsRejected() {
        assertFails { DemoSamples.parseNotes("""{"items":[{"task":"buy tickets","owner":"Sara","deadline":null,"evidence":"Sara will buy tickets."}]}""", DemoSamples.notes) }
    }
    @Test fun malformedOutputIsNotRepairedOrExecuted() {
        assertFails { DemoSamples.parseNotes("""{"items":[""", DemoSamples.notes) }
        assertFails { DemoSamples.parseNotes("""{"tool":"toggle_torch","arguments":{"enable":true}}""", DemoSamples.notes) }
    }
    @Test fun emptyCommitmentsAreValid() { assertTrue(DemoSamples.parseNotes("""{"items":[]}""", "No decisions today.").isEmpty()) }
    @Test fun citedQuoteMustExistInThatSection() {
        assertEquals("venue", DemoSamples.parseFaq("""{"sourceId":"venue","quote":"The Offline AI workshop takes place in Room Cedar on level 2."}""").section?.id)
        assertFails { DemoSamples.parseFaq("""{"sourceId":"venue","quote":"The Wi-Fi password is gemma."}""") }
        assertFails { DemoSamples.parseFaq("""{"sourceId":"unknown","quote":"Room Cedar"}""") }
    }
    @Test fun unansweredQuestionHasNoCitation() {
        assertNull(DemoSamples.parseFaq("""{"sourceId":null,"quote":null}""").section)
        assertFails { DemoSamples.parseFaq("""{"sourceId":"venue","quote":null}""") }
    }
    @Test fun standardCodeFencesAreAccepted() {
        assertTrue(DemoSamples.parseNotes("```json\n{\"items\":[]}\n```", "No decisions.").isEmpty())
    }
}
