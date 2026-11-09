package com.olehkan.olehkan.data.model

data class Product(
    val id: Int,
    val name: String,
    val place: String,
    val price: Int,
    val rating: Double,
    val reviews: Int,
    val stock: Int,
    val category: String,
    val image: String,
    val description: String,
)


fun Product.formattedPrice(): String = "Rp${price.toLong().toString().formatRupiah()}"

private fun String.formatRupiah(): String {
    if (length <= 3) return this
    val result = StringBuilder()
    var count = 0
    for (i in this.length - 1 downTo 0) {
        result.append(this[i])
        count++
        if (count == 3 && i != 0) {
            result.append('.')
            count = 0
        }
    }
    return result.reverse().toString()
}