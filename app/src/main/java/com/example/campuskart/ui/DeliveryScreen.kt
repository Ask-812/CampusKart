// In DeliveryScreen.kt
package com.example.campuskart.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.isEmpty
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.AvailableGigsRepository
import com.example.campuskart.AvailableGig

import com.example.campuskart.ui.OrderItem // Ensure this import is correct

@Composable
fun DeliveryScreen(navController: NavController, viewModel: OrderViewModel) {
    val gigs = AvailableGigsRepository.availableGigs // Get the list of gigs

    var viewmodel: OrderViewModel = viewModel()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Available Delivery Gigs", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        if (gigs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No delivery gigs available right now. Check back soon!", fontSize = 18.sp)
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(gigs, key = { it.id }) { gig ->
                Log.d("DeliveryScreen", "Rendering gig: ID='${gig.id}', Pickup='${gig.pickupLocationName}', Drop='${gig.dropLocationName}'")
                GigListItem(gig = gig, {})
                Divider()
            }
            }
        }
    }
}

// In DeliveryScreen.kt
// ... other imports ...
// ... DeliveryScreen composable remains mostly the same ...

@Composable
fun GigListItem(gig: AvailableGig, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Deliver Items:", // Changed title
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            // Display each item
            gig.items.forEach { item ->
                Text(
                    text = "- ${item.name} (${item.quantity} ${item._unit})", // Assuming OrderItem has these fields
                    fontSize = 16.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "From: ${gig.pickupLocationName}", fontSize = 16.sp)
            Text(text = "To: ${gig.dropLocationName}", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = "Posted by: ${gig.requesterName}", fontSize = 14.sp, color = Color.Gray)
        }
    }
}