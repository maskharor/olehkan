package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product

interface ProductRepository {

    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: Int): Product?

    /**
     * Tambah (productUuid == null) atau edit produk.
     * Identitas di Supabase adalah UUID (Product.uuid), BUKAN Int id.
     * Int id hanya untuk UI/navigasi, dipetakan via ProductIdMapping.
     * Mengembalikan UUID produk dari server.
     * Upload gambar TIDAK dilakukan di sini: upload dulu ke
     * Storage `product-images`, lalu kirim URL-nya sebagai imageUrl.
     */
    suspend fun save(
        productUuid: String?,
        name: String,
        description: String?,
        price: Int,
        stock: Int,
        categoryId: String,
        imageUrl: String?,
        isActive: Boolean,
    ): String

    suspend fun delete(productUuid: String)

    /** Nonaktifkan produk (is_active = false) tanpa menghapus riwayat. */
    suspend fun setActive(productUuid: String, isActive: Boolean)
}
