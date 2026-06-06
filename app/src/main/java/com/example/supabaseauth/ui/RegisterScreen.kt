package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabaseauth.viewmodel.AuthUiState
import com.example.supabaseauth.viewmodel.AuthViewModel

private val Black     = Color(0xFF181725)
private val Gray      = Color(0xFF7C7C7C)
private val Blue      = Color(0xFF00657E)
private val WhiteText = Color(0xFFF9F9F9)

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState,
    onNavigateToLogin: () -> Unit          // ← goes back to Profile/admin, not login page
) {
    var nama           by remember { mutableStateOf("") }
    var email          by remember { mutableStateOf("") }
    var password       by remember { mutableStateOf("") }
    var showPassword   by remember { mutableStateOf(false) }

    // Clear state when leaving
    LaunchedEffect(Unit) { authViewModel.resetUiState() }

    // On success, go back automatically after short delay so admin sees the message
    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            kotlinx.coroutines.delay(1500)
            onNavigateToLogin()
        }
    }

    val isLoading = authUiState is AuthUiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 28.dp)
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text(
                text = "Buat Akun Kasir",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Black
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Isi data berikut untuk membuat akun kasir baru",
                fontSize = 13.sp,
                color = Gray
            )

            Spacer(Modifier.height(32.dp))

            // ── Nama ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = nama,
                onValueChange = { nama = it },
                label         = { Text("Nama Kasir") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            // ── Email ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it },
                label         = { Text("Email") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(14.dp))

            // ── Password ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it },
                label         = { Text("Password") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null
                        )
                    }
                }
            )

            Spacer(Modifier.height(28.dp))

            // ── Status messages ───────────────────────────────────────────────
            when (authUiState) {
                is AuthUiState.Error -> {
                    Text(
                        text  = authUiState.message,
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                    Spacer(Modifier.height(10.dp))
                }
                is AuthUiState.Success -> {
                    Text(
                        text  = "✓ ${authUiState.message}",
                        color = Blue,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(Modifier.height(10.dp))
                }
                else -> Unit
            }

            // ── Create button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    authViewModel.register(
                        email    = email.trim(),
                        password = password,
                        nama     = nama.trim(),

                    )
                },
                enabled  = !isLoading && nama.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Blue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = WhiteText
                    )
                } else {
                    Text("Buat Akun", color = WhiteText, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("← Kembali", color = Blue)
            }
        }
    }
}