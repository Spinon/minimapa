package app.minimapa.core.domain

@JvmInline
value class QuestId(val value: String) {
    init {
        require(value.isNotBlank()) { "Quest id cannot be blank" }
    }
}

@JvmInline
value class PlayerId(val value: String) {
    init {
        require(value.isNotBlank()) { "Player id cannot be blank" }
    }
}

@JvmInline
value class EventId(val value: String) {
    init {
        require(value.isNotBlank()) { "Event id cannot be blank" }
    }
}

data class Version(
    val major: Int,
    val minor: Int = 0,
) : Comparable<Version> {
    init {
        require(major >= 1) { "Major version must be positive" }
        require(minor >= 0) { "Minor version cannot be negative" }
    }

    override fun compareTo(other: Version): Int =
        compareValuesBy(this, other, Version::major, Version::minor)
}

sealed interface FieldValue {
    data class Text(val value: String) : FieldValue
    data class Number(val value: Long) : FieldValue
    data class Decimal(val value: Double) : FieldValue
    data class Flag(val value: Boolean) : FieldValue
    data class TextList(val value: List<String>) : FieldValue
}

data class TypedRequirement(
    val type: String,
    val version: Version,
    val attributes: Map<String, FieldValue>,
) {
    init {
        require(type.matches(Regex("[A-Z][A-Z0-9_]{2,63}"))) { "Requirement type must be canonical" }
    }
}
