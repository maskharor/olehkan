package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review
import com.olehkan.olehkan.data.remote.SupabaseClientManager
import com.olehkan.olehkan.data.remote.dto.ReviewDto
import com.olehkan.olehkan.data.remote.dto.toDto
import com.olehkan.olehkan.data.remote.dto.toDomain
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SupabaseReviewRepository(
    private val fallbackRepository: ReviewRepository = DummyReviewRepository(),
) : ReviewRepository {

    private val _reviewsState = MutableStateFlow<List<Review>>(emptyList())
    override val reviewsFlow: StateFlow<List<Review>> = _reviewsState.asStateFlow()

    override suspend fun getReviewsForProduct(productId: Int?): List<Review> {
        return try {
            val dtos = if (productId != null && productId > 0) {
                SupabaseClientManager.client
                    .from("reviews")
                    .select {
                        filter {
                            eq("product_id", productId.toString())
                        }
                    }
                    .decodeList<ReviewDto>()
            } else {
                SupabaseClientManager.client
                    .from("reviews")
                    .select()
                    .decodeList<ReviewDto>()
            }

            if (dtos.isEmpty()) {
                val fallback = fallbackRepository.getReviewsForProduct(productId)
                _reviewsState.value = fallback
                fallback
            } else {
                val domainReviews = dtos.map { it.toDomain() }
                _reviewsState.value = domainReviews
                domainReviews
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            val fallback = fallbackRepository.getReviewsForProduct(productId)
            _reviewsState.value = fallback
            fallback
        }
    }

    override suspend fun getRatingSummary(productId: Int?): RatingSummary {
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
        try {
            val dto = review.toDto()
            SupabaseClientManager.client
                .from("reviews")
                .insert(dto)

            _reviewsState.update { current ->
                listOf(review) + current
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fallbackRepository.addReview(review)
            _reviewsState.update { current ->
                listOf(review) + current
            }
        }
    }

    override suspend fun updateReview(review: Review) {
        try {
            SupabaseClientManager.client
                .from("reviews")
                .update(
                    {
                        set("rating", review.rating)
                        set("review_text", review.reviewText)
                    }
                ) {
                    filter {
                        eq("id", review.id)
                    }
                }

            _reviewsState.update { current ->
                current.map { if (it.id == review.id) review else it }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fallbackRepository.updateReview(review)
            _reviewsState.update { current ->
                current.map { if (it.id == review.id) review else it }
            }
        }
    }

    override suspend fun deleteReview(reviewId: String) {
        try {
            SupabaseClientManager.client
                .from("reviews")
                .delete {
                    filter {
                        eq("id", reviewId)
                    }
                }

            _reviewsState.update { current ->
                current.filterNot { it.id == reviewId }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fallbackRepository.deleteReview(reviewId)
            _reviewsState.update { current ->
                current.filterNot { it.id == reviewId }
            }
        }
    }
}
