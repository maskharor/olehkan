package com.olehkan.olehkan.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.model.Product
import com.olehkan.olehkan.data.repository.WishlistRepository
import com.olehkan.olehkan.data.repository.WishlistRepositoryProvider
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onProductClick: (Int) -> Unit,
    onAdminClick: () -> Unit,
    viewModel: HomeViewModel = viewModel(),
    wishlistRepository: WishlistRepository = WishlistRepositoryProvider.get(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds by wishlistRepository.favoriteIds.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    HomeContent(
        uiState = uiState,
        searchQuery = viewModel.searchQuery,
        favoriteIds = favoriteIds,
        onSearchChange = viewModel::onSearchQueryChange,
        onCategorySelect = viewModel::onCategorySelected,
        onProductClick = { product: Product -> onProductClick(product.id) },
        onFavoriteToggle = { product: Product ->
            scope.launch {
                wishlistRepository.toggleFavorite(product.id)
            }
        },
        onRetry = viewModel::onRetry,
        onAdminClick = onAdminClick,
    )
}
