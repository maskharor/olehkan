package com.olehkan.olehkan.ui.screens.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.WishlistRepository
import com.olehkan.olehkan.data.repository.WishlistRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val wishlistRepository: WishlistRepository = WishlistRepositoryProvider.get(),
    private val productRepository: ProductRepository = DummyProductRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(FavoritesUiState(isLoading = true))
    val uiState: StateFlow<FavoritesUiState> = _uiState.asStateFlow()

    init {
        observeFavorites()
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            try {
                // Pre-load all available products once
                val allProducts = productRepository.getProducts()
                wishlistRepository.favoriteIds.collect { favIds ->
                    val favProducts = allProducts.filter { it.id in favIds }
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            products = favProducts,
                            errorMessage = null,
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat produk favorit",
                    )
                }
            }
        }
    }

    fun removeFromFavorites(productId: Int) {
        viewModelScope.launch {
            wishlistRepository.removeFromFavorites(productId)
        }
    }

    fun addToFavorites(productId: Int) {
        viewModelScope.launch {
            wishlistRepository.addToFavorites(productId)
        }
    }

    fun toggleFavorite(productId: Int) {
        viewModelScope.launch {
            wishlistRepository.toggleFavorite(productId)
        }
    }

    fun onRetry() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        observeFavorites()
    }
}

