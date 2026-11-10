package com.olehkan.olehkan.data.repository

import kotlinx.coroutines.flow.StateFlow

interface WishlistRepository {
    val favoriteIds: StateFlow<Set<Int>>

    suspend fun loadFavorites(): List<Int>
    suspend fun addToFavorites(productId: Int): Boolean
    suspend fun removeFromFavorites(productId: Int): Boolean
    suspend fun toggleFavorite(productId: Int): Boolean
    fun isFavorite(productId: Int): Boolean
}

