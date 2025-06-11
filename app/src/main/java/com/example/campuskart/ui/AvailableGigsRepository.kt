// In AvailableGigsRepository.kt
package com.example.campuskart

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import com.example.campuskart.ui.OrderItem // Ensure this is the correct import for your OrderItem

// Data class to represent an available gig for the DeliveryScreen
data class AvailableGig(
    val id: String = System.currentTimeMillis().toString(), // Simple unique ID
    val items: List<OrderItem>, // <-- Store the actual list of items
    val pickupLocationName: String,
    val dropLocationName: String,
    val requesterName: String = "Anonymous Student" // Placeholder
    // You can add more fields later, like estimated earnings, etc.
)

object AvailableGigsRepository {
    // ... (rest of the object remains the same)
    val availableGigs = mutableStateListOf<AvailableGig>()
    fun addGig(gig: AvailableGig) {
        Log.d("GigsRepository", "addGig called with: Pickup='${gig.pickupLocationName}', Drop='${gig.dropLocationName}', Items='${gig.items.joinToString()}'")
        availableGigs.add(0, gig)
    }

    fun removeGig(gig: AvailableGig) {
        availableGigs.remove(gig)
    }
}