package app.minimapa.testing.simulation

import app.minimapa.core.config.AppEnvironment
import app.minimapa.core.config.IntegrationMode
import app.minimapa.core.config.RuntimeConfiguration
import app.minimapa.core.config.SimulationConfiguration
import java.time.Duration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class SimulationEnvironmentTest {
    @Test
    fun `same seed creates the same scenario`() {
        val first = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)
        val second = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)

        assertEquals(first.scenario, second.scenario)
        assertEquals(first.clock.now(), second.clock.now())
    }

    @Test
    fun `controlled clock only advances explicitly`() {
        val environment = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)
        val initial = environment.clock.now()

        assertEquals(initial, environment.clock.now())
        assertEquals(initial.plusSeconds(30), environment.clock.advance(Duration.ofSeconds(30)))
    }

    @Test
    fun `simulation rejects sandbox integrations`() {
        val configuration = RuntimeConfiguration(
            appEnvironment = AppEnvironment.STAGING,
            integrationMode = IntegrationMode.SANDBOX,
            allowBillableRequests = false,
            simulation = SimulationConfiguration(true, "sandbox-is-not-mock"),
        )

        assertThrows(IllegalArgumentException::class.java) {
            SimulationEnvironment.create(configuration)
        }
    }
}
