// In PaymentScreen.kt
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.* // Keep existing layout imports
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
// Remove direct import of AvailableGigsRepository if no longer used here
// import com.example.campuskart.AvailableGigsRepository
// import com.example.campuskart.AvailableGig // Not directly used for creation now
import com.example.campuskart.ui.OrderViewModel
import com.example.campuskart.viewmodel.AuthViewModel // For getting current user's name
import com.example.campuskart.viewmodel.GigViewModel // Import GigViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun PaymentScreen(
    navController: NavController,
    orderAmount: Double = 100.0, // Example amount
    orderViewModel: OrderViewModel = viewModel(),
    gigViewModel: GigViewModel = viewModel(), // Get GigViewModel instance
    authViewModel: AuthViewModel = viewModel() // Get AuthViewModel for user info
) {
    val context = LocalContext.current
    // Observe the state of adding a gig
    val addGigState by gigViewModel.addGigState.collectAsState()
    val authState by authViewModel.authState.collectAsState()

    // Handle navigation and messages based on addGigState
    LaunchedEffect(addGigState) {
        when (val state = addGigState) {
            is GigViewModel.AddGigResult.Success -> {
                Toast.makeText(context, "Order posted successfully!", Toast.LENGTH_LONG).show()
                orderViewModel.reset() // Reset the order form
                gigViewModel.resetAddGigState() // Reset the add gig state
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            }
            is GigViewModel.AddGigResult.Error -> {
                Toast.makeText(context, "Error posting order: ${state.message}", Toast.LENGTH_LONG).show()
                Log.e("PaymentScreen", "Error posting gig: ${state.message}")
                gigViewModel.resetAddGigState() // Reset the add gig state
            }
            GigViewModel.AddGigResult.Loading -> {
                // You could show a full-screen loader, or the button itself will show a loader
            }
            GigViewModel.AddGigResult.Idle -> {
                // Do nothing
            }
        }
    }

    // Your existing Column and UI for payment details...
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween // Pushes button to bottom
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) { // For payment details
            Text("Confirm Your Order", fontSize = 24.sp, modifier = Modifier.padding(bottom = 16.dp))
            // Example payment details (replace with your actual UI)
            Text("Order Amount: $$orderAmount", fontSize = 18.sp)
            val deliveryCharge = 20.0
            val platformFee = 5.0
            Text("Delivery Charge: $$deliveryCharge", fontSize = 16.sp)
            Text("Platform Fee: $$platformFee", fontSize = 16.sp)
            Text(
                "Total Amount: $${orderAmount + deliveryCharge + platformFee}",
                fontSize = 20.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(top = 8.dp)
            )
            // You should ideally show order summary (items, pickup, drop) here
            // Fetched from orderViewModel
            Spacer(modifier = Modifier.height(16.dp))
            Text("Items:", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            orderViewModel.orderItems.filter { it.name.isNotBlank() }.forEach { item ->
                Text("- ${item.name} (${item.quantity} ${item._unit})")
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text("Pickup: ${orderViewModel.pickupDetails.ifBlank { "Not specified" }}", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
            Text("Drop: ${orderViewModel.dropDetails.ifBlank { "Not specified" }}", fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)

        }

        Spacer(modifier = Modifier.weight(1f)) // Pushes button to bottom if content is less

        Button(
            onClick = {
                if (addGigState is GigViewModel.AddGigResult.Loading) return@Button // Prevent multiple clicks

                val currentUserDisplayName = authState.currentUser?.displayName ?: "Anonymous User"
                // Ensure there's at least one valid item
                val validItems = orderViewModel.orderItems.filter { it.name.isNotBlank() && it.quantity.isNotBlank() }
                if (validItems.isEmpty()) {
                    Toast.makeText(context, "Please add at least one item to your order.", Toast.LENGTH_SHORT).show()
                    return@Button
                }
                if (orderViewModel.pickupDetails.isBlank() || orderViewModel.dropDetails.isBlank()) {
                    Toast.makeText(context, "Please select pickup and drop locations.", Toast.LENGTH_SHORT).show()
                    return@Button
                }


                gigViewModel.addGig(
                    items = validItems, // Pass the valid list of OrderItems
                    pickupLocationName = orderViewModel.pickupDetails,
                    dropLocationName = orderViewModel.dropDetails,
                    requesterDisplayName = currentUserDisplayName
                )
                // No longer add to local repository:
                // AvailableGigsRepository.addGig(newGig)
                // Navigation and reset is handled by LaunchedEffect observing addGigState
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AE2C0)),
            modifier = Modifier.fillMaxWidth(),
            enabled = addGigState !is GigViewModel.AddGigResult.Loading // Disable button while loading
        ) {
            if (addGigState is GigViewModel.AddGigResult.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White, // Or your desired spinner color
                    strokeWidth = 2.dp
                )
            } else {
                Text("Confirm & Post Order", fontSize = 18.sp, color = Color.Black)
            }
        }
    }
}