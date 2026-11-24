
package com.olehkan.olehkan.ui.screens.orders

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.OrderItemDraft
import java.text.NumberFormat
import java.util.Locale

@Composable
fun CheckoutScreen(
    productId: Int,
    quantity: Int = 1,
    onBack: () -> Unit,
    onDone: (String) -> Unit,
    viewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(
            AppContainer.get(LocalContext.current).orders
        )
    )
) {
    val context = LocalContext.current
    val appContainer = remember(context) {
        AppContainer.get(context)
    }

    val state by viewModel.uiState.collectAsState()

    var product by remember { mutableStateOf<Product?>(null) }
    var isLoadingProduct by remember { mutableStateOf(true) }
    var productError by remember { mutableStateOf<String?>(null) }

    var recipientName by remember { mutableStateOf("") }
    var phoneNumber by remember { mutableStateOf("") }
    var shippingAddress by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var validationError by remember { mutableStateOf<String?>(null) }

    // Mengambil produk berdasarkan ID internal aplikasi.
    LaunchedEffect(productId) {
        isLoadingProduct = true
        productError = null

        try {
            product = appContainer.products.getProductById(productId)
                ?: throw IllegalStateException("Produk tidak ditemukan.")
        } catch (e: Exception) {
            if (e is kotlinx.coroutines.CancellationException) throw e
            productError = e.message ?: "Gagal mengambil informasi produk."
        } finally {
            isLoadingProduct = false
        }
    }

    // Setelah pesanan berhasil dibuat, kirim UUID pesanan ke navigasi.
    LaunchedEffect(state.createdOrder?.id) {
        state.createdOrder?.id?.let { orderId ->
            viewModel.clearCreatedOrder()
            onDone(orderId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "Checkout",
            style = MaterialTheme.typography.headlineMedium
        )

        when {
            isLoadingProduct -> {
                CircularProgressIndicator()
                Text("Memuat informasi produk...")
            }

            productError != null -> {
                Text(
                    text = productError ?: "Gagal memuat produk.",
                    color = MaterialTheme.colorScheme.error
                )

                TextButton(onClick = onBack) {
                    Text("Kembali")
                }
            }

            product != null -> {
                val selectedProduct = product!!

                Text(
                    text = selectedProduct.name,
                    style = MaterialTheme.typography.titleLarge
                )

                Text(
                    text = "Harga: ${formatRupiah(selectedProduct.price.toLong())}"
                )

                Text(
                    text = "Jumlah: $quantity"
                )

                Text(
                    text = "Subtotal: ${
                        formatRupiah(selectedProduct.price.toLong() * quantity)
                    }",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )

                HorizontalSection()

                Text(
                    text = "Informasi Penerima",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = recipientName,
                    onValueChange = { recipientName = it },
                    label = { Text("Nama Penerima") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isCreating
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Nomor Telepon") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = !state.isCreating
                )

                OutlinedTextField(
                    value = shippingAddress,
                    onValueChange = { shippingAddress = it },
                    label = { Text("Alamat Pengiriman") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    enabled = !state.isCreating
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan (Opsional)") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 2,
                    enabled = !state.isCreating
                )

                HorizontalSection()

                Text(
                    text = "Ringkasan Pesanan",
                    style = MaterialTheme.typography.titleMedium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Pembayaran")
                    Text(
                        text = formatRupiah(
                            selectedProduct.price.toLong() * quantity
                        ),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                validationError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                state.error?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Button(
                    onClick = {
                        validationError = when {
                            selectedProduct.uuid.isBlank() ->
                                "ID produk tidak valid."

                            selectedProduct.stock <= 0 ->
                                "Stok produk sudah habis."

                            quantity <= 0 || quantity > selectedProduct.stock ->
                                "Jumlah pembelian melebihi stok tersedia."

                            recipientName.isBlank() ->
                                "Nama penerima wajib diisi."

                            phoneNumber.isBlank() ->
                                "Nomor telepon wajib diisi."

                            shippingAddress.isBlank() ->
                                "Alamat pengiriman wajib diisi."

                            else -> null
                        }

                        if (validationError == null) {
                            viewModel.createOrder(
                                recipientName = recipientName.trim(),
                                phoneNumber = phoneNumber.trim(),
                                shippingAddress = shippingAddress.trim(),
                                notes = notes.trim().ifBlank { null },
                                items = listOf(
                                    OrderItemDraft(
                                        productId = selectedProduct.uuid,
                                        productName = selectedProduct.name,
                                        unitPrice = selectedProduct.price.toLong(),
                                        quantity = quantity
                                    )
                                )
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !state.isCreating &&
                            !isLoadingProduct &&
                            productError == null &&
                            product != null
                ) {
                    if (state.isCreating) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Buat Pesanan")
                    }
                }

                TextButton(
                    onClick = onBack,
                    enabled = !state.isCreating,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kembali")
                }
            }
        }
    }
}

@Composable
private fun HorizontalSection() {
    Spacer(modifier = Modifier.height(4.dp))
}

private fun formatRupiah(amount: Long): String {
    return NumberFormat.getCurrencyInstance(
        Locale("id", "ID")
    ).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(amount)
}
