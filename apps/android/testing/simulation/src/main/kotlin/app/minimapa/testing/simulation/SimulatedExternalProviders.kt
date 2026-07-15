package app.minimapa.testing.simulation

import app.minimapa.core.contracts.IdentityDecision
import app.minimapa.core.contracts.IdentityVerificationProvider
import app.minimapa.core.contracts.IdentityVerificationReceipt
import app.minimapa.core.contracts.IdentityVerificationRequest
import app.minimapa.core.contracts.NotificationProvider
import app.minimapa.core.contracts.NotificationReceipt
import app.minimapa.core.contracts.NotificationRequest
import app.minimapa.core.contracts.PaymentProvider
import app.minimapa.core.contracts.PaymentReceipt
import app.minimapa.core.contracts.PaymentRequest
import app.minimapa.core.contracts.ProviderResult
import app.minimapa.core.contracts.SupportCaseReceipt
import app.minimapa.core.contracts.SupportCaseRequest
import app.minimapa.core.contracts.SupportProvider

class SimulatedIdentityVerificationProvider(
    personas: List<SimulationPersona>,
    private val outcomes: OutcomeScript = OutcomeScript(),
) : IdentityVerificationProvider {
    private val decisions = personas.associate { it.id to it.identityDecision }
    private val receipts = mutableMapOf<String, Pair<IdentityVerificationRequest, ProviderResult<IdentityVerificationReceipt>>>()
    private var sequence = 0

    @Synchronized
    override fun verify(request: IdentityVerificationRequest): ProviderResult<IdentityVerificationReceipt> =
        idempotent(request.idempotencyKey, request, receipts) {
            outcomes.result("identity") {
                IdentityVerificationReceipt(
                    verificationId = "sim-verification-${++sequence}",
                    decision = decisions[request.subjectId] ?: IdentityDecision.REVIEW_REQUIRED,
                )
            }
        }
}

class SimulatedPaymentProvider(
    private val outcomes: OutcomeScript = OutcomeScript(),
) : PaymentProvider {
    private val receipts = mutableMapOf<String, Pair<PaymentRequest, ProviderResult<PaymentReceipt>>>()
    private var sequence = 0

    @Synchronized
    override fun createPayment(request: PaymentRequest): ProviderResult<PaymentReceipt> =
        idempotent(request.idempotencyKey, request, receipts) {
            outcomes.result("payment") {
                PaymentReceipt(
                    paymentId = "sim-payment-${++sequence}",
                    questPrincipal = request.questPrincipal,
                    operationalCost = request.operationalCost,
                    chargedTotal = request.chargedTotal,
                )
            }
        }
}

class SimulatedNotificationProvider(
    private val outcomes: OutcomeScript = OutcomeScript(),
) : NotificationProvider {
    private val receipts = mutableMapOf<String, Pair<NotificationRequest, ProviderResult<NotificationReceipt>>>()
    private var sequence = 0

    val delivered = mutableListOf<NotificationRequest>()

    @Synchronized
    override fun send(request: NotificationRequest): ProviderResult<NotificationReceipt> =
        idempotent(request.idempotencyKey, request, receipts) {
            outcomes.result("notification") {
                delivered += request
                NotificationReceipt("sim-notification-${++sequence}")
            }
        }
}

class SimulatedSupportProvider(
    private val outcomes: OutcomeScript = OutcomeScript(),
) : SupportProvider {
    private val receipts = mutableMapOf<String, Pair<SupportCaseRequest, ProviderResult<SupportCaseReceipt>>>()
    private var sequence = 0

    val openedCases = mutableListOf<SupportCaseRequest>()

    @Synchronized
    override fun openCase(request: SupportCaseRequest): ProviderResult<SupportCaseReceipt> =
        idempotent(request.idempotencyKey, request, receipts) {
            outcomes.result("support") {
                openedCases += request
                SupportCaseReceipt("sim-case-${++sequence}")
            }
        }
}

private fun <T> OutcomeScript.result(service: String, success: () -> T): ProviderResult<T> =
    when (next()) {
        ScriptedOutcome.SUCCESS -> ProviderResult.Success(success())
        ScriptedOutcome.RETRYABLE_FAILURE -> ProviderResult.RetryableFailure(
            code = "SIM_${service.uppercase()}_RETRY",
            message = "Reproducible simulated retryable failure",
        )
        ScriptedOutcome.PERMANENT_FAILURE -> ProviderResult.PermanentFailure(
            code = "SIM_${service.uppercase()}_FAILURE",
            message = "Reproducible simulated permanent failure",
        )
    }

private fun <REQUEST, RECEIPT> idempotent(
    key: String,
    request: REQUEST,
    cache: MutableMap<String, Pair<REQUEST, ProviderResult<RECEIPT>>>,
    operation: () -> ProviderResult<RECEIPT>,
): ProviderResult<RECEIPT> {
    require(key.isNotBlank()) { "Idempotency key cannot be blank" }
    val previous = cache[key]
    if (previous != null) {
        return if (previous.first == request) {
            previous.second
        } else {
            ProviderResult.PermanentFailure(
                code = "IDEMPOTENCY_CONFLICT",
                message = "The same idempotency key was used with a different request",
            )
        }
    }

    return operation().also { cache[key] = request to it }
}
