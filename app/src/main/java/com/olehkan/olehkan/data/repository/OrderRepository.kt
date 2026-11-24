
package com.olehkan.olehkan.data.repository

data class OrderItemDraft(
    val productId: String,
    val productName: String,
    val unitPrice: Long,
    val quantity: Int
) {
    val subtotal: Long
        get() = unitPrice * quantity
}
