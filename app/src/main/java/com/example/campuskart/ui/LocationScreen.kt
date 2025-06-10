package com.example.campuskart.ui

import android.Manifest // For location permission
import android.app.Activity
import android.content.pm.PackageManager // For location permission
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext // For Places Autocomplete and FusedLocationProviderClient
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat // For location permission
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.location.FusedLocationProviderClient // For current location
import com.google.android.gms.location.LocationServices // For current location
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun LocationScreen(
    navController: NavController,
    type: String, // "pickup" or "drop"
    viewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    var locationDetails by remember { mutableStateOf("") }
    // Default to a known location, will be updated by search or current location
    var selectedLocation by remember { mutableStateOf(LatLng(26.1445, 91.7362)) }
    var selectedPlaceName by remember { mutableStateOf<String?>(null) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }
    val markerState = rememberMarkerState(position = selectedLocation)

    // FusedLocationProviderClient for current location
    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    // Launcher for Places Autocomplete
    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            selectedLocation = place.latLng!!
            selectedPlaceName = place.name ?: place.address // Use name or address
            locationDetails = place.address ?: "" // Pre-fill details with address
        }
    }

    // Launcher for location permission request
    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission is granted. Get the last known location.
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    location?.let {
                        selectedLocation = LatLng(it.latitude, it.longitude)
                        selectedPlaceName = "Current Location"
                        locationDetails = "Near my current position" // Or try reverse geocoding
                    }
                }
            } catch (e: SecurityException) {
                // Handle exception if permission was revoked between check and use
                // Or if FusedLocationProviderClient throws a security exception
            }
        } else {
            // Explain to the user that the feature is unavailable because the
            // feature requires a permission that the user has denied.
            // You might want to show a Snackbar or dialog.
        }
    }

    // Function to launch Places Autocomplete
    fun launchAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
            .build(context)
        autocompleteLauncher.launch(intent)
    }

    fun getCurrentLocation() {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) -> {
                // You can use the API that requires the permission.
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                        location?.let {
                            selectedLocation = LatLng(it.latitude, it.longitude)
                            selectedPlaceName = "Current Location"
                            // Optionally, you can perform reverse geocoding here to get an address
                            locationDetails = "Near my current position"
                        }
                    }
                } catch (e: SecurityException) {
                    // This should ideally not happen if permission is already granted
                    // but good to have for robustness.
                }
            }
            else -> {
                // Ask for the permission.
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }


    LaunchedEffect(selectedLocation) {
        cameraPositionState.animate(
            com.google.android.gms.maps.CameraUpdateFactory.newLatLngZoom(selectedLocation, 17f), // Zoom in a bit more
            1000 // Animation duration in ms
        )
        markerState.position = selectedLocation
        // Update ViewModel when selectedLocation actually changes from search/current
        if (type == "pickup") {
            viewModel.pickupLatLng = selectedLocation
            // We'll update details when "Save Location" is clicked
        } else {
            viewModel.dropLatLng = selectedLocation
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(16.dp)
    ) {
        Text(
            text = if (type == "pickup") "Select Pickup Location" else "Select Drop Location",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Search Button
        Button(onClick = { launchAutocomplete() }, modifier = Modifier.fillMaxWidth()) {
            Text(selectedPlaceName ?: "Search for a place")
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Current Location Button
        OutlinedButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "My Location", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allow map to take available space
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                    selectedPlaceName = "Custom Pin" // Or fetch address via reverse geocoding
                    locationDetails = "" // Clear details if map is clicked
                }
            ) {
                Marker(
                    state = markerState,
                    title = selectedPlaceName ?: "Selected Location",
                    snippet = locationDetails.takeIf { it.isNotEmpty() }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = locationDetails,
            onValueChange = { locationDetails = it },
            label = { Text("Additional Location Details (e.g., Flat No, Landmark)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if (selectedPlaceName != null && selectedPlaceName != "Custom Pin") selectedPlaceName!! else "Optional details") }
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalDetails = if (locationDetails.isNotBlank()) {
                    locationDetails
                } else {
                    selectedPlaceName ?: "Selected Location" // Fallback to place name if details are empty
                }

                if (type == "pickup") {
                    viewModel.pickupLatLng = selectedLocation
                    viewModel.pickupDetails = finalDetails
                } else {
                    viewModel.dropLatLng = selectedLocation
                    viewModel.dropDetails = finalDetails
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = viewModel.pickupLatLng != null || viewModel.dropLatLng != null // Enable only if a location is set
        ) {
            Text("Save Location")
        }
    }
}