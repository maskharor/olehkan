package com.olehkan.olehkan.data.repository

import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderStatus
import kotlinx.coroutines.delay

object DummyOrderRepository : OrderRepository {

    private val orders: MutableList<Order> = DummyOrderData.initialOrders.toMutableList()

    override suspend fun getOrders(): List<Order> {
        delay(400)
        return orders.toList()
    }

    override suspend fun getOrdersByStatus(isActive: Boolean): List<Order> {
        delay(400)
        return orders.filter { it.status.isActive == isActive }
    }

    override suspend fun getOrderById(id: String): Order? {
        delay(300)
        return orders.find { it.id == id }
    }

    override suspend fun createOrder(productId: Int, quantity: Int): Order {
        delay(500)
        val product = DummyProductRepository.dummyProducts
            .find { it.id == productId }
            ?: error("Product id=$productId not found")

        val totalPrice = product.price * quantity + 12_000
        val newOrder = Order(
            id = "ORD-${(orders.size + 1).toString().padStart(3, '0')}",
            product = product,
            quantity = quantity,
            status = OrderStatus.MENUNGGU_PEMBAYARAN,
            orderDate = "10 Nov 2026",
            totalPrice = totalPrice,
            shippingCost = 12_000,
            hasTracking = false,
            needsPayment = true,
        )
        orders.add(0, newOrder)
        return newOrder
    }

    override suspend fun updateStatus(orderId: String, newStatus: OrderStatus): Order? {
        delay(300)
        val idx = orders.indexOfFirst { it.id == orderId }
        if (idx < 0) return null
        val updated = orders[idx].copy(
            status = newStatus,
            needsPayment = false,
            hasTracking = newStatus == OrderStatus.DALAM_PENGIRIMAN,
        )
        orders[idx] = updated
        return updated
    }
}

// ============================================
// INI YANG HILANG DI FILE ANDA! (WAJIB ADA)
// ============================================
object DummyOrderData {
    val initialOrders: List<Order> = listOf(
        Order(
            id = "ORD-001",
            product = DummyProductRepository.dummyProducts[0],
            quantity = 1,
            status = OrderStatus.MENUNGGU_PEMBAYARAN,
            orderDate = "12 Jun 2025",
            totalPrice = DummyProductRepository.dummyProducts[0].price + 12_000,
            shippingCost = 12_000,
            hasTracking = false,
            needsPayment = true,
        ),
        Order(
            id = "ORD-002",
            product = DummyProductRepository.dummyProducts[1],
            quantity = 1,
            status = OrderStatus.DALAM_PENGIRIMAN,
            orderDate = "12 Jun 2025",
            totalPrice = DummyProductRepository.dummyProducts[1].price + 12_000,
            shippingCost = 12_000,
            hasTracking = true,
            needsPayment = false,
        ),
        Order(
            id = "ORD-003",
            product = DummyProductRepository.dummyProducts[2],
            quantity = 1,
            status = OrderStatus.SELESAI,
            orderDate = "8 Jun 2025",
            totalPrice = DummyProductRepository.dummyProducts[2].price + 12_000,
            shippingCost = 12_000,
            hasTracking = false,
            needsPayment = false,
        ),
        Order(
            id = "ORD-004",
            product = DummyProductRepository.dummyProducts[3],
            quantity = 1,
            status = OrderStatus.SELESAI,
            orderDate = "1 Jun 2025",
            totalPrice = DummyProductRepository.dummyProducts[3].price + 12_000,
            shippingCost = 12_000,
            hasTracking = false,
            needsPayment = false,
        ),
    )
}