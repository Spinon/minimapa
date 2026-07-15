package app.minimapa.core.policy

import app.minimapa.core.domain.ModuleReference
import app.minimapa.core.domain.AssignmentStrategy
import app.minimapa.core.domain.EventId
import app.minimapa.core.domain.PlayerId
import app.minimapa.core.domain.QuestAggregate
import app.minimapa.core.domain.QuestDefinitionReference
import app.minimapa.core.domain.QuestDraft
import app.minimapa.core.domain.QuestEngine
import app.minimapa.core.domain.QuestId
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.TransitionCommand
import app.minimapa.core.domain.TransitionResult
import app.minimapa.core.domain.Version
import java.time.Instant
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CompositePolicyGateTest {
    @Test
    fun `all mandatory global policies always run`() {
        val gate = CompositePolicyGate(mandatoryRules())

        val result = gate.evaluate(context())

        assertEquals(PolicyDecision.ALLOW, result.decision)
        assertEquals(MandatoryPolicyKind.entries.toSet(), result.rules.map { it.kind }.toSet())
    }

    @Test
    fun `gate cannot be built without a mandatory policy`() {
        val incomplete = mandatoryRules().filterNot { it.kind == MandatoryPolicyKind.IDENTITY }

        assertThrows(IllegalArgumentException::class.java) {
            CompositePolicyGate(incomplete)
        }
    }

    @Test
    fun `module rule cannot replace a global rule`() {
        val duplicate = allowRule("global.identity", MandatoryPolicyKind.IDENTITY)

        assertThrows(IllegalArgumentException::class.java) {
            CompositePolicyGate(mandatoryRules(), listOf(duplicate))
        }
    }

    @Test
    fun `deny wins and preserves every reason for audit`() {
        val moduleRules = listOf(
            rule("module.review", MandatoryPolicyKind.CONTENT, PolicyDecision.REQUIRE_REVIEW, "CONTENT_REVIEW"),
            rule("module.deny", MandatoryPolicyKind.CATEGORY, PolicyDecision.DENY, "CATEGORY_DISABLED"),
        )
        val result = CompositePolicyGate(mandatoryRules(), moduleRules).evaluate(context())

        assertEquals(PolicyDecision.DENY, result.decision)
        assertTrue(result.reasonCodes.containsAll(setOf("CONTENT_REVIEW", "CATEGORY_DISABLED")))
        assertEquals(MandatoryPolicyKind.entries.size + 2, result.rules.size)
    }

    @Test
    fun `quest engine cannot transition when a mandatory policy denies`() {
        val rules = mandatoryRules().map { existingRule ->
            if (existingRule.kind == MandatoryPolicyKind.IDENTITY) {
                rule("global.identity-deny", MandatoryPolicyKind.IDENTITY, PolicyDecision.DENY, "IDENTITY_REQUIRED")
            } else {
                existingRule
            }
        }
        val authorizer = PolicyQuestTransitionAuthorizer(CompositePolicyGate(rules)) {
            QuestPolicyFacts("delivery.small-package", "BR-SP-RIO-CLARO")
        }
        val engine = QuestEngine(authorizer)
        val aggregate = QuestAggregate.from(draft())
        val result = engine.transition(
            aggregate,
            TransitionCommand(
                eventId = EventId("publish-denied"),
                expectedVersion = 0,
                targetState = QuestState.PUBLISHED,
                actorId = PlayerId("player-1"),
                occurredAt = Instant.parse("2026-07-15T12:00:01Z"),
            ),
        )

        assertEquals("POLICY_DENIED", (result as TransitionResult.Rejected).code)
        assertEquals(QuestState.DRAFT, result.aggregate.state)
        assertEquals(0, result.aggregate.events.size)
    }

    private fun mandatoryRules(): List<PolicyRule> = MandatoryPolicyKind.entries.map { kind ->
        allowRule("global.${kind.name.lowercase()}", kind)
    }

    private fun allowRule(id: String, kind: MandatoryPolicyKind) =
        StaticPolicyRule(id, kind, Version(1)) { PolicyDecision.ALLOW to emptySet() }

    private fun rule(
        id: String,
        kind: MandatoryPolicyKind,
        decision: PolicyDecision,
        reason: String,
    ) = StaticPolicyRule(id, kind, Version(1)) { decision to setOf(reason) }

    private fun context() = PolicyContext(
        questId = QuestId("quest-1"),
        module = ModuleReference("local-delivery", Version(1)),
        categoryId = "delivery.small-package",
        state = QuestState.DRAFT,
        action = PolicyAction.PUBLISH,
        actorId = PlayerId("player-1"),
        territoryCode = "BR-SP-RIO-CLARO",
    )

    private fun draft() = QuestDraft(
        id = QuestId("quest-1"),
        creatorId = PlayerId("player-1"),
        module = ModuleReference("local-delivery", Version(1)),
        definition = QuestDefinitionReference("small-package", Version(1)),
        title = "Entrega local",
        description = "",
        assignmentStrategy = AssignmentStrategy.FIRST_ELIGIBLE_ACCEPTS,
        createdAt = Instant.parse("2026-07-15T12:00:00Z"),
    )
}
