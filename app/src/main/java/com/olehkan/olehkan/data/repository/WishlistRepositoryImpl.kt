package com.olehkan.olehkan.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Wishlist Repository berbasis Local In-Memory.
 * (Integrasi Supabase akan diaktifkan pada progress minggu depan).
 */
class WishlistRepositoryImpl(
    initialFavorites: Set<Int> = setOf(1, 2, 3, 4)
) : WishlistRepository {

    private val _favoriteIds = MutableStateFlow(initialFavorites)
    override val favoriteIds: StateFlow<Set<Int>> = _favoriteIds.asStateFlow()

    override suspend fun loadFavorites(): List<Int> {
        return _favoriteIds.value.toList()
    }

    override suspend fun addToFavorites(productId: Int): Boolean {
        _favoriteIds.value = _favoriteIds.value + productId
        return true
    }

    override suspend fun removeFromFavorites(productId: Int): Boolean {
        _favoriteIds.value = _favoriteIds.value - productId
        return true
    }

    override suspend fun toggleFavorite(productId: Int): Boolean {
        return if (isFavorite(productId)) {
            removeFromFavorites(productId)
            false
        } else {
            addToFavorites(productId)
            true
        }
    }

    override fun isFavorite(productId: Int): Boolean {
        return _favoriteIds.value.contains(productId)
    }
}

object WishlistRepositoryProvider {
    private val instance: WishlistRepository by lazy {
        WishlistRepositoryImpl()
    }

    fun get(): WishlistRepository = instance
}
