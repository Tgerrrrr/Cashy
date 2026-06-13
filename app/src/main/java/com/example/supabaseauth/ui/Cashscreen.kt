package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.viewmodel.ActionState
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel

private val CashyDark    = Color(0xFF002F3B)
private val CashyPrimary = Color(0xFF00657E)
private val CashyLight   = Color(0xFFE6F3F6)
private val CashyBg      = Color(0xFFF2F3F2)
private val CashyWhite   = Color.White

/* =========================================
   CASH SCREEN
========================================= */

@Composable
fun CashScreen(
    cashViewModel: CashViewModel = viewModel()
) {
    val cashState   by cashViewModel.cashState.collectAsState()
    val totalSaldo  by cashViewModel.totalSaldo.collectAsState()
    val actionState by cashViewModel.actionState.collectAsState()

    var showAddDialog  by remember { mutableStateOf(false) }
    var editingKas     by remember { mutableStateOf<Kas?>(null) }

    LaunchedEffect(actionState) {
        if (actionState is ActionState.Success) {
            showAddDialog = false
            editingKas = null
            cashViewModel.resetActionState()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyBg)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {

            Spacer(modifier = Modifier.height(20.dp))

            // ── Header Card ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .background(CashyDark, RoundedCornerShape(20.dp))
                    .padding(19.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total Cash Balance",
                            color = CashyWhite.copy(alpha = 0.7f),
                            fontSize = 13.sp
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(CashyWhite.copy(alpha = 0.15f))
                                .clickable { cashViewModel.refresh() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Refresh",
                                color = CashyWhite,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Text(
                        text = formatRupiah(totalSaldo),
                        color = CashyWhite,
                        fontSize = 25.sp,
                        fontWeight = FontWeight.Bold
                    )

                    when (val s = cashState) {
                        is CashState.Success -> {
                            val activeCount = s.cashList.count { it.is_active }
                            Text(
                                text = "$activeCount active cash account${if (activeCount != 1) "s" else ""}",
                                color = CashyWhite.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                        else -> {}
                    }

                    // ── Add Button ────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(CashyWhite.copy(alpha = 0.15f))
                            .clickable { showAddDialog = true }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = CashyWhite
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Add Cash Account",
                            color = CashyWhite,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Cash Accounts",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // ── Content ───────────────────────────────────────────────────
            when (val state = cashState) {

                is CashState.Loading,
                is CashState.Idle -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = CashyPrimary)
                    }
                }

                is CashState.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(
                            items = state.cashList,
                            key = { it.id ?: "" }
                        ) { kas ->
                            CashItem(
                                kas = kas,
                                onEdit = {
                                    editingKas = kas
                                },
                                onToggleActive = {
                                    cashViewModel.setCashActive(
                                        id = kas.id ?: return@CashItem,
                                        isActive = !kas.is_active
                                    )
                                }
                            )
                        }
                    }
                }

                is CashState.Empty -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No cash accounts yet", color = Color.Gray)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(CashyLight)
                                    .clickable { cashViewModel.refresh() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Refresh", color = CashyPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }

                is CashState.Error -> {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(state.message, color = Color.Red, fontSize = 13.sp)
                            Spacer(Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(30.dp))
                                    .background(CashyLight)
                                    .clickable { cashViewModel.refresh() }
                                    .padding(horizontal = 16.dp, vertical = 8.dp)
                            ) {
                                Text("Try Again", color = CashyPrimary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Dialog ────────────────────────────────────────────────────────────
    if (showAddDialog) {
        CashAddDialog(
            isLoading = actionState is ActionState.Loading,
            errorMessage = (actionState as? ActionState.Error)?.message,
            onDismiss = {
                showAddDialog = false
                cashViewModel.resetActionState()
            },
            onSave = { kas ->
                cashViewModel.saveCash(kas, isEdit = false)
            }
        )
    }

    // ── Edit Dialog ───────────────────────────────────────────────────────────
    editingKas?.let { kas ->
        CashEditDialog(
            kas = kas,
            isLoading = actionState is ActionState.Loading,
            errorMessage = (actionState as? ActionState.Error)?.message,
            onDismiss = {
                editingKas = null
                cashViewModel.resetActionState()
            },
            onSave = { updated ->
                cashViewModel.saveCash(updated, isEdit = true)
            }
        )
    }
}

/* =========================================
   CASH ITEM
========================================= */

@Composable
private fun CashItem(
    kas: Kas,
    onEdit: () -> Unit,
    onToggleActive: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(CashyWhite)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Status dot
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(androidx.compose.foundation.shape.CircleShape)
                .background(if (kas.is_active) Color(0xFF2A7F13) else Color(0xFFD32F2F))
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = kas.nama,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Text(
                text = formatRupiah(kas.saldo),
                fontSize = 13.sp,
                color = CashyPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = if (kas.is_active) "Active" else "Inactive",
                fontSize = 11.sp,
                color = if (kas.is_active) Color(0xFF2A7F13) else Color.Gray
            )
        }

        // Toggle active chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(30.dp))
                .background(if (kas.is_active) CashyLight else Color(0xFFFFEBEB))
                .clickable { onToggleActive() }
                .padding(horizontal = 10.dp, vertical = 5.dp)
        ) {
            Text(
                text = if (kas.is_active) "Deactivate" else "Activate",
                fontSize = 11.sp,
                color = if (kas.is_active) CashyPrimary else Color(0xFFD32F2F)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Edit icon
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(CashyLight)
                .clickable { onEdit() }
                .padding(6.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Edit,
                contentDescription = "Edit",
                tint = CashyPrimary,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

/* =========================================
   ADD DIALOG — nama + saldo awal
========================================= */

@Composable
private fun CashAddDialog(
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Kas) -> Unit
) {
    var nama      by remember { mutableStateOf("") }
    var saldoText by remember { mutableStateOf("0") }

    val saldoValue = saldoText.toDoubleOrNull()
    val isValid    = nama.isNotBlank() && saldoValue != null

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = CashyWhite,
        title = {
            Text(
                "Add Cash Account",
                fontWeight = FontWeight.Bold,
                color = CashyDark
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = cashyTextFieldColors()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = saldoText,
                    onValueChange = { saldoText = it },
                    label = { Text("Initial Balance") },
                    singleLine = true,
                    enabled = !isLoading,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    colors = cashyTextFieldColors()
                )
                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (isValid && !isLoading) CashyPrimary else Color.LightGray)
                    .clickable(enabled = isValid && !isLoading) {
                        onSave(
                            Kas(
                                id = null,
                                nama = nama.trim(),
                                saldo = saldoValue ?: 0.0,
                                is_active = true,
                                created_at = null
                            )
                        )
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = CashyWhite
                    )
                } else {
                    Text("Add", color = CashyWhite, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(CashyLight)
                    .clickable(enabled = !isLoading) { onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Cancel", color = CashyPrimary, fontSize = 13.sp)
            }
        }
    )
}

/* =========================================
   EDIT DIALOG
========================================= */

@Composable
private fun CashEditDialog(
    kas: Kas,
    isLoading: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onSave: (Kas) -> Unit
) {
    var nama     by remember { mutableStateOf(kas.nama) }
    val isValid  = nama.isNotBlank()

    AlertDialog(
        onDismissRequest = { if (!isLoading) onDismiss() },
        containerColor = CashyWhite,
        title = {
            Text(
                "Edit Cash Account",
                fontWeight = FontWeight.Bold,
                color = CashyDark
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Account Name") },
                    singleLine = true,
                    enabled = !isLoading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = cashyTextFieldColors()
                )

                Spacer(Modifier.height(12.dp))

                // Saldo ditampilkan read-only dengan info
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CashyLight)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = CashyPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Balance: ${formatRupiah(kas.saldo)}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = CashyDark
                        )
                        Text(
                            text = "Balance cannot be edited directly to maintain transaction history integrity.",
                            fontSize = 11.sp,
                            color = Color.Gray
                        )
                    }
                }

                if (errorMessage != null) {
                    Spacer(Modifier.height(8.dp))
                    Text(errorMessage, color = Color.Red, fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(if (isValid && !isLoading) CashyPrimary else Color.LightGray)
                    .clickable(enabled = isValid && !isLoading) {
                        onSave(kas.copy(nama = nama.trim()))
                    }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp,
                        color = CashyWhite
                    )
                } else {
                    Text("Save", color = CashyWhite, fontSize = 13.sp)
                }
            }
        },
        dismissButton = {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(30.dp))
                    .background(CashyLight)
                    .clickable(enabled = !isLoading) { onDismiss() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Text("Cancel", color = CashyPrimary, fontSize = 13.sp)
            }
        }
    )
}

/* =========================================
   HELPER — TextField colors
========================================= */

@Composable
private fun cashyTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = CashyPrimary,
    unfocusedBorderColor = Color.LightGray,
    focusedLabelColor    = CashyPrimary,
    cursorColor          = CashyPrimary
)