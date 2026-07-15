package app.minimapa.core.config

enum class AppEnvironment {
  LOCAL,
  STAGING,
  PRODUCTION,
}

enum class IntegrationMode {
  MOCK,
  SANDBOX,
  PRODUCTION,
}

data class SimulationConfiguration(
  val enabled: Boolean,
  val scenarioSeed: String,
) {
  init {
    require(scenarioSeed.isNotBlank()) { "Simulation scenario seed cannot be blank." }
  }
}

data class RuntimeConfiguration(
  val appEnvironment: AppEnvironment,
  val integrationMode: IntegrationMode,
  val allowBillableRequests: Boolean,
  val simulation: SimulationConfiguration,
) {
  init {
    require(appEnvironment != AppEnvironment.LOCAL || integrationMode != IntegrationMode.PRODUCTION) {
      "Local builds cannot use production integrations."
    }
    require(integrationMode != IntegrationMode.PRODUCTION || allowBillableRequests) {
      "Production integrations require explicit billable-request authorization."
    }
    require(appEnvironment != AppEnvironment.PRODUCTION || !simulation.enabled) {
      "Production builds cannot run with simulation enabled."
    }
  }

  companion object {
    val SafeLocal =
      RuntimeConfiguration(
        appEnvironment = AppEnvironment.LOCAL,
        integrationMode = IntegrationMode.MOCK,
        allowBillableRequests = false,
        simulation = SimulationConfiguration(enabled = true, scenarioSeed = "rio-claro-local"),
      )

    fun from(values: Map<String, String>): RuntimeConfiguration =
      RuntimeConfiguration(
        appEnvironment = values.enumValue("APP_ENV", AppEnvironment.LOCAL),
        integrationMode = values.enumValue("INTEGRATION_MODE", IntegrationMode.MOCK),
        allowBillableRequests = values.booleanValue("ALLOW_BILLABLE_REQUESTS", false),
        simulation =
          SimulationConfiguration(
            enabled = values.booleanValue("SIMULATION_ENABLED", true),
            scenarioSeed = values["SIMULATION_SCENARIO_SEED"] ?: "rio-claro-local",
          ),
      )
  }
}

private inline fun <reified T : Enum<T>> Map<String, String>.enumValue(
  key: String,
  default: T,
): T =
  this[key]
    ?.trim()
    ?.uppercase()
    ?.let { raw -> enumValues<T>().firstOrNull { it.name == raw } }
    ?: default

private fun Map<String, String>.booleanValue(
  key: String,
  default: Boolean,
): Boolean =
  when (this[key]?.trim()?.lowercase()) {
    "true" -> true
    "false" -> false
    else -> default
  }
