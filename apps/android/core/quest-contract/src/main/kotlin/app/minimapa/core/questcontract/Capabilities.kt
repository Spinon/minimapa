package app.minimapa.core.questcontract

@JvmInline
value class CapabilityId(val value: String) {
    init {
        require(value.matches(Regex("[A-Z][A-Z0-9_]{2,63}"))) { "Capability id must be canonical" }
    }
}

object StandardCapabilities {
    val LOCATION = CapabilityId("LOCATION")
    val ROUTE = CapabilityId("ROUTE")
    val QUOTE = CapabilityId("QUOTE")
    val SCHEDULING = CapabilityId("SCHEDULING")
    val MATERIALS = CapabilityId("MATERIALS")
    val EVIDENCE = CapabilityId("EVIDENCE")
    val REMOTE_EXECUTION = CapabilityId("REMOTE_EXECUTION")
    val MULTI_STOP = CapabilityId("MULTI_STOP")
    val LIVE_TRACKING = CapabilityId("LIVE_TRACKING")

    val all = setOf(
        LOCATION,
        ROUTE,
        QUOTE,
        SCHEDULING,
        MATERIALS,
        EVIDENCE,
        REMOTE_EXECUTION,
        MULTI_STOP,
        LIVE_TRACKING,
    )
}

class CapabilityCatalog(supported: Set<CapabilityId> = StandardCapabilities.all) {
    private val supported = supported.toSet()

    fun supports(capabilities: Set<CapabilityId>): Boolean = supported.containsAll(capabilities)

    fun unsupported(capabilities: Set<CapabilityId>): Set<CapabilityId> = capabilities - supported
}
