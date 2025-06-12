package com.example.campuskart.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.viewmodel.AuthViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.ui.graphics.Brush
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController, viewModel: OrderViewModel) {
    var selectTab by remember { mutableStateOf(0) }
    val authViewModel: AuthViewModel = viewModel() // Get AuthViewModel instance
    val authState by authViewModel.authState.collectAsState()

    // Observe authState to navigate to login if user becomes null (logged out)
    LaunchedEffect(authState.currentUser) {
        if (authState.currentUser == null && !authState.isLoading) {
            // Check isLoading to prevent navigation during initial load if currentUser is briefly null
            navController.navigate("login") {
                popUpTo("home") { inclusive = true } // Remove home from back stack
                launchSingleTop = true          // Avoid multiple login instances
            }
        }
    }
    Scaffold(
        topBar = {
            val appBarBrush = Brush.verticalGradient(colors = listOf(Color(0xFF2AE2C0), Color.White))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(appBarBrush)
            ){
                androidx.compose.material3.TopAppBar(
                    title = { Text("CampusKart") },
                    actions = {
                        IconButton(onClick = {
                            navController.navigate("profile")
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Person,
                                contentDescription = "Profile"
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color.Transparent,
                        titleContentColor = Color.Black,
                        actionIconContentColor = Color.Black
                    )
                )
            }
        },
        bottomBar = {
            BottomNavigationBar(selectTab) {selectTab = it }
        }
    ) {paddingValues ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)) {
        if(selectTab == 0){
            OrderScreen(navController, viewModel)
        } else{
            DeliveryScreen(navController, viewModel)
        }
        }
    }
}