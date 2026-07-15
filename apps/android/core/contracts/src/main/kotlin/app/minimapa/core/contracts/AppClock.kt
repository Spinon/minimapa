package app.minimapa.core.contracts

import java.time.Instant

fun interface AppClock {
    fun now(): Instant
}
