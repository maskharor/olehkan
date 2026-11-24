package com.olehkan.olehkan.data.model

data class Review(
    val id: String,
    val userId: String = "00000000-0000-0000-0000-000000000000",
    val productId: Int,
    val orderItemId: String? = null,
    val userName: String = "Pelanggan",
    val rating: Int,
    val reviewText: String,
    val createdAt: String = "",
    val isMyReview: Boolean = false,
)

data class RatingSummary(
    val averageRating: Double = 5.0,
    val totalReviews: Int = 0,
    val starCounts: Map<Int, Int> = mapOf(
        5 to 0,
        4 to 0,
        3 to 0,
        2 to 0,
        1 to 0,
    ),
    val satisfactionPercentage: Int = 100,
)
