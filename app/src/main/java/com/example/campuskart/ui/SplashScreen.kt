package com.example.campuskart.ui

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.campuskart.R
import kotlinx.coroutines.delay
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campuskart.viewmodel.AuthViewModel

@Composable
fun SplashScreen(navController: NavController, authViewModel: AuthViewModel = viewModel()) {
    val fadeAnim = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        )
    )


    val authState by authViewModel.authState.collectAsState()

    LaunchedEffect(Unit) { // Use Unit so this runs once on composition
        delay(2000) // Keep a small delay for splash visibility

        // Decision point:
        // authState.currentUser will be updated by the AuthViewModel's init block
        // or subsequent login/logout actions.
        // We need to wait for the initial auth state to be potentially loaded.
        // A more robust way might involve observing a specific "initialAuthCheckCompleted" flag in ViewModel
        // For simplicity, we assume after a short delay, authState.currentUser is reflective of persisted state.

        if (authState.currentUser != null) {
            // User is logged in
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
            }
        } else {
            // User is not logged in
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
                launchSingleTop = true
            }
        }
    }
    // Splash Screen UI
    Box(modifier = Modifier.fillMaxSize())
    {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.splashscreen), // 🔥 Use your image here
            contentDescription = "Splash Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop // Ensures the image covers the entire screen
        )

        // App Name with Animation
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "CampusKart",
                fontSize = 36.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White.copy(alpha = fadeAnim.value)
            )
        }
    }
}