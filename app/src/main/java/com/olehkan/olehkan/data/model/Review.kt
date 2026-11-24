package com.olehkan.olehkan.data.model

data class Review(
    val id: String,
    val productId: Int,
    val productName: String = "",
    val userName: String,
    val userAvatar: String? = null,
    val rating: Int,
    val date: String,
    val comment: String,
    val variant: String? = null,
    val isVerifiedBuyer: Boolean = true,
    val images: List<String> = emptyList(),
    val sellerResponse: SellerResponse? = null,
    val helpfulCount: Int = 0,
    val isHelpful: Boolean = false,
    val tags: List<String> = emptyList(),
)

data class SellerResponse(
    val sellerName: String,
    val date: String,
    val message: String,
)

data class RatingSummary(
    val averageRating: Double = 4.8,
    val totalReviews: Int = 128,
    val starCounts: Map<Int, Int> = mapOf(
        5 to 102,
        4 to 18,
        3 to 5,
        2 to 2,
        1 to 1,
    ),
    val satisfactionPercentage: Int = 96,
    val topTags: List<Pair<String, Int>> = listOf(
        "Rasa Enak & Autentik" to 42,
        "Pengemasan Rapi" to 38,
        "Pengiriman Cepat" to 35,
        "Segar & Berkualitas" to 29,
    ),
)
