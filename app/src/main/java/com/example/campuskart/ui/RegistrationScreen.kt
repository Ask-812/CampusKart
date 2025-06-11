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

@Composable
fun RegistrationScreen(
    navController: NavController,
    authViewModel: AuthViewModel = viewModel() // Get the ViewModel instance
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var displayName by remember { mutableStateOf("") }
    val authState by authViewModel.authState.collectAsState()
    val context = LocalContext.current // For Toasts

    // Effect for handling navigation after successful registration or showing errors
    LaunchedEffect(authState) {
        if (authState.isSuccessful && authState.currentUser != null) {
            Toast.makeText(context, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show()
            // Option 1: Navigate to Login screen
            navController.navigate("login") {
                popUpTo("registration") { inclusive = true } // Remove registration from back stack
                launchSingleTop = true
            }
            // Option 2: Navigate directly to Home (if auto-login after registration is desired)
            // navController.navigate("home") {
            //     popUpTo("splash") { inclusive = true } // Or appropriate start of auth flow
            //     launchSingleTop = true
            // }
            authViewModel.resetIsSuccessful() // Reset the flag
        }
        authState.error?.let {
            Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            authViewModel.clearError() // Clear the error
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Create Account", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = displayName,
            onValueChange = { displayName = it },
            label = { Text("Full Name") }, // Made it non-optional for this example
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error?.contains("display name", ignoreCase = true) == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error?.contains("email", ignoreCase = true) == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password (min. 6 characters)") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error?.contains("password", ignoreCase = true) == true
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = { confirmPassword = it },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            isError = authState.error?.contains("password", ignoreCase = true) == true && password != confirmPassword
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (authState.isLoading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    // Basic validation
                    if (displayName.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "All fields except Confirm Password (if password empty) are required.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (password != confirmPassword) {
                        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
                        // Optionally, you could set an error state in the ViewModel
                        // authViewModel.setError("Passwords do not match")
                        return@Button
                    }
                    if (password.length < 6) {
                        Toast.makeText(context, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.registerUser(email, password, displayName.ifBlank { null }) // Pass null if display name is empty
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }
        }

        TextButton(
            onClick = {
                if (!authState.isLoading) {
                    navController.navigate("login") {
                        popUpTo("registration") { inclusive = true }
                    }
                }
            },
            modifier = Modifier.padding(top = 8.dp)
        ) {
            Text("Already have an account? Login")
        }
    }
}