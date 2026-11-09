package com.olehkan.olehkan.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val route: String,
    val label: String,
    val iconSelected: ImageVector,
    val iconUnselected: ImageVector,
)

val bottomNavItems: List<BottomNavItem> = listOf(
    BottomNavItem(
        route = Screen.Home.route,
        label = "Beranda",
        iconSelected = Icons.Filled.Home,
        iconUnselected = Icons.Outlined.Home,
    ),
    BottomNavItem(
        route = Screen.Favorites.route,
        label = "Favorit",
        iconSelected = Icons.Filled.Favorite,
        iconUnselected = Icons.Outlined.FavoriteBorder,
    ),
    BottomNavItem(
        route = Screen.Orders.route,
        label = "Pesanan",
        iconSelected = Icons.Filled.ShoppingBag,
        iconUnselected = Icons.Outlined.ShoppingBag,
    ),
    BottomNavItem(
        route = Screen.Profile.route,
        label = "Profil",
        iconSelected = Icons.Filled.Person,
        iconUnselected = Icons.Outlined.Person,
    ),
)