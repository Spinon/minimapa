package app.minimapa.testing.simulation

import app.minimapa.core.contracts.GeoPoint
import app.minimapa.core.contracts.NavigationFrame
import app.minimapa.core.contracts.NavigationProvider
import app.minimapa.core.contracts.NavigationState
import app.minimapa.core.contracts.ProviderResult
import app.minimapa.core.contracts.RoutePlan
import java.time.Duration

enum class NavigationEvent {
    NORMAL,
    DEGRADED_GPS,
    OFF_ROUTE,
    REROUTE,
}

class SimulatedNavigationProvider(
    private val clock: ControlledClock,
    events: List<NavigationEvent> = emptyList(),
    private val frameInterval: Duration = Duration.ofSeconds(5),
) : NavigationProvider {
    private data class Session(
        val route: RoutePlan,
        var pointIndex: Int = 0,
        var paused: Boolean = false,
        var inBackground: Boolean = false,
    )

    private val events = ArrayDeque(events)
    private val sessions = mutableMapOf<String, Session>()
    private var sequence = 0

    @Synchronized
    override fun start(route: RoutePlan): ProviderResult<String> {
        val id = "sim-navigation-${++sequence}"
        sessions[id] = Session(route)
        return ProviderResult.Success(id)
    }

    @Synchronized
    override fun nextFrame(sessionId: String): ProviderResult<NavigationFrame> {
        val session = sessions[sessionId] ?: return unknownSession(sessionId)
        clock.advance(frameInterval)

        if (session.paused) {
            return ProviderResult.Success(session.frame(NavigationState.PAUSED, accuracy = 5.0))
        }

        return when (events.removeFirstOrNull() ?: NavigationEvent.NORMAL) {
            NavigationEvent.DEGRADED_GPS -> {
                session.advance()
                ProviderResult.Success(session.frame(NavigationState.DEGRADED, accuracy = 80.0))
            }
            NavigationEvent.OFF_ROUTE -> ProviderResult.Success(
                session.frame(
                    state = NavigationState.OFF_ROUTE,
                    accuracy = 12.0,
                    position = session.position.copy(
                        latitude = session.position.latitude + 0.001,
                        longitude = session.position.longitude + 0.001,
                    ),
                ),
            )
            NavigationEvent.REROUTE -> ProviderResult.Success(session.frame(NavigationState.REROUTING, accuracy = 8.0))
            NavigationEvent.NORMAL -> {
                session.advance()
                val state = if (session.pointIndex == session.route.points.lastIndex) {
                    NavigationState.ARRIVED
                } else {
                    NavigationState.EN_ROUTE
                }
                ProviderResult.Success(session.frame(state, accuracy = 5.0))
            }
        }
    }

    @Synchronized
    override fun pause(sessionId: String): ProviderResult<Unit> =
        sessions[sessionId]?.let {
            it.paused = true
            ProviderResult.Success(Unit)
        } ?: unknownSession(sessionId)

    @Synchronized
    override fun resume(sessionId: String): ProviderResult<Unit> =
        sessions[sessionId]?.let {
            it.paused = false
            ProviderResult.Success(Unit)
        } ?: unknownSession(sessionId)

    @Synchronized
    fun setAppInBackground(sessionId: String, inBackground: Boolean): ProviderResult<Unit> =
        sessions[sessionId]?.let {
            it.inBackground = inBackground
            ProviderResult.Success(Unit)
        } ?: unknownSession(sessionId)

    private val Session.position: GeoPoint
        get() = route.points[pointIndex]

    private fun Session.advance() {
        pointIndex = (pointIndex + 1).coerceAtMost(route.points.lastIndex)
    }

    private fun Session.frame(
        state: NavigationState,
        accuracy: Double,
        position: GeoPoint = this.position,
    ) = NavigationFrame(
        position = position,
        state = state,
        capturedAt = clock.now(),
        routeProgress = pointIndex.toDouble() / route.points.lastIndex,
        horizontalAccuracyMeters = accuracy,
    )

    private fun <T> unknownSession(sessionId: String): ProviderResult<T> =
        ProviderResult.PermanentFailure(
            code = "UNKNOWN_NAVIGATION_SESSION",
            message = "Navigation session $sessionId does not exist",
        )
}
