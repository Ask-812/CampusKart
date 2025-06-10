package com.example.campuskart.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.ui.OrderScreen

@Composable
fun PaymentScreen(navController: NavController, orderAmount: Double = 100.0) {
    val deliveryCharge = 20.0
    val platformFee = 5.0
    val totalAmount = orderAmount + deliveryCharge + platformFee
    val orderViewModel: OrderViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Payment Details", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        Text("Order Amount: ₹$orderAmount", fontSize = 18.sp)
        Text("Delivery Charge: ₹$deliveryCharge", fontSize = 18.sp)
        Text("Platform Fee: ₹$platformFee", fontSize = 18.sp)
        Text("Total Amount: ₹$totalAmount", fontSize = 20.sp, color = Color.Red)

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                orderViewModel.reset()
                navController.navigate("order")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AE2C0)), // Aqua Green Button
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Payment", fontSize = 18.sp, color = Color.White)
        }
    }
}
