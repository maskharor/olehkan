package com.olehkan.olehkan.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.OrderStatus
import com.olehkan.olehkan.data.repository.DummyOrderRepository
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.data.repository.OrderRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.coroutines.cancellation.CancellationException

class CheckoutViewModel(
    private val productId: Int,
    private val orderId: String? = null,   // Jika null = order baru (dari Detail)
    private val productRepo: ProductRepository = DummyProductRepository(),
    private val orderRepo: OrderRepository = DummyOrderRepository, // object singleton
) : ViewModel() {

    private val _uiState = MutableStateFlow(CheckoutUiState(isLoading = true))
    val uiState: StateFlow<CheckoutUiState> = _uiState.asStateFlow()

    init {
        loadProduct()
    }

    fun onPaymentMethodSelected(method: PaymentMethod) {
        _uiState.update { it.copy(paymentMethod = method) }
    }

    fun onPayClick() {
        val state = _uiState.value
        val product = state.product ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true, errorMessage = null) }
            try {
                if (orderId != null) {
                    // Alur 2: Bayar Order Lama -> Update status di tempat
                    orderRepo.updateStatus(orderId, OrderStatus.DALAM_PENGIRIMAN)
                } else {
                    // Alur 1: Buat Order Baru (dari Detail)
                    orderRepo.createOrder(productId = product.id, quantity = state.quantity)
                }
                _uiState.update { it.copy(isSubmitting = false, isOrderPlaced = true) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isSubmitting = false, errorMessage = e.message ?: "Gagal memproses pesanan")
                }
            }
        }
    }

    fun onRetry() = loadProduct()

    private fun loadProduct() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val product = productRepo.getProductById(productId)
                if (product == null) {
                    _uiState.update { it.copy(isLoading = false, errorMessage = "Produk tidak ditemukan") }
                } else {
                    _uiState.update { it.copy(isLoading = false, product = product) }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat produk")
                }
            }
        }
    }
}