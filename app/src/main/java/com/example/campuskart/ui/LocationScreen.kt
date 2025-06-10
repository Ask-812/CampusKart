package com.example.campuskart.ui

import android.Manifest // For location permission
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager // For location permission
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.Toast
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
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.location.FusedLocationProviderClient // For current location
import com.google.android.gms.location.LocationServices // For current location
import com.google.android.gms.location.Priority
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState

const val LOCATION_SCREEN_TAG = "LocationScreen"

@Composable
fun LocationScreen(
    navController: NavController,
    type: String, // "pickup" or "drop"
    viewModel: OrderViewModel = viewModel()
) {
    val context = LocalContext.current
    var locationDetails by remember { mutableStateOf(if (type == "pickup") viewModel.pickupDetails else viewModel.dropDetails) }

    // Initialize with ViewModel's LatLng or default
    val initialLatLng = if (type == "pickup") {
        viewModel.pickupLatLng ?: LatLng(26.1445, 91.7362) // Default Guwahati
    } else {
        viewModel.dropLatLng ?: LatLng(26.1445, 91.7362) // Default Guwahati
    }
    var selectedLocation by remember { mutableStateOf(initialLatLng) }
    var selectedPlaceName by remember { mutableStateOf<String?>(if (locationDetails.isNotBlank() && locationDetails != "Near my current position" && locationDetails != "Custom Pin") locationDetails.split("\n").firstOrNull() else null) }


    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }
    val markerState = rememberMarkerState(position = selectedLocation)

    val fusedLocationClient: FusedLocationProviderClient =
        remember { LocationServices.getFusedLocationProviderClient(context) }

    // Check Google Play Services availability
    LaunchedEffect(Unit) {
        val googleApiAvailability = GoogleApiAvailability.getInstance()
        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(context)
        if (resultCode != ConnectionResult.SUCCESS) {
            Log.e(LOCATION_SCREEN_TAG, "Google Play Services not available/outdated. Code: $resultCode")
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                // Consider showing dialog: googleApiAvailability.getErrorDialog(context as Activity, resultCode, 9000)?.show()
                // This requires casting context to Activity, which might not always be safe here.
                // For a composable, you might need a different way to show a system dialog or navigate.
            }
            Toast.makeText(context, "Google Play Services is required for location features.", Toast.LENGTH_LONG).show()
        } else {
            Log.d(LOCATION_SCREEN_TAG, "Google Play Services are available.")
        }
    }

    fun updateLocationUI(location: android.location.Location, source: String) {
        selectedLocation = LatLng(location.latitude, location.longitude)
        selectedPlaceName = "Current Location" // Keep it simple, details can be added separately
        locationDetails = "Near my current position" // Default detail for current location
        Log.d(LOCATION_SCREEN_TAG, "$source location: ${location.latitude}, ${location.longitude}")
        Toast.makeText(context, "$source location updated", Toast.LENGTH_SHORT).show()
    }

    fun fetchFreshLocation(rationale: String = "Fetching fresh location") {
        Log.d(LOCATION_SCREEN_TAG, rationale)
        try {
            val currentLocationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                // .setDurationMillis(10000) // Optional: Max time to wait for location
                .build()
            val cancellationTokenSource = CancellationTokenSource()

            fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.token)
                .addOnSuccessListener { freshLocation: android.location.Location? ->
                    if (freshLocation != null) {
                        updateLocationUI(freshLocation, "Fresh")
                    } else {
                        Log.w(LOCATION_SCREEN_TAG, "Failed to get fresh location (was null).")
                        Toast.makeText(context, "Could not determine current location. Try searching or pin on map.", Toast.LENGTH_LONG).show()
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(LOCATION_SCREEN_TAG, "Failed to get fresh location: ${e.message}", e)
                    Toast.makeText(context, "Error getting current location: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        } catch (e: SecurityException) {
            Log.e(LOCATION_SCREEN_TAG, "SecurityException during fetchFreshLocation: ${e.message}")
            Toast.makeText(context, "Location permission error. Please grant permission.", Toast.LENGTH_SHORT).show()
        }
    }

    val requestPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Log.d(LOCATION_SCREEN_TAG, "ACCESS_FINE_LOCATION permission GRANTED by user.")
            if (!isLocationEnabled(context)) {
                promptEnableLocationServices(context)
                return@rememberLauncherForActivityResult
            }
            // Try last known location first, then fresh
            Log.d(LOCATION_SCREEN_TAG, "Permission granted. Attempting to get last known location.")
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation ->
                    if (lastKnownLocation != null) {
                        updateLocationUI(lastKnownLocation, "Cached")
                    } else {
                        fetchFreshLocation("Last known location is null after permission grant. Requesting fresh.")
                    }
                }.addOnFailureListener { e ->
                    Log.e(LOCATION_SCREEN_TAG, "Error getting lastLocation after permission grant: ${e.message}")
                    fetchFreshLocation("Failed to get last known location. Requesting fresh.")
                }
            } catch (e: SecurityException) {
                Log.e(LOCATION_SCREEN_TAG, "SecurityException after permission grant for lastLocation: ${e.message}")
                Toast.makeText(context, "Location permission error. Please grant permission.", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.w(LOCATION_SCREEN_TAG, "ACCESS_FINE_LOCATION permission DENIED by user.")
            Toast.makeText(context, "Location permission is required to use current location.", Toast.LENGTH_LONG).show()
        }
    }

    fun getCurrentLocation() {
        Log.d(LOCATION_SCREEN_TAG, "getCurrentLocation called.")
        when {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d(LOCATION_SCREEN_TAG, "Permission ALREADY GRANTED.")
                if (!isLocationEnabled(context)) {
                    promptEnableLocationServices(context)
                    return
                }
                Log.d(LOCATION_SCREEN_TAG, "Permission granted & location enabled. Attempting to get last known location.")
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastKnownLocation ->
                        if (lastKnownLocation != null) {
                            updateLocationUI(lastKnownLocation, "Cached")
                        } else {
                            fetchFreshLocation("Last known location is null (permission was already granted). Requesting fresh.")
                        }
                    }.addOnFailureListener { e ->
                        Log.e(LOCATION_SCREEN_TAG, "Error getting lastLocation (permission was already granted): ${e.message}")
                        fetchFreshLocation("Failed to get last known location (permission already granted). Requesting fresh.")
                    }
                } catch (e: SecurityException) {
                    Log.e(LOCATION_SCREEN_TAG, "SecurityException in getCurrentLocation (PERMISSION_GRANTED block): ${e.message}")
                    Toast.makeText(context, "Location permission error. Please ensure it's granted.", Toast.LENGTH_SHORT).show()
                }
            }
            // Optional: You can add shouldShowRequestPermissionRationale logic here if needed for a better UX
            // ActivityCompat.shouldShowRequestPermissionRationale(context as Activity, Manifest.permission.ACCESS_FINE_LOCATION)
            else -> {
                Log.d(LOCATION_SCREEN_TAG, "Permission NOT granted. Launching request...")
                requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }


    // Launcher for Places Autocomplete
    val autocompleteLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val place = Autocomplete.getPlaceFromIntent(result.data!!)
                place.latLng?.let {
                    selectedLocation = it
                    selectedPlaceName = place.name ?: place.address
                    locationDetails = place.address ?: "" // Pre-fill details with address
                    Log.d(LOCATION_SCREEN_TAG, "Place selected: ${place.name}, ${it.latitude}, ${it.longitude}")
                } ?: run {
                    Log.w(LOCATION_SCREEN_TAG, "Selected place has no LatLng.")
                    Toast.makeText(context, "Could not get location for selected place.", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e(LOCATION_SCREEN_TAG, "Error processing autocomplete result: ${e.message}", e)
                Toast.makeText(context, "Error selecting place.", Toast.LENGTH_SHORT).show()
            }
        } else if (result.resultCode == AutocompleteActivity.RESULT_ERROR) {
            val status = Autocomplete.getStatusFromIntent(result.data!!)
            Log.e(LOCATION_SCREEN_TAG, "Autocomplete error: ${status.statusMessage}")
            Toast.makeText(context, "Error searching for place: ${status.statusMessage}", Toast.LENGTH_LONG).show()
        }
    }

    // Function to launch Places Autocomplete
    fun launchAutocomplete() {
        val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)
        try {
            val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(context)
            autocompleteLauncher.launch(intent)
        } catch (e: Exception) {
            Log.e(LOCATION_SCREEN_TAG, "Error launching autocomplete: ${e.message}", e)
            Toast.makeText(context, "Error opening place search.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(selectedLocation) {
        if (cameraPositionState.position.target != selectedLocation) {
            Log.d(LOCATION_SCREEN_TAG, "Animating map to: $selectedLocation")
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(selectedLocation, 17f),
                1000 // Animation duration in ms
            )
        }
        markerState.position = selectedLocation

        // Update ViewModel with LatLng immediately
        // Details are updated on save
        if (type == "pickup") {
            viewModel.pickupLatLng = selectedLocation
        } else {
            viewModel.dropLatLng = selectedLocation
        }
    }

    // Initialize text field if loaded from ViewModel and not yet set by current location/search
    LaunchedEffect(Unit) {
        if (type == "pickup" && viewModel.pickupLatLng == selectedLocation && viewModel.pickupDetails.isNotBlank()) {
            if (locationDetails.isEmpty() || locationDetails == "Near my current position" || locationDetails == "Custom Pin"){
                locationDetails = viewModel.pickupDetails
                if (selectedPlaceName == null || selectedPlaceName == "Current Location" || selectedPlaceName == "Custom Pin"){
                    selectedPlaceName = viewModel.pickupDetails.split("\n").firstOrNull() ?: "Saved Location"
                }
            }
        } else if (type == "drop" && viewModel.dropLatLng == selectedLocation && viewModel.dropDetails.isNotBlank()) {
            if (locationDetails.isEmpty() || locationDetails == "Near my current position" || locationDetails == "Custom Pin"){
                locationDetails = viewModel.dropDetails
                if (selectedPlaceName == null || selectedPlaceName == "Current Location" || selectedPlaceName == "Custom Pin"){
                    selectedPlaceName = viewModel.dropDetails.split("\n").firstOrNull() ?: "Saved Location"
                }
            }
        }
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding() // If you have WindowCompat.setDecorFitsSystemWindows(window, false)
            .padding(16.dp)
    ) {
        Text(
            text = if (type == "pickup") "Select Pickup Location" else "Select Drop Location",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = { launchAutocomplete() }, modifier = Modifier.fillMaxWidth()) {
            Text("Search for a place")
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { getCurrentLocation() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Filled.LocationOn, contentDescription = "My Location", modifier = Modifier.size(ButtonDefaults.IconSize))
            Spacer(Modifier.size(ButtonDefaults.IconSpacing))
            Text("Use Current Location")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f) // Allow map to take available space
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    Log.d(LOCATION_SCREEN_TAG, "Map clicked at: $latLng")
                    selectedLocation = latLng
                    selectedPlaceName = "Custom Pin"
                    locationDetails = "" // Clear details for custom pin, user can add new ones
                }
            ) {
                Marker(
                    state = markerState,
                    title = selectedPlaceName ?: "Selected Location",
                    snippet = if (locationDetails.isNotEmpty() && locationDetails != "Near my current position" && locationDetails != "Custom Pin") locationDetails else null
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = locationDetails,
            onValueChange = { locationDetails = it },
            label = { Text("Additional Location Details (e.g., Flat No, Landmark)") },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(if (selectedPlaceName != null && selectedPlaceName != "Custom Pin" && selectedPlaceName != "Current Location") selectedPlaceName!! else "Optional: e.g., Near main gate") },
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val finalDetailsToSave = if (locationDetails.isNotBlank()) {
                    locationDetails
                } else {
                    selectedPlaceName ?: (if (type == "pickup") "Pickup Location" else "Drop Location")
                }

                if (type == "pickup") {
                    viewModel.pickupLatLng = selectedLocation
                    viewModel.pickupDetails = finalDetailsToSave
                    Log.d(LOCATION_SCREEN_TAG, "Saving Pickup: $selectedLocation, Details: $finalDetailsToSave")
                } else {
                    viewModel.dropLatLng = selectedLocation
                    viewModel.dropDetails = finalDetailsToSave
                    Log.d(LOCATION_SCREEN_TAG, "Saving Drop: $selectedLocation, Details: $finalDetailsToSave")
                }
                navController.popBackStack()
            },
            modifier = Modifier.fillMaxWidth(),
            // Enable save button if a location has been selected (latlng is not default or viewmodel has one)
            enabled = (type == "pickup" && viewModel.pickupLatLng != null) || (type == "drop" && viewModel.dropLatLng != null) || (selectedLocation != LatLng(26.1445, 91.7362))
        ) {
            Text("Save Location")
        }
    }
}

// Helper functions (can be moved to a utility file)
private fun isLocationEnabled(context: Context): Boolean {
    val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    Log.d(LOCATION_SCREEN_TAG, "Location services enabled: $enabled")
    return enabled
}

private fun promptEnableLocationServices(context: Context) {
    Log.w(LOCATION_SCREEN_TAG, "Location services are disabled. Prompting user.")
    Toast.makeText(context, "Please enable location services in settings.", Toast.LENGTH_LONG).show()
    try {
        context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
    } catch (e: Exception) {
        Log.e(LOCATION_SCREEN_TAG, "Could not open location settings: ${e.message}")
        Toast.makeText(context, "Please enable location services via System Settings.", Toast.LENGTH_LONG).show()
    }
}
