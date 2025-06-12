// In MyOrdersScreen.kt

package com.example.campuskart.ui

import android.util.Log
import android.widget.Toast // For showing delete status
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete // For the delete icon
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext // For Toast context
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.AvailableGig
import com.example.campuskart.viewmodel.AuthViewModel
import com.example.campuskart.viewmodel.GigViewModel
import com.example.campuskart.viewmodel.DeleteGigResult // Import the new result state


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyOrdersScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel(),
    gigViewModel: GigViewModel = viewModel()
) {
    val authState by authViewModel.authState.collectAsState()
    val currentUser = authState.currentUser
    val myGigsState by gigViewModel.myPostedGigsState.collectAsState()
    val deleteGigState by gigViewModel.deleteGigState.collectAsState() // Observe delete state

    val context = LocalContext.current // For showing Toasts

    // State for showing confirmation dialog
    var showDeleteConfirmationDialog by remember { mutableStateOf(false) }
    var gigToDelete by remember { mutableStateOf<AvailableGig?>(null) }


    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            gigViewModel.fetchMyPostedGigs(userId)
        }
    }

    // Effect to react to delete operation status
    LaunchedEffect(deleteGigState) {
        when (val state = deleteGigState) {
            is DeleteGigResult.Success -> {
                Toast.makeText(context, "Gig deleted successfully", Toast.LENGTH_SHORT).show()
                gigViewModel.resetDeleteGigState() // Reset state after showing message
            }
            is DeleteGigResult.Error -> {
                Toast.makeText(context, "Error: ${state.message}", Toast.LENGTH_LONG).show()
                gigViewModel.resetDeleteGigState() // Reset state
            }
            is DeleteGigResult.Loading -> {
                // Optionally show a global loading indicator or disable delete buttons
                Log.d("MyOrdersScreen", "Deletion in progress...")
            }
            DeleteGigResult.Idle -> { /* Do nothing */ }
        }
    }


    DisposableEffect(Unit) {
        onDispose {
            gigViewModel.clearMyPostedGigsListener()
            gigViewModel.resetDeleteGigState() // Also reset delete state on dispose
        }
    }

    if (showDeleteConfirmationDialog && gigToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmationDialog = false },
            title = { Text("Confirm Deletion") },
            text = { Text("Are you sure you want to delete this gig?\n'${
                gigToDelete!!.items.joinToString { it.name }
            }'") },
            confirmButton = {
                Button(
                    onClick = {
                        gigToDelete?.let { gigViewModel.deleteGig(it.id) }
                        showDeleteConfirmationDialog = false
                        gigToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteConfirmationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }


    Scaffold(
        topBar = {
            // ... (your TopAppBar code remains the same)
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            if (currentUser == null) {
                Text("Please log in to see your posted gigs.")
                return@Column
            }

            when {
                myGigsState.isLoading -> {
                    // ... (loading indicator)
                }
                myGigsState.error != null -> {
                    // ... (error message)
                }
                myGigsState.gigs.isEmpty() -> {
                    // ... (no gigs message)
                }
                else -> {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(myGigsState.gigs, key = { it.id }) { gig ->
                            MyGigListItem( // Using a potentially modified item
                                gig = gig,
                                onDeleteClick = {
                                    gigToDelete = gig
                                    showDeleteConfirmationDialog = true
                                },
                                onEditClick = {
                                    // TODO: Navigate to an edit screen if you implement editing
                                    Log.d("MyOrdersScreen", "Edit clicked for gig: ${gig.id}")
                                    Toast.makeText(context, "Edit not implemented yet.", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                // Default click action for the item itself
                                Log.d("MyOrdersScreen", "Clicked on own gig item: ${gig.id}")
                            }
                            Divider()
                        }
                    }
                }
            }
        }
    }
}

// You can either modify your existing GigListItem or create a new one like this.
// This example adds a Row with Edit/Delete buttons.
@Composable
fun MyGigListItem(
    gig: AvailableGig,
    onDeleteClick: () -> Unit,
    onEditClick: () -> Unit, // Added for potential edit functionality
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }, // The whole card is clickable
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

            Spacer(modifier = Modifier.height(10.dp)) // Space before buttons

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End, // Pushes buttons to the right
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Optional: Edit Button
                OutlinedButton(
                    onClick = onEditClick,
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text("Edit") // You could use an Icon here too
                }
                Button(
                    onClick = onDeleteClick,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete Gig", tint = MaterialTheme.colorScheme.onError)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Delete", color = MaterialTheme.colorScheme.onError)
                }
            }
        }
    }
}