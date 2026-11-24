
package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Payment
import com.olehkan.olehkan.data.model.PaymentMethod
import com.olehkan.olehkan.data.model.PaymentStatus
import com.olehkan.olehkan.data.network.SupabaseHttp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class SupabasePaymentRepository(
    private val auth: SupabaseAuthRepository
) {

    private fun currentUserId(): String {
        return auth.session.value.profile?.id
            ?: throw IllegalStateException("Silakan login kembali.")
    }

    suspend fun getPaymentByOrderId(orderId: String): Payment? {
        val userId = currentUserId()

        val response = auth.authorized(
            method = "GET",
            path = "/rest/v1/payments?order_id=eq.${SupabaseHttp.encode(orderId)}" +
                    "&user_id=eq.${SupabaseHttp.encode(userId)}" +
                    "&select=*&order=created_at.desc&limit=1"
        )

        val rows = JSONArray(response)

        if (rows.length() == 0) return null

        return parsePayment(rows.getJSONObject(0))
    }

    suspend fun createPayment(
        orderId: String,
        paymentMethod: String
    ): Payment {
        val userId = currentUserId()

        require(
            paymentMethod == PaymentMethod.E_WALLET ||
                    paymentMethod == PaymentMethod.BANK_TRANSFER ||
                    paymentMethod == PaymentMethod.QRIS
        ) {
            "Metode pembayaran tidak valid."
        }

        // Periksa apakah pembayaran sebelumnya sudah ada.
        val existingPayment = getPaymentByOrderId(orderId)

        if (existingPayment?.status == PaymentStatus.PENDING) {
            return existingPayment
        }

        if (existingPayment?.status == PaymentStatus.PAID) {
            throw IllegalStateException("Pesanan ini sudah dibayar.")
        }

        // Ambil nominal langsung dari database, bukan dari input aplikasi.
        val orderResponse = auth.authorized(
            method = "GET",
            path = "/rest/v1/orders?id=eq.${SupabaseHttp.encode(orderId)}" +
                    "&user_id=eq.${SupabaseHttp.encode(userId)}" +
                    "&select=id,user_id,status,total_amount"
        )

        val orderRows = JSONArray(orderResponse)

        check(orderRows.length() > 0) {
            "Pesanan tidak ditemukan."
        }

        val order = orderRows.getJSONObject(0)

        check(order.optString("status") == "PENDING") {
            "Pesanan tidak dapat dibayar karena statusnya bukan menunggu pembayaran."
        }

        val amount = order.getString("total_amount").toDouble().toLong()

        val transactionReference =
            "OKPAY-${UUID.randomUUID().toString().uppercase()}"

        val body = JSONObject()
            .put("order_id", orderId)
            .put("user_id", userId)
            .put("payment_method", paymentMethod)
            .put("amount", amount)
            .put("status", PaymentStatus.PENDING)
            .put("transaction_reference", transactionReference)

        val response = auth.authorized(
            method = "POST",
            path = "/rest/v1/payments",
            body = body.toString(),
            headers = mapOf("Prefer" to "return=representation")
        )

        val paymentRows = JSONArray(response)

        check(paymentRows.length() > 0) {
            "Pembayaran gagal dibuat."
        }

        return parsePayment(paymentRows.getJSONObject(0))
    }

    suspend fun confirmPayment(paymentId: String) {
        val body = JSONObject()
            .put("p_payment_id", paymentId)
            .toString()

        auth.authorized(
            method = "POST",
            path = "/rest/v1/rpc/confirm_payment",
            body = body
        )
    }

    private fun parsePayment(json: JSONObject): Payment {
        return Payment(
            id = json.getString("id"),
            orderId = json.getString("order_id"),
            userId = json.getString("user_id"),
            paymentMethod = json.getString("payment_method"),
            amount = json.getString("amount").toDouble().toLong(),
            status = json.optString("status", PaymentStatus.PENDING),
            transactionReference = json.getString("transaction_reference"),
            paidAt = nullableString(json, "paid_at"),
            createdAt = nullableString(json, "created_at"),
            updatedAt = nullableString(json, "updated_at")
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
