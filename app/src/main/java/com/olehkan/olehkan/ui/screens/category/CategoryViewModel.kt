package com.olehkan.olehkan.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.CategoryNameValidator
import com.olehkan.olehkan.data.repository.CategoryRepository
import kotlinx.coroutines.Job
import com.olehkan.olehkan.data.network.BackendException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CategoryUiState(
    val categories: List<Category> = emptyList(),
    val isLoading: Boolean = true,
    val hasLoaded: Boolean = false,
    val isSaving: Boolean = false,
    val errorMessage: String? = null,
    val nameError: String? = null,
    val successMessage: String? = null,
    val query: String = "",
    val filter: String = "Semua",
    val alphabetical: Boolean = false,
) {
    val visibleCategories: List<Category> get() {
        val filtered = categories.filter { category ->
            category.name.contains(query.trim(), ignoreCase = true) && when (filter) {
                "Bisa dikelola" -> category.canManage
                "Dilindungi" -> category.isProtected || category.productCount > 0
                else -> true
            }
        }
        return if (alphabetical) filtered.sortedBy { it.name.lowercase(java.util.Locale.ROOT) } else filtered
    }
}

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState = _uiState.asStateFlow()
    val name = savedStateHandle.getStateFlow("categoryName", "")
    private var refreshJob: Job? = null

    fun onQueryChange(value: String) { _uiState.update { it.copy(query = value) } }
    fun onFilterChange(value: String) { _uiState.update { it.copy(filter = value) } }
    fun toggleSort() { _uiState.update { it.copy(alphabetical = !it.alphabetical) } }
    fun resetFilters() { _uiState.update { it.copy(query = "", filter = "Semua") } }

    init {
        viewModelScope.launch {
            repository.categories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        refresh()
    }

    fun refresh() {
        if (refreshJob?.isActive == true || _uiState.value.isSaving) return
        refreshJob = viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.refresh()
                _uiState.update {
                    it.copy(isLoading = false, hasLoaded = true, categories = repository.categories.value)
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = e.message ?: "Gagal memuat kategori.")
                }
            }
        }
    }

    fun prepareForm(categoryId: String?) {
        if (savedStateHandle.get<Boolean>("formInitialized") == true) return
        savedStateHandle["categoryName"] =
            _uiState.value.categories.find { it.id == categoryId }?.name.orEmpty()
        savedStateHandle["formInitialized"] = true
    }

    fun onNameChange(value: String) {
        savedStateHandle["categoryName"] = value
        _uiState.update { it.copy(nameError = null, errorMessage = null) }
    }

    fun save(categoryId: String?) {
        if (_uiState.value.isSaving || _uiState.value.isLoading || !_uiState.value.hasLoaded) return
        val error = CategoryNameValidator.error(
            name.value,
            _uiState.value.categories.filter { it.id != categoryId }.map { it.name },
        )
        if (error != null) {
            _uiState.update { it.copy(nameError = error) }
            return
        }
        mutate {
            repository.save(categoryId, name.value)
            if (categoryId == null) "Kategori berhasil ditambahkan." else "Kategori berhasil diperbarui."
        }
    }

    fun delete(categoryId: String) {
        if (_uiState.value.isSaving || _uiState.value.isLoading || !_uiState.value.hasLoaded) return
        mutate {
            repository.delete(categoryId)
            "Kategori berhasil dihapus."
        }
    }

    private fun mutate(action: suspend () -> String) {
        _uiState.update { it.copy(isSaving = true, errorMessage = null, nameError = null) }
        viewModelScope.launch {
            try {
                val message = action()
                _uiState.update { it.copy(isSaving = false, successMessage = message) }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _uiState.update {
                    it.copy(isSaving = false, errorMessage = when {
                        e is BackendException && e.errorCode == "23503" ->
                            "Kategori masih digunakan produk. Muat ulang data sebelum mencoba lagi."
                        e is java.io.IOException && e !is BackendException ->
                            "Tidak dapat terhubung. Periksa koneksi lalu coba lagi."
                        else -> e.message ?: "Perubahan gagal disimpan."
                    })
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
