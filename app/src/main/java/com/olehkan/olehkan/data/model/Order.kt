
package com.olehkan.olehkan.data.model

data class Order(
    val id: String,
    val userId: String,
    val orderNumber: String,
    val status: String = OrderStatus.PENDING,
    val totalAmount: Long,
    val recipientName: String,
    val phoneNumber: String,
    val shippingAddress: String,
    val notes: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    val items: List<OrderItem> = emptyList()
)

object OrderStatus {
    const val PENDING = "PENDING"
    const val PROCESSING = "PROCESSING"
    const val COMPLETED = "COMPLETED"
    const val CANCELLED = "CANCELLED"
}
