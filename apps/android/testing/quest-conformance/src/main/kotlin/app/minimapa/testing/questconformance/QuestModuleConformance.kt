package app.minimapa.testing.questconformance

import app.minimapa.core.domain.EventId
import app.minimapa.core.domain.PlayerId
import app.minimapa.core.domain.QuestAggregate
import app.minimapa.core.domain.QuestDefinitionReference
import app.minimapa.core.domain.QuestDraft
import app.minimapa.core.domain.QuestId
import app.minimapa.core.domain.QuestEngine
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.TransitionCommand
import app.minimapa.core.domain.TransitionAuthorizationDecision
import app.minimapa.core.domain.TransitionResult
import app.minimapa.core.domain.Version
import app.minimapa.core.policy.MandatoryPolicyKind
import app.minimapa.core.policy.PolicyAction
import app.minimapa.core.policy.PolicyContext
import app.minimapa.core.policy.PolicyGate
import app.minimapa.core.questcontract.QuestModuleContract
import app.minimapa.core.questcontract.QuestModuleRegistry
import app.minimapa.core.questcontract.QuestTypeDefinition
import java.time.Instant

data class QuestModuleConformanceVector(
    val contract: QuestModuleContract,
    val validDefinitions: List<QuestTypeDefinition>,
    val invalidDefinitions: List<QuestTypeDefinition>,
    val policyGate: PolicyGate,
)

object QuestModuleConformance {
    fun verify(vector: QuestModuleConformanceVector, coreVersion: Version = Version(1)): List<String> = buildList {
        val registry = QuestModuleRegistry(coreVersion)
        runCatching { registry.register(vector.contract) }
            .onFailure { add("REGISTRATION_FAILED:${it.message}") }
        if (isNotEmpty()) return@buildList

        vector.validDefinitions.forEach { definition ->
            runCatching { registry.registerDefinition(definition) }
                .onFailure { add("VALID_DEFINITION_REJECTED:${definition.id}:${it.message}") }
        }
        vector.invalidDefinitions.forEach { definition ->
            if (vector.contract.validate(definition).isEmpty()) {
                add("INVALID_DEFINITION_ACCEPTED:${definition.id}")
            }
        }

        val visibilityFields = vector.contract.visibilityRules.map { it.field }.toSet()
        vector.validDefinitions.forEach { definition ->
            if (!visibilityFields.containsAll(definition.payload.keys)) {
                add("FIELD_WITHOUT_VISIBILITY:${definition.id}")
            }
        }

        val policy = vector.policyGate.evaluate(policyContext(vector.validDefinitions.first()))
        val evaluatedKinds = policy.rules.map { it.kind }.toSet()
        if (!evaluatedKinds.containsAll(MandatoryPolicyKind.entries)) {
            add("MANDATORY_POLICY_BYPASS")
        }
        val evaluatedRuleIds = policy.rules.map { it.ruleId }.toSet()
        if (!evaluatedRuleIds.containsAll(vector.contract.additionalPolicyRuleIds)) {
            add("MODULE_POLICY_NOT_EVALUATED")
        }

        verifyLifecycle(vector.validDefinitions.first())?.let(::add)
    }

    private fun verifyLifecycle(definition: QuestTypeDefinition): String? {
        val start = Instant.parse("2026-07-15T12:00:00Z")
        val actor = PlayerId("conformance-player")
        val draft = QuestDraft(
            id = QuestId("conformance-quest"),
            creatorId = actor,
            module = definition.module,
            definition = QuestDefinitionReference(definition.id, definition.version),
            title = "Conformance quest",
            description = "",
            assignmentStrategy = definitionStrategy(definition),
            createdAt = start,
        )
        val initial = QuestAggregate.from(draft)
        val engine = QuestEngine { _, _ -> TransitionAuthorizationDecision.ALLOW }
        val publish = TransitionCommand(EventId("publish"), 0, QuestState.PUBLISHED, actor, start.plusSeconds(1))
        val applied = engine.transition(initial, publish) as? TransitionResult.Applied
            ?: return "UNIVERSAL_LIFECYCLE_REJECTED"
        if (engine.transition(applied.aggregate, publish) !is TransitionResult.Duplicate) {
            return "EVENT_IDEMPOTENCY_FAILED"
        }
        val stale = engine.transition(
            applied.aggregate,
            TransitionCommand(EventId("cancel"), 0, QuestState.CANCELLED, actor, start.plusSeconds(2)),
        )
        if (stale !is TransitionResult.Rejected || stale.code != "VERSION_CONFLICT") {
            return "OPTIMISTIC_CONCURRENCY_FAILED"
        }
        return null
    }

    private fun definitionStrategy(definition: QuestTypeDefinition) =
        if (definition.categoryId.startsWith("service.")) {
            app.minimapa.core.domain.AssignmentStrategy.QUOTE_AND_SCHEDULE
        } else {
            app.minimapa.core.domain.AssignmentStrategy.FIRST_ELIGIBLE_ACCEPTS
        }

    private fun policyContext(definition: QuestTypeDefinition) = PolicyContext(
        questId = QuestId("conformance-quest"),
        module = definition.module,
        categoryId = definition.categoryId,
        state = QuestState.DRAFT,
        action = PolicyAction.PUBLISH,
        actorId = PlayerId("conformance-player"),
        territoryCode = "BR-SP-RIO-CLARO",
        facts = definition.payload,
    )
}
