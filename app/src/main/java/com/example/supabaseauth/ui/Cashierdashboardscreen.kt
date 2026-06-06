package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val CashierPrimary   = Color(0xFF1A3C40)
private val CashierSurface   = Color(0xFFFFFFFF)
private val CashierBg        = Color(0xFFF4F6F8)
private val CashierOnDark    = Color(0xFFFFFFFF)

@Composable
fun CashierDashboardScreen(
    onNavigateToKasir  : () -> Unit,
    onNavigateToBarang : () -> Unit,
    onNavigateToKas    : () -> Unit,
    onLogout           : () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashierBg)
    ) {

        // ── Header ────────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashierPrimary)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text = "Selamat Datang, Kasir 👋",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = CashierOnDark
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Pilih menu di bawah untuk memulai",
                    fontSize = 13.sp,
                    color = CashierOnDark.copy(alpha = 0.7f)
                )
            }
        }

        Spacer(Modifier.height(24.dp))

        // ── Menu Cards ────────────────────────────────────────────────────────
        Column(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            CashierMenuCard(
                icon       = Icons.Default.PointOfSale,
                title      = "Kasir",
                subtitle   = "Proses transaksi penjualan",
                color      = Color(0xFF00897B),
                onClick    = onNavigateToKasir
            )

            CashierMenuCard(
                icon       = Icons.Default.Inventory2,
                title      = "Daftar Produk",
                subtitle   = "Lihat stok dan harga produk",
                color      = Color(0xFF1565C0),
                onClick    = onNavigateToBarang
            )

            CashierMenuCard(
                icon       = Icons.Default.AccountBalance,
                title      = "Daftar Kas",
                subtitle   = "Lihat saldo kas yang tersedia",
                color      = Color(0xFF6A1B9A),
                onClick    = onNavigateToKas
            )
        }

        Spacer(Modifier.weight(1f))

        // ── Logout ────────────────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            Button(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFB71C1C),
                    contentColor   = CashierOnDark
                )
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout")
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Logout?") },
            text    = { Text("Apakah kamu yakin ingin keluar?") },
            confirmButton = {
                TextButton(onClick = {
                    showLogoutDialog = false
                    onLogout()
                }) { Text("Logout") }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) { Text("Batal") }
            }
        )
    }
}

@Composable
private fun CashierMenuCard(
    icon    : ImageVector,
    title   : String,
    subtitle: String,
    color   : Color,
    onClick : () -> Unit
) {
    Card(
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = CashierSurface),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = color, modifier = Modifier.size(26.dp))
            }
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title,    fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                Text(subtitle, fontSize = 13.sp, color = Color(0xFF90A4AE))
            }
            Icon(Icons.Default.ChevronRight, null, tint = Color(0xFFB0BEC5))
        }
    }
}