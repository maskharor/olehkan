package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.ProductIdMapping
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

/**
 * Repository Wishlist terintegrasi database Supabase REST API & RPC:
 * - Read: GET /rest/v1/wishlists?select=*,products(*)&order=created_at.desc
 * - Add: POST /rest/v1/rpc/add_to_wishlist dengan body {"p_product_id": "<UUID_PRODUK>"}
 * - Remove: POST /rest/v1/rpc/remove_from_wishlist dengan body {"p_product_id": "<UUID_PRODUK>"}
 * - Menggunakan header resmi Supabase: apikey, Authorization: Bearer <access_token>, Content-Type: application/json
 */
class SupabaseWishlistRepository(
    private val auth: SupabaseAuthRepository,
) : WishlistRepository {

    private val mutex = Mutex()
    private val _favoriteIds = MutableStateFlow<Set<Int>>(emptySet())
    override val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    private val _cachedProducts = MutableStateFlow<List<Product>>(emptyList())
    val cachedProducts: StateFlow<List<Product>> = _cachedProducts.asStateFlow()

    override suspend fun loadFavorites(): List<Int> = withContext(Dispatchers.IO) {
        mutex.withLock {
            try {
                // 1. Read Wishlist via Supabase REST API
                val response = auth.authorized(
                    method = "GET",
                    path = "/rest/v1/wishlists?select=*,products(*)&order=created_at.desc",
                )
                val jsonArray = JSONArray(response)
                val ids = mutableSetOf<Int>()
                val productList = mutableListOf<Product>()

                for (i in 0 until jsonArray.length()) {
                    val row = jsonArray.getJSONObject(i)
                    val uuid = row.getString("product_id")
                    val intId = ProductIdMapping.toId(uuid)
                    ids.add(intId)

                    val prodJson = row.optJSONObject("products")
                    if (prodJson != null) {
                        val product = parseProductFromJson(prodJson, intId, uuid)
                        productList.add(product)
                    }
                }
                _favoriteIds.value = ids
                _cachedProducts.value = productList
                ids.toList()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _favoriteIds.value.toList()
            }
        }
    }

    override suspend fun addToFavorites(productId: Int): Boolean = withContext(Dispatchers.IO) {
        val uuid = ProductIdMapping.toUuid(productId)
        addToWishlist(uuid)
    }

    override suspend fun addToWishlist(productUuid: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            val intId = ProductIdMapping.toId(productUuid)
            try {
                // 2. Tambah Favorit via RPC Supabase
                val body = JSONObject().put("p_product_id", productUuid).toString()
                auth.authorized(
                    method = "POST",
                    path = "/rest/v1/rpc/add_to_wishlist",
                    body = body,
                )
                _favoriteIds.value = _favoriteIds.value + intId
                true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Jika produk sudah ada di database, pastikan tetap sinkron di state
                _favoriteIds.value = _favoriteIds.value + intId
                true
            }
        }
    }

    override suspend fun removeFromFavorites(productId: Int): Boolean = withContext(Dispatchers.IO) {
        val uuid = ProductIdMapping.toUuid(productId)
        removeFromWishlist(uuid)
    }

    override suspend fun removeFromWishlist(productUuid: String): Boolean = withContext(Dispatchers.IO) {
        mutex.withLock {
            val intId = ProductIdMapping.toId(productUuid)
            try {
                // 3. Hapus Favorit via RPC Supabase
                val body = JSONObject().put("p_product_id", productUuid).toString()
                auth.authorized(
                    method = "POST",
                    path = "/rest/v1/rpc/remove_from_wishlist",
                    body = body,
                )
                _favoriteIds.value = _favoriteIds.value - intId
                true
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                _favoriteIds.value = _favoriteIds.value - intId
                false
            }
        }
    }

    override suspend fun toggleFavorite(productId: Int): Boolean {
        return if (isFavorite(productId)) {
            removeFromFavorites(productId)
            false
        } else {
            addToFavorites(productId)
            true
        }
    }

    override fun isFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }

    override fun clearCache() {
        _favoriteIds.value = emptySet()
        _cachedProducts.value = emptyList()
    }

    private fun parseProductFromJson(json: JSONObject, fallbackId: Int, uuid: String): Product {
        val dummy = DummyProductRepository.dummyProducts.find { it.id == fallbackId || it.uuid.equals(uuid, ignoreCase = true) }
        return Product(
            id = fallbackId,
            name = json.optString("name", dummy?.name ?: "Produk Nusantara"),
            place = dummy?.place ?: "Indonesia",
            price = json.optDouble("price", dummy?.price?.toDouble() ?: 0.0).toInt(),
            rating = dummy?.rating ?: 4.8,
            reviews = dummy?.reviews ?: 50,
            stock = json.optInt("stock", dummy?.stock ?: 10),
            category = dummy?.category ?: "Oleh-Oleh",
            image = json.optString("image_url", dummy?.image ?: ""),
            description = json.optString("description", dummy?.description ?: ""),
            sellerName = dummy?.sellerName ?: "OlehKan Official",
            sellerCity = dummy?.sellerCity ?: "Indonesia",
            sellerRating = dummy?.sellerRating ?: 4.9,
            sellerBadge = dummy?.sellerBadge ?: "Official Store",
            uuid = uuid,
        )
    }
}
