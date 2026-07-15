package app.minimapa.core.questcontract

import app.minimapa.core.domain.AssignmentStrategy
import app.minimapa.core.domain.FieldValue
import app.minimapa.core.domain.ModuleReference
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.UniversalRole
import app.minimapa.core.domain.Version

data class CoreCompatibility(
    val minimum: Version,
    val maximumExclusive: Version,
) {
    init {
        require(minimum < maximumExclusive) { "Core compatibility range must not be empty" }
    }

    operator fun contains(version: Version): Boolean = version >= minimum && version < maximumExclusive
}

data class ModuleRole(
    val id: String,
    val displayNameKey: String,
    val universalRole: UniversalRole,
) {
    init {
        require(id.matches(Regex("[A-Z][A-Z0-9_]{2,63}"))) { "Module role id must be canonical" }
        require(displayNameKey.isNotBlank()) { "Role display name key cannot be blank" }
    }
}

data class ModuleSubstate(
    val id: String,
    val universalState: QuestState,
) {
    init {
        require(id.matches(Regex("[A-Z][A-Z0-9_]{2,63}"))) { "Module substate id must be canonical" }
    }
}

enum class ModuleAction {
    CREATE,
    UPDATE_DETAILS,
    UPDATE_EXECUTION,
    SUBMIT_EVIDENCE,
}

enum class FieldExposure {
    PUBLIC_DISCOVERY,
    PARTICIPANTS_AFTER_ASSIGNMENT,
    PRIVATE,
}

data class FieldVisibilityRule(
    val field: String,
    val exposure: FieldExposure,
) {
    init {
        require(field.matches(Regex("[a-z][a-zA-Z0-9_.]{1,95}"))) { "Field path must be canonical" }
    }
}

data class QuestTypeDefinition(
    val id: String,
    val version: Version,
    val module: ModuleReference,
    val creationSchema: SchemaReference,
    val categoryId: String,
    val payload: Map<String, FieldValue>,
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9-]{2,63}"))) { "Definition id must be canonical" }
        require(categoryId.matches(Regex("[a-z][a-z0-9.-]{2,95}"))) { "Category id must be canonical" }
    }
}

data class ContractViolation(
    val code: String,
    val field: String? = null,
)

interface QuestModuleContract {
    val moduleId: String
    val version: Version
    val coreCompatibility: CoreCompatibility
    val schemas: ModuleSchemas
    val roles: Set<ModuleRole>
    val capabilities: Set<CapabilityId>
    val substates: Set<ModuleSubstate>
    val assignmentStrategies: Set<AssignmentStrategy>
    val authorizedRoles: Map<ModuleAction, Set<UniversalRole>>
    val visibilityRules: Set<FieldVisibilityRule>
    val additionalPolicyRuleIds: Set<String>

    fun validate(definition: QuestTypeDefinition): List<ContractViolation>
}
