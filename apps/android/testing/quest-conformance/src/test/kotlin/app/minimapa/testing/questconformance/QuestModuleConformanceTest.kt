package app.minimapa.testing.questconformance

import app.minimapa.core.domain.Version
import app.minimapa.core.policy.CompositePolicyGate
import app.minimapa.core.policy.MandatoryPolicyKind
import app.minimapa.core.policy.PolicyDecision
import app.minimapa.core.policy.StaticPolicyRule
import app.minimapa.core.questcontract.CapabilityId
import app.minimapa.core.questcontract.CoreCompatibility
import app.minimapa.core.questcontract.QuestModuleContract
import app.minimapa.core.questcontract.QuestModuleRegistrationException
import app.minimapa.core.questcontract.QuestModuleRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class QuestModuleConformanceTest {
    @Test
    fun `fixture module passes reusable conformance suite`() {
        val contract = FixtureServiceModule()
        val vector = QuestModuleConformanceVector(
            contract = contract,
            validDefinitions = listOf(
                FixtureServiceDefinitions.showerReplacement(contract),
                FixtureServiceDefinitions.faucetInstallation(contract),
            ),
            invalidDefinitions = listOf(FixtureServiceDefinitions.invalid(contract)),
            policyGate = policyGate(),
        )

        assertEquals(emptyList<String>(), QuestModuleConformance.verify(vector))
    }

    @Test
    fun `new service definitions register by data without a new module`() {
        val contract = FixtureServiceModule()
        val registry = QuestModuleRegistry(Version(1))
        registry.register(contract)

        registry.registerDefinition(FixtureServiceDefinitions.showerReplacement(contract))
        registry.registerDefinition(FixtureServiceDefinitions.faucetInstallation(contract))

        assertEquals("REPLACE_SHOWER", textField(registry, "replace-shower", "serviceCode"))
        assertEquals("INSTALL_FAUCET", textField(registry, "install-faucet", "serviceCode"))
        assertEquals(1, registry.registeredModules().size)
    }

    @Test
    fun `registry rejects a capability outside the approved catalog`() {
        val base = FixtureServiceModule()
        val invalid = object : QuestModuleContract by base {
            override val capabilities = setOf(CapabilityId("ARBITRARY_CLIENT_CODE"))
        }

        assertThrows(QuestModuleRegistrationException::class.java) {
            QuestModuleRegistry(Version(1)).register(invalid)
        }
    }

    @Test
    fun `registry rejects an incompatible module version`() {
        val base = FixtureServiceModule()
        val incompatible = object : QuestModuleContract by base {
            override val coreCompatibility = CoreCompatibility(Version(2), Version(3))
        }

        assertThrows(QuestModuleRegistrationException::class.java) {
            QuestModuleRegistry(Version(1)).register(incompatible)
        }
    }

    @Test
    fun `registry keeps old and new module versions readable together`() {
        val oldModule = FixtureServiceModule(Version(1))
        val newModule = FixtureServiceModule(Version(1, 1))
        val registry = QuestModuleRegistry(Version(1))

        registry.register(oldModule)
        registry.register(newModule)
        val oldDefinition = FixtureServiceDefinitions.showerReplacement(oldModule)
        val newDefinition = FixtureServiceDefinitions.showerReplacement(newModule).copy(version = Version(2))
        registry.registerDefinition(oldDefinition)
        registry.registerDefinition(newDefinition)

        assertEquals(Version(1), registry.resolveDefinition("replace-shower", Version(1)).module.moduleVersion)
        assertEquals(Version(1, 1), registry.resolveDefinition("replace-shower", Version(2)).module.moduleVersion)
    }

    @Test
    fun `definition cannot introduce a field without an explicit visibility rule`() {
        val contract = FixtureServiceModule()
        val registry = QuestModuleRegistry(Version(1))
        registry.register(contract)
        val injected = FixtureServiceDefinitions.showerReplacement(contract).copy(
            id = "injected-field",
            payload = FixtureServiceDefinitions.showerReplacement(contract).payload +
                ("secretBackdoor" to app.minimapa.core.domain.FieldValue.Text("hidden")),
        )

        val error = assertThrows(IllegalArgumentException::class.java) {
            registry.registerDefinition(injected)
        }

        assertTrue(error.message.orEmpty().contains("visibility rule"))
    }

    @Test
    fun `definition missing required fields is rejected`() {
        val contract = FixtureServiceModule()
        val registry = QuestModuleRegistry(Version(1))
        registry.register(contract)

        val error = assertThrows(IllegalArgumentException::class.java) {
            registry.registerDefinition(FixtureServiceDefinitions.invalid(contract))
        }

        assertTrue(error.message.orEmpty().contains("SERVICE_CODE_REQUIRED"))
    }

    private fun policyGate(): CompositePolicyGate {
        val global = MandatoryPolicyKind.entries.map { kind ->
            StaticPolicyRule("global.${kind.name.lowercase()}", kind, Version(1)) {
                PolicyDecision.ALLOW to emptySet()
            }
        }
        val module = StaticPolicyRule(
            "module.service-scope",
            MandatoryPolicyKind.CATEGORY,
            Version(1),
        ) { PolicyDecision.ALLOW to emptySet() }
        return CompositePolicyGate(global, listOf(module))
    }

    private fun textField(registry: QuestModuleRegistry, id: String, field: String): String {
        val definition = registry.resolveDefinition(id, Version(1))
        return (definition.payload.getValue(field) as app.minimapa.core.domain.FieldValue.Text).value
    }
}
