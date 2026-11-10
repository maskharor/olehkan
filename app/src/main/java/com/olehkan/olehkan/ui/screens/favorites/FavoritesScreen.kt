package com.olehkan.olehkan.ui.screens.favorites

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.ui.components.ProductCard
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenDark
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Paper

@Composable
fun FavoritesScreen(
    onProductClick: (Int) -> Unit,
    viewModel: FavoritesViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    FavoritesContent(
        uiState = uiState,
        onProductClick = onProductClick,
        onRemoveFavorite = viewModel::removeFromFavorites,
        onRetry = viewModel::onRetry,
    )
}

@Composable
fun FavoritesContent(
    uiState: FavoritesUiState,
    onProductClick: (Int) -> Unit,
    onRemoveFavorite: (Int) -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        val gridGapH = if (columns >= 3) 12.dp else Dimens.GridGapH
        val gridGapV = if (columns >= 3) 16.dp else Dimens.GridGapV

        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Top Bar: Judul "Favoritmu" (tengah) dan Jumlah Item (kanan)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(top = 16.dp, bottom = 4.dp),
            ) {
                Text(
                    text = "Favoritmu",
                    style = OlehKanType.H2.copy(fontSize = 20.sp),
                    color = GreenDark,
                    modifier = Modifier.align(Alignment.Center)
                )

                Text(
                    text = "${uiState.products.size} item",
                    style = OlehKanType.BodySmall,
                    color = Muted,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            // Subtitle
            Text(
                text = "Koleksi rasa yang ingin kamu bawa pulang.",
                style = OlehKanType.BodySmall,
                color = Muted,
                modifier = Modifier
                    .padding(horizontal = Dimens.ScreenPadding)
                    .padding(top = 8.dp, bottom = 12.dp)
            )

            when {
                uiState.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator(color = Green)
                    }
                }

                uiState.errorMessage != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = "Gagal memuat favorit",
                            style = OlehKanType.H3,
                            color = Ink,
                        )
                        Text(
                            text = uiState.errorMessage,
                            style = OlehKanType.BodySmall,
                            color = Muted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 6.dp),
                        )
                        TextButton(onClick = onRetry) {
                            Text("Coba Lagi", color = Green)
                        }
                    }
                }

                uiState.products.isEmpty() -> {
                    // Empty State sesuai gambar spesifikasi
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f)
                            .padding(horizontal = 24.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(80.dp)
                                    .background(
                                        color = Color(0xFFEAF4EE),
                                        shape = CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.FavoriteBorder,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(34.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Belum ada favorit",
                                style = OlehKanType.SubHeaderTitle,
                                color = Ink,
                                textAlign = TextAlign.Center,
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Ketuk ikon hati pada produk yang kamu suka.",
                                style = OlehKanType.BodySmall,
                                color = Muted,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }

                else -> {
                    // Daftar Wishlist dalam bentuk LazyVerticalGrid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(
                            start = Dimens.ScreenPadding,
                            end = Dimens.ScreenPadding,
                            top = 6.dp,
                            bottom = 24.dp,
                        ),
                        horizontalArrangement = Arrangement.spacedBy(gridGapH),
                        verticalArrangement = Arrangement.spacedBy(gridGapV),
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                    ) {
                        items(uiState.products, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = true,
                                onFavoriteClick = { onRemoveFavorite(product.id) },
                                onCardClick = { onProductClick(product.id) },
                            )
                        }
                    }
                }
            }
        }
    }
}
