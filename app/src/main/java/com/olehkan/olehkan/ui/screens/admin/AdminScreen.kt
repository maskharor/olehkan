package com.olehkan.olehkan.ui.screens.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.Chat
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.ui.components.ProductCard
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.StarGold
import com.olehkan.olehkan.ui.theme.White

@Composable
fun AdminScreen(
    onBack: () -> Unit,
) {
    val storeProducts = DummyProductRepository.dummyProducts

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Ink,
                        )
                    }
                    Text(
                        text = "Profil Toko",
                        style = OlehKanType.SubHeaderTitle,
                        color = Ink,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Bagikan",
                        tint = Ink,
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = 8.dp,
                    bottom = 24.dp
                ),
                horizontalArrangement = Arrangement.spacedBy(Dimens.GridGapH),
                verticalArrangement = Arrangement.spacedBy(Dimens.GridGapV),
                modifier = Modifier.fillMaxSize()
            ) {
                // Store Info Header Card
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Surface(
                        shape = OlehKanShapes.Large,
                        color = White,
                        border = BorderStroke(1.dp, Line),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(54.dp)
                                        .background(GreenLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = Green,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Text(
                                            text = "Sentra Bakpia Pathok 25",
                                            style = OlehKanType.SubHeaderTitle.copy(fontSize = 16.sp),
                                            color = Ink
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .background(Orange.copy(alpha = 0.12f), OlehKanShapes.ExtraSmall)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "Official Store",
                                                style = OlehKanType.Tiny,
                                                color = Orange
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = Muted,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Text(
                                                text = "Yogyakarta",
                                                style = OlehKanType.Caption,
                                                color = Muted
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))
                            HorizontalDivider(color = Line, thickness = 1.dp)
                            Spacer(modifier = Modifier.height(12.dp))

                            // Store Statistics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text("4.9", style = OlehKanType.BodyBold, color = Ink)
                                    }
                                    Text("Rating Toko", style = OlehKanType.Caption, color = Muted)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("98%", style = OlehKanType.BodyBold, color = Ink)
                                    Text("Performa Chat", style = OlehKanType.Caption, color = Muted)
                                }

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("± 1 Jam", style = OlehKanType.BodyBold, color = Ink)
                                    Text("Kecepatan Kirim", style = OlehKanType.Caption, color = Muted)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Follow & Chat buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = {},
                                    shape = OlehKanShapes.Medium,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Green,
                                        contentColor = White
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Ikuti Toko", style = OlehKanType.ButtonPrimary)
                                }

                                OutlinedButton(
                                    onClick = {},
                                    shape = OlehKanShapes.Medium,
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Green),
                                    border = BorderStroke(1.dp, Green.copy(alpha = 0.5f)),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Chat,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Chat Penjual", style = OlehKanType.ButtonPrimary)
                                }
                            }
                        }
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Text(
                        text = "Produk dari Toko Ini",
                        style = OlehKanType.H3,
                        color = Ink,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }

                items(storeProducts, key = { it.id }) { product ->
                    ProductCard(
                        product = product,
                        isFavorite = false,
                        onFavoriteClick = {},
                        onCardClick = {},
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun AdminScreenPreview() {
    OlehKanTheme {
        AdminScreen(onBack = {})
    }
}