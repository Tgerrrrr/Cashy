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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabaseauth.viewmodel.AuthUiState
import com.example.supabaseauth.viewmodel.AuthViewModel

private val CashyDark    = Color(0xFF002F3B)
private val CashyPrimary = Color(0xFF00657E)
private val CashyLight   = Color(0xFFE6F3F6)
private val CashyGray    = Color(0xFF7C7C7C)

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    authUiState: AuthUiState,
    onNavigateBack: () -> Unit
) {
    var nama         by remember { mutableStateOf("") }
    var email        by remember { mutableStateOf("") }
    var password     by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showSuccess  by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { authViewModel.resetUiState() }

    LaunchedEffect(authUiState) {
        if (authUiState is AuthUiState.Success) {
            showSuccess = true
            nama     = ""
            email    = ""
            password = ""
            authViewModel.resetUiState()
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
                color = CashyDark
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "Isi data berikut untuk membuat akun kasir baru",
                fontSize = 13.sp,
                color = CashyGray
            )

            Spacer(Modifier.height(32.dp))

            // ── Success Banner ────────────────────────────────────────────────
            if (showSuccess) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CashyLight)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "✓ Akun kasir berhasil dibuat",
                            color = CashyPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = "Kasir dapat login menggunakan email dan password yang didaftarkan.",
                            color = CashyPrimary,
                            fontSize = 12.sp
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // ── Nama ──────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = nama,
                onValueChange = { nama = it; showSuccess = false },
                label         = { Text("Nama Kasir") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = registerTextFieldColors()
            )

            Spacer(Modifier.height(14.dp))

            // ── Email ─────────────────────────────────────────────────────────
            OutlinedTextField(
                value         = email,
                onValueChange = { email = it; showSuccess = false },
                label         = { Text("Email") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = registerTextFieldColors()
            )

            Spacer(Modifier.height(14.dp))

            // ── Password ──────────────────────────────────────────────────────
            OutlinedTextField(
                value         = password,
                onValueChange = { password = it; showSuccess = false },
                label         = { Text("Password") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                shape         = RoundedCornerShape(12.dp),
                colors        = registerTextFieldColors(),
                visualTransformation = if (showPassword) VisualTransformation.None
                else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { showPassword = !showPassword }) {
                        Icon(
                            imageVector = if (showPassword) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = null,
                            tint = CashyPrimary
                        )
                    }
                }
            )

            Spacer(Modifier.height(24.dp))

            // ── Error message ─────────────────────────────────────────────────
            if (authUiState is AuthUiState.Error) {
                Text(
                    text = authUiState.message,
                    color = Color.Red,
                    fontSize = 13.sp
                )
                Spacer(Modifier.height(10.dp))
            }

            // ── Create button ─────────────────────────────────────────────────
            Button(
                onClick = {
                    showSuccess = false
                    authViewModel.registerKasir(
                        email    = email.trim(),
                        password = password,
                        nama     = nama.trim()
                    )
                },
                enabled  = !isLoading && nama.isNotBlank() && email.isNotBlank() && password.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = CashyPrimary)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier    = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color       = Color.White
                    )
                } else {
                    Text(
                        "Buat Akun",
                        color      = Color.White,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            TextButton(onClick = onNavigateBack) {
                Text("← Kembali", color = CashyPrimary)
            }
        }
    }
}

@Composable
private fun registerTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = CashyPrimary,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor    = CashyPrimary,
    cursorColor          = CashyPrimary
)