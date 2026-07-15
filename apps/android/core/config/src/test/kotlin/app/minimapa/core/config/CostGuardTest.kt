package app.minimapa.core.config

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class CostGuardTest {
  @Test
  fun `safe local defaults are mock-only and simulated`() {
    val configuration = RuntimeConfiguration.SafeLocal

    assertEquals(AppEnvironment.LOCAL, configuration.appEnvironment)
    assertEquals(IntegrationMode.MOCK, configuration.integrationMode)
    assertFalse(configuration.allowBillableRequests)
    assertTrue(configuration.simulation.enabled)
  }

  @Test
  fun `billable request is blocked by safe defaults`() {
    val guard = CostGuard(RuntimeConfiguration.SafeLocal)

    assertThrows(BillableRequestBlockedException::class.java) {
      guard.check(ExternalService.NAVIGATION, potentiallyBillable = true)
    }
  }

  @Test
  fun `local mock request is allowed when it cannot bill`() {
    CostGuard(RuntimeConfiguration.SafeLocal)
      .check(ExternalService.PAYMENTS, potentiallyBillable = false)
  }

  @Test
  fun `production integration requires explicit billing authorization`() {
    assertThrows(IllegalArgumentException::class.java) {
      RuntimeConfiguration(
        appEnvironment = AppEnvironment.STAGING,
        integrationMode = IntegrationMode.PRODUCTION,
        allowBillableRequests = false,
        simulation = SimulationConfiguration(enabled = true, scenarioSeed = "test"),
      )
    }
  }

  @Test
  fun `environment values remain safe when absent or invalid`() {
    val configuration =
      RuntimeConfiguration.from(
        mapOf(
          "APP_ENV" to "unexpected",
          "ALLOW_BILLABLE_REQUESTS" to "not-a-boolean",
        ),
      )

    assertEquals(RuntimeConfiguration.SafeLocal, configuration)
  }
}
