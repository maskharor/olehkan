package com.olehkan.olehkan.data.model

/**
 * Model pesanan.
 *
 * `product` di-embed langsung (bukan `productId`) untuk penyederhanaan
 * di tahap dummy. Di Supabase nanti, ambil via join:
 * `order_items` join `products`.
 */
data class Order(
    val id: String,                       // "ORD-001" (string, nanti UUID)
    val product: Product,
    val quantity: Int = 1,
    val status: OrderStatus,
    val orderDate: String,                // "12 Jun 2025" (nanti Timestamp)
    val totalPrice: Int,                  // harga total
    val shippingCost: Int = 12_000,       // ongkir flat
    val hasTracking: Boolean = false,     // tampilkan progress bar (Dalam Pengiriman)
    val needsPayment: Boolean = false,    // tampilkan tombol "Bayar sekarang"
) {
    /** Format total ke Rupiah */
    fun formattedTotal(): String = formatRupiah(totalPrice)

    /** Format subtotal produk (sebelum ongkir) */
    fun formattedSubtotal(): String = formatRupiah(product.price * quantity)

    /** Format ongkir */
    fun formattedShipping(): String = formatRupiah(shippingCost)

    companion object {
        private fun formatRupiah(value: Int): String {
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