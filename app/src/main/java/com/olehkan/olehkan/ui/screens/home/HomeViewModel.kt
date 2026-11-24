package com.olehkan.olehkan.ui.screens.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.ViewModelProvider
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.SupabaseProductRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState(isLoading = true))
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    var searchQuery by mutableStateOf("")
        private set

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val products = if (repository is SupabaseProductRepository) {
                    repository.getActiveProducts()
                } else {
                    repository.getProducts()
                }
                _uiState.update { current ->
                    current.copy(
                        isLoading = false,
                        products = products,
                        filteredProducts = filterProducts(products, current.selectedCategory, searchQuery),
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat produk",
                    )
                }
            }
        }
    }

    fun onCategorySelected(category: String) {
        _uiState.update { current ->
            current.copy(
                selectedCategory = category,
                filteredProducts = filterProducts(current.products, category, searchQuery),
            )
        }
    }

    fun onSearchQueryChange(query: String) {
        searchQuery = query  // mutableStateOf → instant
        _uiState.update { current ->
            current.copy(
                searchQuery = query,
                filteredProducts = filterProducts(current.products, current.selectedCategory, query),
            )
        }
    }

    fun onRetry() = loadProducts()

    private fun filterProducts(
        products: List<com.olehkan.olehkan.data.model.Product>,
        category: String,
        query: String,
    ): List<com.olehkan.olehkan.data.model.Product> {
        return products.filter { product ->
            val matchesCategory =
                category == HomeUiState.CATEGORY_ALL || product.category == category
            val matchesQuery =
                query.isBlank() || product.name.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }
}
class HomeViewModelFactory(
    private val repository: ProductRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
            return HomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
