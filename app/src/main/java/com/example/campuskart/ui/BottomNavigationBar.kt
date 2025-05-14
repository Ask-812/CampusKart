package com.example.campuskart.ui

import android.R.attr.label
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp

@Composable
fun BottomNavigationBar(selectTab: Int, onTabSelected: (Int) -> Unit) {
    NavigationBar(
        containerColor = Color(0xFF2AE2C0)
    ){
        NavigationBarItem(
            selected = selectTab == 0,
            onClick = { onTabSelected(0) },
            icon = { Icon(Icons.Filled.ShoppingCart, contentDescription = "Order")},
            label = { Text("Order", fontSize = 12.sp)}
        )
        NavigationBarItem(
            selected = selectTab == 1,
            onClick = { onTabSelected(1) },
            icon = { Icon(Icons.Filled.LocationOn, contentDescription = "Delivery")},
            label = { Text("Delivery", fontSize = 12.sp)}
        )
    }
}
