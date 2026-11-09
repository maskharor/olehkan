package com.olehkan.olehkan.ui.screens.home

import com.olehkan.olehkan.data.model.Product

data class HomeUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,

    val products: List<Product> = emptyList(),

    val filteredProducts: List<Product> = emptyList(),

    val selectedCategory: String = CATEGORY_ALL,
    val searchQuery: String = "",
) {
    companion object {
        const val CATEGORY_ALL = "Semua"
        val categories: List<String> = listOf(CATEGORY_ALL, "Makanan", "Camilan", "Manisan")
    }
    val isEmptyFilterResult: Boolean
        get() = !isLoading && errorMessage == null &&
                products.isNotEmpty() && filteredProducts.isEmpty()
}