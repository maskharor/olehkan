package com.olehkan.olehkan.ui.navigation
sealed class Screen(val route: String) {

    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Orders : Screen("orders")
    data object Profile : Screen("profile")

    data object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int): String = "detail/$productId"
    }

    data object Checkout : Screen("checkout/{productId}") {
        fun createRoute(productId: Int): String = "checkout/$productId"
    }

    data object Reviews : Screen("reviews?productId={productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: Int?): String =
            if (productId != null) "reviews?productId=$productId" else "reviews"
    }

    data object Admin : Screen("admin")
}

val bottomNavRoutes: Set<String> = setOf(
    Screen.Home.route,
    Screen.Favorites.route,
    Screen.Orders.route,
    Screen.Profile.route,
)