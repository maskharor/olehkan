package com.olehkan.olehkan.data.model

/**
 * Status pesanan.
 * - MENUNGGU_PEMBAYARAN: belum dibayar
 * - DALAM_PENGIRIMAN: sudah dibayar, sedang dikirim
 * - SELESAI: pesanan diterima
 * - DIBATALKAN: dibatalkan (opsional)
 */
enum class OrderStatus {
    MENUNGGU_PEMBAYARAN,
    DALAM_PENGIRIMAN,
    SELESAI,
    DIBATALKAN;

    /**
     * Label tampilan untuk UI (Bahasa Indonesia).
     */
    val displayLabel: String
        get() = when (this) {
            MENUNGGU_PEMBAYARAN -> "Menunggu pembayaran"
            DALAM_PENGIRIMAN -> "Dalam pengiriman"
            SELESAI -> "Pesanan selesai"
            DIBATALKAN -> "Dibatalkan"
        }

    /**
     * Tab yang cocok untuk status ini.
     * true = tab "Sedang diproses", false = tab "Selesai".
     */
    val isActive: Boolean
        get() = this == MENUNGGU_PEMBAYARAN || this == DALAM_PENGIRIMAN
}