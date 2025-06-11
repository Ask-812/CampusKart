package com.example.campuskart.ui

import PaymentScreen
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.campuskart.LoginScreen
import com.example.campuskart.RegistrationScreen
import com.example.campuskart.ui.*
import com.example.campuskart.viewmodel.AuthViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun Navigation() {
    val navController: NavHostController = rememberNavController()
    val sharedOrderViewModel: OrderViewModel = viewModel()
    val authViewModel: AuthViewModel = viewModel()
    val authState by authViewModel.authState.collectAsState()
    NavHost(navController = navController, startDestination = "splash") {

        composable("splash") { SplashScreen(navController, authViewModel = authViewModel) }
        composable("login") {
            LoginScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("registration") {
            RegistrationScreen(navController = navController, authViewModel = authViewModel)
        }
        composable("home") { HomeScreen(navController, sharedOrderViewModel) }
        composable("delivery") { DeliveryScreen(navController, sharedOrderViewModel) }
        composable(
            "gig_detail/{gigId}/{itemsString}/{pickupLocation}/{dropLocation}/{requesterName}"
        ) { backStackEntry ->
            val gigId = backStackEntry.arguments?.getString("gigId")
            val itemsString = backStackEntry.arguments?.getString("itemsString")?.let { URLDecoder.decode(it, Charsets.UTF_8.name()) }
            val pickupLocation = backStackEntry.arguments?.getString("pickupLocation")?.let { URLDecoder.decode(it, Charsets.UTF_8.name()) }
            val dropLocation = backStackEntry.arguments?.getString("dropLocation")?.let { URLDecoder.decode(it, Charsets.UTF_8.name()) }
            val requesterName = backStackEntry.arguments?.getString("requesterName")?.let { URLDecoder.decode(it, Charsets.UTF_8.name()) }

            GigsDetailScreen(
                navController = navController,
                gigId = gigId,
                itemsString = itemsString,
                pickupLocation = pickupLocation,
                dropLocation = dropLocation,
                requesterName = requesterName
            )
        }
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

fun String.encodeUrl(): String = URLEncoder.encode(this, Charsets.UTF_8.name())