package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.ProductIdMapping
import com.olehkan.olehkan.data.network.SupabaseHttp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository produk Supabase, mengikuti pola SupabaseCategoryRepository:
 * UI -> ViewModel -> Repository -> Supabase REST API (auth.authorized).
 *
 * PENTING: pendekatan tim di main 590a15c dipertahankan.
 * - `Product.id` tetap Int untuk UI/navigasi/wishlist/review.
 * - Identitas di Supabase adalah UUID di `Product.uuid`,
 *   dipetakan via `ProductIdMapping` (1-5 untuk produk yang sudah
 *   di-seed). Produk baru yang belum ada di mapping mendapat
 *   Int id deterministik dari hash UUID-nya (1000+), supaya
 *   tidak bentrok dengan 1-5 dan stabil antar pemuatan.
 * - Tabel `products`: id UUID, category_id UUID FK ke categories.id,
 *   name, description (nullable), price NUMERIC, stock INTEGER,
 *   image_url (nullable), is_active.
 *   Hanya ADMIN yang boleh insert/update/delete (RLS).
 *   Produk yang sudah dipakai transaksi sebaiknya dinonaktifkan,
 *   bukan dihapus — pakai setActive().
 *
 * Gambar TIDAK di-upload di sini: upload dulu ke Storage
 * `product-images` via uploadImage(), lalu kirim URL-nya ke save().
 */
class SupabaseProductRepository(
    private val auth: SupabaseAuthRepository,
) : ProductRepository {

    private val mutex = Mutex()
    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products.asStateFlow()

    /** Muat ulang dari server. activeOnly=true untuk katalog buyer. */
    suspend fun refresh(activeOnly: Boolean = false) {
        mutex.withLock { load(activeOnly) }
    }

    private suspend fun load(activeOnly: Boolean) {
        val categoryRows = JSONArray(
            auth.authorized("GET", "/rest/v1/categories?select=id,name&order=name.asc"),
        )
        val categoryNames = mutableMapOf<String, String>()
        for (i in 0 until categoryRows.length()) {
            val row = categoryRows.getJSONObject(i)
            categoryNames[row.getString("id")] = row.getString("name")
        }
        val filter = if (activeOnly) "&is_active=eq.true" else ""
        val rows = JSONArray(
            auth.authorized(
                "GET",
                "/rest/v1/products?select=id,category_id,name,description,price,stock,image_url,is_active&order=created_at.desc$filter",
            ),
        )
        _products.value = (0 until rows.length()).map { i -> rows.getJSONObject(i).toProduct(categoryNames) }
    }

    override suspend fun getProducts(): List<Product> {
        mutex.withLock {
            if (_products.value.isEmpty()) load(activeOnly = false)
            return _products.value.toList()
        }
    }

    /** Produk aktif saja, untuk katalog buyer (Home / toko). */
    suspend fun getActiveProducts(): List<Product> {
        mutex.withLock {
            load(activeOnly = true)
            return _products.value.toList()
        }
    }

    override suspend fun getProductById(id: Int): Product? {
        val cached = _products.value.find { it.id == id }
        if (cached != null) return cached
        val uuid = ProductIdMapping.toUuid(id)
        val rows = JSONArray(
            auth.authorized(
                "GET",
                "/rest/v1/products?select=id,category_id,name,description,price,stock,image_url,is_active&id=eq.${SupabaseHttp.encode(uuid)}",
            ),
        )
        if (rows.length() == 0) return null
        val categoryRows = JSONArray(
            auth.authorized("GET", "/rest/v1/categories?select=id,name"),
        )
        val categoryNames = mutableMapOf<String, String>()
        for (i in 0 until categoryRows.length()) {
            val row = categoryRows.getJSONObject(i)
            categoryNames[row.getString("id")] = row.getString("name")
        }
        return rows.getJSONObject(0).toProduct(categoryNames)
    }

    override suspend fun save(
        productUuid: String?,
        name: String,
        description: String?,
        price: Int,
        stock: Int,
        categoryId: String,
        imageUrl: String?,
        isActive: Boolean,
    ): String = mutex.withLock {
        require(name.isNotBlank()) { "Nama produk wajib diisi." }
        require(price >= 0) { "Harga tidak boleh negatif." }
        require(stock >= 0) { "Stok tidak boleh negatif." }
        require(categoryId.isNotBlank()) { "Kategori wajib dipilih." }
        val body = JSONObject().apply {
            put("category_id", categoryId)
            put("name", name.trim())
            if (description.isNullOrBlank()) put("description", JSONObject.NULL) else put("description", description.trim())
            put("price", price)
            put("stock", stock)
            if (imageUrl.isNullOrBlank()) put("image_url", JSONObject.NULL) else put("image_url", imageUrl)
            put("is_active", isActive)
        }
        val path = if (productUuid == null) {
            "/rest/v1/products?select=id"
        } else {
            "/rest/v1/products?id=eq.${SupabaseHttp.encode(productUuid)}&select=id"
        }
        val result = JSONArray(
            auth.authorized(
                if (productUuid == null) "POST" else "PATCH",
                path,
                body.toString(),
                mapOf("Prefer" to "return=representation"),
            ),
        )
        check(result.length() == 1) { "Produk tidak ditemukan atau tidak diizinkan." }
        val uuid = result.getJSONObject(0).getString("id")
        runCatching { load(activeOnly = false) }
        uuid
    }

    override suspend fun delete(productUuid: String) {
        mutex.withLock {
            val result = JSONArray(
                auth.authorized(
                    "DELETE",
                    "/rest/v1/products?id=eq.${SupabaseHttp.encode(productUuid)}&select=id",
                    headers = mapOf("Prefer" to "return=representation"),
                ),
            )
            check(result.length() == 1) { "Produk tidak ditemukan atau masih dipakai." }
            _products.value = _products.value.filterNot { it.uuid == productUuid }
        }
    }

    override suspend fun setActive(productUuid: String, isActive: Boolean) {
        mutex.withLock {
            val result = JSONArray(
                auth.authorized(
                    "PATCH",
                    "/rest/v1/products?id=eq.${SupabaseHttp.encode(productUuid)}&select=id",
                    JSONObject().put("is_active", isActive).toString(),
                    mapOf("Prefer" to "return=representation"),
                ),
            )
            check(result.length() == 1) { "Produk tidak ditemukan." }
            _products.value = _products.value.map {
                if (it.uuid == productUuid) it.copy(isActive = isActive) else it
            }
        }
    }

    /**
     * Upload gambar ke Storage bucket `product-images`.
     * Mengembalikan public URL gambar.
     * Hanya ADMIN (RLS bucket). Maks 5 MB, JPEG/PNG/WebP.
     */
    suspend fun uploadImage(fileName: String, bytes: ByteArray, contentType: String): String {
        require(bytes.isNotEmpty()) { "File gambar kosong." }
        require(bytes.size <= 5 * 1024 * 1024) { "Ukuran gambar maksimal 5 MB." }
        val safeName = fileName.trim().replace(Regex("[^A-Za-z0-9._-]"), "-")
        return auth.uploadToStorage(
            path = "/storage/v1/object/product-images/$safeName",
            bytes = bytes,
            contentType = contentType,
        )
    }

    fun clearCache() {
        _products.value = emptyList()
    }

    private fun JSONObject.toProduct(categoryNames: Map<String, String>): Product {
        val uuid = getString("id")
        val categoryId = optString("category_id", "")
        // Produk yang sudah di-seed: pakai mapping tim (1-5).
        // Produk baru: Int id deterministik dari hash UUID, 1000+.
        val intId = ProductIdMapping.uuidToId[uuid]
            ?: (1000 + (uuid.hashCode() and 0x7fffffff) % 9000)
        return Product(
            id = intId,
            name = getString("name"),
            price = optDouble("price", 0.0).toInt(),
            stock = optInt("stock", 0),
            category = categoryNames[categoryId].orEmpty(),
            categoryId = categoryId,
            image = optString("image_url", ""),
            description = optString("description", ""),
            isActive = optBoolean("is_active", true),
            uuid = uuid,
        )
    }
}
