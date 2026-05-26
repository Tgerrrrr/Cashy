package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.data.SupabaseClientProvider.client
import com.example.supabaseauth.model.KasLog
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Penjualan
import com.example.supabaseauth.model.PenjualanUI
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import io.github.jan.supabase.realtime.PostgresAction

sealed class TransactionState {
    object Loading : TransactionState()
    object Empty : TransactionState()
    data class Success(val data: List<PenjualanUI>) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client

    private val _state = MutableStateFlow<TransactionState>(TransactionState.Loading)
    val state: StateFlow<TransactionState> = _state

    private val _transactions = MutableStateFlow<List<PenjualanUI>>(emptyList())
    val transactions: StateFlow<List<PenjualanUI>> = _transactions

    //private val realtime = SupabaseClientProvider.realtimeManager

    fun loadAllTransactions() {
        viewModelScope.launch {
            val penjualan = client
                .from("penjualan")
                .select()
                .decodeList<Penjualan>()

            val pelanggan = client
                .from("pelanggan")
                .select()
                .decodeList<Pelanggan>()

            val kasLogs = client
                .from("kas_log")
                .select()
                .decodeList<KasLog>()

            /* ================= PENJUALAN ================= */

            val transaksiPenjualan = penjualan.map { trx ->

                val nama = pelanggan
                    .find { it.id == trx.pelanggan_id }
                    ?.nama ?: "Guest"

                PenjualanUI(
                    id = trx.id ?: "",
                    pelangganNama = nama,
                    waktu = trx.waktu,
                    total = trx.total,
                    status = trx.status,
                    description = "Order #${(trx.id ?: "").take(8)}"
                )
            }

            /* ================= KAS LOG ================= */

            val transaksiKasLog = kasLogs.map { log ->

                val title = when (log.sumber.lowercase()) {

                    "manual" -> {
                        if (log.perubahan >= 0)
                            "Owner Income"
                        else
                            "Manual Expense"
                    }

                    "penjualan" -> "Sales Income"

                    "pengeluaran" -> "Expense"

                    "pembatalan" -> "Refund"

                    else -> "Cash Activity"
                }

                PenjualanUI(
                    id = log.id,
                    pelangganNama = title,
                    waktu = log.created_at,
                    total = kotlin.math.abs(log.perubahan),
                    status = "selesai",
                    description = log.keterangan ?: "-"
                )
            }
            /* ================= MERGE ================= */

            val merged = (transaksiPenjualan + transaksiKasLog)
                .sortedByDescending { it.waktu }

            _transactions.value = merged

            _state.value =
                if (merged.isEmpty()) TransactionState.Empty
                else TransactionState.Success(merged)
            }
        }

//    private fun observeRealtime() {
//
//        viewModelScope.launch {
//
//            realtime
//                .observeTable("penjualan")
//                .collect {
//
//                    loadAllTransactions()
//                }
//        }
//
//        viewModelScope.launch {
//
//            realtime
//                .observeTable("kas_log")
//                .collect {
//
//                    loadAllTransactions()
//                }
//        }
//    }
    init {
        loadAllTransactions()
        //observeRealtime()
    }
}
