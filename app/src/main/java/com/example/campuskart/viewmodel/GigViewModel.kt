package com.example.campuskart.viewmodel

import android.util.Log
import androidx.core.util.remove
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campuskart.AvailableGig // Assuming this is your Gig data class
import com.example.campuskart.ui.OrderItem // Assuming this is your OrderItem data class
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.firestore.ktx.toObjects
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


sealed class DeleteGigResult {
    object Idle : DeleteGigResult()
    object Loading : DeleteGigResult()
    object Success : DeleteGigResult()
    data class Error(val message: String) : DeleteGigResult()
}
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
    private val _myPostedGigsState = MutableStateFlow(GigListState()) // Reusing GigListState
    val myPostedGigsState: StateFlow<GigListState> = _myPostedGigsState
    private var myPostedGigsListener: ListenerRegistration? = null

    private val _deleteGigState = MutableStateFlow<DeleteGigResult>(DeleteGigResult.Idle)
    val deleteGigState: StateFlow<DeleteGigResult> = _deleteGigState
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
        dropLocationName: String
    ) {
        viewModelScope.launch {
            _addGigState.value = AddGigResult.Loading
            val currentUser = auth.currentUser
            if (currentUser == null) {
                _addGigState.value = AddGigResult.Error("User not logged in.")
                Log.e("GigViewModel", "User not logged in, cannot add gig.")
                return@launch
            }
            val displayName = currentUser.displayName?.takeIf { it.isNotBlank() } ?: "Campus User"
            // Create a unique ID for the gig (can also let Firestore auto-generate)
            val gigId = gigsCollection.document().id // Firestore generated ID

            val firestoreGig = FirestoreGig(
                id = gigId,
                items = items,
                pickupLocationName = pickupLocationName,
                dropLocationName = dropLocationName,
                requesterName = displayName, // Use the passed display name
                requesterId = currentUser.uid,
                status = "available",
                createdAt = System.currentTimeMillis()
            )

            try {
                gigsCollection.document(gigId).set(firestoreGig).await()
                _addGigState.value = AddGigResult.Success
                Log.d("GigViewModel", "Gig added successfully to Firestore with ID: $gigId by ${currentUser.uid}")
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
    fun fetchMyPostedGigs(userId: String) {
        // Cancel any previous listener to avoid multiple listeners for different users or recalls
        myPostedGigsListener?.remove()

        _myPostedGigsState.value = GigListState(isLoading = true, error = null)
        Log.d("GigViewModel", "Fetching gigs for user ID: $userId")

        myPostedGigsListener = gigsCollection
            .whereEqualTo("requesterId", userId) // Filter by the user who created the gig
            .orderBy("createdAt", Query.Direction.DESCENDING) // Show newest first
            .addSnapshotListener { snapshots, e ->
                if (e != null) {
                    Log.w("GigViewModel", "Listen failed for my posted gigs.", e)
                    _myPostedGigsState.value = GigListState(isLoading = false, error = e.message)
                    return@addSnapshotListener
                }

                if (snapshots != null) {
                    Log.d(
                        "GigViewModel",
                        "MyGigs snapshot received with ${snapshots.size()} documents for user $userId."
                    )
                    val gigs = snapshots.documents.mapNotNull { document ->
                        try {
                            val firestoreGig = document.toObject(FirestoreGig::class.java)
                            if (firestoreGig != null) {
                                AvailableGig(
                                    id = firestoreGig.id.ifEmpty { document.id },
                                    items = firestoreGig.items,
                                    pickupLocationName = firestoreGig.pickupLocationName,
                                    dropLocationName = firestoreGig.dropLocationName,
                                    requesterName = firestoreGig.requesterName
                                    // You might want to add 'status' to AvailableGig if you plan to display it
                                )
                            } else {
                                Log.w(
                                    "GigViewModel",
                                    "MyGigs: document.toObject<FirestoreGig>() returned NULL for doc: ${document.id}"
                                )
                                null
                            }
                        } catch (ex: Exception) {
                            Log.e(
                                "GigViewModel",
                                "MyGigs: EXCEPTION during parsing document ${document.id}",
                                ex
                            )
                            null
                        }
                    }
                    _myPostedGigsState.value = GigListState(gigs = gigs, isLoading = false)
                    Log.i(
                        "GigViewModel",
                        "MyGigs: Final processed list size: ${gigs.size} for user $userId."
                    )
                } else {
                    _myPostedGigsState.value =
                        GigListState(isLoading = false, error = "No data received for my gigs")
                    Log.d("GigViewModel", "MyGigs: Current data: null for user $userId")
                }
            }
    }
    fun clearMyPostedGigsListener() {
        Log.d("GigViewModel", "Clearing MyPostedGigs listener.")
        myPostedGigsListener?.remove()
        myPostedGigsListener = null
        _myPostedGigsState.value = GigListState() // Reset the state
    }
    fun deleteGig(gigId: String) {
        viewModelScope.launch {
            _deleteGigState.value = DeleteGigResult.Loading
            Log.d("GigViewModel", "Attempting to delete gig with ID: $gigId")
            try {
                gigsCollection.document(gigId).delete().await()
                _deleteGigState.value = DeleteGigResult.Success
                Log.d("GigViewModel", "Gig deleted successfully: $gigId")
                // The real-time listener for myPostedGigsState should automatically
                // update the list once the item is deleted from Firestore.
                // If not using real-time listener for myPostedGigs, you might need to manually refresh:
                // auth.currentUser?.uid?.let { fetchMyPostedGigs(it) }
            } catch (e: Exception) {
                Log.e("GigViewModel", "Error deleting gig: $gigId", e)
                _deleteGigState.value = DeleteGigResult.Error(e.message ?: "Error deleting gig.")
            }
        }
    }

    // Function to reset the delete state, useful after the UI has shown a message
    fun resetDeleteGigState() {
        _deleteGigState.value = DeleteGigResult.Idle
    }
    override fun onCleared() {
        super.onCleared()
        Log.d("GigViewModel", "onCleared called, clearing MyPostedGigs listener.")
        clearMyPostedGigsListener()
        // Any other cleanup for listeners (like the one in fetchAvailableGigs if you manually manage it)
    }

    // TODO: Add functions to update gig status (for "Accept Gig")
}