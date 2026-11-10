package com.olehkan.olehkan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.olehkan.olehkan.ui.components.BottomNavBar
import com.olehkan.olehkan.ui.navigation.OlehKanNavHost
import com.olehkan.olehkan.ui.navigation.bottomNavRoutes
import com.olehkan.olehkan.ui.theme.OlehKanTheme
import com.olehkan.olehkan.ui.theme.Paper
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        lifecycleScope.launch {
            val vm = com.olehkan.olehkan.ui.screens.orders.OrdersViewModel()
            delay(600)  // tunggu delay 400ms + margin
            val state = vm.uiState.value
            android.util.Log.d("OrdersVM", "Active: ${state.activeCount}, Completed: ${state.completedCount}")
            state.activeOrders.forEach {
                android.util.Log.d("OrdersVM", "[ACTIVE] ${it.id} - ${it.status.displayLabel}")
            }
            state.completedOrders.forEach {
                android.util.Log.d("OrdersVM", "[DONE] ${it.id} - ${it.status.displayLabel}")
            }
        }
        setContent {
            OlehKanTheme {
                val navController = rememberNavController()
                val backStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = backStackEntry?.destination?.route

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        if (currentRoute in bottomNavRoutes) {
                            BottomNavBar(
                                currentRoute = currentRoute,
                                onNavigate = { route ->
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.startDestinationId) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                            )
                        }
                    },
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Paper)
                            .padding(innerPadding)
                    ) {
                        OlehKanNavHost(navController = navController)
                    }
                }
            }
        }
    }
}