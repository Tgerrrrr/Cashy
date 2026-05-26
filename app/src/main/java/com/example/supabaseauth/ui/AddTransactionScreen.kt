package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.viewmodel.AddTransactionState
import com.example.supabaseauth.viewmodel.AddTransactionViewModel
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel

@Composable
fun AddTransactionScreen(
    onBack: () -> Unit = {},
    cashViewModel: CashViewModel = viewModel(),
    addViewModel: AddTransactionViewModel = viewModel()
) {
    var type   by remember { mutableStateOf("expense") }
    var amount by remember { mutableStateOf("") }
    var desc   by remember { mutableStateOf("") }

    val cashState      = cashViewModel.cashState.collectAsStateWithLifecycle().value
    val addState       = addViewModel.state.collectAsStateWithLifecycle().value
    val kasList        = if (cashState is CashState.Success) cashState.cashList else emptyList()

    var selectedKasIndex by remember { mutableStateOf(0) }
    var kasDropdown      by remember { mutableStateOf(false) }

    val selectedKas = kasList.getOrNull(selectedKasIndex)

    // Navigate back on success
    LaunchedEffect(addState) {
        if (addState is AddTransactionState.Success) {
            addViewModel.resetState()
            onBack()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(16.dp)
    ) {

        /* ── TITLE ── */
        Text(
            text = "Add Transaction",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(20.dp))

        /* ── EXPENSE / INCOME TOGGLE ── */
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFEAEAEA))
                .padding(4.dp)
        ) {
            ToggleTab(
                text = "Expense",
                selected = type == "expense",
                onClick = { type = "expense" },
                modifier = Modifier.weight(1f)
            )
            ToggleTab(
                text = "Income",
                selected = type == "income",
                onClick = { type = "income" },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(Modifier.height(20.dp))

        /* ── KAS DROPDOWN ── */
        Text("Pilih Kas *", fontSize = 12.sp, color = Color.Gray)
        Spacer(Modifier.height(6.dp))

        Box {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(30.dp))
                    .background(Color.Black)
                    .clickable { kasDropdown = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = selectedKas?.nama ?: "Loading...",
                    color = Color.White,
                    fontSize = 12.sp
                )
                Icon(Icons.Default.ArrowDropDown, contentDescription = null, tint = Color.White)
            }

            DropdownMenu(
                expanded = kasDropdown,
                onDismissRequest = { kasDropdown = false }
            ) {
                kasList.forEachIndexed { index, kas ->
                    DropdownMenuItem(
                        text = { Text(kas.nama) },
                        onClick = {
                            selectedKasIndex = index
                            kasDropdown = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        /* ── AMOUNT ── */
        Text(
            text = "Enter Amount",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        TextField(
            value = amount,
            onValueChange = { amount = it.filter { c -> c.isDigit() || c == '.' } },
            placeholder = { Text("Rp 0", textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) },
            textStyle = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            ),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(10.dp))

        /* ── DESCRIPTION (expense only) ── */
        if (type == "expense") {
            Text("Deskripsi *", fontSize = 12.sp, color = Color.Gray)
            Spacer(Modifier.height(6.dp))
            TextField(
                value = desc,
                onValueChange = { desc = it },
                placeholder = { Text("Masukkan deskripsi...") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF2F2F2),
                    unfocusedContainerColor = Color(0xFFF2F2F2),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
            )
            Spacer(Modifier.height(30.dp))
        } else {
            Spacer(Modifier.height(20.dp))
        }

        /* ── ERROR MESSAGE ── */
        if (addState is AddTransactionState.Error) {
            Text(
                text = addState.message,
                color = Color.Red,
                fontSize = 12.sp,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
        }

        /* ── SAVE BUTTON ── */
        val isLoading = addState is AddTransactionState.Loading
        val canSave   = selectedKas != null &&
                amount.isNotBlank() &&
                (type == "income" || desc.isNotBlank())

        Button(
            onClick = {
                if (canSave) {
                    addViewModel.save(
                        type   = type,
                        kas    = selectedKas!!,
                        amount = amount.toDoubleOrNull() ?: 0.0,
                        desc   = desc
                    )
                }
            },
            enabled = canSave && !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(20.dp))
            } else {
                Text("Save", color = Color.White)
            }
        }
    }
}

/* ── TOGGLE TAB ── */
@Composable
private fun ToggleTab(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (selected) Color.Black else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (selected) Color.White else Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}