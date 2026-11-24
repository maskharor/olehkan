package com.olehkan.olehkan.data.model

data class Category(
    val id: String,
    val name: String,
    val productCount: Int = 0,
    val isProtected: Boolean = productCount > 0,
)
