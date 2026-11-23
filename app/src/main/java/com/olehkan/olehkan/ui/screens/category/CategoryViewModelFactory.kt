package com.olehkan.olehkan.ui.screens.category

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import com.olehkan.olehkan.data.repository.CategoryRepository

class CategoryViewModelFactory(
    private val repository: CategoryRepository,
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        require(modelClass.isAssignableFrom(CategoryViewModel::class.java))
        @Suppress("UNCHECKED_CAST")
        return CategoryViewModel(repository, extras.createSavedStateHandle()) as T
    }
}
