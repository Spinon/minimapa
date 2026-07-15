package app.minimapa.core.contracts

data class Money(
    val amountMinor: Long,
    val currency: String = "BRL",
) {
    init {
        require(amountMinor >= 0) { "Money cannot be negative" }
        require(currency.matches(Regex("[A-Z]{3}"))) { "Currency must use ISO 4217 format" }
    }
}

enum class IdentityDecision { VERIFIED, REJECTED, REVIEW_REQUIRED }

data class IdentityVerificationRequest(
    val subjectId: String,
    val idempotencyKey: String,
)

data class IdentityVerificationReceipt(
    val verificationId: String,
    val decision: IdentityDecision,
)

fun interface IdentityVerificationProvider {
    fun verify(request: IdentityVerificationRequest): ProviderResult<IdentityVerificationReceipt>
}

data class PaymentRequest(
    val questId: String,
    val payerId: String,
    val beneficiaryId: String,
    val questPrincipal: Money,
    val operationalCost: Money,
    val idempotencyKey: String,
) {
    init {
        require(questPrincipal.currency == operationalCost.currency) { "Payment components must share a currency" }
    }

    val chargedTotal: Money
        get() = Money(questPrincipal.amountMinor + operationalCost.amountMinor, questPrincipal.currency)
}

data class PaymentReceipt(
    val paymentId: String,
    val questPrincipal: Money,
    val operationalCost: Money,
    val chargedTotal: Money,
)

fun interface PaymentProvider {
    fun createPayment(request: PaymentRequest): ProviderResult<PaymentReceipt>
}

data class NotificationRequest(
    val recipientId: String,
    val template: String,
    val idempotencyKey: String,
)

data class NotificationReceipt(val notificationId: String)

fun interface NotificationProvider {
    fun send(request: NotificationRequest): ProviderResult<NotificationReceipt>
}

data class SupportCaseRequest(
    val reporterId: String,
    val subject: String,
    val idempotencyKey: String,
)

data class SupportCaseReceipt(val caseId: String)

fun interface SupportProvider {
    fun openCase(request: SupportCaseRequest): ProviderResult<SupportCaseReceipt>
}
