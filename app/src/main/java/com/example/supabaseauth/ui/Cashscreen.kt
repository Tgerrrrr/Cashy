package com.example.supabaseauth.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel

/* =========================================
   CASH SCREEN
========================================= */

@Composable
fun CashScreen(
    cashViewModel: CashViewModel = viewModel()
) {
    val cashState by cashViewModel.cashState.collectAsState()
    val totalSaldo by cashViewModel.totalSaldo.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {

        when (val state = cashState) {

            is CashState.Loading,
            is CashState.Idle -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is CashState.Success -> {

                val cashList: List<Kas> = state.cashList

                Column(modifier = Modifier.fillMaxSize()) {

                    TotalSaldoSummaryCard(
                        totalSaldo = totalSaldo,
                        jumlahKas = cashList.size,
                        onRefresh = { cashViewModel.refresh() }
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(
                            items = cashList,
                            key = { cash -> cash.id ?: "" }
                        ) { cash ->
                            CashCard(cash)
                        }
                    }
                }
            }

            is CashState.Empty -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {

                        Text("Belum ada data kas")

                        Spacer(Modifier.height(16.dp))

                        Button(onClick = { cashViewModel.refresh() }) {
                            Text("Refresh")
                        }
                    }
                }
            }

            is CashState.Error -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {

                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )

                        Spacer(Modifier.height(12.dp))

                        Text(state.message)

                        Spacer(Modifier.height(12.dp))

                        Button(onClick = { cashViewModel.refresh() }) {
                            Icon(Icons.Default.Refresh, null)
                            Spacer(Modifier.width(8.dp))
                            Text("Coba Lagi")
                        }
                    }
                }
            }
        }
    }
}

/* =========================================
   CASH CARD
========================================= */

@Composable
fun CashCard(cash: Kas) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Row(verticalAlignment = Alignment.CenterVertically) {

                    Icon(Icons.Default.AccountBalance, null)

                    Spacer(Modifier.width(8.dp))

                    Text(
                        text = cash.nama,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                if (cash.is_active) {
                    Text("Aktif")
                }
            }

            Spacer(Modifier.height(12.dp))

            HorizontalDivider()

            Spacer(Modifier.height(12.dp))

            Text("Saldo")

            Spacer(Modifier.height(4.dp))

            Text(
                text = formatRupiah(cash.saldo),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/* =========================================
   TOTAL SALDO CARD
========================================= */

@Composable
fun TotalSaldoSummaryCard(
    totalSaldo: Double,
    jumlahKas: Int,
    onRefresh: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = "Total Saldo Kas",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Button(onClick = onRefresh) {
                    Text("Refresh")
                }
            }

            Spacer(Modifier.height(12.dp))

            Text(
                text = formatRupiah(totalSaldo),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(Modifier.height(4.dp))

            Text(text = "$jumlahKas kas aktif")
        }
    }
}

// formatRupiah defined in CashyTheme.kt