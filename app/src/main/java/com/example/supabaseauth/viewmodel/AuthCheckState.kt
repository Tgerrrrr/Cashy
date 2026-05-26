package com.example.supabaseauth.viewmodel

sealed class AuthCheckState {
    object Checking : AuthCheckState()
    object LoggedIn : AuthCheckState()
    object LoggedOut : AuthCheckState()
}