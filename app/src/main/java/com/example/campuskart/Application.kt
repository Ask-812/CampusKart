package com.example.campuskart

import android.app.Application
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.net.PlacesClient
class CampusKartApp : Application() {

    lateinit var placesClient: PlacesClient // Declare PlacesClient

    override fun onCreate() {
        super.onCreate()

        // Initialize the Places SDK
        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCVQD30gLn2zat9zeUs-fA2peh3-SSUZ9w") // Use the same API Key
        }
        placesClient = Places.createClient(this) // Create a new PlacesClient instance

        if (!Places.isInitialized()) {
            Places.initialize(applicationContext, "AIzaSyCBAt8JmkbKghZktMkaUxgnj_nJ64G9EJk")
        }
    }
}
