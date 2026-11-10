package com.olehkan.olehkan.ui.screens.orders

import com.olehkan.olehkan.data.model.Order

/**
 * Satu UI state untuk OrdersScreen.
 * Single source of truth — semua state ada di sini.
 */
data class OrdersUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    // Data
    val allOrders: List<Order> = emptyList(),          // semua pesanan
    val activeOrders: List<Order> = emptyList(),       // tab "Sedang diproses"
    val completedOrders: List<Order> = emptyList(),    // tab "Selesai"

    // UI
    val selectedTab: OrderTab = OrderTab.ACTIVE,
) {
    /** Jumlah pesanan aktif (untuk badge tab). */
    val activeCount: Int get() = activeOrders.size

    /** Jumlah pesanan selesai. */
    val completedCount: Int get() = completedOrders.size

    /** Apakah tab aktif kosong (untuk empty state). */
    val isCurrentTabEmpty: Boolean
        get() = when (selectedTab) {
            OrderTab.ACTIVE -> activeOrders.isEmpty()
            OrderTab.COMPLETED -> completedOrders.isEmpty()
        }
}

/**
 * Enum tab di OrdersScreen.
 */
enum class OrderTab(val label: String) {
    ACTIVE("Sedang diproses"),
    COMPLETED("Selesai"),
}