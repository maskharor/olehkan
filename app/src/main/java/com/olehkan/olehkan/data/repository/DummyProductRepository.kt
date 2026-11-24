package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product
import kotlinx.coroutines.delay

class DummyProductRepository : ProductRepository {

    private val mutableProducts = dummyProducts.toMutableList()

    override suspend fun getProducts(): List<Product> {
        delay(300)
        return mutableProducts.toList()
    }

    override suspend fun getProductById(id: Int): Product? {
        delay(150)
        return mutableProducts.find { it.id == id }
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
    ): String {
        val uuid = productUuid ?: java.util.UUID.randomUUID().toString()
        val categoryName = when (categoryId) {
            "711df0df-faa3-4b0c-be6c-5a1f146a1134" -> "Makanan"
            "832f74e6-743f-40d9-8a6b-2b5197fb4114" -> "Camilan"
            "da09159f-3f57-4571-beca-ae02a983295a" -> "Manisan"
            "406d10f5-937c-4f93-a6aa-32368c0a7d58" -> "Asinan"
            else -> ""
        }
        val existing = mutableProducts.find { it.uuid == uuid }
        val id = existing?.id ?: ((mutableProducts.maxOfOrNull { it.id } ?: 0) + 1)
        val product = Product(
            id = id, name = name, place = "", price = price, stock = stock,
            category = categoryName, categoryId = categoryId,
            image = imageUrl.orEmpty(), description = description.orEmpty(),
            isActive = isActive, uuid = uuid,
        )
        val index = mutableProducts.indexOfFirst { it.uuid == uuid }
        if (index >= 0) mutableProducts[index] = product else mutableProducts.add(product)
        return uuid
    }

    override suspend fun delete(productUuid: String) {
        mutableProducts.removeAll { it.uuid == productUuid }
    }

    override suspend fun setActive(productUuid: String, isActive: Boolean) {
        val index = mutableProducts.indexOfFirst { it.uuid == productUuid }
        if (index >= 0) mutableProducts[index] = mutableProducts[index].copy(isActive = isActive)
    }

    companion object {
        val dummyProducts: List<Product> = listOf(
            Product(
                id = 1,
                name = "Bakpia Pathok Premium",
                place = "Yogyakarta",
                price = 42_000,
                rating = 4.9,
                reviews = 128,
                stock = 24,
                category = "Makanan",
                image = "https://vocuavzynbsiqwtbcehx.supabase.co/storage/v1/object/public/product-images/bakpia-pathok.jpg",
                description = "Bakpia lembut dengan kulit tipis dan isian kacang hijau pilihan. Dibuat segar setiap hari oleh perajin lokal Pathok.",
                sellerName = "OlehKan",
                sellerCity = "Indonesia",
                sellerRating = 4.9,
                sellerBadge = "Official Store",
                uuid = "929ef347-698d-4ca4-8156-c1513564f68b",
            ),
            Product(
                id = 2,
                name = "Keripik Balado Uni",
                place = "Padang",
                price = 35_000,
                rating = 4.8,
                reviews = 96,
                stock = 12,
                category = "Camilan",
                image = "https://vocuavzynbsiqwtbcehx.supabase.co/storage/v1/object/public/product-images/keripik-balado.jpg",
                description = "Keripik singkong renyah dengan balutan cabai merah khas Minang. Pedas, manis, dan cocok untuk teman perjalanan.",
                sellerName = "OlehKan",
                sellerCity = "Indonesia",
                sellerRating = 4.8,
                sellerBadge = "Super Seller",
                uuid = "997c3c54-221c-4964-9f43-bee00db272d0",
            ),
            Product(
                id = 3,
                name = "Dodol Garut Asli",
                place = "Garut",
                price = 28_000,
                rating = 4.7,
                reviews = 74,
                stock = 38,
                category = "Manisan",
                image = "https://vocuavzynbsiqwtbcehx.supabase.co/storage/v1/object/public/product-images/dodol-garut.jpg",
                description = "Dodol tradisional bertekstur lembut dengan rasa legit dan aroma santan yang khas, dikemas praktis untuk buah tangan.",
                sellerName = "OlehKan",
                sellerCity = "Indonesia",
                sellerRating = 4.7,
                sellerBadge = "Official Store",
                uuid = "6bbf590d-bc05-4f5b-82c8-50b562a318e1",
            ),
            Product(
                id = 4,
                name = "Brem Madiun Klasik",
                place = "Madiun",
                price = 26_000,
                rating = 4.6,
                reviews = 61,
                stock = 9,
                category = "Makanan",
                image = "https://vocuavzynbsiqwtbcehx.supabase.co/storage/v1/object/public/product-images/brem-madiun.jpg",
                description = "Brem putih klasik dengan sensasi lumer di mulut. Fermentasi alami memberikan rasa manis dan sedikit asam yang unik.",
                sellerName = "OlehKan",
                sellerCity = "Indonesia",
                sellerRating = 4.6,
                sellerBadge = "Super Seller",
                uuid = "03961344-3f9f-4634-bdce-11ce48772bac",
            ),
            Product(
                id = 5,
                name = "Produk Uji Wishlist",
                place = "Malang",
                price = 15_000,
                rating = 5.0,
                reviews = 10,
                stock = 10,
                category = "Asinan",
                image = "https://images.unsplash.com/photo-1586511926871-deb1aca37f38?auto=format&fit=crop&w=800&q=85",
                description = "Produk sementara untuk pengujian fitur wishlist",
                sellerName = "OlehKan",
                sellerCity = "Indonesia",
                sellerRating = 5.0,
                sellerBadge = "Official Store",
                uuid = "45fc6b8a-31a9-4df5-89bf-9caa4aedc91a",
            ),
        )
    }
}
