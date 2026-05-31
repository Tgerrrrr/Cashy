package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.KasLog
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Penjualan
import com.example.supabaseauth.model.PenjualanUI
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs

sealed class TransactionState {
    object Loading : TransactionState()
    object Empty : TransactionState()
    data class Success(val data: List<PenjualanUI>) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client

    private val _state =
        MutableStateFlow<TransactionState>(TransactionState.Loading)

    val state: StateFlow<TransactionState> = _state

    private val _transactions =
        MutableStateFlow<List<PenjualanUI>>(emptyList())

    val transactions: StateFlow<List<PenjualanUI>> = _transactions

    init {
        loadAllTransactions()
    }

    fun loadAllTransactions() {

        viewModelScope.launch {

            _state.value = TransactionState.Loading

            try {

                val penjualan = client
                    .from("penjualan")
                    .select()
                    .decodeList<Penjualan>()

                val pelanggan = client
                    .from("pelanggan")
                    .select()
                    .decodeList<Pelanggan>()

                val kasList = client
                    .from("kas")
                    .select()
                    .decodeList<Kas>()

                val kasLogs = client
                    .from("kas_log")
                    .select()
                    .decodeList<KasLog>()

                /* ==========================================
                   SALES TRANSACTIONS
                   ========================================== */

                val transaksiPenjualan = penjualan.map { trx ->

                    val customerName =
                        pelanggan.find {
                            it.id == trx.pelanggan_id
                        }?.nama ?: "Guest"

                    PenjualanUI(
                        id = trx.id ?: "",
                        pelangganNama = customerName,
                        waktu = trx.waktu,
                        total = trx.total,
                        status = trx.status,
                        description = "Order #${(trx.id ?: "").take(8)}"
                    )
                }

                /* ==========================================
                   CASH FLOW TRANSACTIONS
                   ========================================== */

                val transaksiKasLog = kasLogs.map { log ->

                    val kasName =
                        kasList.find {
                            it.id == log.kas_id
                        }?.nama ?: "Unknown Cash"

                    val title = when (log.sumber.lowercase()) {

                        "manual" -> {
                            if (log.perubahan >= 0)
                                "Income on $kasName"
                            else
                                "Expense from $kasName"
                        }

                        "penjualan" ->
                            "Sales on $kasName"

                        "pengeluaran" ->
                            "Expense from $kasName"

                        "pembatalan" ->
                            "Refund on $kasName"

                        else ->
                            "Cash Activity on $kasName"
                    }

                    PenjualanUI(
                        id = log.id,
                        pelangganNama = title,
                        waktu = log.created_at,
                        total = abs(log.perubahan),
                        status = "selesai",
                        description = log.keterangan ?: "-"
                    )
                }

                /* ==========================================
                   MERGE & SORT
                   ========================================== */

                val merged =
                    (transaksiPenjualan + transaksiKasLog)
                        .sortedByDescending { it.waktu }

                _transactions.value = merged

                _state.value =
                    if (merged.isEmpty()) {
                        TransactionState.Empty
                    } else {
                        TransactionState.Success(merged)
                    }

            } catch (e: Exception) {

                _transactions.value = emptyList()

                _state.value =
                    TransactionState.Error(
                        e.message ?: "Failed to load transactions"
                    )
            }
        }
    }

    fun refresh() {
        loadAllTransactions()
    }
}