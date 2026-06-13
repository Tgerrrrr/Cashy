package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.KasLog
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// Raw data class
data class TransactionRaw(
    val id: String,
    val type: TransactionType,
    val waktu: String,
    val total: Double,
    val status: String,
    // Penjualan fields
    val pelangganId: String? = null,
    // KasLog fields
    val kasId: String? = null,
    val kasLogSumber: String? = null,
    val kasLogPerubahan: Double? = null,
    val kasLogKeterangan: String? = null
)

enum class TransactionType { PENJUALAN, KAS_LOG }

sealed class TransactionState {
    object Loading : TransactionState()
    object Empty : TransactionState()
    data class Success(
        val transactions: List<TransactionRaw>,
        val pelangganMap: Map<String, String>,
        val kasMap: Map<String, String>
    ) : TransactionState()
    data class Error(val message: String) : TransactionState()
}

class TransactionViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client

    private val _state =
        MutableStateFlow<TransactionState>(TransactionState.Loading)

    val state: StateFlow<TransactionState> = _state

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

                val pelangganMap = pelanggan.associate { it.id to it.nama }
                val kasMap = kasList.associate { it.id to it.nama }

                val transaksiPenjualan = penjualan.map { trx ->
                    TransactionRaw(
                        id = trx.id ?: "",
                        type = TransactionType.PENJUALAN,
                        waktu = trx.waktu,
                        total = trx.total,
                        status = trx.status,
                        pelangganId = trx.pelanggan_id
                    )
                }

                val transaksiKasLog = kasLogs.map { log ->
                    TransactionRaw(
                        id = log.id,
                        type = TransactionType.KAS_LOG,
                        waktu = log.created_at,
                        total = kotlin.math.abs(log.perubahan),
                        status = "selesai",
                        kasId = log.kas_id,
                        kasLogSumber = log.sumber,
                        kasLogPerubahan = log.perubahan,
                        kasLogKeterangan = log.keterangan
                    )
                }

                val merged = (transaksiPenjualan + transaksiKasLog)
                    .sortedByDescending { it.waktu }

                _state.value = if (merged.isEmpty()) {
                    TransactionState.Empty
                } else {
                    TransactionState.Success(
                        transactions = merged,
                        pelangganMap = pelangganMap as Map<String, String>,
                        kasMap = kasMap as Map<String, String>
                    )
                }

            } catch (e: Exception) {
                _state.value = TransactionState.Error(
                    e.message ?: "Failed to load transactions"
                )
            }
        }
    }

    fun refresh() = loadAllTransactions()
}