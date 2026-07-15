package app.minimapa.testing.simulation

import app.minimapa.core.config.RuntimeConfiguration
import app.minimapa.core.contracts.NavigationFrame
import app.minimapa.core.contracts.NavigationState
import app.minimapa.core.contracts.ProviderResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedNavigationProviderTest {
    @Test
    fun `route simulates degraded gps off-route reroute background and arrival`() {
        val environment = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)
        val provider = SimulatedNavigationProvider(
            clock = environment.clock,
            events = listOf(
                NavigationEvent.DEGRADED_GPS,
                NavigationEvent.OFF_ROUTE,
                NavigationEvent.REROUTE,
            ),
        )
        val sessionId = (provider.start(environment.scenario.route) as ProviderResult.Success).value

        val degraded = provider.nextFrame(sessionId).frame()
        val offRoute = provider.nextFrame(sessionId).frame()
        val rerouting = provider.nextFrame(sessionId).frame()
        provider.setAppInBackground(sessionId, true)
        val backgroundProgress = provider.nextFrame(sessionId).frame()
        provider.setAppInBackground(sessionId, false)
        val arrived = provider.nextFrame(sessionId).frame()

        assertEquals(NavigationState.DEGRADED, degraded.state)
        assertEquals(80.0, degraded.horizontalAccuracyMeters, 0.0)
        assertEquals(NavigationState.OFF_ROUTE, offRoute.state)
        assertEquals(NavigationState.REROUTING, rerouting.state)
        assertTrue(backgroundProgress.routeProgress > degraded.routeProgress)
        assertEquals(NavigationState.ARRIVED, arrived.state)
        assertEquals(1.0, arrived.routeProgress, 0.0)
    }

    @Test
    fun `paused route does not advance until resumed`() {
        val environment = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)
        val provider = SimulatedNavigationProvider(environment.clock)
        val sessionId = (provider.start(environment.scenario.route) as ProviderResult.Success).value

        provider.pause(sessionId)
        val paused = provider.nextFrame(sessionId).frame()
        provider.resume(sessionId)
        val resumed = provider.nextFrame(sessionId).frame()

        assertEquals(NavigationState.PAUSED, paused.state)
        assertEquals(0.0, paused.routeProgress, 0.0)
        assertEquals(NavigationState.EN_ROUTE, resumed.state)
        assertTrue(resumed.routeProgress > paused.routeProgress)
    }

    private fun ProviderResult<NavigationFrame>.frame(): NavigationFrame =
        (this as ProviderResult.Success).value
}
