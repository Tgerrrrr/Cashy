package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Pengeluaran
import com.example.supabaseauth.repository.ExpenseRepository
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant
import java.time.format.DateTimeFormatter

sealed class AddTransactionState {
    object Idle : AddTransactionState()
    object Loading : AddTransactionState()
    object Success : AddTransactionState()
    data class Error(val message: String) : AddTransactionState()
}

class AddTransactionViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client
    private val repo = ExpenseRepository(client)

    private val _state = MutableStateFlow<AddTransactionState>(AddTransactionState.Idle)
    val state: StateFlow<AddTransactionState> = _state

    fun save(
        type: String,
        kas: Kas,
        amount: Double,
        desc: String
    ) {
        viewModelScope.launch {
            _state.value = AddTransactionState.Loading
            try {
                val now = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

                if (type == "expense") {
                    val expense = Pengeluaran(
                        kas_id    = kas.id!!,
                        tanggal   = now,
                        deskripsi = desc,
                        total     = amount
                    )
                    repo.addExpense(expense)

                } else {
                    val newSaldo = kas.saldo + amount
                    client.from("kas")
                        .update(
                            buildJsonObject { put("saldo", newSaldo) }
                        ) {
                            filter {
                                eq("id", kas.id!!)
                            }
                        }
                }

                _state.value = AddTransactionState.Success

            } catch (e: Exception) {
                _state.value = AddTransactionState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun resetState() {
        _state.value = AddTransactionState.Idle
    }
}