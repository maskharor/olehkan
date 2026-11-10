package com.olehkan.olehkan.ui.screens.checkout

import com.olehkan.olehkan.data.model.Product

/**
 * Satu UI state untuk CheckoutScreen.
 */
data class CheckoutUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val errorMessage: String? = null,

    // Data produk yang di-checkout
    val product: Product? = null,
    val quantity: Int = 1,
    val shippingCost: Int = 12_000,

    // Metode pembayaran
    val paymentMethod: PaymentMethod = PaymentMethod.BCA_VA,

    // Alamat (hardcode — Figma)
    val addressName: String = "Rumah • Nadia",
    val addressDetail: String = "Jl. Melati No. 24, Bandung\nJawa Barat, 40123",

    // Sukses
    val isOrderPlaced: Boolean = false,
) {
    /** Subtotal (harga × qty) */
    val subtotal: Int get() = (product?.price ?: 0) * quantity

    /** Total akhir */
    val total: Int get() = subtotal + shippingCost

    /** Format Rupiah */
    fun formattedTotal(): String = formatRupiah(total)
    fun formattedSubtotal(): String = formatRupiah(subtotal)
    fun formattedShipping(): String = formatRupiah(shippingCost)
    fun formattedUnitPrice(): String = formatRupiah(product?.price ?: 0)

    companion object {
        fun formatRupiah(value: Int): String {
            val s = value.toString()
            if (s.length <= 3) return "Rp$s"
            val result = StringBuilder()
            var count = 0
            for (i in s.length - 1 downTo 0) {
                result.append(s[i])
                count++
                if (count == 3 && i != 0) {
                    result.append('.')
                    count = 0
                }
            }
            return "Rp${result.reverse()}"
        }
    }
}

/**
 * Metode pembayaran.
 */
enum class PaymentMethod(
    val displayLabel: String,
    val code: String,
) {
    BCA_VA("BCA Virtual Account", "VA"),
    GOPAY("GoPay", "GP"),
    CARD("Kartu debit / kredit", "CC"),
}