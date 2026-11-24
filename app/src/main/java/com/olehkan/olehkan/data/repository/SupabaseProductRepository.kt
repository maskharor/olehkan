package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.remote.SupabaseClientManager
import com.olehkan.olehkan.data.remote.dto.ProductDto
import com.olehkan.olehkan.data.remote.dto.toDomain
import io.github.jan.supabase.postgrest.from

class SupabaseProductRepository(
    private val fallbackRepository: ProductRepository = DummyProductRepository(),
) : ProductRepository {

    override suspend fun getProducts(): List<Product> {
        return try {
            val dtos = SupabaseClientManager.client
                .from("products")
                .select()
                .decodeList<ProductDto>()

            if (dtos.isEmpty()) {
                fallbackRepository.getProducts()
            } else {
                dtos.map { it.toDomain() }
            }
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fallbackRepository.getProducts()
        }
    }

    override suspend fun getProductById(id: Int): Product? {
        return try {
            val dto = SupabaseClientManager.client
                .from("products")
                .select {
                    filter {
                        eq("id", id)
                    }
                }
                .decodeSingleOrNull<ProductDto>()

            dto?.toDomain() ?: fallbackRepository.getProductById(id)
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            fallbackRepository.getProductById(id)
        }
    }
}
