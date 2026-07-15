package app.minimapa.testing.simulation

import app.minimapa.core.contracts.AppClock
import java.time.Duration
import java.time.Instant

class ControlledClock(initialTime: Instant) : AppClock {
    private var currentTime = initialTime

    @Synchronized
    override fun now(): Instant = currentTime

    @Synchronized
    fun advance(duration: Duration): Instant {
        require(!duration.isNegative) { "Clock cannot move backwards" }
        currentTime = currentTime.plus(duration)
        return currentTime
    }

    @Synchronized
    fun set(instant: Instant) {
        require(!instant.isBefore(currentTime)) { "Clock cannot move backwards" }
        currentTime = instant
    }
}
