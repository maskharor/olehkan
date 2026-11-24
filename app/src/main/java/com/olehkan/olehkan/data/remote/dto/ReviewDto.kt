package com.olehkan.olehkan.data.remote.dto

import com.olehkan.olehkan.data.model.ProductIdMapping
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

fun ReviewDto.toDomain(currentUserId: String = ""): Review {
    val intProductId = ProductIdMapping.toId(productId)
    val uId = userId ?: ""
    val isMine = (uId.isNotBlank() && uId == currentUserId) || uId.isBlank()

    return Review(
        id = id ?: "",
        userId = uId,
        productId = intProductId,
        orderItemId = orderItemId,
        userName = if (isMine) "Saya (Nadia)" else "Pelanggan OlehKan",
        rating = rating,
        reviewText = reviewText,
        createdAt = createdAt?.take(10) ?: "Baru saja",
        isMyReview = isMine,
    )
}

fun Review.toDto(): ReviewDto {
    val uuidProduct = ProductIdMapping.toUuid(productId)
    return ReviewDto(
        id = if (id.isNotBlank() && !id.startsWith("rev_")) id else null,
        userId = userId.takeIf { it.isNotBlank() && it.contains("-") },
        productId = uuidProduct,
        orderItemId = orderItemId?.takeIf { it.isNotBlank() && it.contains("-") },
        rating = rating,
        reviewText = reviewText,
    )
}
