package app.minimapa.core.domain

import java.time.Instant

enum class UniversalRole {
    REQUESTER,
    CANDIDATE,
    ASSIGNEE,
    BENEFICIARY,
}

enum class AssignmentStrategy {
    FIRST_ELIGIBLE_ACCEPTS,
    OWNER_SELECTS_APPLICATION,
    QUOTE_AND_SCHEDULE,
    INVITE_ONLY,
}

enum class QuestState {
    DRAFT,
    PUBLISHED,
    MATCHING,
    ASSIGNED,
    ACTIVE,
    COMPLETED,
    CANCELLED,
    EXPIRED,
    DISPUTED,
}

data class ModuleReference(
    val moduleId: String,
    val moduleVersion: Version,
) {
    init {
        require(moduleId.matches(Regex("[a-z][a-z0-9-]{2,63}"))) { "Module id must be canonical" }
    }
}

data class QuestDefinitionReference(
    val definitionId: String,
    val definitionVersion: Version,
) {
    init {
        require(definitionId.matches(Regex("[a-z][a-z0-9-]{2,63}"))) { "Definition id must be canonical" }
    }
}

data class QuestParty(
    val role: UniversalRole,
    val playerId: PlayerId,
)

data class QuestDraft(
    val id: QuestId,
    val creatorId: PlayerId,
    val module: ModuleReference,
    val definition: QuestDefinitionReference,
    val title: String,
    val description: String,
    val assignmentStrategy: AssignmentStrategy,
    val requirements: List<TypedRequirement> = emptyList(),
    val createdAt: Instant,
) {
    init {
        require(title.isNotBlank()) { "Quest title cannot be blank" }
        require(title.length <= 120) { "Quest title is too long" }
        require(description.length <= 4_000) { "Quest description is too long" }
    }
}

data class QuestEvent(
    val id: EventId,
    val questId: QuestId,
    val sequence: Long,
    val from: QuestState,
    val to: QuestState,
    val actorId: PlayerId,
    val occurredAt: Instant,
    val reasonCode: String?,
)
