
package com.olehkan.olehkan.ui.screens.payment

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Payment
import com.olehkan.olehkan.data.model.PaymentMethod
import com.olehkan.olehkan.data.model.PaymentStatus
import com.olehkan.olehkan.data.repository.SupabasePaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PaymentUiState(
    val payment: Payment? = null,
    val selectedMethod: String = PaymentMethod.E_WALLET,
    val isLoading: Boolean = false,
    val isCreating: Boolean = false,
    val isConfirming: Boolean = false,
    val isPaid: Boolean = false,
    val error: String? = null
)

class PaymentViewModel(
    private val repository: SupabasePaymentRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentUiState())
    val uiState: StateFlow<PaymentUiState> = _uiState.asStateFlow()

    /**
     * Memilih metode pembayaran.
     */
    fun selectMethod(method: String) {
        if (
            method != PaymentMethod.E_WALLET &&
            method != PaymentMethod.BANK_TRANSFER &&
            method != PaymentMethod.QRIS
        ) return

        _uiState.value = _uiState.value.copy(
            selectedMethod = method,
            error = null
        )
    }

    /**
     * Mengambil pembayaran berdasarkan pesanan.
     */
    fun loadPayment(orderId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val payment = repository.getPaymentByOrderId(orderId)

                _uiState.value = _uiState.value.copy(
                    payment = payment,
                    isPaid = payment?.status == PaymentStatus.PAID,
                    isLoading = false
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Gagal memuat pembayaran."
                )
            }
        }
    }

    /**
     * Membuat transaksi pembayaran.
     */
    fun createPayment(orderId: String) {
        if (_uiState.value.isCreating) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCreating = true,
                error = null
            )

            try {
                val payment = repository.createPayment(
                    orderId = orderId,
                    paymentMethod = _uiState.value.selectedMethod
                )

                _uiState.value = _uiState.value.copy(
                    payment = payment,
                    isCreating = false,
                    isPaid = payment.status == PaymentStatus.PAID
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCreating = false,
                    error = e.message ?: "Gagal membuat pembayaran."
                )
            }
        }
    }

    /**
     * Mengonfirmasi pembayaran simulasi.
     */
    fun confirmPayment() {
        val payment = _uiState.value.payment ?: return

        if (_uiState.value.isConfirming || payment.status != PaymentStatus.PENDING) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isConfirming = true,
                error = null
            )

            try {
                repository.confirmPayment(payment.id)

                val updatedPayment = repository.getPaymentByOrderId(
                    payment.orderId
                )

                _uiState.value = _uiState.value.copy(
                    payment = updatedPayment,
                    isConfirming = false,
                    isPaid = updatedPayment?.status == PaymentStatus.PAID
                )

                check(updatedPayment?.status == PaymentStatus.PAID) {
                    "Pembayaran belum berhasil diperbarui."
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isConfirming = false,
                    error = e.message ?: "Gagal mengonfirmasi pembayaran."
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun resetState() {
        _uiState.value = PaymentUiState()
    }
}

class PaymentViewModelFactory(
    private val repository: SupabasePaymentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PaymentViewModel::class.java)) {
            return PaymentViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class: ${modelClass.name}"
        )
    }
}
