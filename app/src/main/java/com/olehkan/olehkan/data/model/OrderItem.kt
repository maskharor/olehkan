
package com.olehkan.olehkan.data.model

data class OrderItem(
    val id: String,
    val orderId: String,
    val productId: String,
    val productName: String,
    val unitPrice: Long,
    val quantity: Int,
    val subtotal: Long,
    val createdAt: String? = null
)
