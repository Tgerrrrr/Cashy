package com.example.supabaseauth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Profile
import com.example.supabaseauth.repository.AuthRepository
import com.example.supabaseauth.util.AuthErrorMapper
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val client = SupabaseClientProvider.client

    // ─────────────────────────────
    // STATE
    // ─────────────────────────────
    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail

    private val _currentUserName = MutableStateFlow<String?>(null)
    val currentUserName: StateFlow<String?> = _currentUserName

    private val _currentUserRole = MutableStateFlow("cashier")
    val currentUserRole: StateFlow<String> = _currentUserRole

    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId

    init {
        checkSession()
    }

    // ─────────────────────────────
    // SESSION CHECK
    // ─────────────────────────────
    fun checkSession() {
        viewModelScope.launch {

            _authCheckState.value = AuthCheckState.Checking

            try {
                withContext(Dispatchers.IO) {
                    client.auth.loadFromStorage()
                }

                val user = client.auth.currentUserOrNull()

                if (user == null) {
                    _authCheckState.value = AuthCheckState.LoggedOut
                    return@launch
                }

                try {
                    withContext(Dispatchers.IO) {
                        client.auth.retrieveUserForCurrentSession(updateSession = true)
                    }
                } catch (e: Exception) {
                    Log.e("AUTH", "stored session invalid: ${e.message}")
                    withContext(Dispatchers.IO) {
                        client.auth.clearSession()
                    }
                    _currentUserId.value = null
                    _currentUserEmail.value = null
                    _currentUserName.value = null
                    _currentUserRole.value = "cashier"
                    _authCheckState.value = AuthCheckState.LoggedOut
                    return@launch
                }

                val profile = try {
                    client
                        .from("profiles")
                        .select(Columns.list("id", "nama", "email", "role")) {
                            filter { eq("id", user.id) }
                        }
                        .decodeSingle<Profile>()
                } catch (e: Exception) {
                    Log.e("AUTH", "profile fetch error: ${e.message}")
                    null
                }

                _currentUserId.value = user.id
                _currentUserEmail.value = profile?.email ?: user.email
                _currentUserName.value = profile?.nama
                _currentUserRole.value = profile?.role ?: "cashier"

                _authCheckState.value = AuthCheckState.LoggedIn

            } catch (e: Exception) {
                Log.e("AUTH", "session error: ${e.message}")
                _authCheckState.value = AuthCheckState.LoggedOut
            }
        }
    }

    // ─────────────────────────────
    // LOGIN
    // ─────────────────────────────
    fun login(email: String, password: String) {
        viewModelScope.launch {

            _authUiState.value = AuthUiState.Loading

            val result = repository.login(email, password)

            result.fold(
                onSuccess = { profile ->

                    val user = client.auth.currentUserOrNull()

                    _currentUserId.value = profile.id
                    _currentUserEmail.value = profile.email ?: user?.email
                    _currentUserName.value = profile.nama
                    _currentUserRole.value = profile.role

                    _authUiState.value = AuthUiState.Success("Login successful")
                    _authCheckState.value = AuthCheckState.LoggedIn
                },
                onFailure = {
                    _authUiState.value = AuthUiState.Error(
                        AuthErrorMapper.map(it.message)
                    )
                }
            )
        }
    }

    // ─────────────────────────────
    // REGISTER
    // ─────────────────────────────
    fun register(email: String, password: String, nama: String) {
        viewModelScope.launch {

            _authUiState.value = AuthUiState.Loading

            val result = repository.register(email, password, nama, "cashier")

            result.fold(
                onSuccess = {

                    _currentUserEmail.value = email
                    _currentUserName.value = nama
                    _currentUserRole.value = "cashier"

                    _authUiState.value = AuthUiState.Success("Account created successfully")
                    _authCheckState.value = AuthCheckState.LoggedIn
                },
                onFailure = {
                    _authUiState.value = AuthUiState.Error(
                        AuthErrorMapper.map(it.message)
                    )
                }
            )
        }
    }

    // ─────────────────────────────
    // UPDATE PROFILE
    // ─────────────────────────────
    fun updateProfile(userId: String, nama: String) {
        viewModelScope.launch {

            _authUiState.value = AuthUiState.Loading

            val result = repository.updateProfile(userId, nama)

            result.fold(
                onSuccess = {

                    _currentUserName.value = nama
                    _authUiState.value = AuthUiState.Success("Profile updated")

                    // refresh session data
                    _currentUserEmail.value = client.auth.currentUserOrNull()?.email

                },
                onFailure = {
                    _authUiState.value = AuthUiState.Error(
                        AuthErrorMapper.map(it.message)
                    )
                }
            )
        }
    }

    // ─────────────────────────────
    // ─────────────────────────────
    // ─────────────────────────────
    // LOGOUT
    // ─────────────────────────────
    fun logout() {
        viewModelScope.launch {

            try {
                withContext(Dispatchers.IO) {
                    client.auth.signOut()
                }
            } finally {

                _currentUserId.value = null
                _currentUserEmail.value = null
                _currentUserName.value = null
                _currentUserRole.value = "cashier"

                _authUiState.value = AuthUiState.Idle
                _authCheckState.value = AuthCheckState.LoggedOut
            }
        }
    }

    fun resetUiState() {
        _authUiState.value = AuthUiState.Idle
    }
}
