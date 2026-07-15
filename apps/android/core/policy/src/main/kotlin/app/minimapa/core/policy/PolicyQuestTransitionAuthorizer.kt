package app.minimapa.core.policy

import app.minimapa.core.domain.FieldValue
import app.minimapa.core.domain.QuestAggregate
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.QuestTransitionAuthorizer
import app.minimapa.core.domain.TransitionAuthorizationDecision
import app.minimapa.core.domain.TransitionCommand

data class QuestPolicyFacts(
    val categoryId: String,
    val territoryCode: String,
    val facts: Map<String, FieldValue> = emptyMap(),
)

fun interface QuestPolicyFactsProvider {
    fun factsFor(aggregate: QuestAggregate): QuestPolicyFacts
}

class PolicyQuestTransitionAuthorizer(
    private val gate: PolicyGate,
    private val factsProvider: QuestPolicyFactsProvider,
) : QuestTransitionAuthorizer {
    override fun authorize(
        aggregate: QuestAggregate,
        command: TransitionCommand,
    ): TransitionAuthorizationDecision {
        val facts = factsProvider.factsFor(aggregate)
        val evaluation = gate.evaluate(
            PolicyContext(
                questId = aggregate.draft.id,
                module = aggregate.draft.module,
                categoryId = facts.categoryId,
                state = aggregate.state,
                action = command.targetState.toPolicyAction(),
                actorId = command.actorId,
                territoryCode = facts.territoryCode,
                facts = facts.facts,
            ),
        )
        return when (evaluation.decision) {
            PolicyDecision.ALLOW -> TransitionAuthorizationDecision.ALLOW
            PolicyDecision.REQUIRE_REVIEW -> TransitionAuthorizationDecision.REQUIRE_REVIEW
            PolicyDecision.DENY -> TransitionAuthorizationDecision.DENY
        }
    }
}

private fun QuestState.toPolicyAction(): PolicyAction = when (this) {
    QuestState.DRAFT -> error("A quest cannot transition back to draft")
    QuestState.PUBLISHED -> PolicyAction.PUBLISH
    QuestState.MATCHING -> PolicyAction.MATCH
    QuestState.ASSIGNED -> PolicyAction.ASSIGN
    QuestState.ACTIVE -> PolicyAction.START
    QuestState.COMPLETED -> PolicyAction.COMPLETE
    QuestState.CANCELLED -> PolicyAction.CANCEL
    QuestState.EXPIRED -> PolicyAction.EXPIRE
    QuestState.DISPUTED -> PolicyAction.DISPUTE
}
