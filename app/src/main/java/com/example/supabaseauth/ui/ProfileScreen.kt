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

@Composable
fun ProfileScreen(
    currentUserEmail: String?,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    var showEditProfile by remember { mutableStateOf(false) }
    var showChangePassword by remember { mutableStateOf(false) }
    var showAboutApp by remember { mutableStateOf(false) }

    var name by remember { mutableStateOf(currentUserEmail?.substringBefore("@") ?: "User") }
    var email by remember { mutableStateOf(currentUserEmail ?: "") }

    val initial = name.firstOrNull()?.uppercaseChar() ?: 'U'

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

                Text(
                    text = name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = CashyColors.TextOnDark
                )

                Text(
                    text = email,
                    fontSize = 13.sp,
                    color = CashyColors.TextOnDark.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── MENU ─────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            ProfileMenuItem(
                icon = Icons.Default.Person,
                label = "Edit Profile",
                onClick = { showEditProfile = true }
            )

            ProfileMenuItem(
                icon = Icons.Default.Lock,
                label = "Change Password",
                onClick = { showChangePassword = true }
            )

            ProfileMenuItem(
                icon = Icons.Default.Info,
                label = "About App",
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
                    contentColor = CashyColors.TextOnDark
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }

    // ─────────────────────────────────────────────
    // EDIT PROFILE DIALOG
    // ─────────────────────────────────────────────
    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile") },
            text = {
                Column {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Name") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showEditProfile = false }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ─────────────────────────────────────────────
    // CHANGE PASSWORD DIALOG
    // ─────────────────────────────────────────────
    if (showChangePassword) {

        var oldPass by remember { mutableStateOf("") }
        var newPass by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { showChangePassword = false },
            title = { Text("Change Password") },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("Old Password") }
                    )
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("New Password") }
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // dummy validation
                    showChangePassword = false
                }) {
                    Text("Update")
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePassword = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ─────────────────────────────────────────────
    // ABOUT APP DIALOG
    // ─────────────────────────────────────────────
    if (showAboutApp) {
        AlertDialog(
            onDismissRequest = { showAboutApp = false },
            title = { Text("About App") },
            text = {
                Text(
                    "Cashy is a modern inventory and sales management app designed to help small businesses track products, sales, and customers efficiently. Version 1.0.0 (Demo Build)."
                )
            },
            confirmButton = {
                TextButton(onClick = { showAboutApp = false }) {
                    Text("Close")
                }
            }
        )
    }

    // ─────────────────────────────────────────────
    // LOGOUT DIALOG
    // ─────────────────────────────────────────────
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("Logout?") },
            text = { Text("Are you sure you want to logout from this account?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) {
                    Text("Logout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("Cancel")
                }
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
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CashyColors.Surface),
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick
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