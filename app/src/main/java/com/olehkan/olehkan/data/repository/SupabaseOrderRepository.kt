
package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderItem
import com.olehkan.olehkan.data.model.OrderStatus
import com.olehkan.olehkan.data.network.SupabaseHttp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SupabaseOrderRepository(
    private val auth: SupabaseAuthRepository
) {

    private fun currentUserId(): String {
        return auth.session.value.profile?.id
            ?: throw IllegalStateException("Silakan login kembali.")
    }

    suspend fun getOrders(): List<Order> {
        val userId = currentUserId()

        val response = auth.authorized(
            method = "GET",
            path = "/rest/v1/orders?user_id=eq.${SupabaseHttp.encode(userId)}" +
                    "&select=*&order=created_at.desc"
        )

        val rows = JSONArray(response)
        val orders = mutableListOf<Order>()

        for (i in 0 until rows.length()) {
            val order = parseOrder(rows.getJSONObject(i))
            val items = getOrderItems(order.id)
            orders.add(order.copy(items = items))
        }

        return orders
    }

    suspend fun getOrderById(orderId: String): Order? {
        val userId = currentUserId()

        val response = auth.authorized(
            method = "GET",
            path = "/rest/v1/orders?id=eq.${SupabaseHttp.encode(orderId)}" +
                    "&user_id=eq.${SupabaseHttp.encode(userId)}&select=*"
        )

        val rows = JSONArray(response)
        if (rows.length() == 0) return null

        val order = parseOrder(rows.getJSONObject(0))
        return order.copy(items = getOrderItems(order.id))
    }

    suspend fun createOrder(
        recipientName: String,
        phoneNumber: String,
        shippingAddress: String,
        notes: String?,
        items: List<OrderItemDraft>
    ): Order {
        val userId = currentUserId()

        require(recipientName.isNotBlank()) {
            "Nama penerima wajib diisi."
        }
        require(phoneNumber.isNotBlank()) {
            "Nomor telepon wajib diisi."
        }
        require(shippingAddress.isNotBlank()) {
            "Alamat pengiriman wajib diisi."
        }
        require(items.isNotEmpty()) {
            "Pesanan harus memiliki minimal satu produk."
        }

        items.forEach { item ->
            require(item.productId.isNotBlank() && item.productName.isNotBlank()) {
                "Data produk tidak valid."
            }
            require(item.quantity > 0) {
                "Jumlah produk harus lebih dari nol."
            }
            require(item.unitPrice >= 0) {
                "Harga produk tidak valid."
            }
            require(
                item.unitPrice == 0L ||
                        item.quantity.toLong() <= Long.MAX_VALUE / item.unitPrice
            ) {
                "Subtotal produk terlalu besar."
            }
        }

        var totalAmount = 0L
        items.forEach { item ->
            totalAmount = Math.addExact(totalAmount, item.subtotal)
        }

        val orderNumber =
            "OK-${System.currentTimeMillis()}-${UUID.randomUUID().toString().take(8)}"

        val orderBody = JSONObject()
            .put("user_id", userId)
            .put("order_number", orderNumber)
            .put("status", OrderStatus.PENDING)
            .put("total_amount", totalAmount)
            .put("recipient_name", recipientName.trim())
            .put("phone_number", phoneNumber.trim())
            .put("shipping_address", shippingAddress.trim())
            .put(
                "notes",
                notes?.trim()?.takeIf { it.isNotBlank() } ?: JSONObject.NULL
            )

        val orderResponse = auth.authorized(
            method = "POST",
            path = "/rest/v1/orders",
            body = orderBody.toString(),
            headers = mapOf("Prefer" to "return=representation")
        )

        val orderRows = JSONArray(orderResponse)

        check(orderRows.length() > 0) {
            "Pesanan gagal dibuat. Data tidak dikembalikan server."
        }

        val createdOrder = parseOrder(orderRows.getJSONObject(0))

        try {
            val itemRows = JSONArray()

            items.forEach { item ->
                itemRows.put(
                    JSONObject()
                        .put("order_id", createdOrder.id)
                        .put("product_id", item.productId)
                        .put("product_name", item.productName)
                        .put("unit_price", item.unitPrice)
                        .put("quantity", item.quantity)
                        .put("subtotal", item.subtotal)
                )
            }

            auth.authorized(
                method = "POST",
                path = "/rest/v1/order_items",
                body = itemRows.toString(),
                headers = mapOf("Prefer" to "return=minimal")
            )

        } catch (e: Exception) {
            try {
                auth.authorized(
                    method = "DELETE",
                    path = "/rest/v1/orders?id=eq.${SupabaseHttp.encode(createdOrder.id)}"
                )
            } catch (rollbackError: Exception) {
                e.addSuppressed(rollbackError)
            }

            throw e
        }

        return getOrderById(createdOrder.id)
            ?: createdOrder
    }

    suspend fun cancelOrder(orderId: String) {
        val userId = currentUserId()

        val body = JSONObject()
            .put("status", OrderStatus.CANCELLED)
            .toString()

        val response = auth.authorized(
            method = "PATCH",
            path = "/rest/v1/orders?id=eq.${SupabaseHttp.encode(orderId)}" +
                    "&user_id=eq.${SupabaseHttp.encode(userId)}" +
                    "&status=eq.${OrderStatus.PENDING}",
            body = body,
            headers = mapOf("Prefer" to "return=representation")
        )

        check(JSONArray(response).length() > 0) {
            "Pesanan tidak dapat dibatalkan. Pastikan statusnya masih menunggu."
        }
    }

    private suspend fun getOrderItems(orderId: String): List<OrderItem> {
        val response = auth.authorized(
            method = "GET",
            path = "/rest/v1/order_items?order_id=eq.${SupabaseHttp.encode(orderId)}" +
                    "&select=*&order=created_at.asc"
        )

        val rows = JSONArray(response)

        return List(rows.length()) { index ->
            parseOrderItem(rows.getJSONObject(index))
        }
    }

    private fun parseOrder(json: JSONObject): Order {
        return Order(
            id = json.getString("id"),
            userId = json.getString("user_id"),
            orderNumber = json.getString("order_number"),
            status = json.optString("status", OrderStatus.PENDING),
            totalAmount = json.getString("total_amount").toDouble().toLong(),
            recipientName = json.optString("recipient_name"),
            phoneNumber = json.optString("phone_number"),
            shippingAddress = json.optString("shipping_address"),
            notes = nullableString(json, "notes"),
            createdAt = nullableString(json, "created_at"),
            updatedAt = nullableString(json, "updated_at")
        )
    }

    private fun parseOrderItem(json: JSONObject): OrderItem {
        return OrderItem(
            id = json.getString("id"),
            orderId = json.getString("order_id"),
            productId = json.getString("product_id"),
            productName = json.optString("product_name"),
            unitPrice = json.getString("unit_price").toDouble().toLong(),
            quantity = json.getInt("quantity"),
            subtotal = json.getString("subtotal").toDouble().toLong(),
            createdAt = nullableString(json, "created_at")
        )
    }

    private fun nullableString(json: JSONObject, key: String): String? {
        return if (json.isNull(key)) {
            null
        } else {
            json.optString(key).takeUnless {
                it.isBlank() || it == "null"
            }
        }
    }
}
