package com.example.campuskart.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campuskart.ui.*

@Composable
fun Navigation() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController) }
        composable("delivery") { DeliveryScreen(navController) }
        composable("payment") { PaymentScreen(navController) }
        composable("order") {
            val orderViewModel: OrderViewModel = viewModel()
            OrderScreen(navController, orderViewModel)
        }

        composable("select_location/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "pickup"
            val orderViewModel: OrderViewModel = viewModel() // ← same ViewModel is used
            LocationScreen(navController, type, orderViewModel)
        }
    }
}
