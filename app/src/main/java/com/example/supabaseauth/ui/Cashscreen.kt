package com.example.supabaseauth.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import com.example.supabaseauth.model.KasLog
import com.example.supabaseauth.viewmodel.KasLogState
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.window.DialogProperties
import com.example.supabaseauth.viewmodel.ActionState

/* =========================================
   CASH SCREEN
========================================= */

@Composable
fun CashScreen(
    cashViewModel: CashViewModel = viewModel()
) {
    val cashState by cashViewModel.cashState.collectAsState()
    val totalSaldo by cashViewModel.totalSaldo.collectAsState()
    val actionState by cashViewModel.actionState.collectAsState()

    var showLogDialog by remember { mutableStateOf(false) }
    var selectedKasForLog by remember { mutableStateOf<Kas?>(null) }
    val kasLogState by cashViewModel.kasLogState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }
    var editingKas by remember { mutableStateOf<Kas?>(null) }

    // Tutup dialog otomatis saat aksi sukses
    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            showDialog = false
            editingKas = null
            cashViewModel.resetActionState()
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = {
                editingKas = null
                showDialog = true
            }) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Kas")
            }
        }
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {

            when (val state = cashState) {

                is CashState.Loading,
                is CashState.Idle -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                is CashState.Success -> {
                    val cashList: List<Kas> = state.cashList

                    Column(modifier = Modifier.fillMaxSize()) {

                        TotalSaldoSummaryCard(
                            totalSaldo = totalSaldo,
                            jumlahKas = cashList.count { it.is_active },
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
                                CashCard(
                                    cash = cash,
                                    onEdit = {
                                        editingKas = cash
                                        showDialog = true
                                    },
                                    onToggleActive = {
                                        cashViewModel.setCashActive(
                                            id = cash.id ?: return@CashCard,
                                            isActive = !cash.is_active
                                        )
                                    },
                                    onShowLog = {
                                        selectedKasForLog = cash
                                        cashViewModel.loadKasLog(cash.id ?: "")
                                        showLogDialog = true
                                    }
                                )
                            }
                        }
                    }
                }

                is CashState.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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

    if (showDialog) {
        CashFormDialog(
            initialKas = editingKas,
            isLoading = actionState is ActionState.Loading,
            errorMessage = (actionState as? ActionState.Error)?.message,
            onDismiss = {
                showDialog = false
                editingKas = null
                cashViewModel.resetActionState()
            },
            onSave = { kas, isEdit ->
                cashViewModel.saveCash(kas, isEdit)
            }
        )
    }

    if (showLogDialog) {
        KasLogDialog(
            kasName = selectedKasForLog?.nama ?: "",
            logState = kasLogState,
            onDismiss = {
                showLogDialog = false
                selectedKasForLog = null
                cashViewModel.resetKasLogState()
            }
        )
    }
}

/* =========================================
   CASH CARD
========================================= */

@Composable
fun CashCard(
    cash: Kas,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit,
    onShowLog: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onShowLog() },
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

                Row(verticalAlignment = Alignment.CenterVertically) {

                    AssistChip(
                        onClick = onToggleActive,
                        label = { Text(if (cash.is_active) "Aktif" else "Nonaktif") }
                    )

                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
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
   CASH FORM DIALOG (ADD / EDIT)
========================================= */

@Composable
fun CashFormDialog(
    initialKas: Kas?,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Kas, Boolean) -> Unit
) {
    val isEdit = initialKas != null

    var nama by remember { mutableStateOf(initialKas?.nama ?: "") }
    var saldoText by remember {
        mutableStateOf(
            if (initialKas != null) initialKas.saldo.toString() else "0"
        )
    }

    val saldoValue = saldoText.toDoubleOrNull()
    val isValid = nama.isNotBlank() && saldoValue != null

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = true
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = {
            Text(if (isEdit) "Edit Kas" else "Tambah Kas")
        },
        text = {
            Column {

                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Kas") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = saldoText,
                    onValueChange = { saldoText = it },
                    label = { Text("Saldo Awal") },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isValid && !isLoading,
                onClick = {
                    val kas = Kas(
                        id = initialKas?.id,
                        nama = nama.trim(),
                        saldo = saldoValue ?: 0.0,
                        is_active = initialKas?.is_active ?: true,
                        created_at = initialKas?.created_at
                    )
                    onSave(kas, isEdit)
                }
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(if (isEdit) "Simpan" else "Tambah")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !isLoading) {
                Text("Batal")
            }
        }
    )
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

@Composable
fun KasLogDialog(
    kasName: String,
    logState: KasLogState,
    onDismiss: () -> Unit
) {
    val isLoading = false
    AlertDialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            dismissOnBackPress = !isLoading,
            dismissOnClickOutside = !isLoading,
            usePlatformDefaultWidth = true
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp,
        title = { Text("Riwayat Kas: $kasName") },
        text = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 100.dp, max = 400.dp)
            ) {
                when (logState) {

                    is KasLogState.Loading -> {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator()
                        }
                    }

                    is KasLogState.Empty -> {
                        Text("Belum ada riwayat transaksi kas")
                    }

                    is KasLogState.Error -> {
                        Text(
                            text = logState.message,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    is KasLogState.Success -> {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(logState.logs) { log ->
                                KasLogItem(log)
                            }
                        }
                    }

                    is KasLogState.Idle -> {}
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        }
    )
}

@Composable
fun KasLogItem(log: KasLog) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = if (log.perubahan >= 0) "+ ${formatRupiah(log.perubahan)}"
                else "- ${formatRupiah(-log.perubahan)}",
                fontWeight = FontWeight.Bold,
                color = if (log.perubahan >= 0)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.error
            )
            Text(
                text = "Saldo: ${formatRupiah(log.saldo_akhir)}",
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (!log.keterangan.isNullOrBlank()) {
            Text(
                text = log.keterangan,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Text(
            text = "${log.sumber} • ${log.created_at}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.outline
        )

        HorizontalDivider(Modifier.padding(top = 8.dp))
    }
}

// formatRupiah defined in CashyTheme.kt