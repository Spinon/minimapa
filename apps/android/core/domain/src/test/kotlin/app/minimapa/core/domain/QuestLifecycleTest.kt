package app.minimapa.core.domain

import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestLifecycleTest {
    private val creator = PlayerId("player-requester")
    private val start = Instant.parse("2026-07-15T12:00:00Z")
    private val engine = QuestEngine { _, _ -> TransitionAuthorizationDecision.ALLOW }

    @Test
    fun `universal lifecycle reaches completion through valid states`() {
        var quest = QuestAggregate.from(draft())
        val targets = listOf(
            QuestState.PUBLISHED,
            QuestState.MATCHING,
            QuestState.ASSIGNED,
            QuestState.ACTIVE,
            QuestState.COMPLETED,
        )

        targets.forEachIndexed { index, target ->
            val result = engine.transition(quest, command(index, quest.version, target))
            assertTrue(result is TransitionResult.Applied)
            quest = (result as TransitionResult.Applied).aggregate
        }

        assertEquals(QuestState.COMPLETED, quest.state)
        assertEquals(5, quest.version)
        assertEquals((1L..5L).toList(), quest.events.map { it.sequence })
    }

    @Test
    fun `cannot skip universal states`() {
        val quest = QuestAggregate.from(draft())
        val result = engine.transition(quest, command(1, 0, QuestState.ACTIVE))

        assertEquals("INVALID_STATE_TRANSITION", (result as TransitionResult.Rejected).code)
    }

    @Test
    fun `optimistic version rejects a concurrent stale transition`() {
        val quest = QuestAggregate.from(draft())
        val first = engine.transition(quest, command(1, 0, QuestState.PUBLISHED)) as TransitionResult.Applied
        val stale = engine.transition(first.aggregate, command(2, 0, QuestState.CANCELLED))

        assertEquals("VERSION_CONFLICT", (stale as TransitionResult.Rejected).code)
    }

    @Test
    fun `same event and payload are idempotent`() {
        val quest = QuestAggregate.from(draft())
        val command = command(1, 0, QuestState.PUBLISHED)
        val applied = engine.transition(quest, command) as TransitionResult.Applied
        val duplicate = engine.transition(applied.aggregate, command)

        assertTrue(duplicate is TransitionResult.Duplicate)
        assertEquals(1, (duplicate as TransitionResult.Duplicate).aggregate.events.size)
    }

    @Test
    fun `idempotent replay succeeds even when policy changed after commit`() {
        var allow = true
        val changingEngine = QuestEngine { _, _ ->
            if (allow) TransitionAuthorizationDecision.ALLOW else TransitionAuthorizationDecision.DENY
        }
        val quest = QuestAggregate.from(draft())
        val command = command(1, 0, QuestState.PUBLISHED)
        val applied = changingEngine.transition(quest, command) as TransitionResult.Applied
        allow = false

        val replay = changingEngine.transition(applied.aggregate, command)

        assertTrue(replay is TransitionResult.Duplicate)
    }

    @Test
    fun `same event id with changed payload is rejected`() {
        val quest = QuestAggregate.from(draft())
        val applied = engine.transition(quest, command(1, 0, QuestState.PUBLISHED)) as TransitionResult.Applied
        val conflict = engine.transition(applied.aggregate, command(1, 1, QuestState.CANCELLED))

        assertEquals("EVENT_ID_CONFLICT", (conflict as TransitionResult.Rejected).code)
    }

    @Test
    fun `published quest can cancel and completed quest can dispute`() {
        val initial = QuestAggregate.from(draft())
        val published = engine.transition(initial, command(1, 0, QuestState.PUBLISHED)) as TransitionResult.Applied
        assertTrue(
            engine.transition(published.aggregate, command(2, 1, QuestState.CANCELLED)) is TransitionResult.Applied,
        )

        var completed = QuestAggregate.from(draft())
        listOf(
            QuestState.PUBLISHED,
            QuestState.MATCHING,
            QuestState.ASSIGNED,
            QuestState.ACTIVE,
            QuestState.COMPLETED,
        ).forEachIndexed { index, state ->
            completed = (
                engine.transition(completed, command(index + 10, completed.version, state)) as TransitionResult.Applied
            ).aggregate
        }
        assertTrue(
            engine.transition(completed, command(20, completed.version, QuestState.DISPUTED)) is TransitionResult.Applied,
        )
    }

    private fun draft() = QuestDraft(
        id = QuestId("quest-1"),
        creatorId = creator,
        module = ModuleReference("local-delivery", Version(1)),
        definition = QuestDefinitionReference("small-package", Version(1)),
        title = "Levar um pacote",
        description = "Entrega local simulada",
        assignmentStrategy = AssignmentStrategy.FIRST_ELIGIBLE_ACCEPTS,
        createdAt = start,
    )

    private fun command(index: Int, version: Long, target: QuestState) = TransitionCommand(
        eventId = EventId("event-$index"),
        expectedVersion = version,
        targetState = target,
        actorId = creator,
        occurredAt = start.plusSeconds(index.toLong()),
    )
}
