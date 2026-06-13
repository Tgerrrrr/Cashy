package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel
import com.example.supabaseauth.viewmodel.TransactionRaw
import com.example.supabaseauth.viewmodel.TransactionState
import com.example.supabaseauth.viewmodel.TransactionType
import com.example.supabaseauth.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

// ─── UI model ────────────────────────────────────────────────────────────────
private data class TransactionDisplayItem(
    val id: String,
    val title: String,
    val description: String,
    val waktu: String,
    val total: Double,
    val status: String
)

// ─── Mapper ──────────────────────────────────────────────────────────────────
private fun TransactionRaw.toDisplayItem(
    pelangganMap: Map<String, String>,
    kasMap: Map<String, String>
): TransactionDisplayItem = when (type) {

    TransactionType.PENJUALAN -> TransactionDisplayItem(
        id = id,
        title = pelangganMap[pelangganId] ?: "Guest",
        description = "Order #${id.take(8)}",
        waktu = waktu,
        total = total,
        status = status
    )

    TransactionType.KAS_LOG -> {
        val kasName = kasMap[kasId] ?: "Unknown Cash"
        val sumber = kasLogSumber?.lowercase() ?: ""
        val perubahan = kasLogPerubahan ?: 0.0

        val title = when (sumber) {
            "manual"      -> if (perubahan >= 0) "Income on $kasName" else "Expense from $kasName"
            "penjualan"   -> "Payment on $kasName"
            "pengeluaran" -> "Expense from $kasName"
            "pembatalan"  -> "Refund on $kasName"
            else          -> "Cash Activity on $kasName"
        }

        TransactionDisplayItem(
            id = id,
            title = title,
            description = if (sumber == "penjualan") "Transaction " else kasLogKeterangan ?: "-",
            waktu = waktu,
            total = total,
            status = status
        )
    }
}

// ─── Screen ──────────────────────────────────────────────────────────────────
@Composable
fun TransactionScreen(
    cashViewModel: CashViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    onAddTransaction: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {
    val cashState by cashViewModel.cashState.collectAsStateWithLifecycle()
    val totalSaldo by cashViewModel.totalSaldo.collectAsStateWithLifecycle()
    val txState by transactionViewModel.state.collectAsStateWithLifecycle()

    var selectedKasId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    val kasList = when (cashState) {
        is CashState.Success -> (cashState as CashState.Success).cashList
        else -> emptyList()
    }

    val selectedKas = kasList.find { it.id == selectedKasId }
    val currentSaldo = selectedKas?.saldo ?: totalSaldo

    LaunchedEffect(kasList) {
        if (selectedKasId == null && kasList.isNotEmpty())
            selectedKasId = kasList.first().id
        if (selectedKasId != null && kasList.none { it.id == selectedKasId })
            selectedKasId = kasList.firstOrNull()?.id
    }

    val displayItems: List<TransactionDisplayItem> = remember(txState) {
        when (txState) {
            is TransactionState.Success -> {
                val s = txState as TransactionState.Success
                s.transactions.map { it.toDisplayItem(s.pelangganMap, s.kasMap) }
            }
            else -> emptyList()
        }
    }

    val filteredItems = remember(displayItems, filter, searchQuery) {
        displayItems.filter { item ->
            val matchesFilter = when (filter) {
                "Completed" -> item.status.equals("selesai", ignoreCase = true)
                "Cancelled" -> item.status.equals("batal",   ignoreCase = true)
                "Failed"    -> item.status.equals("gagal",   ignoreCase = true)
                else        -> true
            }
            val matchesSearch = searchQuery.isBlank() ||
                    item.id.contains(searchQuery, ignoreCase = true) ||
                    item.title.contains(searchQuery, ignoreCase = true) ||
                    item.description.contains(searchQuery, ignoreCase = true) ||
                    item.status.contains(searchQuery, ignoreCase = true)

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        // ── Search ────────────────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFF2F3F2))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(imageVector = Icons.Default.Search, contentDescription = null)
            Spacer(modifier = Modifier.width(10.dp))
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Transaction") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor   = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor  = Color.Transparent,
                    focusedIndicatorColor   = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── Cash Card ─────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(Color(0xFF002F3B), RoundedCornerShape(20.dp))
                .padding(19.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Cash Balance", color = Color.White, fontSize = 13.sp)

                    Box {
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White)
                                .clickable { dropdownExpanded = true }
                                .padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = selectedKas?.nama ?: "Select Kas", fontSize = 12.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,
                            onDismissRequest = { dropdownExpanded = false }
                        ) {
                            kasList.forEach { kas ->
                                DropdownMenuItem(
                                    text = { Text(kas.nama) },
                                    onClick = {
                                        selectedKasId = kas.id
                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Text(
                    text = formatRupiah(currentSaldo),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable { onAddTransaction() }
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Add Transaction", fontSize = 12.sp)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // ── Title ─────────────────────────────────────────────────────────────
        Text(
            text = "Transaction History",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // ── Filter Chips ──────────────────────────────────────────────────────
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Completed", "Cancelled", "Failed").forEach { item ->
                TransactionFilterChip(
                    text = item,
                    selected = filter == item,
                    onClick = { filter = item }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Content ───────────────────────────────────────────────────────────
        when (txState) {
            is TransactionState.Loading -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color(0xFF00657E))
                }
            }
            is TransactionState.Error -> {
                Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Error: ${(txState as TransactionState.Error).message}",
                        color = Color.Red,
                        fontSize = 13.sp
                    )
                }
            }
            else -> {
                if (filteredItems.isEmpty()) {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No transactions found")
                    }
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(filteredItems) { item ->
                            TransactionItem(
                                title = item.title,
                                desc = "${item.description} • ${item.status}",
                                time = item.waktu,
                                amount = item.total,
                                status = item.status
                            )
                        }
                    }
                }
            }
        }
    }
}

// ─── Transaction Item ─────────────────────────────────────────────────────────
@Composable
private fun TransactionItem(
    title: String,
    desc: String,
    time: String,
    amount: Double,
    status: String
) {
    val dotColor = when (status.lowercase()) {
        "selesai" -> Color(0xFF2A7F13)
        else      -> Color(0xFFD32F2F)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = desc, fontSize = 12.sp, color = Color.Gray)
            Text(text = time, fontSize = 10.sp, color = Color.Gray)
        }
        Text(text = formatRupiah(amount), fontSize = 12.sp)
    }
}

// ─── Filter Chip ──────────────────────────────────────────────────────────────
@Composable
private fun TransactionFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (selected) Color(0xFF00657E) else Color(0xFFE6F3F6))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 12.sp,
            color = if (selected) Color.White else Color(0xFF00657E)
        )
    }
}