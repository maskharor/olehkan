package com.olehkan.olehkan.ui.screens.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory untuk CheckoutViewModel.
 *
 * Kenapa perlu factory? Karena CheckoutViewModel punya constructor
 * dengan parameter (productId), bukan default constructor.
 *
 * `viewModel()` di Composable tidak bisa instantiate ViewModel
 * dengan parameter tanpa factory. Factory ini meneruskan productId.
 */
class CheckoutViewModelFactory(
    private val productId: Int,
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CheckoutViewModel::class.java)) {
            return CheckoutViewModel(productId = productId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}