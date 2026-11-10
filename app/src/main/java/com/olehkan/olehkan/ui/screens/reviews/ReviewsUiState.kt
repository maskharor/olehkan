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
    val isWritingReviewSheetOpen: Boolean = false,
    val newReviewRating: Int = 5,
    val newReviewComment: String = "",
    val newReviewVariant: String = "Variasi Default",
    val selectedNewTags: Set<String> = emptySet(),
    val isSubmitting: Boolean = false,
    val userMessage: String? = null,
    val errorMessage: String? = null,
) {
    companion object {
        const val FILTER_ALL = "Semua"
        const val FILTER_5_STAR = "5★ Star"
        const val FILTER_4_STAR = "4★ Star"
        const val FILTER_3_STAR = "3★ Star"
        const val FILTER_WITH_PHOTO = "Dengan Foto"
        const val FILTER_WITH_COMMENT = "Dengan Komentar"

        val filterOptions = listOf(
            FILTER_ALL,
            FILTER_5_STAR,
            FILTER_4_STAR,
            FILTER_3_STAR,
            FILTER_WITH_PHOTO,
            FILTER_WITH_COMMENT,
        )

        val quickTags = listOf(
            "Rasa Enak & Autentik",
            "Pengemasan Rapi",
            "Pengiriman Cepat",
            "Segar & Berkualitas",
            "Harga Worth It",
            "Sesuai Deskripsi",
        )
    }
}
