
    package com.olehkan.olehkan.ui.screens.orders

    import androidx.lifecycle.ViewModel
    import androidx.lifecycle.ViewModelProvider
    import androidx.lifecycle.viewModelScope
    import com.olehkan.olehkan.data.model.Order
    import com.olehkan.olehkan.data.model.OrderStatus
    import com.olehkan.olehkan.data.repository.OrderItemDraft
    import com.olehkan.olehkan.data.repository.SupabaseOrderRepository
    import kotlinx.coroutines.flow.MutableStateFlow
    import kotlinx.coroutines.flow.StateFlow
    import kotlinx.coroutines.flow.asStateFlow
    import kotlinx.coroutines.launch

    data class OrderUiState(
        val orders: List<Order> = emptyList(),
        val selectedOrder: Order? = null,
        val isLoading: Boolean = false,
        val isCreating: Boolean = false,
        val isCancelling: Boolean = false,
        val error: String? = null,
        val createdOrder: Order? = null
    ) {
        val ongoingOrders: List<Order>
            get() = orders.filter {
                it.status == OrderStatus.PENDING ||
                        it.status == OrderStatus.PROCESSING
            }

        val completedOrders: List<Order>
            get() = orders.filter {
                it.status == OrderStatus.COMPLETED ||
                        it.status == OrderStatus.CANCELLED
            }
    }

    class OrderViewModel(
        private val repository: SupabaseOrderRepository
    ) : ViewModel() {

        private val _uiState = MutableStateFlow(OrderUiState())
        val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

        /**
         * Mengambil seluruh pesanan pengguna.
         */
        fun loadOrders() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null
                )

                try {
                    val orders = repository.getOrders()

                    _uiState.value = _uiState.value.copy(
                        orders = orders,
                        isLoading = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat pesanan."
                    )
                }
            }
        }

        /**
         * Mengambil detail pesanan berdasarkan UUID.
         */
        fun loadOrder(orderId: String) {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isLoading = true,
                    error = null,
                    selectedOrder = null
                )

                try {
                    val order = repository.getOrderById(orderId)

                    _uiState.value = _uiState.value.copy(
                        selectedOrder = order,
                        isLoading = false,
                        error = if (order == null) {
                            "Pesanan tidak ditemukan."
                        } else {
                            null
                        }
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Gagal memuat detail pesanan."
                    )
                }
            }
        }

        /**
         * Membuat pesanan baru.
         */
        fun createOrder(
            recipientName: String,
            phoneNumber: String,
            shippingAddress: String,
            notes: String?,
            items: List<OrderItemDraft>
        ) {
            if (_uiState.value.isCreating) return

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isCreating = true,
                    error = null,
                    createdOrder = null
                )

                try {
                    val order = repository.createOrder(
                        recipientName = recipientName,
                        phoneNumber = phoneNumber,
                        shippingAddress = shippingAddress,
                        notes = notes,
                        items = items
                    )

                    _uiState.value = _uiState.value.copy(
                        orders = listOf(order) + _uiState.value.orders,
                        selectedOrder = order,
                        createdOrder = order,
                        isCreating = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isCreating = false,
                        error = e.message ?: "Gagal membuat pesanan."
                    )
                }
            }
        }

        /**
         * Membatalkan pesanan.
         */
        fun cancelOrder(orderId: String) {
            if (_uiState.value.isCancelling) return

            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(
                    isCancelling = true,
                    error = null
                )

                try {
                    repository.cancelOrder(orderId)

                    val updatedOrder = repository.getOrderById(orderId)

                    _uiState.value = _uiState.value.copy(
                        orders = _uiState.value.orders.map { order ->
                            if (order.id == orderId && updatedOrder != null) {
                                updatedOrder
                            } else {
                                order
                            }
                        },
                        selectedOrder = if (
                            _uiState.value.selectedOrder?.id == orderId
                        ) {
                            updatedOrder
                        } else {
                            _uiState.value.selectedOrder
                        },
                        isCancelling = false
                    )
                } catch (e: Exception) {
                    _uiState.value = _uiState.value.copy(
                        isCancelling = false,
                        error = e.message ?: "Gagal membatalkan pesanan."
                    )
                }
            }
        }

        /**
         * Membersihkan pesan error.
         */
        fun clearError() {
            _uiState.value = _uiState.value.copy(error = null)
        }

        /**
         * Dipanggil setelah navigasi ke halaman detail berhasil.
         * Mencegah navigasi berulang akibat StateFlow.
         */
        fun clearCreatedOrder() {
            _uiState.value = _uiState.value.copy(createdOrder = null)
        }
    }

    /**
     * Factory agar OrderViewModel dapat menerima repository.
     */
    class OrderViewModelFactory(
        private val repository: SupabaseOrderRepository
    ) : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(OrderViewModel::class.java)) {
                return OrderViewModel(repository) as T
            }

            throw IllegalArgumentException(
                "Unknown ViewModel class: ${modelClass.name}"
            )
        }
    }
