
package com.olehkan.olehkan.data.model

data class Payment(
    val id: String,
    val orderId: String,
    val userId: String,
    val paymentMethod: String,
    val amount: Long,
    val status: String = PaymentStatus.PENDING,
    val transactionReference: String,
    val paidAt: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

object PaymentMethod {
    const val E_WALLET = "E_WALLET"
    const val BANK_TRANSFER = "BANK_TRANSFER"
    const val QRIS = "QRIS"
}

object PaymentStatus {
    const val PENDING = "PENDING"
    const val PAID = "PAID"
    const val FAILED = "FAILED"
    const val CANCELLED = "CANCELLED"
}
