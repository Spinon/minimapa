package app.minimapa.testing.simulation

import app.minimapa.core.config.RuntimeConfiguration
import app.minimapa.core.contracts.IdentityVerificationRequest
import app.minimapa.core.contracts.Money
import app.minimapa.core.contracts.NotificationRequest
import app.minimapa.core.contracts.PaymentReceipt
import app.minimapa.core.contracts.PaymentRequest
import app.minimapa.core.contracts.ProviderResult
import app.minimapa.core.contracts.SupportCaseRequest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test

class SimulatedExternalProvidersTest {
    private val environment = SimulationEnvironment.create(RuntimeConfiguration.SafeLocal)

    @Test
    fun `identity uses scenario persona and is idempotent`() {
        val persona = environment.scenario.personas.first()
        val provider = SimulatedIdentityVerificationProvider(environment.scenario.personas)
        val request = IdentityVerificationRequest(persona.id, "identity-1")

        val first = provider.verify(request)
        val repeated = provider.verify(request)

        assertSame(first, repeated)
        val receipt = (first as ProviderResult.Success).value
        assertEquals(persona.identityDecision, receipt.decision)
    }

    @Test
    fun `payment returns full principal and separate zero-margin operational cost`() {
        val provider = SimulatedPaymentProvider()
        val request = PaymentRequest(
            questId = "quest-1",
            payerId = "requester-1",
            beneficiaryId = "executor-1",
            questPrincipal = Money(3_500),
            operationalCost = Money(210),
            idempotencyKey = "payment-1",
        )

        val first = provider.createPayment(request)
        val repeated = provider.createPayment(request)
        val receipt: PaymentReceipt = (first as ProviderResult.Success).value

        assertSame(first, repeated)
        assertEquals(Money(3_500), receipt.questPrincipal)
        assertEquals(Money(210), receipt.operationalCost)
        assertEquals(Money(3_710), receipt.chargedTotal)
    }

    @Test
    fun `same payment key with changed terms is rejected`() {
        val provider = SimulatedPaymentProvider()
        val original = payment("same-key", 2_000)
        val changed = payment("same-key", 2_100)

        provider.createPayment(original)
        val conflict = provider.createPayment(changed)

        assertTrue(conflict is ProviderResult.PermanentFailure)
        assertEquals("IDEMPOTENCY_CONFLICT", (conflict as ProviderResult.PermanentFailure).code)
    }

    @Test
    fun `scripted retry and failure are reproducible`() {
        val script = OutcomeScript(
            listOf(
                ScriptedOutcome.RETRYABLE_FAILURE,
                ScriptedOutcome.PERMANENT_FAILURE,
                ScriptedOutcome.SUCCESS,
            ),
        )
        val provider = SimulatedNotificationProvider(script)

        assertTrue(provider.send(notification("1")) is ProviderResult.RetryableFailure)
        assertTrue(provider.send(notification("2")) is ProviderResult.PermanentFailure)
        assertTrue(provider.send(notification("3")) is ProviderResult.Success)
        assertEquals(1, provider.delivered.size)
    }

    @Test
    fun `support stores only successful cases`() {
        val provider = SimulatedSupportProvider()
        val request = SupportCaseRequest("requester-1", "Objeto danificado", "case-1")

        val result = provider.openCase(request)

        assertTrue(result is ProviderResult.Success)
        assertEquals(listOf(request), provider.openedCases)
    }

    private fun payment(key: String, principal: Long) = PaymentRequest(
        questId = "quest-1",
        payerId = "requester-1",
        beneficiaryId = "executor-1",
        questPrincipal = Money(principal),
        operationalCost = Money(100),
        idempotencyKey = key,
    )

    private fun notification(key: String) = NotificationRequest(
        recipientId = "user-1",
        template = "quest_updated",
        idempotencyKey = "notification-$key",
    )
}
