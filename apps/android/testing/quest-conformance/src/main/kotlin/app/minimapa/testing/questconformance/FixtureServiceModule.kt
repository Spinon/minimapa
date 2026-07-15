package app.minimapa.testing.questconformance

import app.minimapa.core.domain.AssignmentStrategy
import app.minimapa.core.domain.FieldValue
import app.minimapa.core.domain.ModuleReference
import app.minimapa.core.domain.QuestState
import app.minimapa.core.domain.UniversalRole
import app.minimapa.core.domain.Version
import app.minimapa.core.questcontract.ContractViolation
import app.minimapa.core.questcontract.CoreCompatibility
import app.minimapa.core.questcontract.FieldExposure
import app.minimapa.core.questcontract.FieldVisibilityRule
import app.minimapa.core.questcontract.ModuleAction
import app.minimapa.core.questcontract.ModuleRole
import app.minimapa.core.questcontract.ModuleSchemas
import app.minimapa.core.questcontract.ModuleSubstate
import app.minimapa.core.questcontract.QuestModuleContract
import app.minimapa.core.questcontract.QuestTypeDefinition
import app.minimapa.core.questcontract.SchemaPurpose
import app.minimapa.core.questcontract.SchemaReference
import app.minimapa.core.questcontract.StandardCapabilities

class FixtureServiceModule(
    override val version: Version = Version(1),
) : QuestModuleContract {
    override val moduleId = "fixture-service"
    override val coreCompatibility = CoreCompatibility(Version(1), Version(2))
    override val schemas = ModuleSchemas(
        creation = SchemaReference("service.creation", version, SchemaPurpose.CREATION),
        detail = SchemaReference("service.detail", version, SchemaPurpose.DETAIL),
        execution = SchemaReference("service.execution", version, SchemaPurpose.EXECUTION),
    )
    override val roles = setOf(
        ModuleRole("CLIENT", "role.client", UniversalRole.REQUESTER),
        ModuleRole("PROFESSIONAL", "role.professional", UniversalRole.ASSIGNEE),
        ModuleRole("PAYEE", "role.payee", UniversalRole.BENEFICIARY),
    )
    override val capabilities = setOf(
        StandardCapabilities.LOCATION,
        StandardCapabilities.QUOTE,
        StandardCapabilities.SCHEDULING,
        StandardCapabilities.MATERIALS,
        StandardCapabilities.EVIDENCE,
    )
    override val substates = setOf(
        ModuleSubstate("SCHEDULED", QuestState.ASSIGNED),
        ModuleSubstate("DIAGNOSING", QuestState.ACTIVE),
        ModuleSubstate("AWAITING_MATERIALS", QuestState.ACTIVE),
        ModuleSubstate("AWAITING_OWNER_APPROVAL", QuestState.ACTIVE),
    )
    override val assignmentStrategies = setOf(AssignmentStrategy.QUOTE_AND_SCHEDULE)
    override val authorizedRoles = mapOf(
        ModuleAction.CREATE to setOf(UniversalRole.REQUESTER),
        ModuleAction.UPDATE_DETAILS to setOf(UniversalRole.REQUESTER, UniversalRole.ASSIGNEE),
        ModuleAction.UPDATE_EXECUTION to setOf(UniversalRole.ASSIGNEE),
        ModuleAction.SUBMIT_EVIDENCE to setOf(UniversalRole.REQUESTER, UniversalRole.ASSIGNEE),
    )
    override val visibilityRules = setOf(
        FieldVisibilityRule("serviceCode", FieldExposure.PUBLIC_DISCOVERY),
        FieldVisibilityRule("skillCode", FieldExposure.PUBLIC_DISCOVERY),
        FieldVisibilityRule("requiresAccessInstructions", FieldExposure.PRIVATE),
    )
    override val additionalPolicyRuleIds = setOf("module.service-scope")

    override fun validate(definition: QuestTypeDefinition): List<ContractViolation> = buildList {
        val serviceCode = definition.payload["serviceCode"] as? FieldValue.Text
        val skillCode = definition.payload["skillCode"] as? FieldValue.Text
        if (serviceCode?.value.isNullOrBlank()) add(ContractViolation("SERVICE_CODE_REQUIRED", "serviceCode"))
        if (skillCode?.value.isNullOrBlank()) add(ContractViolation("SKILL_CODE_REQUIRED", "skillCode"))
        if (definition.module != ModuleReference(moduleId, version)) {
            add(ContractViolation("MODULE_VERSION_MISMATCH"))
        }
    }
}

object FixtureServiceDefinitions {
    fun showerReplacement(contract: FixtureServiceModule) = definition(
        contract = contract,
        id = "replace-shower",
        category = "service.plumbing.shower",
        serviceCode = "REPLACE_SHOWER",
        skillCode = "PLUMBING",
    )

    fun faucetInstallation(contract: FixtureServiceModule) = definition(
        contract = contract,
        id = "install-faucet",
        category = "service.plumbing.faucet",
        serviceCode = "INSTALL_FAUCET",
        skillCode = "PLUMBING",
    )

    fun invalid(contract: FixtureServiceModule) = QuestTypeDefinition(
        id = "invalid-service",
        version = Version(1),
        module = ModuleReference(contract.moduleId, contract.version),
        creationSchema = contract.schemas.creation,
        categoryId = "service.invalid",
        payload = mapOf("requiresAccessInstructions" to FieldValue.Flag(true)),
    )

    private fun definition(
        contract: FixtureServiceModule,
        id: String,
        category: String,
        serviceCode: String,
        skillCode: String,
    ) = QuestTypeDefinition(
        id = id,
        version = Version(1),
        module = ModuleReference(contract.moduleId, contract.version),
        creationSchema = contract.schemas.creation,
        categoryId = category,
        payload = mapOf(
            "serviceCode" to FieldValue.Text(serviceCode),
            "skillCode" to FieldValue.Text(skillCode),
            "requiresAccessInstructions" to FieldValue.Flag(true),
        ),
    )
}
