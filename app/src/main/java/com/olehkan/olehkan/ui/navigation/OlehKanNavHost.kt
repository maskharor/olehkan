package com.olehkan.olehkan.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.olehkan.olehkan.ui.screens.admin.AdminScreen
import com.olehkan.olehkan.ui.screens.checkout.CheckoutScreen
import com.olehkan.olehkan.ui.screens.detail.DetailScreen
import com.olehkan.olehkan.ui.screens.favorites.FavoritesScreen
import com.olehkan.olehkan.ui.screens.home.HomeScreen
import com.olehkan.olehkan.ui.screens.orders.OrdersScreen
import com.olehkan.olehkan.ui.screens.profile.ProfileScreen
import com.olehkan.olehkan.ui.screens.reviews.ReviewsScreen

@Composable
fun OlehKanNavHost(
    navController: NavHostController,
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
    ) {

        composable(Screen.Home.route) {
            HomeScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.Detail.createRoute(productId))
                },
                onAdminClick = {
                    navController.navigate(Screen.Admin.route)
                },
            )
        }

        composable(Screen.Favorites.route) {
            FavoritesScreen(
                onProductClick = { productId ->
                    navController.navigate(Screen.Detail.createRoute(productId))
                },
            )
        }

        composable(Screen.Orders.route) {
            OrdersScreen(
                onPayClick = { productId ->
                    navController.navigate(Screen.Checkout.createRoute(productId))
                },
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(
                onAdminClick = {
                    navController.navigate(Screen.Admin.route)
                },
                onReviewsClick = {
                    navController.navigate(Screen.Reviews.createRoute(null))
                },
            )
        }

        composable(
            route = Screen.Detail.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            DetailScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onCheckoutClick = { id ->
                    navController.navigate(Screen.Checkout.createRoute(id))
                },
                onReviewsClick = { id ->
                    navController.navigate(Screen.Reviews.createRoute(id))
                },
            )
        }

        composable(
            route = Screen.Checkout.route,
            arguments = listOf(navArgument("productId") { type = NavType.IntType }),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            CheckoutScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onDone = {
                    navController.navigate(Screen.Orders.route) {
                        popUpTo(Screen.Home.route)
                    }
                },
            )
        }

        composable(
            route = Screen.Reviews.route,
            arguments = listOf(
                navArgument(Screen.Reviews.ARG_PRODUCT_ID) {
                    type = NavType.IntType
                    defaultValue = -1
                },
            ),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt(Screen.Reviews.ARG_PRODUCT_ID)
                ?.takeIf { it > 0 }
            ReviewsScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Screen.Admin.route) {
            AdminScreen(
                onBack = { navController.popBackStack() },
            )
        }
    }
}