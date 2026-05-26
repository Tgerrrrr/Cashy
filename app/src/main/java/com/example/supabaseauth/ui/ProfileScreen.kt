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
    val initial = currentUserEmail?.firstOrNull()?.uppercaseChar() ?: 'U'

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
    ) {
        // ── HEADER ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashyColors.Primary)
                .padding(start = 20.dp, end = 20.dp, top = 48.dp, bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // Avatar
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(CashyColors.TextOnDark.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text       = initial.toString(),
                        fontSize   = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color      = CashyColors.TextOnDark
                    )
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    text       = currentUserEmail?.substringBefore("@") ?: "Pengguna",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = CashyColors.TextOnDark
                )
                Text(
                    text     = currentUserEmail ?: "-",
                    fontSize = 13.sp,
                    color    = CashyColors.TextOnDark.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // ── MENU ITEMS ───────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ProfileMenuItem(icon = Icons.Default.Person,   label = "Edit Profil")
            ProfileMenuItem(icon = Icons.Default.Lock,     label = "Ubah Password")
            ProfileMenuItem(icon = Icons.Default.Info,     label = "Tentang Aplikasi")
        }

        Spacer(Modifier.height(24.dp))

        // ── LOGOUT ───────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 16.dp)) {
            Button(
                onClick = { showLogoutDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape  = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CashyColors.Error,
                    contentColor   = CashyColors.TextOnDark
                )
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Keluar", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Keluar?") },
            text    = { Text("Yakin ingin keluar dari akun ini?") },
            confirmButton = {
                TextButton(
                    onClick = { showLogoutDialog = false; onLogout() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = CashyColors.Error)
                ) { Text("Keluar") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun ProfileMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Card(
        shape     = RoundedCornerShape(12.dp),
        colors    = CardDefaults.cardColors(containerColor = CashyColors.Surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier  = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = CashyColors.Primary, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(14.dp))
            Text(label, fontSize = 14.sp, color = CashyColors.TextPrimary, modifier = Modifier.weight(1f))
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = CashyColors.NavUnselected, modifier = Modifier.size(20.dp))
        }
    }
}