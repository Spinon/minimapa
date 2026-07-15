package app.minimapa.testing.simulation

enum class ScriptedOutcome {
    SUCCESS,
    RETRYABLE_FAILURE,
    PERMANENT_FAILURE,
}

class OutcomeScript(outcomes: List<ScriptedOutcome> = emptyList()) {
    private val remaining = ArrayDeque(outcomes)

    @Synchronized
    fun next(): ScriptedOutcome = remaining.removeFirstOrNull() ?: ScriptedOutcome.SUCCESS
}
