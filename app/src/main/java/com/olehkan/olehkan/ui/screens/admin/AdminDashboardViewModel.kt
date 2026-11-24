package com.olehkan.olehkan.ui.screens.admin

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.network.BackendException
import com.olehkan.olehkan.data.repository.AdminDashboardRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
) {
    val visibleCategories: List<Category>
        get() = categories.asReversed().filter { it.name.contains(query.trim(), ignoreCase = true) }.take(6)
}

class AdminDashboardViewModel(application: Application) : AndroidViewModel(application) {
    private val container = AppContainer.get(application)
    private val repository = AdminDashboardRepository(container.auth)
    private val _uiState = MutableStateFlow(AdminDashboardState())
    val uiState = _uiState.asStateFlow()
    private var loadJob: Job? = null

    init {
        viewModelScope.launch {
            container.categories.categories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
    }

    fun refresh() {
        if (loadJob?.isActive == true || _uiState.value.importing) return
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
