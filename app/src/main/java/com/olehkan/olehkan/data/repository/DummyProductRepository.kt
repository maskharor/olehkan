package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product
import kotlinx.coroutines.delay

class DummyProductRepository : ProductRepository {

    override suspend fun getProducts(): List<Product> {
        delay(300)
        return dummyProducts
    }

    override suspend fun getProductById(id: Int): Product? {
        delay(150)
        return dummyProducts.find { it.id == id }
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
                image = "https://images.unsplash.com/photo-1586511926871-deb1aca37f38?auto=format&fit=crop&w=800&q=85",
                description = "Bakpia lembut dengan kulit tipis dan isian kacang hijau pilihan. Dibuat segar setiap hari oleh perajin lokal Pathok.",
                sellerName = "Sentra Bakpia Pathok 25",
                sellerCity = "Yogyakarta",
                sellerRating = 4.9,
                sellerBadge = "Official Store",
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
                image = "https://images.unsplash.com/photo-1770782912846-9e4560df0031?auto=format&fit=crop&w=800&q=85",
                description = "Keripik singkong renyah dengan balutan cabai merah khas Minang. Pedas, manis, dan cocok untuk teman perjalanan.",
                sellerName = "Dapur Balado Uni Padang",
                sellerCity = "Padang",
                sellerRating = 4.8,
                sellerBadge = "Super Seller",
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
                image = "https://images.unsplash.com/photo-1542897903-c9e9a8a0fd48?auto=format&fit=crop&w=800&q=85",
                description = "Dodol tradisional bertekstur lembut dengan rasa legit dan aroma santan yang khas, dikemas praktis untuk buah tangan.",
                sellerName = "Dodol Warisan Picnic Garut",
                sellerCity = "Garut",
                sellerRating = 4.7,
                sellerBadge = "Official Store",
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
                image = "https://images.unsplash.com/photo-1554367028-76d3d8711196?auto=format&fit=crop&w=800&q=85",
                description = "Brem putih klasik dengan sensasi lumer di mulut. Fermentasi alami memberikan rasa manis dan sedikit asam yang unik.",
                sellerName = "Sumber Rasa Madiun",
                sellerCity = "Madiun",
                sellerRating = 4.6,
                sellerBadge = "Super Seller",
            ),
        )
    }
}
