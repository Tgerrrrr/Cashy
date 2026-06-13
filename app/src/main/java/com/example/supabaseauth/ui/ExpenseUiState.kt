package com.example.supabaseauth.ui

import com.example.supabaseauth.model.Pengeluaran

data class ExpenseUiState(
    val isLoading: Boolean = false,
    val expenses: List<Pengeluaran> = emptyList(),
    val error: String? = null
)