package com.olehkan.olehkan.ui.screens.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.WishlistRepository
import com.olehkan.olehkan.data.repository.WishlistRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class DetailUiState(
    val isLoading: Boolean = true,
    val product: Product? = null,
    val isFavorite: Boolean = false,
    val errorMessage: String? = null,
)

class DetailViewModel(
    private val productRepository: ProductRepository = DummyProductRepository(),
    private val wishlistRepository: WishlistRepository = WishlistRepositoryProvider.get(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()

    private var currentProductId: Int = -1

    fun loadProduct(productId: Int) {
        currentProductId = productId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val product = productRepository.getProductById(productId)
                val isFav = wishlistRepository.isFavorite(productId)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        product = product,
                        isFavorite = isFav,
                    )
                }

                // Keep observing favoriteIds flow in case it changes
                wishlistRepository.favoriteIds.collect { favIds ->
                    _uiState.update { it.copy(isFavorite = favIds.contains(productId)) }
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

    fun toggleFavorite() {
        if (currentProductId <= 0) return
        viewModelScope.launch {
            wishlistRepository.toggleFavorite(currentProductId)
        }
    }

    fun removeFromWishlist() {
        if (currentProductId <= 0) return
        viewModelScope.launch {
            wishlistRepository.removeFromFavorites(currentProductId)
        }
    }

    fun addToWishlist() {
        if (currentProductId <= 0) return
        viewModelScope.launch {
            wishlistRepository.addToFavorites(currentProductId)
        }
    }
}

