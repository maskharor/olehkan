package com.olehkan.olehkan.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.Order
import com.olehkan.olehkan.data.model.OrderStatus
import com.olehkan.olehkan.ui.theme.CardBeige
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.White

/**
 * Kartu pesanan — reusable, dipakai di OrdersContent.
 *
 * Menampilkan: status badge, tanggal, produk, total, tracking (jika ada),
 * dan tombol aksi (jika ada).
 */
@Composable
fun OrderCard(
    order: Order,
    onPayClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(OlehKanShapes.XL)
            .background(Paper)
            .padding(15.dp)
    ) {
        // ===== STATUS + TANGGAL =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StatusBadge(order.status)
            Text(
                order.orderDate,
                style = OlehKanType.Tiny,
                color = Muted,
            )
        }

        Spacer(Modifier.height(12.dp))

        // ===== PRODUK =====
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(11.dp),
        ) {
            AsyncImage(
                model = order.product.image,
                contentDescription = order.product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(68.dp)
                    .clip(OlehKanShapes.SmallMedium),
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    order.product.name,
                    style = OlehKanType.ProductName,
                    color = Ink,
                )
                Text(
                    "${order.quantity} barang • ${order.product.place}",
                    style = OlehKanType.Caption,
                    color = Muted,
                )
                Text(
                    "Rp${formatNumber(order.product.price)}",
                    style = OlehKanType.BodyBold,
                    color = Green,
                )
            }
        }

        // ===== TRACKING (jika Dalam Pengiriman) =====
        if (order.hasTracking) {
            Spacer(Modifier.height(16.dp))
            TrackingBar()
        }

        // ===== FOOTER: TOTAL + AKSI =====
        Spacer(Modifier.height(14.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Line)
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Total belanja", style = OlehKanType.Tiny, color = Muted)
                Text(
                    order.formattedTotal(),
                    style = OlehKanType.BodyBold,
                    color = Ink,
                )
            }
            if (order.needsPayment) {
                Button(
                    onClick = { onPayClick(order.id) },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Green),
                    modifier = Modifier.height(36.dp),
                ) {
                    Text(
                        "Bayar sekarang",
                        style = OlehKanType.CaptionBold,
                        color = White,
                    )
                }
            }
        }
    }
}

// ===== Sub-components =====

@Composable
private fun StatusBadge(status: OrderStatus) {
    val (bg, fg) = when (status) {
        OrderStatus.MENUNGGU_PEMBAYARAN -> ColorPair(Color(0xFFFFF0DF), Color(0xFFA8561E))
        OrderStatus.DALAM_PENGIRIMAN -> ColorPair(Color(0xFFE6F1F8), Color(0xFF276395))
        OrderStatus.SELESAI -> ColorPair(Color(0xFFE8F2ED), Color(0xFF1F5A45))
        OrderStatus.DIBATALKAN -> ColorPair(Color(0xFFFDE8E8), Color(0xFFA82A2A))
    }
    Box(
        modifier = Modifier
            .clip(OlehKanShapes.Pill)
            .background(bg)
            .padding(horizontal = 9.dp, vertical = 6.dp)
    ) {
        Text(
            status.displayLabel,
            style = OlehKanType.Tiny,
            color = fg,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun TrackingBar() {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        // Garis dengan 3 titik
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(3) { index ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(RoundedCornerShape(50))
                        .background(if (index < 2) Green else Color(0xFFDFE5E2))
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Dikemas", style = OlehKanType.Tiny, color = Muted)
            Text("Dikirim", style = OlehKanType.Tiny, color = Muted)
            Text("Diterima", style = OlehKanType.Tiny, color = Muted)
        }
    }
}

private data class ColorPair(val bg: androidx.compose.ui.graphics.Color, val fg: androidx.compose.ui.graphics.Color)

private fun formatNumber(value: Int): String {
    val s = value.toString()
    if (s.length <= 3) return s
    val result = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        result.append(s[i])
        count++
        if (count == 3 && i != 0) {
            result.append('.')
            count = 0
        }
    }
    return result.reverse().toString()
}