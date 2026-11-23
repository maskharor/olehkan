package com.olehkan.olehkan.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.CategoryNameValidator
import java.io.IOException
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Kategori disimpan lokal. Kategori dari katalog diimpor bila belum tersedia.
 * Relasi produk masih memakai nama; rename/hapus kategori terpakai harus ditolak.
 */
class LocalCategoryRepository(
    private val preferences: SharedPreferences,
    private val productRepository: ProductRepository = DummyProductRepository(),
) : CategoryRepository {
    private val mutex = Mutex()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories = _categories.asStateFlow()

    override suspend fun refresh() = withContext(Dispatchers.IO) {
        mutex.withLock {
            _categories.value = loadCategories()
        }
    }

    override suspend fun save(categoryId: String?, name: String): String = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = loadCategories()
            val normalized = CategoryNameValidator.normalize(name)
            val existing = categoryId?.let { id ->
                current.find { it.id == id } ?: throw IllegalArgumentException("Kategori tidak ditemukan.")
            }
            val error = CategoryNameValidator.error(
                normalized,
                current.filter { it.id != categoryId }.map { it.name },
            )
            require(error == null) { error.orEmpty() }
            require(existing == null || existing.productCount == 0 || existing.name == normalized) {
                "Kategori masih dipakai produk dan tidak dapat diganti namanya."
            }
            val id = existing?.id ?: UUID.randomUUID().toString()
            val updated = if (existing == null) {
                current + Category(id = id, name = normalized)
            } else {
                current.map { if (it.id == id) it.copy(name = normalized) else it }
            }
            persist(updated)
            _categories.value = updated
            id
        }
    }

    override suspend fun delete(categoryId: String) = withContext(Dispatchers.IO) {
        mutex.withLock {
            val current = loadCategories()
            val category = current.find { it.id == categoryId }
                ?: throw IllegalArgumentException("Kategori tidak ditemukan.")
            require(category.productCount == 0) {
                "Kategori masih dipakai produk dan tidak dapat dihapus."
            }
            val updated = current.filterNot { it.id == categoryId }
            persist(updated)
            _categories.value = updated
        }
    }

    private suspend fun loadCategories(): List<Category> {
        val products = productRepository.getProducts()
        val raw = preferences.getString(DATA_KEY, null)
        val stored = if (raw == null) emptyList() else decode(raw)
        // Jangan menghapus relasi bila katalog memperoleh kategori baru.
        val missing = products.map { it.category }.distinct()
            .filter { name -> stored.none { it.name == name } }
            .map { Category(id = UUID.randomUUID().toString(), name = it) }
        val categories = (stored + missing).map { category ->
            category.copy(productCount = products.count { it.category == category.name })
        }
        if (raw == null || missing.isNotEmpty()) persist(categories)
        return categories
    }

    private fun decode(raw: String): List<Category> {
        try {
            val array = JSONArray(raw)
            val result = (0 until array.length()).map { index ->
                val item = array.getJSONObject(index)
                Category(id = item.getString("id"), name = item.getString("name"))
            }
            require(result.all { it.id.isNotBlank() && CategoryNameValidator.error(it.name) == null })
            require(result.map { it.id }.distinct().size == result.size)
            require(result.map { it.name.lowercase(java.util.Locale.ROOT) }.distinct().size == result.size)
            return result
        } catch (e: Exception) {
            throw IOException("Data kategori lokal tidak dapat dibaca. Data tidak ditimpa.", e)
        }
    }

    private fun persist(categories: List<Category>) {
        val array = JSONArray()
        categories.forEach { category ->
            array.put(JSONObject().put("id", category.id).put("name", category.name))
        }
        if (!preferences.edit().putString(DATA_KEY, array.toString()).commit()) {
            throw IOException("Gagal menyimpan kategori ke perangkat. Silakan coba lagi.")
        }
    }

    private companion object {
        const val DATA_KEY = "categories_v1"
    }
}

object CategoryRepositoryProvider {
    fun get(context: Context): CategoryRepository =
        com.olehkan.olehkan.data.AppContainer.get(context).categories
}
