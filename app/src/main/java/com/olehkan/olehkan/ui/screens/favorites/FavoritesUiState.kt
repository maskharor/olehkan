package com.olehkan.olehkan.ui.screens.favorites

import com.olehkan.olehkan.data.model.Product

data class FavoritesUiState(
    val isLoading: Boolean = false,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
)

