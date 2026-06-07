package com.example.supabaseauth.ui

import androidx.compose.foundation.BorderStroke
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

private val CashierPrimary = Color(0xFF1A3C40)
private val CashierSurface = Color(0xFFFFFFFF)
private val CashierBg      = Color(0xFFF4F6F8)
private val CashierOnDark  = Color(0xFFFFFFFF)
private val CashierSubText = Color(0xFF90A4AE)

@Composable
fun CashierDashboardScreen(
    onNavigateToKasir     : () -> Unit,
    onNavigateToBarang    : () -> Unit,
    onNavigateToPelanggan : () -> Unit,
    onLogout              : () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashierBg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(CashierPrimary)
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(CashierOnDark.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.SupportAgent, null, tint = CashierOnDark, modifier = Modifier.size(26.dp))
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        "Selamat Datang, Kasir 👋",
                        fontSize   = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color      = CashierOnDark
                    )
                    Text(
                        "Pilih menu untuk memulai",
                        fontSize = 13.sp,
                        color    = CashierOnDark.copy(alpha = 0.65f)
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Menu Utama",
            fontSize   = 12.sp,
            fontWeight = FontWeight.Bold,
            color      = CashierSubText,
            modifier   = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
        )

        Column(
            modifier            = Modifier.padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            CashierMenuCard(
                icon     = Icons.Default.PointOfSale,
                title    = "Kasir",
                subtitle = "Proses transaksi penjualan",
                color    = Color(0xFF00897B),
                onClick  = onNavigateToKasir
            )
            CashierMenuCard(
                icon     = Icons.Default.Inventory2,
                title    = "Daftar Produk",
                subtitle = "Lihat stok dan harga produk",
                color    = Color(0xFF1565C0),
                onClick  = onNavigateToBarang
            )
            CashierMenuCard(
                icon     = Icons.Default.PeopleAlt,
                title    = "Pelanggan",
                subtitle = "Daftar & kelola anggota",
                color    = Color(0xFF6A1B9A),
                onClick  = onNavigateToPelanggan
            )
        }

        Spacer(Modifier.weight(1f))

        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)) {
            OutlinedButton(
                onClick  = { showLogoutDialog = true },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(12.dp),
                colors   = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFB71C1C)),
                border   = BorderStroke(1.dp, Color(0xFFB71C1C))
            ) {
                Icon(Icons.Default.Logout, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Logout", fontWeight = FontWeight.SemiBold)
            }
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title   = { Text("Konfirmasi Logout") },
            text    = { Text("Apakah kamu yakin ingin keluar dari sesi ini?") },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("Logout", color = Color(0xFFB71C1C))
                }
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
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = CashierSurface),
        modifier  = Modifier.fillMaxWidth().clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier          = Modifier.padding(18.dp),
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