package com.olehkan.olehkan.ui.screens.home

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.olehkan.olehkan.ui.screens.category.CategoryViewModel
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
    categoryViewModel: CategoryViewModel,
    customerName: String = "Pelanggan",
    viewModel: HomeViewModel = viewModel(),
    wishlistRepository: WishlistRepository = WishlistRepositoryProvider.get(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val favoriteIds by wishlistRepository.favoriteIds.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()
    val categoryState by categoryViewModel.uiState.collectAsStateWithLifecycle()
    val categoryNames = (categoryState.categories.map { it.name } +
        uiState.products.map { it.category }).distinct()
    val categories = listOf(HomeUiState.CATEGORY_ALL) + categoryNames
    LaunchedEffect(categories, uiState.selectedCategory, categoryState.hasLoaded) {
        if (categoryState.hasLoaded && uiState.selectedCategory !in categories) {
            viewModel.onCategorySelected(HomeUiState.CATEGORY_ALL)
        }
    }

    HomeContent(
        uiState = uiState,
        categories = categories,
        customerName = customerName,
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
