// In PaymentScreen.kt
// ... imports ...
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campuskart.AvailableGig
import com.example.campuskart.AvailableGigsRepository
import com.example.campuskart.ui.OrderViewModel // Ensure this is imported
import androidx.lifecycle.viewmodel.compose.viewModel // Ensure this is imported for viewModels
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination

@Composable
fun PaymentScreen(
    navController: NavController,
    orderAmount: Double = 100.0,
    orderViewModel: OrderViewModel = viewModel() // Directly get the OrderViewModel instance
) {
    // ... (your existing deliveryCharge, platformFee, totalAmount) ...
    // No need to call viewModel() again if it's passed as a parameter
    // val orderViewModel: OrderViewModel = viewModel() // This line is redundant if using the parameter

    Column(
        // ...
    ) {
        // ... (Your Text views for payment details) ...

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                // 1. Get the items list (it's already List<OrderItem>)
                val currentOrderItems = orderViewModel.orderItems.toList() // Create a snapshot copy

                // 2. Create the AvailableGig object
                val newGig = AvailableGig(
                    items = currentOrderItems, // Pass the list of OrderItems
                    pickupLocationName = orderViewModel.pickupDetails.ifBlank { "Not Specified" }, // Fallback
                    dropLocationName = orderViewModel.dropDetails.ifBlank { "Not Specified" },   // Fallback
                    requesterName = "A Student" // Placeholder for now
                )

                // 3. Add to the repository
                AvailableGigsRepository.addGig(newGig)

                // 4. Reset the OrderViewModel for the requester
                orderViewModel.reset()

                // 5. Navigate to home
                navController.navigate("home") {
                    popUpTo(navController.graph.findStartDestination().id) {
                        inclusive = true
                    }
                    launchSingleTop = true
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2AE2C0)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Confirm Payment", fontSize = 18.sp, color = Color.White) // Assuming text color should be White for contrast
        }
    }
}