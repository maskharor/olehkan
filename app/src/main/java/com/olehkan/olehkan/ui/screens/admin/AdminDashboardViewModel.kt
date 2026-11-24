package com.olehkan.olehkan.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.network.BackendException
import com.olehkan.olehkan.data.repository.AdminDashboardRepository
import com.olehkan.olehkan.data.repository.AdminInventoryProduct
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryInventoryState(
    val loading: Boolean = false,
    val loaded: Boolean = false,
    val products: List<AdminInventoryProduct> = emptyList(),
    val hasMore: Boolean = false,
    val nextOffset: Int = 0,
    val error: String? = null,
)

data class AdminDashboardState(
    val loading: Boolean = true,
    val categoryCount: Long? = null,
    val buyerCount: Long? = null,
    val error: String? = null,
    val categoriesLoading: Boolean = true,
    val categoriesError: String? = null,
    val categories: List<Category> = emptyList(),
    val query: String = "",
    val updatedAt: Long? = null,
    val message: String? = null,
    val importError: String? = null,
    val importing: Boolean = false,
    val toolsExpanded: Boolean = false,
    val confirmImport: Boolean = false,
    val expandedCategoryId: String? = null,
    val inventory: Map<String, CategoryInventoryState> = emptyMap(),
) {
    val visibleCategories: List<Category>
        get() = categories.asReversed().filter { it.name.contains(query.trim(), ignoreCase = true) }
}

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer.get(application)
    private val repository = AdminDashboardRepository(container.auth)
    private val _uiState = MutableStateFlow(AdminDashboardState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null
    private val productJobs = mutableMapOf<String, Job>()

    init {
        viewModelScope.launch {
            container.categories.categories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun refresh() {
        if (loadJob?.isActive == true || _uiState.value.importing) return
        productJobs.values.forEach { it.cancel() }
        productJobs.clear()
        _uiState.update { it.copy(inventory = emptyMap()) }
        loadJob = viewModelScope.launch {
            _uiState.update { it.copy(loading = true, categoriesLoading = true,
                error = null, categoriesError = null) }
            try {
                val overview = repository.loadOverview()
                _uiState.update { it.copy(categoryCount = overview.categoryCount,
                    buyerCount = overview.buyerCount, updatedAt = System.currentTimeMillis()) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(categoryCount = null, buyerCount = null, updatedAt = null,
                    error = if (e is BackendException && e.statusCode == 404)
                        "Ringkasan belum tersedia. Periksa fungsi admin_overview pada backend."
                    else failure(e, "Ringkasan toko belum dapat dimuat.")) }
            } finally {
                _uiState.update { it.copy(loading = false) }
            }
            // Kegagalan RPC tidak menghalangi akses daftar kategori.
            try {
                container.categories.refresh()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(categoriesError = failure(e, "Kategori belum dapat dimuat.")) }
            } finally {
                _uiState.update { it.copy(categoriesLoading = false) }
            }
            _uiState.value.expandedCategoryId?.let { id ->
                if (_uiState.value.categories.any { it.id == id }) loadProducts(id)
            }
        }
    }

    fun toggleCategory(categoryId: String) {
        val expanded = _uiState.value.expandedCategoryId != categoryId
        _uiState.update { it.copy(expandedCategoryId = if (expanded) categoryId else null) }
        if (expanded && _uiState.value.inventory[categoryId]?.loaded != true) loadProducts(categoryId)
    }

    fun loadProducts(categoryId: String, more: Boolean = false) {
        if (productJobs[categoryId]?.isActive == true) return
        val previous = _uiState.value.inventory[categoryId] ?: CategoryInventoryState()
        if (more && (!previous.hasMore || !previous.loaded)) return
        productJobs[categoryId] = viewModelScope.launch {
            updateInventory(categoryId) { it.copy(loading = true, error = null) }
            try {
                val page = repository.loadProducts(categoryId, if (more) previous.nextOffset else 0)
                updateInventory(categoryId) {
                    it.copy(loading = false, loaded = true, error = null,
                        products = if (more) (previous.products + page.products).distinctBy { p -> p.id } else page.products,
                        hasMore = page.hasMore,
                        nextOffset = (if (more) previous.nextOffset else 0) + page.products.size)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                updateInventory(categoryId) { it.copy(loading = false,
                    error = failure(e, "Produk belum dapat dimuat. Periksa koneksi dan izin akses produk.")) }
            }
        }
    }

    private fun updateInventory(id: String, update: (CategoryInventoryState) -> CategoryInventoryState) {
        _uiState.update { state ->
            state.copy(inventory = state.inventory + (id to update(state.inventory[id] ?: CategoryInventoryState())))
        }
    }

    fun onQueryChange(value: String) { _uiState.update { it.copy(query = value) } }
    fun toggleTools() { _uiState.update { it.copy(toolsExpanded = !it.toolsExpanded) } }
    fun requestImport() {
        if (!_uiState.value.importing && loadJob?.isActive != true)
            _uiState.update { it.copy(confirmImport = true) }
    }
    fun dismissImport() { _uiState.update { it.copy(confirmImport = false) } }

    fun importLocalCategories() {
        if (_uiState.value.importing || loadJob?.isActive == true) return
        _uiState.update { it.copy(importing = true, confirmImport = false, message = null, importError = null) }
        viewModelScope.launch {
            try {
                val count = container.categories.importLocal()
                _uiState.update { it.copy(message = if (count == 0)
                    "Tidak ada kategori lokal baru untuk diimpor."
                    else "$count kategori berhasil diimpor. Data lokal tetap disimpan.") }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update { it.copy(importError = if (e is IllegalStateException) e.message
                    else failure(e, "Impor belum berhasil. Data lokal tetap disimpan.")) }
            } finally {
                _uiState.update { it.copy(importing = false) }
            }
            refresh()
        }
    }

    private fun failure(e: Exception, fallback: String): String =
        if (e is BackendException) e.message ?: fallback else fallback
}
