package app.minimapa.core.config

enum class ExternalService {
  MAPS,
  NAVIGATION,
  IDENTITY,
  PAYMENTS,
  NOTIFICATIONS,
  SUPPORT,
}

class BillableRequestBlockedException(
  service: ExternalService,
) : IllegalStateException("Billable request to $service is blocked by CostGuard.")

class CostGuard(
  private val configuration: RuntimeConfiguration,
) {
  fun check(
    service: ExternalService,
    potentiallyBillable: Boolean,
  ) {
    if (potentiallyBillable && !configuration.allowBillableRequests) {
      throw BillableRequestBlockedException(service)
    }
  }
}
