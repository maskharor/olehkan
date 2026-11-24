package com.olehkan.olehkan.ui.navigation
sealed class Screen(val route: String) {

    data object Splash : Screen("splash")
    data object Welcome : Screen("welcome")
    data object Home : Screen("home")
    data object Favorites : Screen("favorites")
    data object Orders : Screen("orders")
    data object Profile : Screen("profile")
    data object OrderDetail : Screen("order-detail/{orderId}") {
        const val ARG_ORDER_ID = "orderId"

        fun createRoute(orderId: String): String =
            "order-detail/$orderId"
    }

    data object Payment : Screen("payment/{orderId}") {
        const val ARG_ORDER_ID = "orderId"

        fun createRoute(orderId: String): String =
            "payment/$orderId"
    }

    data object Detail : Screen("detail/{productId}") {
        fun createRoute(productId: Int): String = "detail/$productId"
    }

    data object Checkout : Screen("checkout/{productId}?quantity={quantity}") {
        const val ARG_PRODUCT_ID = "productId"
        const val ARG_QUANTITY = "quantity"
        fun createRoute(productId: Int, quantity: Int = 1): String = "checkout/$productId?quantity=$quantity"
    }

    data object Reviews : Screen("reviews?productId={productId}") {
        const val ARG_PRODUCT_ID = "productId"
        fun createRoute(productId: Int?): String =
            if (productId != null) "reviews?productId=$productId" else "reviews"
    }

    data object Categories : Screen("categories")
    data object CategoryDetail : Screen("categories/{categoryId}") {
        fun createRoute(categoryId: String): String = "categories/$categoryId"
    }
    data object CategoryForm : Screen("category-form?categoryId={categoryId}") {
        fun createRoute(categoryId: String? = null): String =
            if (categoryId == null) "category-form" else "category-form?categoryId=$categoryId"
    }

    data object Store : Screen("store")
    data object AdminDashboard : Screen("admin-dashboard")
    data object EditProfile : Screen("edit-profile")
    data object Admin : Screen("admin")
}

val bottomNavRoutes: Set<String> = setOf(
    Screen.Home.route,
    Screen.Favorites.route,
    Screen.Orders.route,
    Screen.Profile.route,
)