package com.olehkan.olehkan.ui.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.ui.components.ProductCard
import com.olehkan.olehkan.ui.theme.Dimens
import com.olehkan.olehkan.ui.theme.Green
import com.olehkan.olehkan.ui.theme.GreenDark
import com.olehkan.olehkan.ui.theme.Ink
import com.olehkan.olehkan.ui.theme.Muted
import com.olehkan.olehkan.ui.theme.OlehKanShapes
import com.olehkan.olehkan.ui.theme.OlehKanType
import com.olehkan.olehkan.ui.theme.Orange
import com.olehkan.olehkan.ui.theme.Paper
import com.olehkan.olehkan.ui.theme.SearchPlaceholder
import com.olehkan.olehkan.ui.theme.White

@Composable
fun HomeContent(
    uiState: HomeUiState,
    searchQuery: String,
    favoriteIds: Set<Int>,
    onSearchChange: (String) -> Unit,
    onCategorySelect: (String) -> Unit,
    onProductClick: (Product) -> Unit,
    onFavoriteToggle: (Product) -> Unit,
    onRetry: () -> Unit,
    onAdminClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxSize()) {
        val isLandscape = maxWidth > maxHeight

        val columns = when {
            maxWidth >= 900.dp -> 4
            maxWidth >= 600.dp -> 3
            else -> 2
        }
        val gridGapH = if (columns >= 3) 10.dp else Dimens.GridGapH
        val gridGapV = if (columns >= 3) 14.dp else Dimens.GridGapV

        val gridState = rememberLazyGridState()
        val isCollapsed by remember {
            derivedStateOf {
                gridState.firstVisibleItemIndex > 0 ||
                        gridState.firstVisibleItemScrollOffset > 60
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Paper)
        ) {
            if (isLandscape) {
                LandscapeTopBar(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    onAdminClick = onAdminClick,
                )
            } else {
                CollapsiblePortraitHero(
                    searchQuery = searchQuery,
                    onSearchChange = onSearchChange,
                    onAdminClick = onAdminClick,
                    collapsed = isCollapsed,
                )
            }

            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(
                    start = Dimens.ScreenPadding,
                    end = Dimens.ScreenPadding,
                    top = if (isLandscape) 8.dp else 16.dp,
                    bottom = 24.dp,
                ),
                horizontalArrangement = Arrangement.spacedBy(gridGapH),
                verticalArrangement = Arrangement.spacedBy(gridGapV),
                modifier = Modifier.fillMaxSize(),
            ) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom,
                        ) {
                            Column {
                                Text(
                                    "PILIHAN TERBAIK",
                                    style = OlehKanType.Eyebrow,
                                    color = Orange,
                                )
                                Spacer(Modifier.height(5.dp))
                                Text(
                                    "Populer minggu ini",
                                    style = OlehKanType.H2,
                                    color = Ink,
                                )
                            }
                            TextButton(onClick = { }) {
                                Text(
                                    "Lihat semua",
                                    style = OlehKanType.CaptionBold,
                                    color = Green,
                                )
                            }
                        }

                        Spacer(Modifier.height(if (isLandscape) 8.dp else 14.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            contentPadding = PaddingValues(0.dp),
                        ) {
                            items(HomeUiState.categories) { category ->
                                val selected = category == uiState.selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(OlehKanShapes.Pill)
                                        .background(if (selected) Green else White)
                                        .clickable { onCategorySelect(category) }
                                        .padding(horizontal = 16.dp, vertical = 9.dp)
                                ) {
                                    Text(
                                        text = category,
                                        style = OlehKanType.ChipLabel,
                                        color = if (selected) White else Muted,
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(if (isLandscape) 8.dp else 16.dp))
                    }
                }

                when {
                    uiState.isLoading -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            LoadingPlaceholder()
                        }
                    }
                    uiState.errorMessage != null -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            ErrorState(
                                message = uiState.errorMessage,
                                onRetry = onRetry,
                            )
                        }
                    }
                    uiState.isEmptyFilterResult || uiState.filteredProducts.isEmpty() -> {
                        item(span = { GridItemSpan(maxLineSpan) }) {
                            EmptyState(
                                if (uiState.isEmptyFilterResult) "Produk tidak ditemukan"
                                else "Belum ada produk"
                            )
                        }
                    }
                    else -> {
                        items(uiState.filteredProducts, key = { it.id }) { product ->
                            ProductCard(
                                product = product,
                                isFavorite = product.id in favoriteIds,
                                onFavoriteClick = { onFavoriteToggle(product) },
                                onCardClick = { onProductClick(product) },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CollapsiblePortraitHero(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAdminClick: () -> Unit,
    collapsed: Boolean,
) {
    val heroHeight by animateDpAsState(
        targetValue = if (collapsed) 90.dp else 316.dp,
        animationSpec = tween(durationMillis = 300),
        label = "heroHeight",
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(heroHeight)
            .clip(OlehKanShapes.HeroBottom)
            .background(
                Brush.linearGradient(
                    colors = listOf(GreenDark, Green, Color(0xFF215841)),
                    start = Offset(0f, 0f),
                    end = Offset.Infinite,
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 22.dp,
                    vertical = if (collapsed) 12.dp else 28.dp,
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text(
                        "Selamat datang,",
                        style = OlehKanType.BodySmall,
                        color = White.copy(alpha = 0.72f),
                    )
                    Text(
                        "Nadia",
                        style = OlehKanType.SubHeaderTitle,
                        color = White,
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(9.dp)) {
                    HeroActionButton(
                        onClick = onAdminClick,
                        icon = {
                            Icon(
                                Icons.Filled.Storefront,
                                contentDescription = "Toko saya",
                                tint = White,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                    )
                    HeroActionButton(
                        onClick = { },
                        icon = {
                            Icon(
                                Icons.Filled.Notifications,
                                contentDescription = "Notifikasi",
                                tint = White,
                                modifier = Modifier.size(20.dp),
                            )
                        },
                        showDot = true,
                    )
                }
            }

            AnimatedVisibility(
                visible = !collapsed,
                enter = fadeIn() + expandVertically(),
                exit = fadeOut() + shrinkVertically(),
            ) {
                Column {
                    Spacer(Modifier.height(40.dp))
                    Text(
                        "JELAJAHI RASA NUSANTARA",
                        style = OlehKanType.Eyebrow,
                        color = White.copy(alpha = 0.72f),
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Bawa pulang cerita\ndari setiap daerah.",
                        style = OlehKanType.Hero,
                        color = White,
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White, OlehKanShapes.Hero),
                placeholder = {
                    Text(
                        if (collapsed) "Cari oleh-oleh..."
                        else "Cari oleh-oleh favoritmu...",
                        style = OlehKanType.BodySmall,
                        color = SearchPlaceholder,
                    )
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = Muted)
                },
                singleLine = true,
                shape = OlehKanShapes.Hero,
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedBorderColor = Color.Transparent,
                    focusedBorderColor = Color.Transparent,
                    unfocusedContainerColor = White,
                    focusedContainerColor = White,
                ),
            )
        }
    }
}

@Composable
private fun LandscapeTopBar(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    onAdminClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(GreenDark)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            "OlehKan",
            style = OlehKanType.SubHeaderTitle,
            color = White,
        )

        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchChange,
            modifier = Modifier
                .weight(1f)
                .height(48.dp),
            placeholder = {
                Text(
                    "Cari oleh-oleh...",
                    style = OlehKanType.BodySmall,
                    color = SearchPlaceholder,
                )
            },
            leadingIcon = {
                Icon(
                    Icons.Filled.Search,
                    contentDescription = null,
                    tint = Muted,
                    modifier = Modifier.size(18.dp),
                )
            },
            singleLine = true,
            shape = OlehKanShapes.Pill,
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = Color.Transparent,
                focusedBorderColor = Color.Transparent,
                unfocusedContainerColor = White,
                focusedContainerColor = White,
            ),
        )

        HeroActionButton(
            onClick = onAdminClick,
            icon = {
                Icon(
                    Icons.Filled.Storefront,
                    contentDescription = "Toko saya",
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            },
        )
        HeroActionButton(
            onClick = { },
            icon = {
                Icon(
                    Icons.Filled.Notifications,
                    contentDescription = "Notifikasi",
                    tint = White,
                    modifier = Modifier.size(20.dp),
                )
            },
            showDot = true,
        )
    }
}

@Composable
private fun HeroActionButton(
    onClick: () -> Unit,
    icon: @Composable () -> Unit,
    showDot: Boolean = false,
) {
    Box {
        Surface(
            onClick = onClick,
            shape = CircleShape,
            color = White.copy(alpha = 0.12f),
            modifier = Modifier.size(40.dp),
        ) {
            Box(contentAlignment = Alignment.Center) {
                icon()
            }
        }
        if (showDot) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(7.dp)
                    .background(Color(0xFFFF9D5C), CircleShape)
            )
        }
    }
}

@Composable
private fun LoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(color = Green)
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Gagal memuat", style = OlehKanType.H3, color = Ink)
        Text(
            text = message,
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
private fun EmptyState(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 60.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = OlehKanType.Body,
            color = Muted,
            textAlign = TextAlign.Center,
        )
    }
}