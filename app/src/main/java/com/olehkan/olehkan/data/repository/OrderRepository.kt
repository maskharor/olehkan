package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderStatus

/**
 * Kontrak untuk sumber data pesanan.
 *
 * Phase 5 (sekarang): implementasi = [DummyOrderRepository]
 * Phase 7 (nanti):    implementasi = [SupabaseOrderRepository]
 */
interface OrderRepository {

    /** Ambil semua pesanan. */
    suspend fun getOrders(): List<Order>

    /** Ambil pesanan berdasarkan status (tab filter). */
    suspend fun getOrdersByStatus(isActive: Boolean): List<Order>

    /** Ambil pesanan berdasarkan ID (untuk detail). */
    suspend fun getOrderById(id: String): Order?

    /**
     * Simulasi checkout: buat pesanan baru dengan status MENUNGGU_PEMBAYARAN.
     * Mengembalikan Order yang berhasil dibuat.
     */
    suspend fun createOrder(productId: Int, quantity: Int): Order

    /**
     * Ubah status pesanan (misal bayar → DALAM_PENGIRIMAN).
     * Mengembalikan Order yang di-update.
     */
    suspend fun updateStatus(orderId: String, newStatus: OrderStatus): Order?
}