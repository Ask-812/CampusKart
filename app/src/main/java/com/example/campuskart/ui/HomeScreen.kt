package com.example.campuskart.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController

@Composable
fun HomeScreen(navController: NavController) {
    var selectTab by remember { mutableStateOf(0) }
    Scaffold(
        bottomBar = {
            BottomNavigationBar(selectTab) {selectTab = it }
        }
    ) {paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
        if(selectTab == 0){
            OrderScreen(navController, viewModel())
        } else{
            DeliveryScreen(navController, viewModel())
        }
        }
    }
}