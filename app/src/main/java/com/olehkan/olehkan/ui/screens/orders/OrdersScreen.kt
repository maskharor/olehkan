package com.olehkan.olehkan.ui.screens.orders

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun OrdersScreen(
    onPayClick: (productId: Int, orderId: String) -> Unit,   // <-- WAJIB 2 param
    viewModel: OrdersViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    OrdersContent(
        uiState = uiState,
        onTabSelected = viewModel::onTabSelected,
        onPayClick = { orderId ->
            // Cari ordernya, lalu teruskan productId & orderId ke Checkout
            val order = uiState.allOrders.find { it.id == orderId }
            order?.let {
                onPayClick(it.product.id, it.id)
            }
        },
        onRetry = viewModel::onRetry,
    )
}