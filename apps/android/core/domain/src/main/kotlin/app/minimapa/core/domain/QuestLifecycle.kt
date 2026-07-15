package app.minimapa.core.domain

import java.time.Instant

data class TransitionCommand(
    val eventId: EventId,
    val expectedVersion: Long,
    val targetState: QuestState,
    val actorId: PlayerId,
    val occurredAt: Instant,
    val reasonCode: String? = null,
)

sealed interface TransitionResult {
    data class Applied(val aggregate: QuestAggregate, val event: QuestEvent) : TransitionResult
    data class Duplicate(val aggregate: QuestAggregate, val event: QuestEvent) : TransitionResult
    data class Rejected(val aggregate: QuestAggregate, val code: String) : TransitionResult
}

enum class TransitionAuthorizationDecision {
    ALLOW,
    REQUIRE_REVIEW,
    DENY,
}

fun interface QuestTransitionAuthorizer {
    fun authorize(aggregate: QuestAggregate, command: TransitionCommand): TransitionAuthorizationDecision
}

class QuestEngine(
    private val authorizer: QuestTransitionAuthorizer,
) {
    fun transition(aggregate: QuestAggregate, command: TransitionCommand): TransitionResult {
        aggregate.replayResult(command)?.let { return it }
        return when (authorizer.authorize(aggregate, command)) {
            TransitionAuthorizationDecision.ALLOW -> aggregate.applyTransition(command)
            TransitionAuthorizationDecision.REQUIRE_REVIEW ->
                TransitionResult.Rejected(aggregate, "POLICY_REVIEW_REQUIRED")
            TransitionAuthorizationDecision.DENY ->
                TransitionResult.Rejected(aggregate, "POLICY_DENIED")
        }
    }
}

class QuestAggregate private constructor(
    val draft: QuestDraft,
    val state: QuestState,
    val version: Long,
    val parties: Set<QuestParty>,
    val events: List<QuestEvent>,
) {
    internal fun replayResult(command: TransitionCommand): TransitionResult? {
        val previous = events.firstOrNull { it.id == command.eventId }
        if (previous != null) {
            return if (
                previous.to == command.targetState &&
                previous.actorId == command.actorId &&
                previous.reasonCode == command.reasonCode
            ) {
                TransitionResult.Duplicate(this, previous)
            } else {
                TransitionResult.Rejected(this, "EVENT_ID_CONFLICT")
            }
        }
        return null
    }

    internal fun applyTransition(command: TransitionCommand): TransitionResult {
        replayResult(command)?.let { return it }
        if (command.expectedVersion != version) {
            return TransitionResult.Rejected(this, "VERSION_CONFLICT")
        }
        if (!QuestLifecycle.canTransition(state, command.targetState)) {
            return TransitionResult.Rejected(this, "INVALID_STATE_TRANSITION")
        }
        if (command.occurredAt.isBefore(events.lastOrNull()?.occurredAt ?: draft.createdAt)) {
            return TransitionResult.Rejected(this, "NON_MONOTONIC_EVENT_TIME")
        }

        val event = QuestEvent(
            id = command.eventId,
            questId = draft.id,
            sequence = version + 1,
            from = state,
            to = command.targetState,
            actorId = command.actorId,
            occurredAt = command.occurredAt,
            reasonCode = command.reasonCode,
        )
        val updated = QuestAggregate(
            draft = draft,
            state = command.targetState,
            version = version + 1,
            parties = parties,
            events = events + event,
        )
        return TransitionResult.Applied(updated, event)
    }

    companion object {
        fun from(draft: QuestDraft): QuestAggregate = QuestAggregate(
            draft = draft,
            state = QuestState.DRAFT,
            version = 0,
            parties = setOf(QuestParty(UniversalRole.REQUESTER, draft.creatorId)),
            events = emptyList(),
        )
    }
}

object QuestLifecycle {
    private val transitions = mapOf(
        QuestState.DRAFT to setOf(QuestState.PUBLISHED, QuestState.CANCELLED),
        QuestState.PUBLISHED to setOf(QuestState.MATCHING, QuestState.CANCELLED, QuestState.EXPIRED),
        QuestState.MATCHING to setOf(QuestState.ASSIGNED, QuestState.CANCELLED, QuestState.EXPIRED),
        QuestState.ASSIGNED to setOf(QuestState.ACTIVE, QuestState.CANCELLED, QuestState.DISPUTED),
        QuestState.ACTIVE to setOf(QuestState.COMPLETED, QuestState.CANCELLED, QuestState.DISPUTED),
        QuestState.COMPLETED to setOf(QuestState.DISPUTED),
        QuestState.CANCELLED to emptySet(),
        QuestState.EXPIRED to emptySet(),
        QuestState.DISPUTED to emptySet(),
    )

    fun canTransition(from: QuestState, to: QuestState): Boolean = to in transitions.getValue(from)
}
