package com.example.campuskart.ui

import PaymentScreen
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
    val sharedOrderViewModel: OrderViewModel = viewModel()
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController) }
        composable("home") { HomeScreen(navController, sharedOrderViewModel) }
        composable("delivery") { DeliveryScreen(navController, sharedOrderViewModel) }

        composable("payment") { PaymentScreen(navController, 100.00, sharedOrderViewModel) }
        composable("order") {
            OrderScreen(navController, sharedOrderViewModel)
        }

        composable("select_location/{type}") { backStackEntry ->
            val type = backStackEntry.arguments?.getString("type") ?: "pickup"
            LocationScreen(navController, type, sharedOrderViewModel)
        }
    }
}
