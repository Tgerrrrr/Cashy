package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Pengeluaran
import com.example.supabaseauth.repository.ExpenseRepository
import io.github.jan.supabase.postgrest.from
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

    private val _state =
        MutableStateFlow<AddTransactionState>(AddTransactionState.Idle)

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

                val kasId = kas.id
                    ?: throw IllegalArgumentException("Kas ID is null")

                val now =
                    DateTimeFormatter.ISO_INSTANT.format(Instant.now())

                when (type.lowercase()) {

                    /* ==========================================
                       EXPENSE
                       ========================================== */

                    "expense" -> {

                        val expense = Pengeluaran(
                            kas_id = kasId,
                            tanggal = now,
                            deskripsi = desc,
                            total = amount
                        )

                        repo.addExpense(expense)

                        // Assumes ExpenseRepository:
                        // 1. inserts pengeluaran
                        // 2. updates kas
                        // 3. inserts kas_log
                    }

                    /* ==========================================
                       INCOME
                       ========================================== */

                    else -> {

                        val newSaldo = kas.saldo + amount

                        /* INSERT KAS LOG */

                        client
                            .from("kas_log")
                            .insert(
                                buildJsonObject {

                                    put("kas_id", kasId)

                                    put(
                                        "perubahan",
                                        amount
                                    )

                                    put(
                                        "saldo_akhir",
                                        newSaldo
                                    )

                                    put(
                                        "keterangan",
                                        if (desc.isBlank())
                                            "Manual income"
                                        else
                                            desc
                                    )

                                    put(
                                        "sumber",
                                        "manual"
                                    )
                                }
                            )

                        /* UPDATE CASH BALANCE */

                        client
                            .from("kas")
                            .update(
                                buildJsonObject {
                                    put(
                                        "saldo",
                                        newSaldo
                                    )
                                }
                            ) {
                                filter {
                                    eq(
                                        "id",
                                        kasId
                                    )
                                }
                            }
                    }
                }

                _state.value =
                    AddTransactionState.Success

            } catch (e: Exception) {

                _state.value =
                    AddTransactionState.Error(
                        e.message ?: "Unknown error"
                    )
            }
        }
    }

    fun resetState() {
        _state.value =
            AddTransactionState.Idle
    }
}