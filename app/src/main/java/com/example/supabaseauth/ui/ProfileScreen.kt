package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.supabaseauth.viewmodel.AuthUiState

@Composable
fun ProfileScreen(
    currentUserName: String?,
    currentUserEmail: String?,
    currentUserId: String?,
    authUiState: AuthUiState,
    onUpdateProfile: (String, String) -> Unit,
    onLogout: () -> Unit,
    onCreateCashier: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showEditProfile  by remember { mutableStateOf(false) }
    var showAboutApp     by remember { mutableStateOf(false) }

    var name         by remember { mutableStateOf(currentUserName ?: currentUserEmail?.substringBefore("@") ?: "User") }
    var email        by remember { mutableStateOf(currentUserEmail ?: "") }
    var profileError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentUserName, currentUserEmail) {
        name  = currentUserName ?: currentUserEmail?.substringBefore("@") ?: "User"
        email = currentUserEmail ?: ""
    }

    val initial   = name.firstOrNull()?.uppercaseChar() ?: 'U'
    val isLoading = authUiState is AuthUiState.Loading

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
    ) {

        // ── HEADER ─────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashyColors.Primary)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CashyColors.TextOnDark.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initial.toString(),
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = CashyColors.TextOnDark
                    )
                }

                Spacer(Modifier.height(10.dp))

                Text(name,  fontSize = 18.sp, fontWeight = FontWeight.Bold, color = CashyColors.TextOnDark)
                Text(email, fontSize = 13.sp, color = CashyColors.TextOnDark.copy(alpha = 0.7f))

                if (isLoading) {
                    Spacer(Modifier.height(10.dp))
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = CashyColors.TextOnDark,
                        strokeWidth = 2.dp
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        when (authUiState) {
            is AuthUiState.Error -> {
                Text(
                    text = authUiState.message,
                    color = CashyColors.Error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(10.dp))
            }
            is AuthUiState.Success -> {
                Text(
                    text = authUiState.message,
                    color = CashyColors.Primary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.height(10.dp))
            }
            else -> Unit
        }

        // ── MENU ─────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            ProfileMenuItem(
                icon    = Icons.Default.Person,
                label   = "Edit Profile",
                onClick = { showEditProfile = true }
            )

            // ← NEW: Create Cashier Account button
            ProfileMenuItem(
                icon    = Icons.Default.PersonAdd,
                label   = "Buat Akun Kasir",
                onClick = onCreateCashier
            )

            ProfileMenuItem(
                icon    = Icons.Default.Info,
                label   = "About App",
                onClick = { showAboutApp = true }
            )
        }

        Spacer(Modifier.height(24.dp))

        // ── LOGOUT ───────────────────────────
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CashyColors.Error,
                    contentColor   = CashyColors.TextOnDark
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }

    // ── EDIT PROFILE DIALOG ───────────────────────────────────────────────────
    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value         = name,
                        onValueChange = { name = it },
                        label         = { Text("Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value         = email,
                        onValueChange = {},
                        label         = { Text("Email") },
                        enabled       = false
                    )
                    profileError?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = CashyColors.Error, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                TextButton(
                    enabled = !isLoading,
                    onClick = {
                        val cleanedName = name.trim()
                        when {
                            currentUserId.isNullOrBlank() -> profileError = "User belum login"
                            cleanedName.isBlank()         -> profileError = "Name tidak boleh kosong"
                            else -> {
                                profileError = null
                                onUpdateProfile(currentUserId, cleanedName)
                                showEditProfile = false
                            }
                        }
                    }
                ) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }

    // ── ABOUT APP DIALOG ──────────────────────────────────────────────────────
    if (showAboutApp) {
        AlertDialog(
            onDismissRequest = { showAboutApp = false },
            title   = { Text("About App") },
            text    = { Text("Cashy is a modern inventory and sales management app designed to help small businesses track products, sales, and customers efficiently. Version 1.0.0 (Demo Build).") },
            confirmButton = {
                TextButton(onClick = { showAboutApp = false }) { Text("Close") }
            }
        )
    }

    // ── LOGOUT DIALOG ────────────────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Logout?") },
            text    = { Text("Are you sure you want to logout from this account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CashyColors.Surface),
        modifier = Modifier.fillMaxWidth(),
        onClick  = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, null, tint = CashyColors.Primary)
            Spacer(Modifier.width(14.dp))
            Text(label, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, null)
        }
    }
}