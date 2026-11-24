package com.olehkan.olehkan.ui.navigation

import androidx.compose.runtime.Composable
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.screens.auth.LoginScreen
import com.olehkan.olehkan.ui.screens.auth.RegisterScreen
import com.olehkan.olehkan.ui.screens.admin.AdminDashboardScreen
import com.olehkan.olehkan.ui.screens.profile.EditProfileScreen
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductFormViewModelFactory
import com.olehkan.olehkan.ui.screens.admin.productform.AdminProductListViewModelFactory
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

import com.olehkan.olehkan.data.AppContainer
import com.olehkan.olehkan.data.model.UserProfile
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.data.repository.CategoryRepositoryProvider
import com.olehkan.olehkan.ui.screens.admin.AdminDashboardScreen
import com.olehkan.olehkan.ui.screens.admin.AdminScreen
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.screens.category.CategoryViewModelFactory
import com.olehkan.olehkan.ui.screens.detail.DetailScreen
import com.olehkan.olehkan.ui.screens.favorites.FavoritesScreen
import com.olehkan.olehkan.ui.screens.home.HomeScreen
import com.olehkan.olehkan.ui.screens.orders.CheckoutScreen
import com.olehkan.olehkan.ui.screens.orders.OrderViewModel
import com.olehkan.olehkan.ui.screens.orders.OrderViewModelFactory
import com.olehkan.olehkan.ui.screens.orders.OrdersScreen
import com.olehkan.olehkan.ui.screens.profile.EditProfileScreen
import com.olehkan.olehkan.ui.screens.profile.ProfileScreen
import com.olehkan.olehkan.ui.screens.reviews.ReviewsScreen
import com.olehkan.olehkan.ui.screens.splash.SplashScreen
import com.olehkan.olehkan.ui.screens.orders.OrderDetailScreen
import com.olehkan.olehkan.ui.screens.payment.PaymentScreen
import com.olehkan.olehkan.ui.screens.payment.PaymentViewModel
import com.olehkan.olehkan.ui.screens.payment.PaymentViewModelFactory

@Composable
fun OlehKanNavHost(
    navController: NavHostController,
    authViewModel: AuthViewModel,
    profile: UserProfile,
) {
    val context = LocalContext.current
    val appContainer = remember(context.applicationContext) {
        AppContainer.get(context.applicationContext)
    }
    val categoryFactory = remember(context.applicationContext) {
        CategoryViewModelFactory(CategoryRepositoryProvider.get(context))
    }
    val productFormFactory = remember(context.applicationContext) {
        val container = AppContainer.get(context.applicationContext)
        AdminProductFormViewModelFactory(container.products, container.categories)
    }
    val productListFactory = remember(context.applicationContext) {
        AdminProductListViewModelFactory(AppContainer.get(context.applicationContext).products)
    }
    val orderViewModel: OrderViewModel = viewModel(
        factory = OrderViewModelFactory(appContainer.orders)
    )
    val paymentViewModel: PaymentViewModel = viewModel(
        factory = PaymentViewModelFactory(appContainer.payments)
    )
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

        composable(Screen.Login.route) {
            LoginScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                },
            )
        }

        composable(Screen.Register.route) {
            RegisterScreen(
                viewModel = authViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                },
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
                viewModel = orderViewModel,
                onOrderClick = { orderId ->
                    navController.navigate(
                        Screen.OrderDetail.createRoute(orderId)
                    )
                }
            )
        }

        composable(
            route = Screen.OrderDetail.route,
            arguments = listOf(
                navArgument(Screen.OrderDetail.ARG_ORDER_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments
                ?.getString(Screen.OrderDetail.ARG_ORDER_ID)
                ?: return@composable

            OrderDetailScreen(
                orderId = orderId,
                viewModel = orderViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onPayClick = { id ->
                    navController.navigate(
                        Screen.Payment.createRoute(id)
                    )
                }
            )
        }

        composable(
            route = Screen.Payment.route,
            arguments = listOf(
                navArgument(Screen.Payment.ARG_ORDER_ID) {
                    type = NavType.StringType
                }
            )
        ) { backStackEntry ->

            val orderId = backStackEntry.arguments
                ?.getString(Screen.Payment.ARG_ORDER_ID)
                ?: return@composable

            PaymentScreen(
                orderId = orderId,
                orderViewModel = orderViewModel,
                paymentViewModel = paymentViewModel,
                onBack = {
                    navController.popBackStack()
                },
                onPaymentSuccess = {
                    navController.navigate(
                        Screen.OrderDetail.createRoute(orderId)
                    ) {
                        popUpTo(Screen.Payment.route) {
                            inclusive = true
                        }
                        launchSingleTop = true
                    }
                }
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
                onCheckoutClick = { id, quantity ->
                    navController.navigate(Screen.Checkout.createRoute(id, quantity))
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
            arguments = listOf(
                navArgument("productId") { type = NavType.IntType },
                navArgument("quantity") {
                    type = NavType.IntType
                    defaultValue = 1
                },
            ),
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getInt("productId") ?: 0
            val quantity = backStackEntry.arguments?.getInt("quantity") ?: 1
            CheckoutScreen(
                productId = productId,
                quantity = quantity,
                onBack = { navController.popBackStack() },
                onDone = { orderId: String ->
                    navController.navigate(
                        Screen.Payment.createRoute(orderId)
                    ) {
                        popUpTo(Screen.Checkout.route) {
                            inclusive = true
                        }
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
                    onProducts = { navController.navigate(Screen.AdminProducts.route) },
                    onProfile = { navController.navigate(Screen.Profile.route) },
                )
            }
        }
        categoryNavigation(navController, categoryFactory, isAdmin = profile.role == UserRole.ADMIN)
        productNavigation(navController, productFormFactory, productListFactory, isAdmin = profile.role == UserRole.ADMIN)

        // Route lama tetap ada, tetapi kini mengikuti pembatasan akses admin.
        composable(Screen.Admin.route) {
            AdminRoute(profile.role == UserRole.ADMIN, onBack = { navController.popBackStack() }) {
                AdminDashboardScreen(
                    profile = profile,
                    onCategories = { navController.navigate(Screen.Categories.route) },
                    onCategoryClick = { id -> navController.navigate(Screen.CategoryDetail.createRoute(id)) },
                    onAddCategory = { navController.navigate(Screen.CategoryForm.createRoute()) },
                    onProducts = { navController.navigate(Screen.AdminProducts.route) },
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