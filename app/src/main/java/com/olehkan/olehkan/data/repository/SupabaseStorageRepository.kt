package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.remote.SupabaseClientManager
import io.github.jan.supabase.storage.storage

class SupabaseStorageRepository {

    suspend fun uploadReviewImage(fileName: String, byteArray: ByteArray): String? {
        return try {
            val bucket = SupabaseClientManager.client.storage.from("review-images")
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        }
    }

    suspend fun uploadProductImage(fileName: String, byteArray: ByteArray): String? {
        return try {
            val bucket = SupabaseClientManager.client.storage.from("product-images")
            bucket.upload(fileName, byteArray) {
                upsert = true
            }
            bucket.publicUrl(fileName)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            null
        }
    }
}
