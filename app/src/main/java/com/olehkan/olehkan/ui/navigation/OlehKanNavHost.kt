package com.olehkan.olehkan.ui.navigation

import androidx.compose.runtime.Composable
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.screens.admin.AdminDashboardScreen
import com.olehkan.olehkan.ui.screens.profile.EditProfileScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.data.repository.CategoryRepositoryProvider
import com.olehkan.olehkan.ui.screens.category.CategoryViewModelFactory
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
import com.olehkan.olehkan.ui.screens.splash.SplashScreen

@Composable
fun OlehKanNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profile: UserProfile,
) {
    val context = LocalContext.current
    val categoryFactory = remember(context.applicationContext) {
        CategoryViewModelFactory(CategoryRepositoryProvider.get(context))
    }
    NavHost(
        navController = navController,
        startDestination = if (profile.role == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Home.route,
    ) {

        composable(Screen.Splash.route) {
            SplashScreen(
                onFinish = {
                    val destination = if (profile.role == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Home.route
                    navController.navigate(destination) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                categoryViewModel = viewModel(factory = categoryFactory),
                customerName = profile.fullName,
                onProductClick = { productId ->
                    navController.navigate(Screen.Detail.createRoute(productId))
                },
                onAdminClick = {
                    navController.navigate(if (profile.role == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Store.route)
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
                viewModel = authViewModel,
                onEditClick = {
                    authViewModel.prepareEditProfile()
                    navController.navigate(Screen.EditProfile.route)
                },
                onBack = {
                    if (!navController.popBackStack()) {
                        navController.navigate(if (profile.role == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Home.route) {
                            launchSingleTop = true
                        }
                    }
                },
                onCategoriesClick = { navController.navigate(Screen.Categories.route) },
                onAdminClick = {
                    navController.navigate(if (profile.role == UserRole.ADMIN) Screen.AdminDashboard.route else Screen.Store.route)
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
                onStoreClick = {
                    navController.navigate(Screen.Store.route)
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

        composable(Screen.EditProfile.route) {
            EditProfileScreen(authViewModel, onBack = { navController.popBackStack() })
        }
        composable(Screen.AdminDashboard.route) {
            AdminRoute(profile.role == UserRole.ADMIN, onBack = { navController.popBackStack() }) {
                AdminDashboardScreen(
                    profile = profile,
                    onCategories = { navController.navigate(Screen.Categories.route) },
                    onCategoryClick = { id -> navController.navigate(Screen.CategoryDetail.createRoute(id)) },
                    onAddCategory = { navController.navigate(Screen.CategoryForm.createRoute()) },
                    onProfile = { navController.navigate(Screen.Profile.route) },
                )
            }
        }
        categoryNavigation(navController, categoryFactory, isAdmin = profile.role == UserRole.ADMIN)

        // Route lama tetap ada, tetapi kini mengikuti pembatasan akses admin.
        composable(Screen.Admin.route) {
            AdminRoute(profile.role == UserRole.ADMIN, onBack = { navController.popBackStack() }) {
                AdminDashboardScreen(
                    profile = profile,
                    onCategories = { navController.navigate(Screen.Categories.route) },
                    onCategoryClick = { id -> navController.navigate(Screen.CategoryDetail.createRoute(id)) },
                    onAddCategory = { navController.navigate(Screen.CategoryForm.createRoute()) },
                    onProfile = { navController.navigate(Screen.Profile.route) },
                )
            }
        }
        composable(Screen.Store.route) {
            // Gunakan ulang tampilan profil toko yang sudah dibuat anggota kelompok.
            AdminScreen(onBack = { navController.popBackStack() })
        }
    }
}