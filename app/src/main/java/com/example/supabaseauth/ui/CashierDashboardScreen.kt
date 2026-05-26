package com.example.supabaseauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun CashierDashboardScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "CASHIER DASHBOARD",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = {
            navController.navigate("sales")
        }) {
            Text("Transaksi Penjualan")
        }

        Button(onClick = {
            navController.navigate("customer")
        }) {
            Text("Pelanggan")
        }

        Button(onClick = {
            navController.navigate("expense")
        }) {
            Text("Pengeluaran")
        }
    }
}