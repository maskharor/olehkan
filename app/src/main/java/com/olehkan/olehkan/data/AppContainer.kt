package com.olehkan.olehkan.data

import android.content.Context
import com.olehkan.olehkan.data.auth.EncryptedSessionStore
import com.olehkan.olehkan.data.network.SupabaseHttp
import com.olehkan.olehkan.data.repository.SupabaseAuthRepository
import com.olehkan.olehkan.data.repository.SupabaseCategoryRepository
import com.olehkan.olehkan.data.repository.SupabaseProductRepository
import com.olehkan.olehkan.data.repository.SupabaseWishlistRepository
import com.olehkan.olehkan.data.repository.WishlistRepositoryProvider

class AppContainer private constructor(context: Context) {
    val auth = SupabaseAuthRepository(SupabaseHttp(context), EncryptedSessionStore(context))
    val categories = SupabaseCategoryRepository(auth,
        context.getSharedPreferences("olehkan_categories", Context.MODE_PRIVATE))
    val wishlist = SupabaseWishlistRepository(auth)
    val products = SupabaseProductRepository(auth)

    init {
        WishlistRepositoryProvider.initialize(wishlist)
    }

    companion object {
        @Volatile private var instance: AppContainer? = null
        fun get(context: Context): AppContainer = instance ?: synchronized(this) {
            instance ?: AppContainer(context.applicationContext).also { instance = it }
        }
    }
}
