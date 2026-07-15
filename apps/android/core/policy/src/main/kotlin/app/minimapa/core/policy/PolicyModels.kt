package app.minimapa.core.policy

import app.minimapa.core.domain.FieldValue
import app.minimapa.core.domain.ModuleReference
import app.minimapa.core.domain.PlayerId
import app.minimapa.core.domain.QuestId
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.Version

enum class PolicyDecision {
    ALLOW,
    REQUIRE_REVIEW,
    DENY,
}

enum class MandatoryPolicyKind {
    CATEGORY,
    AGE,
    IDENTITY,
    JURISDICTION,
    LOCATION,
    CONTENT,
    CONSUMER,
    PAYMENT,
}

enum class PolicyAction {
    PUBLISH,
    MATCH,
    REVEAL_LOCATION,
    APPLY,
    ACCEPT,
    ASSIGN,
    START,
    COMPLETE,
    CANCEL,
    EXPIRE,
    DISPUTE,
    REWARD,
    PAY,
}

data class PolicyContext(
    val questId: QuestId,
    val module: ModuleReference,
    val categoryId: String,
    val state: QuestState,
    val action: PolicyAction,
    val actorId: PlayerId,
    val territoryCode: String,
    val facts: Map<String, FieldValue> = emptyMap(),
) {
    init {
        require(categoryId.isNotBlank()) { "Category id cannot be blank" }
        require(territoryCode.isNotBlank()) { "Territory code cannot be blank" }
    }
}

data class RuleEvaluation(
    val ruleId: String,
    val kind: MandatoryPolicyKind,
    val version: Version,
    val decision: PolicyDecision,
    val reasonCodes: Set<String>,
)

data class PolicyEvaluation(
    val decision: PolicyDecision,
    val policySetVersion: String,
    val reasonCodes: Set<String>,
    val rules: List<RuleEvaluation>,
)

interface PolicyRule {
    val id: String
    val kind: MandatoryPolicyKind
    val version: Version

    fun evaluate(context: PolicyContext): RuleEvaluation
}

fun interface PolicyGate {
    fun evaluate(context: PolicyContext): PolicyEvaluation
}
