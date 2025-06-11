package com.example.campuskart.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campuskart.AvailableGig // Assuming this is your Gig data class
import com.example.campuskart.ui.OrderItem // Assuming this is your OrderItem data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Represents the state for loading gigs
data class GigListState(
    val gigs: List<AvailableGig> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

// Data class for Firestore representation, including requesterId
data class FirestoreGig(
    val id: String = "", // Firestore will generate this if not set, or use AvailableGig's id
    val items: List<OrderItem> = emptyList(),
    val pickupLocationName: String = "",
    val dropLocationName: String = "",
    val requesterName: String = "", // Original requester name
    val requesterId: String = "",   // UID of the user who created the gig
    val status: String = "available", // e.g., "available", "accepted", "completed"
    val createdAt: Long = System.currentTimeMillis() // Timestamp
)

class GigViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private val gigsCollection = db.collection("gigs")

    private val _gigListState = MutableStateFlow(GigListState())
    val gigListState: StateFlow<GigListState> = _gigListState

    private val _addGigState = MutableStateFlow<AddGigResult>(AddGigResult.Idle)
    val addGigState: StateFlow<AddGigResult> = _addGigState

    sealed class AddGigResult {
        object Idle : AddGigResult()
        object Loading : AddGigResult()
        object Success : AddGigResult()
        data class Error(val message: String) : AddGigResult()
    }

    init {
        fetchAvailableGigs()
    }

    // Function to add a gig to Firestore
    fun addGig(
        items: List<OrderItem>,
        pickupLocationName: String,
        dropLocationName: String,
        requesterDisplayName: String // Display name of the requester
    ) {
        viewModelScope.launch {
            _addGigState.value = AddGigResult.Loading
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _addGigState.value = AddGigResult.Error("User not logged in.")
                Log.e("GigViewModel", "User not logged in, cannot add gig.")
                return@launch
            }

            // Create a unique ID for the gig (can also let Firestore auto-generate)
            val gigId = gigsCollection.document().id // Firestore generated ID

            val firestoreGig = FirestoreGig(
                id = gigId,
                items = items,
                pickupLocationName = pickupLocationName,
                dropLocationName = dropLocationName,
                requesterName = requesterDisplayName, // Use the passed display name
                requesterId = currentUser.uid,
                status = "available",
                createdAt = System.currentTimeMillis()
            )

            try {
                gigsCollection.document(gigId).set(firestoreGig).await()
                _addGigState.value = AddGigResult.Success
                Log.d("GigViewModel", "Gig added successfully to Firestore with ID: $gigId")
                // Optionally, you might want to call fetchAvailableGigs() again here
                // if you don't use Firestore's real-time listeners for the list,
                // or if the list isn't automatically updated.
                // However, the real-time listener below should handle this.
            } catch (e: Exception) {
                Log.e("GigViewModel", "Error adding gig to Firestore", e)
                _addGigState.value = AddGigResult.Error(e.message ?: "Error adding gig.")
            }
        }
    }

    fun resetAddGigState() {
        _addGigState.value = AddGigResult.Idle
    }


    // Function to fetch available gigs (could be made real-time)
    fun fetchAvailableGigs() {
        _gigListState.value = _gigListState.value.copy(isLoading = true, error = null)
        // Listen for real-time updates
        gigsCollection
            .whereEqualTo("status", "available") // Only fetch gigs with "available" status
            .orderBy("createdAt", Query.Direction.DESCENDING) // Show newest first
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("GigViewModel", "Listen failed.", e)
                    _gigListState.value = _gigListState.value.copy(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d("GigViewModel", "Snapshot received with ${snapshots.size()} documents.")
                    val gigs = snapshots.documents.mapNotNull { document ->
                        Log.i("GigViewModel", "----------------------------------------------------")
                        Log.i("GigViewModel", "Processing document ID: ${document.id}")

                        val rawData = document.data
                        Log.d("GigViewModel", "Raw data for doc ${document.id}: $rawData")

                        try {
                            val firestoreGig = document.toObject(FirestoreGig::class.java) // Using .java for clarity, <FirestoreGig>() is also fine

                            if (firestoreGig != null) {
                                Log.i("GigViewModel", "SUCCESS: Parsed FirestoreGig: ID='${firestoreGig.id}', Status='${firestoreGig.status}', Requester='${firestoreGig.requesterName}', ItemsCount=${firestoreGig.items.size}")
                                AvailableGig(
                                    id = firestoreGig.id.ifEmpty { document.id },
                                    items = firestoreGig.items, // Make sure OrderItem fields match what's in Firestore
                                    pickupLocationName = firestoreGig.pickupLocationName,
                                    dropLocationName = firestoreGig.dropLocationName,
                                    requesterName = firestoreGig.requesterName
                                )
                            } else {
                                Log.w("GigViewModel", "FAILED: document.toObject<FirestoreGig>() returned NULL for document: ${document.id}")
                                null
                            }
                        } catch (ex: Exception) {
                            Log.e("GigViewModel", "EXCEPTION during parsing document ${document.id}", ex) // Log the full exception
                            null
                        }
                    }
                    _gigListState.value = GigListState(gigs = gigs, isLoading = false)
                    Log.i("GigViewModel", "Final processed gigs list size: ${gigs.size}. List: $gigs")
                    Log.i("GigViewModel", "====================================================")
                } else {
                    _gigListState.value = _gigListState.value.copy(isLoading = false, error = "No data received")
                    Log.d("GigViewModel", "Current data: null")
                }
            }
    }
    // TODO: Add functions to fetch gigs by requesterId (for "My Orders")
    // TODO: Add functions to update gig status (for "Accept Gig")
}