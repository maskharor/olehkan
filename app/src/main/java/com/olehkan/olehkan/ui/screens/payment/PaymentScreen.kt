
package com.olehkan.olehkan.ui.screens.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
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
import com.olehkan.olehkan.data.model.PaymentMethod
import com.olehkan.olehkan.data.model.PaymentStatus
import com.olehkan.olehkan.ui.screens.orders.OrderViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    orderId: String,
    orderViewModel: OrderViewModel,
    paymentViewModel: PaymentViewModel,
    onBack: () -> Unit,
    onPaymentSuccess: () -> Unit
) {
    val orderState by orderViewModel.uiState.collectAsState()
    val paymentState by paymentViewModel.uiState.collectAsState()

    LaunchedEffect(orderId) {
        orderViewModel.loadOrder(orderId)
        paymentViewModel.loadPayment(orderId)
    }

    LaunchedEffect(paymentState.isPaid) {
        if (paymentState.isPaid) {
            onPaymentSuccess()
        }
    }

    val order = orderState.selectedOrder

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Text(
            "Pembayaran Pesanan",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(20.dp))

        when {
            orderState.isLoading || paymentState.isLoading -> {
                CircularProgressIndicator()
            }

            orderState.error != null -> {
                Text(orderState.error ?: "Gagal memuat pesanan.")
                OutlinedButton(onClick = onBack) {
                    Text("Kembali")
                }
            }

            order == null -> {
                Text("Pesanan tidak ditemukan.")
            }

            else -> {
                PaymentContent(
                    order = order,
                    paymentViewModel = paymentViewModel,
                    paymentState = paymentState,
                    onBack = onBack
                )
            }
        }
    }
}

@Composable
private fun PaymentContent(
    order: Order,
    paymentViewModel: PaymentViewModel,
    paymentState: PaymentUiState,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
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
            Text("Nomor Pesanan")
            Text(
                order.orderNumber,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(12.dp))

            Text("Total Pembayaran")
            Text(
                formatRupiah(order.totalAmount),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (paymentState.payment == null) {
            Text(
                "Pilih Metode Pembayaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            PaymentMethodOption(
                title = "E-Wallet",
                description = "Pembayaran simulasi melalui dompet digital",
                selected = paymentState.selectedMethod == PaymentMethod.E_WALLET,
                onClick = {
                    paymentViewModel.selectMethod(PaymentMethod.E_WALLET)
                }
            )

            PaymentMethodOption(
                title = "Bank Transfer",
                description = "Pembayaran simulasi melalui transfer bank",
                selected = paymentState.selectedMethod == PaymentMethod.BANK_TRANSFER,
                onClick = {
                    paymentViewModel.selectMethod(PaymentMethod.BANK_TRANSFER)
                }
            )

            PaymentMethodOption(
                title = "QRIS",
                description = "Pembayaran simulasi menggunakan QRIS",
                selected = paymentState.selectedMethod == PaymentMethod.QRIS,
                onClick = {
                    paymentViewModel.selectMethod(PaymentMethod.QRIS)
                }
            )

            Text(
                "Pembayaran ini hanya simulasi. Tidak ada uang sungguhan yang ditransfer.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (paymentState.error != null) {
                Text(
                    paymentState.error,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(Modifier.weight(1f))

            Button(
                onClick = {
                    paymentViewModel.createPayment(order.id)
                },
                enabled = !paymentState.isCreating &&
                        order.status == "PENDING",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (paymentState.isCreating) {
                    CircularProgressIndicator(
                        modifier = Modifier.height(22.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Lanjutkan Pembayaran")
                }
            }
        } else {
            val payment = paymentState.payment

            Text(
                "Detail Pembayaran",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Text("Metode: ${methodLabel(payment.paymentMethod)}")
            Text("Referensi: ${payment.transactionReference}")
            Text("Status: ${payment.status}")
            Text("Nominal: ${formatRupiah(payment.amount)}")

            if (payment.status == PaymentStatus.PENDING) {
                Text(
                    "Tekan tombol berikut untuk menyelesaikan simulasi pembayaran.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (paymentState.error != null) {
                    Text(
                        paymentState.error,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        paymentViewModel.confirmPayment()
                    },
                    enabled = !paymentState.isConfirming,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    if (paymentState.isConfirming) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(22.dp),
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Konfirmasi Pembayaran Simulasi")
                    }
                }
            } else {
                Text(
                    if (payment.status == PaymentStatus.PAID) {
                        "Pembayaran berhasil."
                    } else {
                        "Status pembayaran: ${payment.status}"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kembali")
        }
    }
}

@Composable
private fun PaymentMethodOption(
    title: String,
    description: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.surface
                },
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = onClick
        )

        Column {
            Text(title, fontWeight = FontWeight.Bold)
            Text(
                description,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

private fun methodLabel(method: String): String {
    return when (method) {
        PaymentMethod.E_WALLET -> "E-Wallet"
        PaymentMethod.BANK_TRANSFER -> "Bank Transfer"
        PaymentMethod.QRIS -> "QRIS"
        else -> method
    }
}

private fun formatRupiah(amount: Long): String {
    return NumberFormat.getCurrencyInstance(
        Locale("id", "ID")
    ).apply {
        maximumFractionDigits = 0
        minimumFractionDigits = 0
    }.format(amount)
}
