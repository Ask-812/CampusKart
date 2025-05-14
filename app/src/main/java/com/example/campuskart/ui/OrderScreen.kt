package com.example.campuskart.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.LatLng

data class OrderItem(var name: String, var quantity: String)

class OrderViewModel : ViewModel() {
    var pickupLatLng by mutableStateOf<LatLng?>(null)
    var pickupDetails by mutableStateOf("")

    var dropLatLng by mutableStateOf<LatLng?>(null)
    var dropDetails by mutableStateOf("")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderScreen(navController: NavController) {
    var orderItems by remember { mutableStateOf(mutableStateListOf(OrderItem("", ""))) }
    var pickupLocation by remember { mutableStateOf("") }
    var dropLocation by remember { mutableStateOf("") }
    var viewmodel by remember { mutableStateOf(OrderViewModel()) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Place an Order", fontSize = 24.sp, color = Color.Black)

        Spacer(modifier = Modifier.height(16.dp))

        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                itemsIndexed(orderItems) { index, item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = item.name,
                            onValueChange = {
                                orderItems[index] = orderItems[index].copy(name = it)
                            },
                            label = { Text("Item Name") },
                            modifier = Modifier.weight(2f)
                        )

                        OutlinedTextField(
                            value = item.quantity,
                            onValueChange = {
                                orderItems[index] = orderItems[index].copy(quantity = it)
                            },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { orderItems.removeAt(index) }, modifier =  Modifier.align(Alignment.CenterVertically)) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Item")
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement =  Arrangement.End){
                        Button(
                            onClick = {
                                orderItems.add(OrderItem("", ""))
                            },
                            modifier = Modifier
                                .padding(vertical = 8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AE2C0))
                        ) {
                            Text("+ Add Item", color = Color.Black)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("select_location/pickup") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (viewmodel.pickupLatLng != null)
                    "Pickup: ${viewmodel.pickupDetails}"
                else "Select Pickup Location"
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = { navController.navigate("select_location/drop") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = if (viewmodel.dropLatLng != null)
                    "Drop: ${viewmodel.dropDetails}"
                else "Select Drop Location"
            )
        }


        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                println("Order placed with items: $orderItems")
                println("Pickup: ${viewmodel.pickupLatLng} - ${viewmodel.pickupDetails}")
                println("Drop: ${viewmodel.dropLatLng} - ${viewmodel.dropDetails}")
                navController.navigate("payment")
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AE2C0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Place Order", fontSize = 18.sp, color = Color.Black)
        }
    }
}
