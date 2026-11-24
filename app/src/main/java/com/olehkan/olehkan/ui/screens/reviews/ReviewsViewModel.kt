package com.olehkan.olehkan.ui.screens.reviews

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.olehkan.olehkan.data.model.Review
import androidx.lifecycle.ViewModelProvider
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
    private val productRepository: ProductRepository,
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
                } else null

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

    fun openCreateForm() {
        _uiState.update {
            it.copy(
                isFormSheetOpen = true,
                editingReview = null,
                formRating = 5,
                formReviewText = "",
            )
        }
    }

    fun openEditForm(review: Review) {
        _uiState.update {
            it.copy(
                isFormSheetOpen = true,
                editingReview = review,
                formRating = review.rating,
                formReviewText = review.reviewText,
            )
        }
    }

    fun closeFormSheet() {
        _uiState.update {
            it.copy(
                isFormSheetOpen = false,
                editingReview = null,
                formReviewText = "",
            )
        }
    }

    fun setFormRating(rating: Int) {
        _uiState.update { it.copy(formRating = rating) }
    }

    fun setFormReviewText(text: String) {
        _uiState.update { it.copy(formReviewText = text) }
    }

    fun saveReview() {
        val currentState = _uiState.value
        if (currentState.formReviewText.isBlank()) {
            _uiState.update { it.copy(userMessage = "Silakan tulis testimoni ulasan terlebih dahulu.") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSubmitting = true) }
            try {
                val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                val currentDate = dateFormat.format(Date())

                val editing = currentState.editingReview
                if (editing == null) {
                    // Create New Review (Insert)
                    val pId = currentState.productId ?: 1
                    val newReview = Review(
                        id = "rev_${System.currentTimeMillis()}",
                        productId = pId,
                        userName = "Nadia (Saya)",
                        rating = currentState.formRating,
                        reviewText = currentState.formReviewText.trim(),
                        createdAt = currentDate,
                        isMyReview = true,
                    )
                    reviewRepository.addReview(newReview)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isFormSheetOpen = false,
                            formReviewText = "",
                            formRating = 5,
                            userMessage = "Ulasan berhasil dikirim ke Supabase!",
                        )
                    }
                } else {
                    // Update Existing Review (Update)
                    val updatedReview = editing.copy(
                        rating = currentState.formRating,
                        reviewText = currentState.formReviewText.trim(),
                    )
                    reviewRepository.updateReview(updatedReview)
                    _uiState.update {
                        it.copy(
                            isSubmitting = false,
                            isFormSheetOpen = false,
                            editingReview = null,
                            formReviewText = "",
                            formRating = 5,
                            userMessage = "Ulasan berhasil diperbarui di Supabase!",
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(
                        isSubmitting = false,
                        userMessage = "Gagal menyimpan ulasan: ${e.message}",
                    )
                }
            }
        }
    }

    fun deleteReview(reviewId: String) {
        viewModelScope.launch {
            try {
                reviewRepository.deleteReview(reviewId)
                _uiState.update {
                    it.copy(userMessage = "Ulasan berhasil dihapus dari Supabase!")
                }
            } catch (e: Exception) {
                if (e is kotlinx.coroutines.CancellationException) throw e
                _uiState.update {
                    it.copy(userMessage = "Gagal menghapus ulasan: ${e.message}")
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
                it.reviewText.lowercase(Locale.ROOT).contains(q) ||
                        it.userName.lowercase(Locale.ROOT).contains(q)
            }
        }

        return when (filter) {
            ReviewsUiState.FILTER_5_STAR -> list.filter { it.rating == 5 }
            ReviewsUiState.FILTER_4_STAR -> list.filter { it.rating == 4 }
            ReviewsUiState.FILTER_3_STAR -> list.filter { it.rating == 3 }
            ReviewsUiState.FILTER_2_STAR -> list.filter { it.rating == 2 }
            ReviewsUiState.FILTER_1_STAR -> list.filter { it.rating == 1 }
            else -> list
        }
    }
}

class ReviewsViewModelFactory(
    private val productRepository: ProductRepository,
    private val reviewRepository: ReviewRepository = ReviewRepositoryProvider.get(),
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ReviewsViewModel::class.java)) {
            return ReviewsViewModel(reviewRepository, productRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
