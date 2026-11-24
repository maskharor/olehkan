package com.olehkan.olehkan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.SystemBarStyle
import android.os.Build
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.olehkan.olehkan.data.model.UserRole
import com.olehkan.olehkan.ui.components.BottomNavBar
import com.olehkan.olehkan.ui.navigation.OlehKanNavHost
import com.olehkan.olehkan.ui.navigation.Screen
import com.olehkan.olehkan.ui.navigation.bottomNavRoutes
import com.olehkan.olehkan.ui.screens.auth.AuthViewModel
import com.olehkan.olehkan.ui.screens.auth.SessionGate
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.Paper

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Tema aplikasi saat ini selalu terang; biarkan Activity mengatur ikon sistem.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
        )
        if (Build.VERSION.SDK_INT >= 29) window.isNavigationBarContrastEnforced = false
        setContent {
            OlehKanTheme {
                val authViewModel: AuthViewModel = viewModel()
                SessionGate(authViewModel) { profile ->
                    // Pergantian akun/role selalu memperoleh back stack baru.
                    key(profile.id, profile.role) {
                        val navController = rememberNavController()
                        val backStackEntry by navController.currentBackStackEntryAsState()
                        val currentRoute = backStackEntry?.destination?.route
                        Scaffold(
                            modifier = Modifier.fillMaxSize(),
                            bottomBar = {
                                if (profile.role == UserRole.BUYER && currentRoute in bottomNavRoutes) {
                                    BottomNavBar(
                                        currentRoute = currentRoute,
                                        onNavigate = { route ->
                                            navController.navigate(route) {
                                                popUpTo(Screen.Home.route) { saveState = true }
                                                launchSingleTop = true
                                                restoreState = true
                                            }
                                        },
                                    )
                                }
                            },
                        ) { innerPadding ->
                            Box(
                                Modifier.fillMaxSize().background(Paper).padding(innerPadding).consumeWindowInsets(innerPadding)
                            ) {
                                OlehKanNavHost(navController, authViewModel, profile)
                            }
                        }
                    }
                }
            }
        }
    }
}
