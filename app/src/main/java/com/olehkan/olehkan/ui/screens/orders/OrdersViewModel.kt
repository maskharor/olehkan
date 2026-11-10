package com.olehkan.olehkan.ui.screens.orders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderStatus
import com.olehkan.olehkan.data.repository.DummyOrderRepository
import com.olehkan.olehkan.data.repository.OrderRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

/**
 * ViewModel untuk OrdersScreen.
 *
 * Best practice (sesuai modul):
 * - StateFlow untuk UI state
 * - _uiState private + asStateFlow()
 * - .update { } thread-safe
 * - viewModelScope.launch + try/catch
 * - Repository via constructor parameter
 */
class OrdersViewModel(
    private val repository: OrderRepository = DummyOrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OrdersUiState(isLoading = true))
    val uiState: StateFlow<OrdersUiState> = _uiState.asStateFlow()

    init {
        loadOrders()
    }

    // ============================================
    // EVENT: dari UI
    // ============================================

    /** Dipanggil saat user klik tab. */
    fun onTabSelected(tab: OrderTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /** Dipanggil saat tombol "Coba lagi" di error state. */
    fun onRetry() = loadOrders()

    /**
     * Dipanggil saat user klik "Bayar sekarang" di OrderCard.
     * Simulasi: ubah status MENUNGGU_PEMBAYARAN → DALAM_PENGIRIMAN.
     */
    fun onPayOrder(orderId: String) {
        viewModelScope.launch {
            try {
                val updated = repository.updateStatus(
                    orderId = orderId,
                    newStatus = OrderStatus.DALAM_PENGIRIMAN,
                )
                if (updated != null) {
                    loadOrders()  // reload agar UI update
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(errorMessage = e.message) }
            }
        }
    }

    // ============================================
    // Internal
    // ============================================

    private fun loadOrders() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val all = repository.getOrders()
                applyOrders(all)
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat pesanan")
                }
            }
        }
    }

    private fun applyOrders(orders: List<Order>) {
        _uiState.update {
            it.copy(
                isLoading = false,
                allOrders = orders,
                activeOrders = orders.filter { order -> order.status.isActive },
                completedOrders = orders.filter { order -> !order.status.isActive },
            )
        }
    }
}