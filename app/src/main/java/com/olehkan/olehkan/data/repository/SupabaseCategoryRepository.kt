package com.olehkan.olehkan.data.repository

import android.content.SharedPreferences
import com.olehkan.olehkan.data.model.Category
import com.olehkan.olehkan.data.model.CategoryNameValidator
import com.olehkan.olehkan.data.network.SupabaseHttp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class SupabaseCategoryRepository(
    private val auth: SupabaseAuthRepository,
    private val legacyPreferences: SharedPreferences,
) : CategoryRepository {
    private val mutex = Mutex()
    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    override val categories = _categories.asStateFlow()

    override suspend fun refresh() {
        mutex.withLock { load() }
    }

    private suspend fun load() {
        val rows = JSONArray(auth.authorized("GET", "/rest/v1/categories?select=id,name,catalog_locked&order=created_at.asc,id.asc"))
        _categories.value = (0 until rows.length()).map { index ->
            val row = rows.getJSONObject(index)
            val name = row.getString("name")
            Category(row.getString("id"), name,
                DummyProductRepository.dummyProducts.count { it.category == name },
                isProtected = row.getBoolean("catalog_locked"))
        }
    }

    override suspend fun save(categoryId: String?, name: String): String = mutex.withLock {
        val normalized = CategoryNameValidator.normalize(name)
        val error = CategoryNameValidator.error(normalized)
        require(error == null) { error.orEmpty() }
        val path = if (categoryId == null) "/rest/v1/categories?select=id"
            else "/rest/v1/categories?id=eq.${SupabaseHttp.encode(categoryId)}&select=id"
        val result = JSONArray(auth.authorized(
            if (categoryId == null) "POST" else "PATCH", path,
            JSONObject().put("name", normalized).toString(), mapOf("Prefer" to "return=representation"),
        ))
        check(result.length() == 1) { "Kategori tidak ditemukan atau dilindungi dari perubahan." }
        val id = result.getJSONObject(0).getString("id")
        // Hasil tulis sudah dikonfirmasi server; perbarui cache meskipun pemuatan ulang gagal.
        if (categoryId == null) _categories.value = _categories.value + Category(id, normalized)
        else _categories.value = _categories.value.map { if (it.id == id) it.copy(name = normalized) else it }
        id
    }

    override suspend fun delete(categoryId: String) {
        mutex.withLock {
            val result = JSONArray(auth.authorized("DELETE",
                "/rest/v1/categories?id=eq.${SupabaseHttp.encode(categoryId)}&select=id",
                headers = mapOf("Prefer" to "return=representation")))
            check(result.length() == 1) { "Kategori tidak ditemukan atau masih dipakai katalog." }
            _categories.value = _categories.value.filterNot { it.id == categoryId }
        }
    }

    /** Hanya dipanggil dari aksi eksplisit admin; data lokal asli tetap disimpan. */
    suspend fun importLocal(): Int = withContext(Dispatchers.IO) {
        val raw = legacyPreferences.getString("categories_v1", null) ?: return@withContext 0
        val rows = JSONArray(raw)
        refresh()
        var imported = 0
        for (index in 0 until rows.length()) {
            val name = CategoryNameValidator.normalize(rows.getJSONObject(index).getString("name"))
            if (_categories.value.none { it.name.equals(name, ignoreCase = true) }) {
                try {
                    save(null, name)
                    imported++
                } catch (e: Exception) {
                    if (e is kotlinx.coroutines.CancellationException) throw e
                    throw IllegalStateException("$imported kategori telah diimpor; impor terhenti. Data lokal tetap aman.", e)
                }
            }
        }
        imported
    }

    fun clearCache() { _categories.value = emptyList() }
}
