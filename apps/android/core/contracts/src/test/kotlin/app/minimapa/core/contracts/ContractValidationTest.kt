package app.minimapa.core.contracts

import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ContractValidationTest {
    @Test
    fun `payment total keeps principal and operational cost explicit`() {
        val request = PaymentRequest(
            questId = "quest-1",
            payerId = "payer-1",
            beneficiaryId = "executor-1",
            questPrincipal = Money(2_000),
            operationalCost = Money(160),
            idempotencyKey = "payment-1",
        )

        assertEquals(Money(2_160), request.chargedTotal)
        assertEquals(Money(2_000), request.questPrincipal)
        assertEquals(Money(160), request.operationalCost)
    }

    @Test
    fun `route rejects an invalid shape`() {
        assertThrows(IllegalArgumentException::class.java) {
            RoutePlan(listOf(GeoPoint(-22.4, -47.5)))
        }
    }
}
