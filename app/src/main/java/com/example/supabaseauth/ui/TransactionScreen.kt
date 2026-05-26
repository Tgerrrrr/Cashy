package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel
import com.example.supabaseauth.viewmodel.TransactionViewModel
import java.text.NumberFormat
import java.util.Locale

@Composable
fun TransactionScreen(
    cashViewModel: CashViewModel = viewModel(),
    transactionViewModel: TransactionViewModel = viewModel(),
    onAddTransaction: () -> Unit = {},
    onOpenHistory: () -> Unit = {}
) {

    /* ================= STATES ================= */

    val cashState by cashViewModel.cashState.collectAsStateWithLifecycle()
    val totalSaldo by cashViewModel.totalSaldo.collectAsStateWithLifecycle()
    val transactions by transactionViewModel.transactions.collectAsStateWithLifecycle()

    /* ================= LOCAL UI STATES ================= */

    var selectedKasId by remember { mutableStateOf<String?>(null) }
    var dropdownExpanded by remember { mutableStateOf(false) }
    var filter by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    /* ================= CASH LIST ================= */

    val kasList = when (cashState) {
        is CashState.Success -> (cashState as CashState.Success).cashList
        else -> emptyList()
    }

    /* ================= SELECTED KAS ================= */

    val selectedKas = kasList.find { it.id == selectedKasId }

    val currentSaldo = selectedKas?.saldo ?: totalSaldo

    /* ================= FORMATTER ================= */

    fun formatRupiah(value: Double): String {
        return NumberFormat
            .getCurrencyInstance(Locale("in", "ID"))
            .format(value)
    }

    /* ================= DEFAULT SELECTED KAS ================= */

    LaunchedEffect(kasList) {

        if (selectedKasId == null && kasList.isNotEmpty()) {
            selectedKasId = kasList.first().id
        }

        /*
        IMPORTANT:
        If selected kas no longer exists after realtime update,
        reset it safely.
         */

        if (
            selectedKasId != null &&
            kasList.none { it.id == selectedKasId }
        ) {
            selectedKasId = kasList.firstOrNull()?.id
        }
    }

//    /* ================= INITIAL LOAD ================= */
//
//    LaunchedEffect(Unit) {
//
//        transactionViewModel.loadAllTransactions()
//
//        /*
//        If your CashViewModel does not auto load,
//        uncomment this:
//         */
//
//        // cashViewModel.loadCash()
//    }

    /* ================= FILTERED TRANSACTIONS ================= */

    val filteredTransactions = remember(
        transactions,
        filter,
        searchQuery
    ) {

        transactions.filter { trx ->

            val matchesFilter = when (filter) {

                "Completed" -> trx.status.equals(
                    "selesai",
                    ignoreCase = true
                )

                "Cancelled" -> trx.status.equals(
                    "batal",
                    ignoreCase = true
                )

                "Failed" -> trx.status.equals(
                    "gagal",
                    ignoreCase = true
                )

                else -> true
            }

            val matchesSearch =

                searchQuery.isBlank() ||

                        trx.id.contains(
                            searchQuery,
                            ignoreCase = true
                        ) ||

                        trx.pelangganNama.contains(
                            searchQuery,
                            ignoreCase = true
                        ) ||

                        trx.status.contains(
                            searchQuery,
                            ignoreCase = true
                        ) ||

                        trx.description.contains(
                            searchQuery,
                            ignoreCase = true
                        )

            matchesFilter && matchesSearch
        }
    }

    /* ================= UI ================= */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
    ) {

        Spacer(modifier = Modifier.height(20.dp))

        /* ================= SEARCH ================= */

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

            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )

            Spacer(modifier = Modifier.width(10.dp))

            TextField(
                value = searchQuery,

                onValueChange = {
                    searchQuery = it
                },

                placeholder = {
                    Text("Search Transaction")
                },

                singleLine = true,

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),

                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ================= CASH CARD ================= */

        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .background(
                    Color(0xFF002F3B),
                    RoundedCornerShape(20.dp)
                )
                .padding(19.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                /* ================= HEADER ================= */

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        text = "Cash Balance",
                        color = Color.White,
                        fontSize = 13.sp
                    )

                    /* ================= DROPDOWN ================= */

                    Box {

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(30.dp))
                                .background(Color.White)
                                .clickable {
                                    dropdownExpanded = true
                                }
                                .padding(
                                    horizontal = 12.dp,
                                    vertical = 6.dp
                                ),

                            verticalAlignment = Alignment.CenterVertically
                        ) {

                            Text(
                                text = selectedKas?.nama ?: "Select Kas",
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.width(6.dp))

                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = null
                            )
                        }

                        DropdownMenu(
                            expanded = dropdownExpanded,

                            onDismissRequest = {
                                dropdownExpanded = false
                            }
                        ) {

                            kasList.forEach { kas ->

                                DropdownMenuItem(

                                    text = {
                                        Text(kas.nama)
                                    },

                                    onClick = {

                                        selectedKasId = kas.id

                                        dropdownExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                /* ================= SALDO ================= */

                Text(
                    text = formatRupiah(currentSaldo),
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                /* ================= ACTION BUTTON ================= */

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {

                    Row(
                        modifier = Modifier
                            .height(40.dp)
                            .weight(1f)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color(0xFFF0F0F0))
                            .clickable {
                                onAddTransaction()
                            }
                            .padding(horizontal = 12.dp),

                        verticalAlignment = Alignment.CenterVertically
                    ) {

                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Add Transaction",
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        /* ================= TITLE ================= */

        Text(
            text = "Transaction History",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        /* ================= FILTER ================= */

        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            listOf(
                "All",
                "Completed",
                "Cancelled",
                "Failed"
            ).forEach { item ->

                FilterChip(
                    text = item,
                    selected = filter == item,
                    onClick = {
                        filter = item
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        /* ================= TRANSACTION LIST ================= */

        if (filteredTransactions.isEmpty()) {

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {

                Text("No transactions found")
            }

        } else {

            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {

                items(filteredTransactions) { trx ->

                    TransactionItem(
                        name = trx.pelangganNama,
                        desc = "${trx.description} • ${trx.status}",
                        time = trx.waktu,
                        amount = trx.total,
                        status = trx.status
                    )
                }
            }
        }
    }
}

/* ================= TRANSACTION ITEM ================= */

@Composable
private fun TransactionItem(
    name: String,
    desc: String,
    time: String,
    amount: Double,
    status: String
) {

    fun formatRupiah(value: Double): String {

        return NumberFormat
            .getCurrencyInstance(Locale("in", "ID"))
            .format(value)
    }

    val dotColor = when (status.lowercase()) {

        "selesai" -> Color(0xFF2A7F13)

        else -> Color(0xFFD32F2F)
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

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = name,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = desc,
                fontSize = 12.sp,
                color = Color.Gray
            )

            Text(
                text = time,
                fontSize = 10.sp,
                color = Color.Gray
            )
        }

        Text(
            text = formatRupiah(amount),
            fontSize = 12.sp
        )
    }
}

/* ================= FILTER CHIP ================= */

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(
                if (selected)
                    Color(0xFF00657E)
                else
                    Color(0xFFE6F3F6)
            )
            .clickable {
                onClick()
            }
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
    ) {

        Text(
            text = text,
            fontSize = 12.sp,
            color =
                if (selected)
                    Color.White
                else
                    Color(0xFF00657E)
        )
    }
}