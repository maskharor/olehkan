package com.olehkan.olehkan.ui.screens.reviews

import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.model.RatingSummary
import com.olehkan.olehkan.data.model.Review

data class ReviewsUiState(
    val isLoading: Boolean = true,
    val productId: Int? = null,
    val product: Product? = null,
    val reviews: List<Review> = emptyList(),
    val filteredReviews: List<Review> = emptyList(),
    val ratingSummary: RatingSummary = RatingSummary(),
    val selectedFilter: String = FILTER_ALL,
    val searchQuery: String = "",
    val isFormSheetOpen: Boolean = false,
    val editingReview: Review? = null,
    val formRating: Int = 5,
    val formReviewText: String = "",
    val isSubmitting: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
) {
    val isEditing: Boolean
        get() = editingReview != null

    companion object {
        const val FILTER_ALL = "Semua"
        const val FILTER_5_STAR = "Bintang 5"
        const val FILTER_4_STAR = "Bintang 4"
        const val FILTER_3_STAR = "Bintang 3"
        const val FILTER_2_STAR = "Bintang 2"
        const val FILTER_1_STAR = "Bintang 1"

        val filterOptions = listOf(
            FILTER_ALL,
            FILTER_5_STAR,
            FILTER_4_STAR,
            FILTER_3_STAR,
            FILTER_2_STAR,
            FILTER_1_STAR,
        )
    }
}
