package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Category
import kotlinx.coroutines.flow.StateFlow

interface CategoryRepository {
    val categories: StateFlow<List<Category>>
    suspend fun refresh()
    suspend fun save(categoryId: String?, name: String): String
    suspend fun delete(categoryId: String)
}
