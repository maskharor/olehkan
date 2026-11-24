package com.olehkan.olehkan.ui.navigation

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductFormScreen
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductFormViewModelFactory
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductListScreen
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductListViewModelFactory

fun NavGraphBuilder.productNavigation(
    navController: NavHostController,
    formFactory: AdminProductFormViewModelFactory,
    listFactory: AdminProductListViewModelFactory,
    isAdmin: Boolean = false,
) {
    composable(Screen.AdminProducts.route) {
        AdminRoute(isAdmin, onBack = { navController.popBackStack() }) {
            AdminProductListScreen(
                onBack = { navController.popBackStack() },
                onAdd = { navController.navigate(Screen.ProductForm.createRoute()) },
                onEdit = { navController.navigate(Screen.ProductForm.createRoute(it)) },
                onManageCategories = { navController.navigate(Screen.Categories.route) },
                viewModel = viewModel(factory = listFactory),
            )
        }
    }
    composable(
        route = Screen.ProductForm.route,
        arguments = listOf(navArgument("productUuid") {
            type = NavType.StringType
            nullable = true
            defaultValue = null
        }),
    ) { entry ->
        AdminRoute(isAdmin, onBack = { navController.popBackStack() }) {
            AdminProductFormScreen(
                productUuid = entry.arguments?.getString("productUuid"),
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
                onDeleted = { navController.popBackStack() },
                viewModel = viewModel(factory = formFactory),
            )
        }
    }
}
