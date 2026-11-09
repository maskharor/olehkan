package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Product

interface ProductRepository {

    suspend fun getProducts(): List<Product>
    suspend fun getProductById(id: Int): Product?
}