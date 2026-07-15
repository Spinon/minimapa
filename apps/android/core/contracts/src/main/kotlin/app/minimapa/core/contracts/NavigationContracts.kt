package app.minimapa.core.contracts

import java.time.Instant

data class GeoPoint(
    val latitude: Double,
    val longitude: Double,
) {
    init {
        require(latitude in -90.0..90.0) { "Latitude must be between -90 and 90" }
        require(longitude in -180.0..180.0) { "Longitude must be between -180 and 180" }
    }
}

data class RoutePlan(
    val points: List<GeoPoint>,
) {
    init {
        require(points.size >= 2) { "A route requires at least two points" }
    }
}

enum class NavigationState {
    EN_ROUTE,
    PAUSED,
    OFF_ROUTE,
    REROUTING,
    DEGRADED,
    ARRIVED,
}

data class NavigationFrame(
    val position: GeoPoint,
    val state: NavigationState,
    val capturedAt: Instant,
    val routeProgress: Double,
    val horizontalAccuracyMeters: Double,
) {
    init {
        require(routeProgress in 0.0..1.0) { "Route progress must be between 0 and 1" }
        require(horizontalAccuracyMeters >= 0) { "Accuracy cannot be negative" }
    }
}

interface NavigationProvider {
    fun start(route: RoutePlan): ProviderResult<String>
    fun nextFrame(sessionId: String): ProviderResult<NavigationFrame>
    fun pause(sessionId: String): ProviderResult<Unit>
    fun resume(sessionId: String): ProviderResult<Unit>
}
