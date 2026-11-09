package com.olehkan.olehkan.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.model.Product

@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    onAdminClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var favoriteIds by remember { mutableStateOf(setOf(1, 3)) }

    HomeContent(
        uiState = uiState,
        searchQuery = viewModel.searchQuery,
        favoriteIds = favoriteIds,
        onSearchChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelected,
        onProductClick = { product: Product -> onProductClick(product.id) },
        onFavoriteToggle = { product: Product ->
            favoriteIds = if (product.id in favoriteIds) {
                favoriteIds - product.id
            } else {
                favoriteIds + product.id
            }
        },
        onRetry = viewModel::onRetry,
        onAdminClick = onAdminClick,
    )
}