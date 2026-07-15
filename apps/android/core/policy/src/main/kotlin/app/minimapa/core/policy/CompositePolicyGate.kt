package app.minimapa.core.policy

class CompositePolicyGate(
    globalRules: Collection<PolicyRule>,
    moduleRules: Collection<PolicyRule> = emptyList(),
) : PolicyGate {
    private val globalRules = globalRules.toList()
    private val moduleRules = moduleRules.toList()
    private val allRules = this.globalRules + this.moduleRules

    init {
        val missing = MandatoryPolicyKind.entries - this.globalRules.map { it.kind }.toSet()
        require(missing.isEmpty()) { "Missing mandatory global policies: ${missing.joinToString()}" }
        require(allRules.map { it.id }.distinct().size == allRules.size) {
            "Policy rule ids must be unique across global and module rules"
        }
        require(allRules.all { it.id.matches(Regex("[a-z][a-z0-9.-]{2,95}")) }) {
            "Policy rule ids must be canonical"
        }
    }

    override fun evaluate(context: PolicyContext): PolicyEvaluation {
        val evaluations = allRules.map { rule ->
            rule.evaluate(context).also {
                require(it.ruleId == rule.id && it.kind == rule.kind && it.version == rule.version) {
                    "Policy rule ${rule.id} returned forged metadata"
                }
                require(it.reasonCodes.isNotEmpty() || it.decision == PolicyDecision.ALLOW) {
                    "Non-allow policy decisions require a reason code"
                }
            }
        }
        val decision = when {
            evaluations.any { it.decision == PolicyDecision.DENY } -> PolicyDecision.DENY
            evaluations.any { it.decision == PolicyDecision.REQUIRE_REVIEW } -> PolicyDecision.REQUIRE_REVIEW
            else -> PolicyDecision.ALLOW
        }
        val version = allRules
            .sortedBy { it.id }
            .joinToString("|") { "${it.id}@${it.version.major}.${it.version.minor}" }

        return PolicyEvaluation(
            decision = decision,
            policySetVersion = version,
            reasonCodes = evaluations.flatMapTo(linkedSetOf()) { it.reasonCodes },
            rules = evaluations,
        )
    }
}

class StaticPolicyRule(
    override val id: String,
    override val kind: MandatoryPolicyKind,
    override val version: app.minimapa.core.domain.Version,
    private val evaluator: (PolicyContext) -> Pair<PolicyDecision, Set<String>>,
) : PolicyRule {
    override fun evaluate(context: PolicyContext): RuleEvaluation {
        val (decision, reasons) = evaluator(context)
        return RuleEvaluation(id, kind, version, decision, reasons)
    }
}
