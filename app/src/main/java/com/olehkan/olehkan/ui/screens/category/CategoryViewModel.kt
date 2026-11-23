package com.olehkan.olehkan.ui.screens.category

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.CategoryNameValidator
import com.olehkan.olehkan.data.repository.CategoryRepository
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
)

class CategoryViewModel(
    private val repository: CategoryRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CategoryUiState())
    val uiState = _uiState.asStateFlow()
    val name = savedStateHandle.getStateFlow("categoryName", "")

    init {
        viewModelScope.launch {
            repository.categories.collect { categories ->
                _uiState.update { it.copy(categories = categories) }
            }
        }
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
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
                    it.copy(isSaving = false, errorMessage = e.message ?: "Perubahan gagal disimpan.")
                }
            }
        }
    }

    fun consumeSuccess() {
        _uiState.update { it.copy(successMessage = null) }
    }
}
