package com.olehkan.olehkan.ui.screens.reviews

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
fun ReviewsScreen(
    productId: Int?,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("REVIEW", style = OlehKanType.H1)
        Text(
            if (productId != null) "productId = $productId (tulis ulasan)"
            else "Semua ulasan",
            style = OlehKanType.Body
        )
        Button(onClick = onBack) {
            Text("← Kembali")
        }
    }
}