package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.ProductIdMapping
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class WishlistRepositoryImpl(
    initialFavorites: Set<Int> = emptySet()
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

    override suspend fun addToWishlist(productUuid: String): Boolean {
        val intId = ProductIdMapping.toId(productUuid)
        return addToFavorites(intId)
    }

    override suspend fun removeFromFavorites(productId: Int): Boolean {
        _favoriteIds.value = _favoriteIds.value - productId
        return true
    }

    override suspend fun removeFromWishlist(productUuid: String): Boolean {
        val intId = ProductIdMapping.toId(productUuid)
        return removeFromFavorites(intId)
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

    override fun clearCache() {
        _favoriteIds.value = emptySet()
    }
}

object WishlistRepositoryProvider {
    @Volatile private var instance: WishlistRepository? = null

    fun initialize(repository: WishlistRepository) {
        instance = repository
    }

    fun get(): WishlistRepository = instance ?: synchronized(this) {
        instance ?: WishlistRepositoryImpl().also { instance = it }
    }
}
