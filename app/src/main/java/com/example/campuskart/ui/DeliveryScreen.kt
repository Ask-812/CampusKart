// In DeliveryScreen.kt
package com.example.campuskart.ui

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
// import androidx.compose.ui.geometry.isEmpty // Not typically needed for list check
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
// Remove: import com.example.campuskart.AvailableGigsRepository
import com.example.campuskart.AvailableGig // Your data class for UI
import com.example.campuskart.viewmodel.GigViewModel // Import GigViewModel
// import com.example.campuskart.ui.OrderItem // Already imported via AvailableGig or GigViewModel likely

@Composable
fun DeliveryScreen(
    navController: NavController,
    orderViewModel: OrderViewModel, // Assuming OrderViewModel might still be needed for other things or passed from Home
    gigViewModel: GigViewModel = viewModel() // Get GigViewModel instance
) {
    // val gigs = AvailableGigsRepository.availableGigs // Remove this line

    val gigListState by gigViewModel.gigListState.collectAsState()
    // gigViewModel.fetchAvailableGigs() // The init block in GigViewModel now calls this

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Available Delivery Gigs", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))

        when {
            gigListState.isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            gigListState.error != null -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "Error loading gigs: ${gigListState.error}",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            gigListState.gigs.isEmpty() -> { // Check the fetched list
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No delivery gigs available right now. Check back soon!", fontSize = 18.sp)
                }
            }
            else -> {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(gigListState.gigs, key = { it.id }) { gig ->
                        Log.d("DeliveryScreen", "Rendering gig from Firestore: ID='${gig.id}', Pickup='${gig.pickupLocationName}', Drop='${gig.dropLocationName}'")
                        GigListItem(gig = gig) {
                            // Ensure URL encoding is still appropriate if these strings can contain special chars
                            val itemsString = gig.items.joinToString(separator = "\n") {
                                "- ${it.name} (${it.quantity} ${it._unit})"
                            }.encodeUrl()
                            val pickup = gig.pickupLocationName.encodeUrl()
                            val drop = gig.dropLocationName.encodeUrl()
                            val requester = gig.requesterName.encodeUrl()

                            navController.navigate(
                                "gig_detail/${gig.id}/$itemsString/$pickup/$drop/$requester"
                            )
                        }
                        Divider()
                    }
                }
            }
        }
    }
}

// GigListItem composable remains the same
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
                text = "Deliver Items:",
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))

            gig.items.forEach { item ->
                Text(
                    text = "- ${item.name} (${item.quantity} ${item._unit})",
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