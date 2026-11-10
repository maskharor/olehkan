package com.olehkan.olehkan.ui.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Review
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.data.repository.ProductRepository
import com.olehkan.olehkan.data.repository.ReviewRepository
import com.olehkan.olehkan.data.repository.ReviewRepositoryProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewsViewModel(
    private val reviewRepository: ReviewRepository = ReviewRepositoryProvider.get(),
    private val productRepository: ProductRepository = DummyProductRepository(),
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReviewsUiState())
    val uiState: StateFlow<ReviewsUiState> = _uiState.asStateFlow()

    private var currentProductId: Int? = null

    fun load(productId: Int?) {
        currentProductId = productId
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, productId = productId, errorMessage = null) }
            try {
                val product = if (productId != null && productId > 0) {
                    productRepository.getProductById(productId)
                } else {
                    null
                }

                reviewRepository.reviewsFlow.collect { allReviews ->
                    val relevantReviews = if (productId != null && productId > 0) {
                        allReviews.filter { it.productId == productId }
                    } else {
                        allReviews
                    }

                    val summary = reviewRepository.getRatingSummary(productId)

                    _uiState.update { state ->
                        val filtered = filterReviews(
                            reviews = relevantReviews,
                            filter = state.selectedFilter,
                            query = state.searchQuery,
                        )
                        state.copy(
                            isLoading = false,
                            product = product,
                            reviews = relevantReviews,
                            filteredReviews = filtered,
                            ratingSummary = summary,
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Gagal memuat ulasan produk",
                    )
                }
            }
        }
    }

    fun onFilterSelected(filter: String) {
        _uiState.update { state ->
            val filtered = filterReviews(state.reviews, filter, state.searchQuery)
            state.copy(selectedFilter = filter, filteredReviews = filtered)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.update { state ->
            val filtered = filterReviews(state.reviews, state.selectedFilter, query)
            state.copy(searchQuery = query, filteredReviews = filtered)
        }
    }

    fun openWriteReviewSheet() {
        _uiState.update { it.copy(isWritingReviewSheetOpen = true) }
    }

    fun closeWriteReviewSheet() {
        _uiState.update { it.copy(isWritingReviewSheetOpen = false) }
    }

    fun setNewReviewRating(rating: Int) {
        _uiState.update { it.copy(newReviewRating = rating) }
    }

    fun setNewReviewComment(comment: String) {
        _uiState.update { it.copy(newReviewComment = comment) }
    }

    fun toggleNewReviewTag(tag: String) {
        _uiState.update { state ->
            val current = state.selectedNewTags
            val newTags = if (tag in current) current - tag else current + tag
            state.copy(selectedNewTags = newTags)
        }
    }

    fun toggleHelpful(reviewId: String) {
        viewModelScope.launch {
            reviewRepository.toggleHelpful(reviewId)
        }
    }

    fun submitReview() {
        val currentState = _uiState.value
        if (currentState.newReviewComment.isBlank()) {
            _uiState.update { it.copy(userMessage = "Silakan tulis ulasan terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val pId = currentState.productId ?: 1
                val pName = currentState.product?.name ?: "Bakpia Pathok Premium"

                val newReview = Review(
                    id = "rev_${System.currentTimeMillis()}",
                    productId = pId,
                    productName = pName,
                    userName = "Nadia (Anda)",
                    rating = currentState.newReviewRating,
                    date = currentDate,
                    comment = currentState.newReviewComment.trim(),
                    variant = "Varian: Original Premium",
                    isVerifiedBuyer = true,
                    images = emptyList(),
                    helpfulCount = 0,
                    isHelpful = false,
                    tags = currentState.selectedNewTags.toList(),
                )

                reviewRepository.addReview(newReview)

                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        isWritingReviewSheetOpen = false,
                        newReviewComment = "",
                        selectedNewTags = emptySet(),
                        newReviewRating = 5,
                        userMessage = "Ulasan berhasil dikirim! Terima kasih.",
                    )
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        userMessage = "Gagal mengirim ulasan: ${e.message}",
                    )
                }
            }
        }
    }

    fun clearUserMessage() {
        _uiState.update { it.copy(userMessage = null) }
    }

    private fun filterReviews(reviews: List<Review>, filter: String, query: String): List<Review> {
        var list = reviews
        if (query.isNotBlank()) {
            val q = query.trim().lowercase(Locale.ROOT)
            list = list.filter {
                it.comment.lowercase(Locale.ROOT).contains(q) ||
                        it.userName.lowercase(Locale.ROOT).contains(q) ||
                        it.productName.lowercase(Locale.ROOT).contains(q)
            }
        }

        return when (filter) {
            ReviewsUiState.FILTER_5_STAR -> list.filter { it.rating == 5 }
            ReviewsUiState.FILTER_4_STAR -> list.filter { it.rating == 4 }
            ReviewsUiState.FILTER_3_STAR -> list.filter { it.rating == 3 }
            ReviewsUiState.FILTER_WITH_PHOTO -> list.filter { it.images.isNotEmpty() }
            ReviewsUiState.FILTER_WITH_COMMENT -> list.filter { it.comment.isNotBlank() }
            else -> list
        }
    }
}
