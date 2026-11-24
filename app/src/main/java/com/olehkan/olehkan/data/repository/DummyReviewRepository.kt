package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review
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
        delay(200)
        _reviewsState.update { current ->
            listOf(review) + current
        }
    }

    override suspend fun updateReview(review: Review) {
        delay(200)
        _reviewsState.update { current ->
            current.map { item ->
                if (item.id == review.id) review else item
            }
        }
    }

    override suspend fun deleteReview(reviewId: String) {
        delay(200)
        _reviewsState.update { current ->
            current.filterNot { it.id == reviewId }
        }
    }

    companion object {
        val initialDummyReviews: List<Review> = listOf(
            Review(
                id = "rev_101",
                productId = 1,
                userName = "Dewi Rahmawati",
                rating = 5,
                createdAt = "10 Nov 2026",
                reviewText = "Bakpia Pathok nya super lembut dan isian kacang hijaunya melimpah! Rasa manisnya pas tidak bikin enek, sangat cocok untuk oleh-oleh keluarga. Packing kardus tebal.",
                isMyReview = false,
            ),
            Review(
                id = "rev_102",
                productId = 1,
                userName = "Nadia (Saya)",
                rating = 5,
                createdAt = "08 Nov 2026",
                reviewText = "Pengiriman kilat banget, kemarin pesan hari ini sudah sampai. Kualitas tekstur bakpianya masih fresh seperti baru matang dari oven. Sangat direkomendasikan!",
                isMyReview = true,
            ),
            Review(
                id = "rev_103",
                productId = 2,
                userName = "Rina Melati",
                rating = 5,
                createdAt = "06 Nov 2026",
                reviewText = "Keripik balado uni benar-benar pedas manis gurih khas Minang! Krenyes banget dan cabainya beneran berasa rempahnya.",
                isMyReview = false,
            ),
            Review(
                id = "rev_104",
                productId = 3,
                userName = "Agus Kurniawan",
                rating = 4,
                createdAt = "03 Nov 2026",
                reviewText = "Dodol Garut rasanya khas banget, manis legit dan aromanya santan alami. Kemasan rapi dan aman.",
                isMyReview = false,
            ),
        )
    }
}
