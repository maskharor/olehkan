package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review
import kotlinx.coroutines.flow.StateFlow

interface ReviewRepository {
    val reviewsFlow: StateFlow<List<Review>>
    suspend fun getReviewsForProduct(productId: Int?): List<Review>
    suspend fun getRatingSummary(productId: Int?): RatingSummary
    suspend fun addReview(review: Review)
    suspend fun toggleHelpful(reviewId: String)
}

object ReviewRepositoryProvider {
    private var instance: ReviewRepository? = null

    fun get(): ReviewRepository {
        return instance ?: DummyReviewRepository().also { instance = it }
    }
}
