package com.olehkan.olehkan.ui.screens.admin.productform

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.SupabaseProductRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AdminProductListUiState(
    val isLoading: Boolean = true,
    val products: List<Product> = emptyList(),
    val errorMessage: String? = null,
    val isDeleting: Boolean = false,
    val successMessage: String? = null,
)

/**
 * ViewModel Daftar Produk Admin (Fitur 2, MVVM).
 * Tampilan list: LazyVerticalGrid (lihat AdminProductListScreen).
 * Admin melihat SEMUA produk (aktif + nonaktif), bukan hanya yang aktif.
 */
class AdminProductListViewModel(
    private val productRepository: ProductRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminProductListUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                // Admin: semua produk, termasuk yang nonaktif.
                val products = productRepository.getProducts()
                _uiState.update { it.copy(isLoading = false, products = products) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat produk.")
                }
            }
        }
    }

    fun delete(productUuid: String) {
        if (_uiState.value.isDeleting) return
        _uiState.update { it.copy(isDeleting = true, errorMessage = null) }
        viewModelScope.launch {
            try {
                productRepository.delete(productUuid)
                _uiState.update {
                    it.copy(isDeleting = false, successMessage = "Produk berhasil dihapus.")
                }
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isDeleting = false, errorMessage = e.message ?: "Produk gagal dihapus.")
                }
            }
        }
    }

    fun toggleActive(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.setActive(product.uuid, !product.isActive)
                loadProducts()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(errorMessage = e.message ?: "Gagal mengubah status produk.")
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}

class AdminProductListViewModelFactory(
    private val productRepository: ProductRepository,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AdminProductListViewModel::class.java)) {
            return AdminProductListViewModel(productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
