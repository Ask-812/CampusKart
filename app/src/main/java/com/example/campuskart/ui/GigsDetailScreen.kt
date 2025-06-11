package com.example.campuskart.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

// Simplified data class for passing to this screen for now
// Ideally, you'd pass a Gig ID and fetch from repository, or pass the whole Gig object
// if it's Parcelable or if you're using Compose Navigation's new type-safe argument passing.
// For now, let's assume we pass strings.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GigsDetailScreen(
    navController: NavController,
    gigId: String?, // You'll use this to fetch the actual gig
    itemsString: String?,
    pickupLocation: String?,
    dropLocation: String?,
    requesterName: String?
) {
    // In a real app, you'd use gigId to fetch the full AvailableGig object
    // from AvailableGigsRepository here, perhaps in a LaunchedEffect.
    // val gig = AvailableGigsRepository.availableGigs.find { it.id == gigId }
    // For this example, we're just displaying the passed strings.

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gig Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF2AE2C0),
                    titleContentColor = Color.Black,
                    navigationIconContentColor = Color.Black
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.Start
        ) {
            if (pickupLocation == null) { // Basic check if data is missing
                Text("Gig details not found or an error occurred.", fontSize = 18.sp)
                return@Column
            }

            Text("Deliver Items:", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            // itemsString would be a pre-formatted string or you'd parse it
            Text(itemsString ?: "No items listed", fontSize = 16.sp)

            Spacer(modifier = Modifier.height(16.dp))
            Text("From: $pickupLocation", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("To: $dropLocation", fontSize = 16.sp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Posted by: $requesterName", fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom

            Button(
                onClick = {
                    // TODO: Implement "Accept Gig" functionality
                    // For now, just pop back or show a toast
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(Color(0xFF2AE2C0))
            ) {
                Text("Accept Gig (Placeholder)", color = Color.Black, fontSize = 18.sp)
            }
        }
    }
}