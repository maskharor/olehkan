package com.olehkan.olehkan.data.model

data class Category(
    val id: String,
    val name: String,
    // Hitungan produk yang dapat dibaca akun aktif; bukan jumlah stok.
    val productCount: Int = 0,
    val isProtected: Boolean = productCount > 0,
    val usageKnown: Boolean = true,
) {
    val canManage: Boolean get() = usageKnown && !isProtected && productCount == 0
}
