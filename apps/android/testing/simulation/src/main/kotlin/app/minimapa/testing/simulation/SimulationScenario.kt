package app.minimapa.testing.simulation

import app.minimapa.core.config.IntegrationMode
import app.minimapa.core.config.RuntimeConfiguration
import app.minimapa.core.contracts.GeoPoint
import app.minimapa.core.contracts.IdentityDecision
import app.minimapa.core.contracts.RoutePlan
import java.time.Instant
import kotlin.random.Random

enum class PersonaRole { REQUESTER, EXECUTOR, ADMIN }

data class SimulationPersona(
    val id: String,
    val displayName: String,
    val roles: Set<PersonaRole>,
    val identityDecision: IdentityDecision,
)

enum class SimulationFeature {
    LOCAL_DELIVERY,
    TURN_BY_TURN,
    PAYMENTS,
    SUPPORT,
}

data class SimulationScenario(
    val seed: String,
    val initialTime: Instant,
    val personas: List<SimulationPersona>,
    val route: RoutePlan,
    val enabledFeatures: Set<SimulationFeature>,
)

object ScenarioCatalog {
    fun rioClaro(seed: String): SimulationScenario {
        val random = Random(seed.hashCode())
        val requesterNames = listOf("Ayla", "Cecilia", "Lina")
        val executorNames = listOf("Bento", "Davi", "Ravi")

        return SimulationScenario(
            seed = seed,
            initialTime = Instant.parse("2026-07-15T12:00:00Z"),
            personas = listOf(
                SimulationPersona(
                    id = "requester-${random.nextInt(1000, 9999)}",
                    displayName = requesterNames[random.nextInt(requesterNames.size)],
                    roles = setOf(PersonaRole.REQUESTER),
                    identityDecision = IdentityDecision.VERIFIED,
                ),
                SimulationPersona(
                    id = "executor-${random.nextInt(1000, 9999)}",
                    displayName = executorNames[random.nextInt(executorNames.size)],
                    roles = setOf(PersonaRole.EXECUTOR),
                    identityDecision = IdentityDecision.VERIFIED,
                ),
                SimulationPersona(
                    id = "unsafe-${random.nextInt(1000, 9999)}",
                    displayName = "Viajante não verificado",
                    roles = setOf(PersonaRole.REQUESTER),
                    identityDecision = IdentityDecision.REVIEW_REQUIRED,
                ),
                SimulationPersona(
                    id = "admin-local",
                    displayName = "Administrador local",
                    roles = setOf(PersonaRole.ADMIN),
                    identityDecision = IdentityDecision.VERIFIED,
                ),
            ),
            route = RoutePlan(
                points = listOf(
                    GeoPoint(-22.4102, -47.5604),
                    GeoPoint(-22.4090, -47.5578),
                    GeoPoint(-22.4071, -47.5546),
                    GeoPoint(-22.4054, -47.5512),
                ),
            ),
            enabledFeatures = SimulationFeature.entries.toSet(),
        )
    }
}

class SimulationEnvironment private constructor(
    val scenario: SimulationScenario,
    val clock: ControlledClock,
) {
    companion object {
        fun create(configuration: RuntimeConfiguration): SimulationEnvironment {
            require(configuration.simulation.enabled) { "Simulation must be enabled" }
            require(configuration.integrationMode == IntegrationMode.MOCK) {
                "Simulation only accepts mock integrations"
            }
            require(!configuration.allowBillableRequests) {
                "Simulation cannot allow billable requests"
            }

            val scenario = ScenarioCatalog.rioClaro(configuration.simulation.scenarioSeed)
            return SimulationEnvironment(scenario, ControlledClock(scenario.initialTime))
        }
    }
}
