package com.olehkan.olehkan.ui.screens.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.ui.theme.OlehKanType

@Composable
fun DetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onCheckoutClick: (Int) -> Unit,
    onReviewsClick: (Int) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("📦 DETAIL", style = OlehKanType.H1)
        Text("productId = $productId", style = OlehKanType.Body)
        Button(onClick = { onCheckoutClick(productId) }) {
            Text("Checkout (id=$productId)")
        }
        Button(onClick = { onReviewsClick(productId) }) {
            Text("Lihat Ulasan")
        }
        Button(onClick = onBack) {
            Text("← Kembali")
        }
    }
}