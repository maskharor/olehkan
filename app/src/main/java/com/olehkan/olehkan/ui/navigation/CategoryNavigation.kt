package com.olehkan.olehkan.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.olehkan.olehkan.ui.screens.category.CategoryDetailScreen
import com.olehkan.olehkan.ui.screens.category.CategoryFormScreen
import com.olehkan.olehkan.ui.screens.category.CategoryListScreen
import com.olehkan.olehkan.ui.screens.category.CategoryViewModelFactory

fun NavGraphBuilder.categoryNavigation(
    navController: NavHostController,
    factory: CategoryViewModelFactory,
    isAdmin: Boolean = false,
) {
    composable(Screen.Categories.route) {
        AdminRoute(isAdmin, onBack = { navController.popBackStack() }) {
        CategoryListScreen(
            onBack = { navController.popBackStack() },
            onAdd = { navController.navigate(Screen.CategoryForm.createRoute()) },
            onCategoryClick = { navController.navigate(Screen.CategoryDetail.createRoute(it)) },
            viewModel = viewModel(factory = factory),
        )
        }
    }
    composable(
        route = Screen.CategoryDetail.route,
        arguments = listOf(navArgument("categoryId") { type = NavType.StringType }),
    ) { entry ->
        AdminRoute(isAdmin, onBack = { navController.popBackStack() }) {
        CategoryDetailScreen(
            categoryId = entry.arguments?.getString("categoryId").orEmpty(),
            onBack = { navController.popBackStack() },
            onEdit = { navController.navigate(Screen.CategoryForm.createRoute(it)) },
            onDeleted = { navController.popBackStack() },
            viewModel = viewModel(factory = factory),
        )
        }
    }
    composable(
        route = Screen.CategoryForm.route,
        arguments = listOf(navArgument("categoryId") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
    ) { entry ->
        AdminRoute(isAdmin, onBack = { navController.popBackStack() }) {
        CategoryFormScreen(
            categoryId = entry.arguments?.getString("categoryId"),
            onBack = { navController.popBackStack() },
            onSaved = { navController.popBackStack() },
            viewModel = viewModel(factory = factory),
        )
        }
    }
}
