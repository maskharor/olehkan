package com.olehkan.olehkan.data.model

data class Category(
    val id: String,
    val name: String,
    // Dihitung dari katalog produk, bukan nilai yang dapat diedit pengguna.
    val productCount: Int = 0,
    val isProtected: Boolean = productCount > 0,
)
