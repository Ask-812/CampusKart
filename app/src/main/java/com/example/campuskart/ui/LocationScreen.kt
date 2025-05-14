package com.example.campuskart.ui

import androidx.navigation.NavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.data.position
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberMarkerState

@Composable
fun LocationScreen(
    navController: NavController,
    type: String,
    viewModel: OrderViewModel = viewModel()
) {
    var locationDetails by remember { mutableStateOf("") }
    var selectedLocation by remember { mutableStateOf(LatLng(26.1445, 91.7362)) }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(selectedLocation, 15f)
    }
    val markerState = rememberMarkerState(position = selectedLocation)
    LaunchedEffect(selectedLocation) {
        markerState.position = selectedLocation
    }
    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)) {

        Text(
            text = if (type == "pickup") "Select Pickup Location" else "Select Drop Location",
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Box(modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                onMapClick = { latLng ->
                    selectedLocation = latLng
                }
            ) {
                Marker(
                    state = markerState,
                    title = "Selected Location"
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = locationDetails,
            onValueChange = { locationDetails = it },
            label = { Text("Additional Location Details") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            if (type == "pickup") {
                viewModel.pickupLatLng = selectedLocation
                viewModel.pickupDetails = locationDetails
            } else {
                viewModel.dropLatLng = selectedLocation
                viewModel.dropDetails = locationDetails
            }
            navController.popBackStack()
        }, modifier = Modifier.fillMaxWidth()) {
            Text("Save Location")
        }
    }
}
