package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review
import com.olehkan.olehkan.data.model.SellerResponse
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class DummyReviewRepository : ReviewRepository {

    private val _reviewsState = MutableStateFlow(initialDummyReviews)
    override val reviewsFlow: StateFlow<List<Review>> = _reviewsState.asStateFlow()

    override suspend fun getReviewsForProduct(productId: Int?): List<Review> {
        delay(150)
        val all = _reviewsState.value
        return if (productId != null && productId > 0) {
            all.filter { it.productId == productId }
        } else {
            all
        }
    }

    override suspend fun getRatingSummary(productId: Int?): RatingSummary {
        delay(100)
        val productReviews = getReviewsForProduct(productId)
        if (productReviews.isEmpty()) {
            return RatingSummary(
                averageRating = 5.0,
                totalReviews = 0,
                starCounts = mapOf(5 to 0, 4 to 0, 3 to 0, 2 to 0, 1 to 0),
                satisfactionPercentage = 100,
            )
        }

        val total = productReviews.size
        val avg = productReviews.map { it.rating }.average()
        val counts = (1..5).associateWith { star ->
            productReviews.count { it.rating == star }
        }
        val satisfiedCount = productReviews.count { it.rating >= 4 }
        val satisfaction = if (total > 0) (satisfiedCount * 100 / total) else 100

        return RatingSummary(
            averageRating = String.format("%.1f", avg).replace(",", ".").toDoubleOrNull() ?: avg,
            totalReviews = total,
            starCounts = counts,
            satisfactionPercentage = satisfaction,
        )
    }

    override suspend fun addReview(review: Review) {
        delay(300)
        _reviewsState.update { current ->
            listOf(review) + current
        }
    }

    override suspend fun toggleHelpful(reviewId: String) {
        _reviewsState.update { current ->
            current.map { item ->
                if (item.id == reviewId) {
                    val newIsHelpful = !item.isHelpful
                    val newCount = if (newIsHelpful) item.helpfulCount + 1 else (item.helpfulCount - 1).coerceAtLeast(0)
                    item.copy(isHelpful = newIsHelpful, helpfulCount = newCount)
                } else {
                    item
                }
            }
        }
    }

    companion object {
        val initialDummyReviews: List<Review> = listOf(
            Review(
                id = "rev_101",
                productId = 1,
                productName = "Bakpia Pathok Premium",
                userName = "Dewi Rahmawati",
                userAvatar = null,
                rating = 5,
                date = "10 Nov 2026",
                comment = "Bakpia Pathok nya super lembut dan isian kacang hijaunya melimpah! Rasa manisnya pas tidak bikin enek, sangat cocok untuk oleh-oleh keluarga. Packing kardus tebal dan tidak ada yang hancur pas sampai Jakarta.",
                variant = "Varian: Kacang Hijau Original",
                isVerifiedBuyer = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1586511926871-deb1aca37f38?auto=format&fit=crop&w=600&q=80",
                    "https://images.unsplash.com/photo-1542897903-c9e9a8a0fd48?auto=format&fit=crop&w=600&q=80"
                ),
                sellerResponse = SellerResponse(
                    sellerName = "Sentra Bakpia Pathok 25",
                    date = "10 Nov 2026",
                    message = "Terima kasih Kak Dewi atas ulasan positifnya! Senang sekali Bakpia Pathok kami bisa dinikmati bersama keluarga di Jakarta. Ditunggu pesanan berikutnya ya kak! 🙏"
                ),
                helpfulCount = 18,
                isHelpful = false,
                tags = listOf("Rasa Enak & Autentik", "Pengemasan Rapi", "Segar & Berkualitas")
            ),
            Review(
                id = "rev_102",
                productId = 1,
                productName = "Bakpia Pathok Premium",
                userName = "Budi Santoso",
                userAvatar = null,
                rating = 5,
                date = "08 Nov 2026",
                comment = "Pengiriman kilat banget, kemarin pesan hari ini sudah sampai. Kualitas tekstur bakpianya masih fresh seperti baru matang dari oven. Top recommended!",
                variant = "Varian: Keju Spesial",
                isVerifiedBuyer = true,
                images = emptyList(),
                helpfulCount = 9,
                isHelpful = true,
                tags = listOf("Pengiriman Cepat", "Segar & Berkualitas")
            ),
            Review(
                id = "rev_103",
                productId = 2,
                productName = "Keripik Balado Uni",
                userName = "Rina Melati",
                userAvatar = null,
                rating = 5,
                date = "06 Nov 2026",
                comment = "Keripik balado uni benar-benar pedas manis gurih khas Minang! Krenyes banget dan cabainya beneran berasa rempahnya. Anak-anak dan suami suka sekali.",
                variant = "Varian: Pedas Manis 250g",
                isVerifiedBuyer = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1770782912846-9e4560df0031?auto=format&fit=crop&w=600&q=80"
                ),
                sellerResponse = SellerResponse(
                    sellerName = "Dapur Balado Uni Padang",
                    date = "07 Nov 2026",
                    message = "Rancak bana! Mokasih banyak Kak Rina sudah berbelanja keripik balado Uni Padang. Ditunggu orderan selanjutnya ya! 🌶️"
                ),
                helpfulCount = 14,
                isHelpful = false,
                tags = listOf("Rasa Enak & Autentik", "Pengemasan Rapi")
            ),
            Review(
                id = "rev_104",
                productId = 3,
                productName = "Dodol Garut Asli",
                userName = "Agus Kurniawan",
                userAvatar = null,
                rating = 4,
                date = "03 Nov 2026",
                comment = "Dodol Garut rasanya khas banget, manis legit dan aromanya santan alami. Kemasan rapi dan aman. Agak sedikit kenyal tapi tetep enak dipadu teh hangat.",
                variant = "Varian: Wijen Mix Rasa",
                isVerifiedBuyer = true,
                images = emptyList(),
                helpfulCount = 5,
                isHelpful = false,
                tags = listOf("Pengemasan Rapi", "Rasa Enak & Autentik")
            ),
            Review(
                id = "rev_105",
                productId = 4,
                productName = "Brem Madiun Klasik",
                userName = "Siti Aisyah",
                userAvatar = null,
                rating = 5,
                date = "01 Nov 2026",
                comment = "Sensasi lumer di mulutnya beneran nagih! Nostalgia masa kecil banget rasa Brem Madiun ini. Seller amanah dan respon pesan ramah.",
                variant = "Varian: Pack Isi 5",
                isVerifiedBuyer = true,
                images = listOf(
                    "https://images.unsplash.com/photo-1554367028-76d3d8711196?auto=format&fit=crop&w=600&q=80"
                ),
                helpfulCount = 7,
                isHelpful = false,
                tags = listOf("Rasa Enak & Autentik", "Pengiriman Cepat")
            ),
            Review(
                id = "rev_106",
                productId = 1,
                productName = "Bakpia Pathok Premium",
                userName = "Hendra Wijaya",
                userAvatar = null,
                rating = 4,
                date = "28 Okt 2026",
                comment = "Kulit bakpianya krispi di luar dan lembut di dalam. Expired date masih lama 2 minggu. Sangat cocok dijadikan buah tangan saat pulang dari Jogja.",
                variant = "Varian: Cokelat Lumer",
                isVerifiedBuyer = true,
                images = emptyList(),
                helpfulCount = 3,
                isHelpful = false,
                tags = listOf("Segar & Berkualitas")
            )
        )
    }
}
