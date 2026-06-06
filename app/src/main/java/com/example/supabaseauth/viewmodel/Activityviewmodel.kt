package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.model.InventoryLog
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// ── State ─────────────────────────────────────────────────────────────────────

sealed class ActivityState {
    object Loading : ActivityState()
    object Empty   : ActivityState()
    data class Success(val entries: List<ActivityEntryUI>) : ActivityState()
    data class Error(val message: String) : ActivityState()
}

// ── ViewModel ─────────────────────────────────────────────────────────────────

class ActivityViewModel : ViewModel() {

    private val client = SupabaseClientProvider.client

    private val _state = MutableStateFlow<ActivityState>(ActivityState.Loading)
    val state: StateFlow<ActivityState> = _state.asStateFlow()

    init { loadByCategory("All") }

    fun loadByCategory(category: String) {
        viewModelScope.launch {
            _state.value = ActivityState.Loading
            try {
                val entries = mutableListOf<ActivityEntryUI>()

                // ── Sales entries ────────────────────────────────────────────
                if (category == "All" || category == "Sales") {
                    val penjualanList = client
                        .from("penjualan")
                        .select()
                        .decodeList<Penjualan>()

                    penjualanList.forEach { p ->
                        entries.add(
                            ActivityEntryUI(
                                id        = p.id ?: "",
                                title     = "Penjualan",
                                details   = listOf(
                                    "Total: Rp ${p.total}",
                                    "Bayar: Rp ${p.jumlah_bayar}",
                                    "Status: ${p.status}"
                                ),
                                timestamp = p.waktu,
                                category  = "Sales"
                            )
                        )
                    }
                }

                // ── Inventory entries ────────────────────────────────────────
                if (category == "All" || category == "Inventory") {
                    val logs = client
                        .from("inventory_log")
                        .select()
                        .decodeList<InventoryLog>()

                    logs.forEach { log ->
                        entries.add(
                            ActivityEntryUI(
                                id        = log.id ?: "",
                                title     = "Inventori: ${log.keterangan}",
                                details   = listOf(
                                    "Perubahan stok: ${log.perubahan}",
                                    "Stok akhir: ${log.stokAkhir}"
                                ),
                                timestamp = log.createdAt ?: "",
                                category  = "Inventory"
                            )
                        )
                    }
                }

                // ── Sort by timestamp descending ─────────────────────────────
                val sorted = entries.sortedByDescending { it.timestamp }

                _state.value = if (sorted.isEmpty()) ActivityState.Empty
                else ActivityState.Success(sorted)

            } catch (e: Exception) {
                _state.value = ActivityState.Error(e.message ?: "Gagal memuat aktivitas")
            }
        }
    }
}