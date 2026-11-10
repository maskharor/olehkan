package com.olehkan.olehkan.ui.screens.checkout

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.ui.theme.CardBeige
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.White

@Composable
fun CheckoutContent(
    uiState: CheckoutUiState,
    onBack: () -> Unit,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onPayClick: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // ==== Header ====
        CheckoutHeader(onBack = onBack)
        Box(Modifier.fillMaxWidth().height(1.dp).background(Line))

        // ==== Content ====
        when {
            uiState.isLoading -> LoadingState()
            uiState.errorMessage != null -> ErrorState(uiState.errorMessage, onRetry)
            uiState.product == null -> EmptyState()
            else -> CheckoutForm(
                uiState = uiState,
                onPaymentMethodSelected = onPaymentMethodSelected,
                onPayClick = onPayClick,
            )
        }
    }
}

// ============================================
// SUB-COMPONENTS
// ============================================

@Composable
private fun CheckoutHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(White)
                .border(1.dp, Line, CircleShape)
                .clickable(onClick = onBack),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Kembali",
                tint = Ink,
            )
        }
        Text(
            "Pembayaran",
            style = OlehKanType.SubHeaderTitle,
            color = Ink,
            modifier = Modifier
                .weight(1f)
                .padding(end = 38.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CheckoutForm(
    uiState: CheckoutUiState,
    onPaymentMethodSelected: (PaymentMethod) -> Unit,
    onPayClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(22.dp),
    ) {
        // ==== 1. Alamat ====
        CheckoutSection(title = "Alamat pengiriman") {
            AddressCard(
                name = uiState.addressName,
                detail = uiState.addressDetail,
            )
        }

        // ==== 2. Ringkasan pesanan ====
        CheckoutSection(title = "Ringkasan pesanan") {
            uiState.product?.let { product ->
                ProductSummary(
                    product = product,
                    quantity = uiState.quantity,
                    formattedUnitPrice = uiState.formattedUnitPrice(),
                )
            }
        }

        // ==== 3. Metode pembayaran ====
        CheckoutSection(title = "Metode pembayaran") {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                PaymentMethod.entries.forEach { method ->
                    PaymentOption(
                        method = method,
                        selected = uiState.paymentMethod == method,
                        onClick = { onPaymentMethodSelected(method) },
                    )
                }
            }
        }

        // ==== 4. Ringkasan harga ====
        PriceSummary(uiState = uiState)

        // ==== 5. Tombol bayar ====
        Button(
            onClick = onPayClick,
            enabled = !uiState.isSubmitting,
            modifier = Modifier
                .fillMaxWidth()
                .height(51.dp),
            shape = RoundedCornerShape(13.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Green,
                disabledContainerColor = Green.copy(alpha = 0.6f),
            ),
        ) {
            if (uiState.isSubmitting) {
                CircularProgressIndicator(
                    color = White,
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Text(
                    "Bayar ${uiState.formattedTotal()}",
                    style = OlehKanType.ButtonLarge,
                    color = White,
                )
            }
        }
    }
}

@Composable
private fun CheckoutSection(title: String, content: @Composable () -> Unit) {
    Column {
        Text(
            title,
            style = OlehKanType.H3,
            color = Ink,
        )
        Spacer(Modifier.height(11.dp))
        content()
    }
}

@Composable
private fun AddressCard(name: String, detail: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(15.dp))
            .border(1.dp, Line, RoundedCornerShape(15.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .size(35.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(GreenLight),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.LocationOn,
                contentDescription = null,
                tint = Green,
                modifier = Modifier.size(18.dp),
            )
        }
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(name, style = OlehKanType.BodyBold, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(detail, style = OlehKanType.Caption, color = Muted)
        }
        Text(
            "Ubah",
            style = OlehKanType.CaptionBold,
            color = Green,
            modifier = Modifier.clickable { /* TODO */ },
        )
    }
}

@Composable
private fun ProductSummary(
    product: Product,
    quantity: Int,
    formattedUnitPrice: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CardBeige)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        AsyncImage(
            model = product.image,
            contentDescription = product.name,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(55.dp)
                .clip(RoundedCornerShape(10.dp)),
        )
        Spacer(Modifier.size(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(product.name, style = OlehKanType.BodyBold, color = Ink)
            Spacer(Modifier.height(4.dp))
            Text(
                "$quantity × $formattedUnitPrice",
                style = OlehKanType.Caption,
                color = Muted,
            )
        }
    }
}

@Composable
private fun PaymentOption(
    method: PaymentMethod,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(13.dp))
            .background(if (selected) Color(0xFFFBFDFB) else White)
            .border(
                width = 1.dp,
                color = if (selected) Green else Line,
                shape = RoundedCornerShape(13.dp),
            )
            .clickable(onClick = onClick)
            .padding(11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(width = 34.dp, height = 27.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(Color(0xFFEDF1EE)),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                method.code,
                style = OlehKanType.Tiny,
                color = Green,
            )
        }
        Spacer(Modifier.size(11.dp))
        Text(
            method.displayLabel,
            style = OlehKanType.BodyBold,
            color = Ink,
            modifier = Modifier.weight(1f),
        )
        // Radio indicator
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(if (selected) Green else Color.Transparent)
                .border(
                    width = 1.dp,
                    color = if (selected) Green else Color(0xFFCFD5D1),
                    shape = CircleShape,
                ),
            contentAlignment = Alignment.Center,
        ) {
            if (selected) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    tint = White,
                    modifier = Modifier.size(12.dp),
                )
            }
        }
    }
}

@Composable
private fun PriceSummary(uiState: CheckoutUiState) {
    Column {
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
        Spacer(Modifier.height(12.dp))
        PriceRow("Subtotal", uiState.formattedSubtotal(), isBold = false)
        Spacer(Modifier.height(6.dp))
        PriceRow("Pengiriman", uiState.formattedShipping(), isBold = false)
        Spacer(Modifier.height(12.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
        Spacer(Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Total pembayaran", style = OlehKanType.BodyBold, color = Ink)
            Text(
                uiState.formattedTotal(),
                style = OlehKanType.PriceBig,
                color = Green,
            )
        }
    }
}

@Composable
private fun PriceRow(label: String, value: String, isBold: Boolean) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = OlehKanType.Caption, color = Muted)
        Text(value, style = OlehKanType.CaptionBold, color = Ink)
    }
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Green)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Gagal memuat", style = OlehKanType.H3, color = Ink)
        Text(
            message,
            style = OlehKanType.BodySmall,
            color = Muted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 6.dp),
        )
        TextButton(onClick = onRetry) {
            Text("Coba lagi", color = Green)
        }
    }
}

@Composable
private fun EmptyState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Text("Produk tidak ditemukan", style = OlehKanType.Body, color = Muted)
    }
}