package app.minimapa.core.questcontract

import app.minimapa.core.domain.Version

enum class SchemaPurpose {
    CREATION,
    DETAIL,
    EXECUTION,
}

data class SchemaReference(
    val id: String,
    val version: Version,
    val purpose: SchemaPurpose,
) {
    init {
        require(id.matches(Regex("[a-z][a-z0-9.-]{2,95}"))) { "Schema id must be canonical" }
    }
}

data class ModuleSchemas(
    val creation: SchemaReference,
    val detail: SchemaReference,
    val execution: SchemaReference,
) {
    init {
        require(creation.purpose == SchemaPurpose.CREATION)
        require(detail.purpose == SchemaPurpose.DETAIL)
        require(execution.purpose == SchemaPurpose.EXECUTION)
    }
}
