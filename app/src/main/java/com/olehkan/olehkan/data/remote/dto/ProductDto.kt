package com.olehkan.olehkan.data.remote.dto

import com.olehkan.olehkan.data.model.Product
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductDto(
    val id: Int? = null,
    val name: String = "",
    val place: String = "",
    val price: Int = 0,
    val rating: Double = 5.0,
    val reviews: Int = 0,
    val stock: Int = 0,
    val category: String = "",
    val image: String = "",
    val description: String = "",
    @SerialName("seller_name") val sellerName: String = "Toko Oleh-Oleh Nusantara",
    @SerialName("seller_city") val sellerCity: String = "",
    @SerialName("seller_rating") val sellerRating: Double = 4.9,
    @SerialName("seller_badge") val sellerBadge: String = "Official Store",
)

fun ProductDto.toDomain(): Product {
    val city = sellerCity.ifBlank { place }
    return Product(
        id = id ?: 0,
        name = name,
        place = place,
        price = price,
        rating = rating,
        reviews = reviews,
        stock = stock,
        category = category,
        image = image,
        description = description,
        sellerName = sellerName,
        sellerCity = city,
        sellerRating = sellerRating,
        sellerBadge = sellerBadge,
    )
}

fun Product.toDto(): ProductDto {
    return ProductDto(
        id = if (id > 0) id else null,
        name = name,
        place = place,
        price = price,
        rating = rating,
        reviews = reviews,
        stock = stock,
        category = category,
        image = image,
        description = description,
        sellerName = sellerName,
        sellerCity = sellerCity,
        sellerRating = sellerRating,
        sellerBadge = sellerBadge,
    )
}
