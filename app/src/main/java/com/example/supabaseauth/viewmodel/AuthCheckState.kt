package com.example.supabaseauth.viewmodel

sealed class AuthCheckState {
    object Checking : AuthCheckState()
    data class LoggedIn(val role: String = "admin") : AuthCheckState()
    object LoggedOut : AuthCheckState()
}