package com.example.supabaseauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@Composable
fun AdminDashboardScreen(navController: NavController) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {

        Text(
            text = "ADMIN DASHBOARD",
            style = MaterialTheme.typography.headlineMedium
        )

        Button(onClick = {
            navController.navigate("product")
        }) {
            Text("Kelola Produk")
        }

        Button(onClick = {
            navController.navigate("customer")
        }) {
            Text("Kelola Pelanggan")
        }

        Button(onClick = {
            navController.navigate("cash")
        }) {
            Text("Kelola Kas")
        }

        Button(onClick = {
            navController.navigate("expense")
        }) {
            Text("Kelola Pengeluaran")
        }

        Button(onClick = {
            navController.navigate("sales")
        }) {
            Text("Data Penjualan")
        }
    }
}