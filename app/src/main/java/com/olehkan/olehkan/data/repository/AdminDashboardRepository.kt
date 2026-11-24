package com.olehkan.olehkan.data.repository

import org.json.JSONObject
import org.json.JSONArray
import java.math.BigDecimal
import com.olehkan.olehkan.data.network.SupabaseHttp

data class AdminOverview(val categoryCount: Long, val buyerCount: Long)

data class AdminInventoryProduct(
    val id: String,
    val name: String,
    val price: BigDecimal,
    val stock: Int,
    val imageUrl: String?,
    val active: Boolean,
)
data class AdminInventoryPage(val products: List<AdminInventoryProduct>, val hasMore: Boolean)

/** Ringkasan tetap dihitung backend; tidak menggantikan kegagalan dengan data dummy. */
class AdminDashboardRepository(
    private val requestOverview: suspend () -> String,
) {
    private var requestProducts: (suspend (String) -> String)? = null

    constructor(auth: SupabaseAuthRepository) : this({
        auth.authorized("POST", "/rest/v1/rpc/admin_overview")
    }) {
        requestProducts = { path -> auth.authorized("GET", path) }
    }

    constructor(overview: suspend () -> String, products: suspend (String) -> String) : this(overview) {
        requestProducts = products
    }

    /** Halaman kecil agar membuka satu kategori tidak mengunduh seluruh katalog. */
    suspend fun loadProducts(categoryId: String, offset: Int = 0): AdminInventoryPage {
        require(categoryId.isNotBlank() && offset >= 0)
        val request = checkNotNull(requestProducts) { "Sumber produk belum tersedia." }
        val rows = JSONArray(request(
            "/rest/v1/products?select=id,name,price,stock,image_url,is_active" +
                "&category_id=eq.${SupabaseHttp.encode(categoryId)}" +
                "&order=created_at.asc,id.asc&limit=21&offset=$offset"
        ))
        val products = (0 until minOf(rows.length(), 20)).map { index ->
            val row = rows.getJSONObject(index)
            val price = row.getString("price").toBigDecimal()
            val stock = row.getInt("stock")
            check(price >= BigDecimal.ZERO && stock >= 0) { "Data stok produk tidak valid." }
            AdminInventoryProduct(
                row.getString("id"), row.getString("name"), price, stock,
                row.optString("image_url").takeIf { it.isNotBlank() && it != "null" },
                row.getBoolean("is_active"),
            )
        }
        return AdminInventoryPage(products, hasMore = rows.length() > 20)
    }

    suspend fun loadOverview(): AdminOverview {
        val json = JSONObject(requestOverview())
        fun count(key: String): Long {
            val value = json.opt(key)?.toString()?.toLongOrNull()
            check(value != null && value >= 0) { "Format ringkasan toko tidak valid." }
            return value
        }
        return AdminOverview(count("category_count"), count("buyer_count"))
    }
}
