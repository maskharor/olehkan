
package com.olehkan.olehkan.ui.screens.orders

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderItem
import com.olehkan.olehkan.data.model.OrderStatus
import java.text.NumberFormat
import java.util.Locale

@Composable
fun OrderDetailScreen(
    orderId: String,
    viewModel: OrderViewModel,
    onBack: () -> Unit,
    onPayClick: (String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        viewModel.loadOrder(orderId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(onClick = onBack) {
                Text("Kembali")
            }

            Text(
                text = "Detail Pesanan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp)
            )
        }

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(Modifier.height(12.dp))
                    Text("Memuat detail pesanan...")
                }
            }

            state.error != null && state.selectedOrder == null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.error ?: "Terjadi kesalahan.")
                    Spacer(Modifier.height(12.dp))
                    OutlinedButton(
                        onClick = { viewModel.loadOrder(orderId) }
                    ) {
                        Text("Coba Lagi")
                    }
                }
            }

            state.selectedOrder != null -> {
                OrderDetailContent(
                    order = state.selectedOrder!!,
                    isCancelling = state.isCancelling,
                    onCancel = {
                        viewModel.cancelOrder(orderId)
                    },
                    onPayClick = onPayClick
                )
            }

            else -> {
                Text(
                    "Pesanan tidak ditemukan.",
                    modifier = Modifier.padding(24.dp)
                )
            }
        }
    }
}

@Composable
private fun OrderDetailContent(
    order: Order,
    isCancelling: Boolean,
    onCancel: () -> Unit,
    onPayClick: (String) -> Unit
) {
    val canCancel = order.status == OrderStatus.PENDING ||
            order.status == OrderStatus.PROCESSING

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp)
            ) {
                Text(
                    "Nomor Pesanan",
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    order.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(Modifier.height(12.dp))

                Text("Status Pesanan")
                Text(
                    statusLabel(order.status),
                    color = statusColor(order.status),
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item {
            DetailSection(title = "Informasi Penerima") {
                DetailRow("Nama", order.recipientName)
                DetailRow("Nomor Telepon", order.phoneNumber)
                DetailRow("Alamat", order.shippingAddress)
                if (!order.notes.isNullOrBlank()) {
                    DetailRow("Catatan", order.notes)
                }
            }
        }

        item {
            Text(
                "Produk Pesanan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(order.items, key = { it.id }) { item ->
            OrderItemRow(item)
        }

        item {
            DetailSection(title = "Rincian Pembayaran") {
                DetailRow(
                    "Total Produk",
                    formatRupiah(order.totalAmount)
                )
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                DetailRow(
                    "Total Pembayaran",
                    formatRupiah(order.totalAmount),
                    bold = true
                )
            }
        }

        if (order.status == OrderStatus.PENDING) {
            item {
                Button(
                    onClick = {
                        onPayClick(order.id)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Bayar Sekarang")
                }
            }
        }

        if (canCancel) {
            item {
                Button(
                    onClick = onCancel,
                    enabled = !isCancelling,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    if (isCancelling) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp),
                            color = MaterialTheme.colorScheme.onError
                        )
                    } else {
                        Text("Batalkan Pesanan")
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DetailSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))
        content()
    }
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
    bold: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label,
            modifier = Modifier.weight(1f),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            value,
            modifier = Modifier.weight(1.5f),
            fontWeight = if (bold) FontWeight.Bold else FontWeight.Normal,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun OrderItemRow(item: OrderItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                MaterialTheme.colorScheme.surface,
                RoundedCornerShape(12.dp)
            )
            .padding(14.dp)
    ) {
        Text(
            item.productName,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        DetailRow(
            "${item.quantity} × ${formatRupiah(item.unitPrice)}",
            formatRupiah(item.subtotal)
        )
    }
}

private fun formatRupiah(amount: Long): String {
    val formatter = NumberFormat.getCurrencyInstance(
        Locale("id", "ID")
    )
    formatter.maximumFractionDigits = 0
    formatter.minimumFractionDigits = 0
    return formatter.format(amount)
}

private fun statusLabel(status: String): String {
    return when (status) {
        OrderStatus.PENDING -> "Menunggu Pembayaran"
        OrderStatus.PROCESSING -> "Sedang Diproses"
        OrderStatus.COMPLETED -> "Selesai"
        OrderStatus.CANCELLED -> "Dibatalkan"
        else -> status
    }
}

@Composable
private fun statusColor(status: String) =
    when (status) {
        OrderStatus.PENDING -> MaterialTheme.colorScheme.tertiary
        OrderStatus.PROCESSING -> MaterialTheme.colorScheme.primary
        OrderStatus.COMPLETED -> MaterialTheme.colorScheme.primary
        OrderStatus.CANCELLED -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.onSurface
    }
