package app.minimapa.core.questcontract

import app.minimapa.core.domain.ModuleReference
import app.minimapa.core.domain.UniversalRole
import app.minimapa.core.domain.Version

class QuestModuleRegistrationException(message: String) : IllegalArgumentException(message)

class QuestModuleRegistry(
    private val coreVersion: Version,
    private val capabilityCatalog: CapabilityCatalog = CapabilityCatalog(),
) {
    private val modules = linkedMapOf<ModuleReference, QuestModuleContract>()
    private val definitions = linkedMapOf<Pair<String, Version>, QuestTypeDefinition>()

    fun register(contract: QuestModuleContract) {
        val reference = ModuleReference(contract.moduleId, contract.version)
        if (reference in modules) {
            throw QuestModuleRegistrationException("Module $reference is already registered")
        }
        if (coreVersion !in contract.coreCompatibility) {
            throw QuestModuleRegistrationException("Module $reference is incompatible with core $coreVersion")
        }
        val unsupported = capabilityCatalog.unsupported(contract.capabilities)
        if (unsupported.isNotEmpty()) {
            throw QuestModuleRegistrationException("Unsupported capabilities: $unsupported")
        }
        val mappedRoles = contract.roles.map { it.universalRole }.toSet()
        if (!mappedRoles.containsAll(setOf(UniversalRole.REQUESTER, UniversalRole.ASSIGNEE))) {
            throw QuestModuleRegistrationException("Modules must map requester and assignee roles")
        }
        if (contract.roles.map { it.id }.distinct().size != contract.roles.size) {
            throw QuestModuleRegistrationException("Module role ids must be unique")
        }
        if (contract.assignmentStrategies.isEmpty()) {
            throw QuestModuleRegistrationException("Module must support an assignment strategy")
        }
        if (contract.authorizedRoles.keys != ModuleAction.entries.toSet()) {
            throw QuestModuleRegistrationException("Module must define authorization for every module action")
        }
        if (contract.authorizedRoles.values.any { it.isEmpty() || !mappedRoles.containsAll(it) }) {
            throw QuestModuleRegistrationException("Module action authorization must use mapped roles")
        }
        if (contract.authorizedRoles.getValue(ModuleAction.CREATE) != setOf(UniversalRole.REQUESTER)) {
            throw QuestModuleRegistrationException("Only the requester can create a quest")
        }
        if (UniversalRole.ASSIGNEE !in contract.authorizedRoles.getValue(ModuleAction.UPDATE_EXECUTION)) {
            throw QuestModuleRegistrationException("The assignee must be able to update execution")
        }
        if (contract.additionalPolicyRuleIds.any { !it.matches(Regex("[a-z][a-z0-9.-]{2,95}")) }) {
            throw QuestModuleRegistrationException("Additional policy rule ids must be canonical")
        }

        modules[reference] = contract
    }

    fun registerDefinition(definition: QuestTypeDefinition) {
        val contract = resolve(definition.module)
        require(definition.creationSchema == contract.schemas.creation) {
            "Definition must use the module's approved creation schema"
        }
        val visibleFields = contract.visibilityRules.map { it.field }.toSet()
        require(visibleFields.containsAll(definition.payload.keys)) {
            "Every definition field requires an explicit visibility rule"
        }
        val violations = contract.validate(definition)
        require(violations.isEmpty()) {
            "Invalid quest definition: ${violations.joinToString { it.code }}"
        }
        val key = definition.id to definition.version
        require(key !in definitions) { "Quest definition $key is already registered" }
        definitions[key] = definition
    }

    fun resolve(reference: ModuleReference): QuestModuleContract =
        modules[reference] ?: throw NoSuchElementException("Module $reference is not registered")

    fun resolveDefinition(id: String, version: Version): QuestTypeDefinition =
        definitions[id to version] ?: throw NoSuchElementException("Definition $id@$version is not registered")

    fun registeredModules(): Set<ModuleReference> = modules.keys.toSet()
}
