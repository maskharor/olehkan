package com.olehkan.olehkan.ui.screens.checkout

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Screen checkout — SMART.
 *
 * Menggantikan placeholder Phase 4 dengan MVVM lengkap.
 * ViewModel dibuat via factory (karena butuh productId).
 *
 * Setelah `isOrderPlaced == true`, otomatis panggil `onDone()`
 * untuk navigate ke Orders.
 */
@Composable
fun CheckoutScreen(
    productId: Int,
    onBack: () -> Unit,
    onDone: () -> Unit,
    viewModel: CheckoutViewModel = viewModel(
        factory = CheckoutViewModelFactory(productId = productId),
        key = "checkout_$productId",  // key unik per productId
    ),
    orderId: String?,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Auto navigate setelah order berhasil dibuat
    LaunchedEffect(uiState.isOrderPlaced) {
        if (uiState.isOrderPlaced) {
            onDone()
        }
    }

    CheckoutContent(
        uiState = uiState,
        onBack = onBack,
        onPaymentMethodSelected = viewModel::onPaymentMethodSelected,
        onPayClick = viewModel::onPayClick,
        onRetry = viewModel::onRetry,
    )
}