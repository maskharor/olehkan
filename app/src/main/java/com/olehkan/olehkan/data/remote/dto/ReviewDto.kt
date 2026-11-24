package com.olehkan.olehkan.data.remote.dto

import com.olehkan.olehkan.data.model.Review
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ReviewDto(
    val id: String? = null,
    @SerialName("user_id") val userId: String? = null,
    @SerialName("product_id") val productId: String,
    @SerialName("order_item_id") val orderItemId: String? = null,
    val rating: Int,
    @SerialName("review_text") val reviewText: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
)

fun ReviewDto.toDomain(currentUserId: String = "00000000-0000-0000-0000-000000000000"): Review {
    val pId = productId.toIntOrNull() ?: 1
    val uId = userId ?: "00000000-0000-0000-0000-000000000000"
    val isMine = (uId == currentUserId || uId == "00000000-0000-0000-0000-000000000000")

    return Review(
        id = id ?: "",
        userId = uId,
        productId = pId,
        orderItemId = orderItemId,
        userName = if (isMine) "Saya (Nadia)" else "Pelanggan OlehKan",
        rating = rating,
        reviewText = reviewText,
        createdAt = createdAt?.take(10) ?: "Baru saja",
        isMyReview = isMine,
    )
}

fun Review.toDto(): ReviewDto {
    return ReviewDto(
        id = if (id.isNotBlank() && !id.startsWith("rev_")) id else null,
        userId = userId.ifBlank { "00000000-0000-0000-0000-000000000000" },
        productId = productId.toString(),
        orderItemId = orderItemId,
        rating = rating,
        reviewText = reviewText,
    )
}
