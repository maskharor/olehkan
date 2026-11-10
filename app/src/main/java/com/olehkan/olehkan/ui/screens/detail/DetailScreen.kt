package com.olehkan.olehkan.ui.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.RateReview
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.olehkan.olehkan.data.model.formattedPrice
import com.olehkan.olehkan.data.repository.DummyProductRepository
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenDark
import com.olehkan.olehkan.ui.theme.GreenLight
import com.olehkan.olehkan.ui.theme.HeartRed
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Line
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.StarGold
import com.olehkan.olehkan.ui.theme.WarningBg
import com.olehkan.olehkan.ui.theme.WarningText
import com.olehkan.olehkan.ui.theme.White

@Composable
fun DetailScreen(
    productId: Int,
    onBack: () -> Unit,
    onCheckoutClick: (Int) -> Unit,
    onReviewsClick: (Int) -> Unit,
    onStoreClick: () -> Unit = {},
    viewModel: DetailViewModel = viewModel(),
) {
    LaunchedEffect(productId) {
        viewModel.loadProduct(productId)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    DetailContent(
        uiState = uiState,
        productId = productId,
        onBack = onBack,
        onCheckoutClick = onCheckoutClick,
        onReviewsClick = onReviewsClick,
        onStoreClick = onStoreClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onRemoveFromWishlist = viewModel::removeFromWishlist,
        onAddToWishlist = viewModel::addToWishlist,
        onRetry = { viewModel.loadProduct(productId) },
    )
}

@Composable
fun DetailContent(
    uiState: DetailUiState,
    productId: Int,
    onBack: () -> Unit,
    onCheckoutClick: (Int) -> Unit,
    onReviewsClick: (Int) -> Unit,
    onStoreClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    onRemoveFromWishlist: () -> Unit,
    onAddToWishlist: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator(color = Green)
                }
            }

            uiState.errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Gagal memuat detail", style = OlehKanType.H3, color = Ink)
                    Text(
                        text = uiState.errorMessage,
                        style = OlehKanType.BodySmall,
                        color = Muted,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    TextButton(onClick = onRetry) {
                        Text("Coba Lagi", color = Green)
                    }
                    Button(onClick = onBack) {
                        Text("Kembali")
                    }
                }
            }

            uiState.product != null -> {
                val product = uiState.product
                val scrollState = rememberScrollState()

                Column(modifier = Modifier.fillMaxSize()) {
                    // Top App Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Kembali",
                                tint = Ink,
                            )
                        }

                        Text(
                            text = "Detail Produk",
                            style = OlehKanType.SubHeaderTitle,
                            color = Ink,
                        )

                        Surface(
                            onClick = onToggleFavorite,
                            shape = CircleShape,
                            color = White,
                            shadowElevation = 2.dp,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (uiState.isFavorite) Icons.Filled.Favorite
                                    else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (uiState.isFavorite) "Hapus dari wishlist"
                                    else "Tambah ke wishlist",
                                    tint = if (uiState.isFavorite) HeartRed else Muted,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // Content Scrollable
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(scrollState)
                            .padding(horizontal = Dimens.ScreenPadding)
                    ) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Product Image with soft shadow
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(1.1f)
                                .clip(OlehKanShapes.Large)
                                .background(Muted.copy(alpha = 0.08f))
                        ) {
                            AsyncImage(
                                model = product.image,
                                contentDescription = product.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize(),
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Place / Origin Tag
                        Text(
                            text = product.place.uppercase(),
                            style = OlehKanType.ProductPlace,
                            color = Orange,
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Product Name
                        Text(
                            text = product.name,
                            style = OlehKanType.H1,
                            color = Ink,
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Price and Prominent Stock Display
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = product.formattedPrice(),
                                style = OlehKanType.Hero.copy(fontSize = 24.sp),
                                color = Green,
                            )

                            // Tampilan Stok Jelas & Menarik
                            Surface(
                                shape = OlehKanShapes.Pill,
                                color = if (product.stock > 10) GreenLight else WarningBg,
                                border = BorderStroke(
                                    1.dp,
                                    if (product.stock > 10) Green.copy(alpha = 0.25f) else Orange.copy(alpha = 0.35f)
                                ),
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .background(
                                                color = if (product.stock > 10) Green else Orange,
                                                shape = CircleShape
                                            )
                                    )
                                    Text(
                                        text = if (product.stock > 10) "Stok: ${product.stock} pcs"
                                        else "Sisa ${product.stock} pcs lagi!",
                                        style = OlehKanType.CaptionBold,
                                        color = if (product.stock > 10) GreenDark else WarningText,
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Info Highlight Bar (Rating, Ulasan [Clickable], Kategori)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(White, OlehKanShapes.Medium)
                                .padding(vertical = 11.dp, horizontal = 14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Rating (bisa diklik menuju ulasan)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onReviewsClick(product.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.Star,
                                    contentDescription = null,
                                    tint = StarGold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${product.rating}",
                                    style = OlehKanType.BodyBold,
                                    color = Ink
                                )
                                Text(
                                    text = " / 5.0",
                                    style = OlehKanType.Caption,
                                    color = Muted
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Line))

                            // Ulasan (dapat diklik langsung menuju ke halaman ulasan)
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.clickable { onReviewsClick(product.id) }
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.RateReview,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(5.dp))
                                Text(
                                    text = "${product.reviews} Ulasan",
                                    style = OlehKanType.BodySmall.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Medium),
                                    color = Green
                                )
                            }

                            Box(modifier = Modifier.width(1.dp).height(16.dp).background(Line))

                            // Kategori
                            Text(
                                text = product.category,
                                style = OlehKanType.BodySmall,
                                color = Muted
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))
                        HorizontalDivider(color = Line, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Description Section
                        Text(
                            text = "Deskripsi Produk",
                            style = OlehKanType.H3,
                            color = Ink,
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = product.description,
                            style = OlehKanType.Body,
                            color = Ink.copy(alpha = 0.85f),
                            lineHeight = OlehKanType.Body.lineHeight,
                        )

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Line, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Informasi Penjual & Tombol Kunjungi Toko
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Informasi Penjual",
                                style = OlehKanType.H3,
                                color = Ink,
                            )
                            TextButton(
                                onClick = onStoreClick,
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Kunjungi Toko",
                                    style = OlehKanType.CaptionBold,
                                    color = Green,
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Card Informasi Penjual yang Rapi & Elegan
                        Surface(
                            shape = OlehKanShapes.Large,
                            color = White,
                            border = BorderStroke(1.dp, Line),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onStoreClick() }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                            ) {
                                // Avatar Toko
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .background(GreenLight, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Storefront,
                                        contentDescription = null,
                                        tint = Green,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }

                                // Detail Penjual
                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(5.dp),
                                ) {
                                    // Baris 1: Nama Toko
                                    Text(
                                        text = product.sellerName,
                                        style = OlehKanType.ProductName.copy(fontSize = 15.sp),
                                        color = Ink,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                    )

                                    // Baris 2: Badge & Kota Asal
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    ) {
                                        Surface(
                                            shape = OlehKanShapes.Pill,
                                            color = Orange.copy(alpha = 0.12f),
                                        ) {
                                            Text(
                                                text = product.sellerBadge,
                                                style = OlehKanType.Tiny.copy(fontWeight = FontWeight.Bold),
                                                color = Orange,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                            )
                                        }

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Outlined.LocationOn,
                                                contentDescription = null,
                                                tint = Muted,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(2.dp))
                                            Text(
                                                text = product.sellerCity,
                                                style = OlehKanType.Caption,
                                                color = Muted
                                            )
                                        }
                                    }

                                    // Baris 3: Rating & Status
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Text(
                                            text = "${product.sellerRating}",
                                            style = OlehKanType.CaptionBold,
                                            color = Ink
                                        )
                                        Text(
                                            text = "• Penjual Terpercaya",
                                            style = OlehKanType.Caption,
                                            color = Muted
                                        )
                                    }
                                }

                                // Panah navigasi
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = "Kunjungi toko",
                                    tint = Muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        HorizontalDivider(color = Line, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(16.dp))

                        // Card Navigasi Ulasan Pembeli
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "Ulasan Pembeli",
                                style = OlehKanType.H3,
                                color = Ink,
                            )
                            TextButton(onClick = { onReviewsClick(product.id) }) {
                                Text(
                                    text = "Lihat Semua (${product.reviews})",
                                    style = OlehKanType.CaptionBold,
                                    color = Green,
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = null,
                                    tint = Green,
                                    modifier = Modifier.size(13.dp),
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Surface(
                            shape = OlehKanShapes.Large,
                            color = White,
                            border = BorderStroke(1.dp, Line),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onReviewsClick(product.id) }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .background(WarningBg, CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Star,
                                            contentDescription = null,
                                            tint = StarGold,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = "${product.rating} dari 5.0",
                                                style = OlehKanType.ProductName,
                                                color = Ink
                                            )
                                        }
                                        Text(
                                            text = "Berdasarkan ${product.reviews} pembeli terverifikasi",
                                            style = OlehKanType.Caption,
                                            color = Muted
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                                    contentDescription = "Buka ulasan",
                                    tint = Muted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(28.dp))
                    }

                    // Bottom Bar with Wishlist action & Checkout
                    Surface(
                        color = White,
                        shadowElevation = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.ScreenPadding, vertical = 14.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            // Hapus / Tambah Wishlist Button
                            if (uiState.isFavorite) {
                                OutlinedButton(
                                    onClick = onRemoveFromWishlist,
                                    shape = OlehKanShapes.Medium,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = HeartRed,
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.DeleteOutline,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Hapus Favorit", style = OlehKanType.ButtonPrimary)
                                }
                            } else {
                                OutlinedButton(
                                    onClick = onAddToWishlist,
                                    shape = OlehKanShapes.Medium,
                                    colors = ButtonDefaults.outlinedButtonColors(
                                        contentColor = Green,
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.FavoriteBorder,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Tambah Favorit", style = OlehKanType.ButtonPrimary)
                                }
                            }

                            // Checkout Button
                            Button(
                                onClick = { onCheckoutClick(product.id) },
                                shape = OlehKanShapes.Medium,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Green,
                                    contentColor = White,
                                ),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ShoppingCart,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Beli Sekarang", style = OlehKanType.ButtonPrimary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun DetailScreenPreview() {
    OlehKanTheme {
        DetailContent(
            uiState = DetailUiState(
                isLoading = false,
                product = DummyProductRepository.dummyProducts.first(),
                isFavorite = true,
            ),
            productId = 1,
            onBack = {},
            onCheckoutClick = {},
            onReviewsClick = {},
            onStoreClick = {},
            onToggleFavorite = {},
            onRemoveFromWishlist = {},
            onAddToWishlist = {},
            onRetry = {},
        )
    }
}
