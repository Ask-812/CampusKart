package com.example.campuskart

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.campuskart.viewmodel.AuthViewModel // Import your AuthViewModel
// No need to import AuthState directly unless you use it as a type explicitly somewhere else

@Composable
fun LoginScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Get the ViewModel instance
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current // For Toasts

    // Effect for handling navigation after successful login or showing errors
    LaunchedEffect(authState) {
        if (authState.isSuccessful && authState.currentUser != null) {
            Toast.makeText(context, "Login Successful!", Toast.LENGTH_SHORT).show()
            navController.navigate("home") {
                popUpTo("splash") { inclusive = true } // Or popUpTo("login_flow_start_route") if you group login/reg
                launchSingleTop = true
            }
            authViewModel.resetIsSuccessful() // Reset the flag after navigation
        }
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError() // Clear the error after showing it
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Login to CampusKart", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error != null // Example: Highlight field on error
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error != null // Example: Highlight field on error
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    // Basic validation (optional, can be more sophisticated)
                    if (email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Email and password cannot be empty", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.loginUser(email, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }
        }

        TextButton(
            onClick = {
                if (!authState.isLoading) { // Prevent navigation while loading
                    navController.navigate("registration")
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Don't have an account? Register")
        }
    }
}