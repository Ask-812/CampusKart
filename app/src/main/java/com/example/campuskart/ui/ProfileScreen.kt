// Create a new file: app/src/main/java/com/example/campuskart/ui/ProfileScreen.kt
package com.example.campuskart.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = authState.currentUser

    Scaffold(
        topBar = {
        val appBarBrush = Brush.verticalGradient(colors = listOf(Color(0xFF2AE2C0), Color.White))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(appBarBrush)
        ){
            TopAppBar(
                title = { Text("Profile") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors( // Consistent styling
                    containerColor = Color.Transparent,
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            currentUser?.let { user ->
                Text("Name: ${user.displayName ?: "N/A"}", fontSize = 20.sp)
                Text("Email: ${user.email ?: "N/A"}", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(20.dp))
            }

            Button(
                onClick = {
                    // For now, a placeholder action:
                     navController.navigate("my_orders") // If MyOrdersScreen is separate
                    println("My Orders Clicked - Placeholder")
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("My Posted Gigs")
            }

            // You can add other profile items here like "Edit Profile", "Payment Methods"

            Spacer(modifier = Modifier.weight(1f)) // Pushes logout to the bottom

            Button(
                onClick = {
                    authViewModel.logoutUser()
                    // Navigation back to login will be handled by the LaunchedEffect in HomeScreen
                    // or SplashScreen that observes authState.
                    // To be safe, also pop back from profile screen after initiating logout.
                    navController.popBackStack("home", inclusive = false, saveState = false)

                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Logout", color = MaterialTheme.colorScheme.onError)
            }
        }
    }
}