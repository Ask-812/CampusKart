package com.example.campuskart.viewmodel

import android.util.Log
import androidx.compose.animation.core.copy
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
// import com.google.firebase.firestore.ktx.firestore // Optional
// import com.google.firebase.ktx.Firebase // Optional
import kotlinx.coroutines.channels.awaitClose // Make sure this import is present
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// Represents the different states of authentication
data class AuthState(
    val currentUser: FirebaseUser? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isSuccessful: Boolean = false // To signal successful registration/login
)

class AuthViewModel : ViewModel() {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // Optional: For Firestore to save user profile details like display name
    // private val db = Firebase.firestore

    // Observe auth state changes from Firebase using callbackFlow
    val authStateChanges: StateFlow<FirebaseUser?> = callbackFlow {
        val authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            trySend(firebaseAuth.currentUser) // Send the current user
            Log.d("AuthViewModel", "Auth State Changed: User is ${firebaseAuth.currentUser?.uid}")
        }
        auth.addAuthStateListener(authStateListener)

        // This will be called when the flow is cancelled or closed
        awaitClose {
            Log.d("AuthViewModel", "Removing AuthStateListener")
            auth.removeAuthStateListener(authStateListener)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000), // Keep active for 5s after last subscriber
        initialValue = auth.currentUser // Set initial value
    )

    // _authState can now be driven by authStateChanges or updated directly by other methods
    private val _authState = MutableStateFlow(AuthState(currentUser = auth.currentUser))
    val authState: StateFlow<AuthState> = _authState

    init {
        // Update _authState whenever authStateChanges emits a new value
        viewModelScope.launch {
            authStateChanges.collect { firebaseUser ->
                // You might want to preserve isLoading and error states
                // or decide how a direct auth change impacts them.
                // For simplicity, here we just update the user.
                // You can merge this more carefully with existing _authState.value
                _authState.value = _authState.value.copy(currentUser = firebaseUser)
            }
        }
    }


    fun registerUser(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isSuccessful = false)
            try {
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Registration successful: ${result.user?.uid}")

                if (displayName != null && result.user != null) {
                    val profileUpdates = UserProfileChangeRequest.Builder()
                        .setDisplayName(displayName)
                        .build()
                    result.user!!.updateProfile(profileUpdates).await()
                    Log.d("AuthViewModel", "Display name updated for ${result.user?.uid}")
                }
                // The authStateChanges flow will automatically update the currentUser in _authState.
                // We just need to signal success and stop loading.
                _authState.value = _authState.value.copy(isLoading = false, isSuccessful = true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Registration failed", e)
                _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Registration failed")
            }
        }
    }

    fun loginUser(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = _authState.value.copy(isLoading = true, error = null, isSuccessful = false)
            try {
                val result = auth.signInWithEmailAndPassword(email, password).await()
                Log.d("AuthViewModel", "Login successful: ${result.user?.uid}")
                // authStateChanges flow will update the currentUser.
                _authState.value = _authState.value.copy(isLoading = false, isSuccessful = true)
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Login failed", e)
                _authState.value = _authState.value.copy(isLoading = false, error = e.message ?: "Login failed")
            }
        }
    }

    fun logoutUser() {
        auth.signOut()
        // authStateChanges flow will update the currentUser to null.
        // We might want to clear other states explicitly here.
        _authState.value = AuthState(currentUser = null, isSuccessful = false, error = null, isLoading = false)
        Log.d("AuthViewModel", "User logged out")
    }

    fun clearError() {
        _authState.value = _authState.value.copy(error = null)
    }

    fun resetIsSuccessful() {
        if (_authState.value.isSuccessful) {
            _authState.value = _authState.value.copy(isSuccessful = false)
        }
    }
}