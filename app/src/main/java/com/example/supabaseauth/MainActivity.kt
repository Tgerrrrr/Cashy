package com.example.supabaseauth

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.supabaseauth.ui.AppNavigation
import com.example.supabaseauth.ui.theme.SupabaseAuthComposeTheme
import com.example.supabaseauth.viewmodel.AuthViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()

        setContent {
            SupabaseAuthComposeTheme {

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {

                    // ─────────────────────────────
                    // VIEWMODEL
                    // ─────────────────────────────
                    val authViewModel: AuthViewModel = viewModel()

                    // ─────────────────────────────
                    // NAV CONTROLLER
                    // ─────────────────────────────
                    val navController = rememberNavController()

                    // ─────────────────────────────
                    // APP NAVIGATION (NO startDestination)
                    // ─────────────────────────────
                    AppNavigation(
                        navController = navController,
                        authViewModel = authViewModel
                    )
                }
            }
        }
    }
}