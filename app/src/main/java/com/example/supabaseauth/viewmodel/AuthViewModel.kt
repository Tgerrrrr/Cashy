package com.example.supabaseauth.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.repository.AuthRepository
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AuthViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val client = SupabaseClientProvider.client

    private val _authUiState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authUiState: StateFlow<AuthUiState> = _authUiState.asStateFlow()

    private val _authCheckState = MutableStateFlow<AuthCheckState>(AuthCheckState.Checking)
    val authCheckState: StateFlow<AuthCheckState> = _authCheckState.asStateFlow()

    private val _currentUserEmail = MutableStateFlow<String?>(null)
    val currentUserEmail: StateFlow<String?> = _currentUserEmail.asStateFlow()

    private val _currentUserRole = MutableStateFlow("cashier")
    val currentUserRole: StateFlow<String> = _currentUserRole.asStateFlow()

    init {
        checkSession()
    }

    fun checkSession() {
        viewModelScope.launch {
            _authCheckState.value = AuthCheckState.Checking
            try {
                withContext(Dispatchers.IO) {
                    client.auth.loadFromStorage()
                }
                val user = client.auth.currentUserOrNull()
                Log.d("AUTH", "checkSession user=${user?.email}")
                if (user != null) {
                    val profile = try {
                        client.from("profiles")
                            .select(Columns.list("id", "nama", "role")) {
                                filter { eq("id", user.id) }
                            }
                            .decodeSingle<com.example.supabaseauth.model.Profile>()
                    } catch (e: Exception) {
                        Log.e("AUTH", "profile fetch error: ${e.message}")
                        null
                    }
                    _currentUserEmail.value = user.email
                    _currentUserRole.value = profile?.role ?: "cashier"
                    _authCheckState.value = AuthCheckState.LoggedIn
                } else {
                    _authCheckState.value = AuthCheckState.LoggedOut
                }
            } catch (e: Exception) {
                Log.e("AUTH", "checkSession error: ${e.message}", e)
                _authCheckState.value = AuthCheckState.LoggedOut
            }
        }
    }

    fun login(email: String, password: String) {
        Log.d("AUTH", "login() called with $email")
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.login(email, password)
            result.fold(
                onSuccess = { profile ->
                    Log.d("AUTH", "login success, role=${profile.role}")
                    _currentUserEmail.value = client.auth.currentUserOrNull()?.email
                    _currentUserRole.value = profile.role
                    _authUiState.value = AuthUiState.Success()
                    _authCheckState.value = AuthCheckState.LoggedIn
                },
                onFailure = { e ->
                    Log.e("AUTH", "login error: ${e.message}", e)
                    _authUiState.value = AuthUiState.Error(e.message ?: "Login gagal")
                }
            )
        }
    }

    fun register(email: String, password: String, nama: String) {
        viewModelScope.launch {
            _authUiState.value = AuthUiState.Loading
            val result = repository.register(email, password, nama, "cashier")
            result.fold(
                onSuccess = {
                    _currentUserEmail.value = email
                    _currentUserRole.value = "cashier"
                    _authUiState.value = AuthUiState.Success()
                    _authCheckState.value = AuthCheckState.LoggedIn
                },
                onFailure = { e ->
                    Log.e("AUTH", "register error: ${e.message}", e)
                    _authUiState.value = AuthUiState.Error(e.message ?: "Register gagal")
                }
            )
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    client.auth.signOut()
                }
            } catch (e: Exception) {
                Log.e("AUTH", "logout error: ${e.message}", e)
            } finally {
                _currentUserEmail.value = null
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