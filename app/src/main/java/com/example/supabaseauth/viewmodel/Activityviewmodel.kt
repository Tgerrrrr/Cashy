package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class ActivityState {

    object Loading : ActivityState()

    object Empty : ActivityState()

    data class Success(
        val entries: List<ActivityEntryUI>
    ) : ActivityState()

    data class Error(
        val message: String
    ) : ActivityState()
}

class ActivityViewModel : ViewModel() {

    private val repository =
        ActivityRepository()

    private val _state =
        MutableStateFlow<ActivityState>(
            ActivityState.Loading
        )

    val state: StateFlow<ActivityState> =
        _state.asStateFlow()

    init {
        loadByCategory("All")
    }

    fun loadByCategory(
        category: String
    ) {

        viewModelScope.launch {

            _state.value =
                ActivityState.Loading

            try {

                val entries =
                    mutableListOf<ActivityEntryUI>()

                if (
                    category == "All" ||
                    category == "Sales"
                ) {

                    val penjualanList =
                        repository.getSales()

                    penjualanList.forEach { p ->

                        entries.add(
                            ActivityEntryUI(
                                id =
                                    p.id ?: "",

                                title =
                                    "Penjualan",

                                details =
                                    listOf(
                                        "Total: Rp ${p.total}",
                                        "Bayar: Rp ${p.jumlah_bayar}",
                                        "Status: ${p.status}"
                                    ),

                                timestamp =
                                    p.waktu,

                                category =
                                    "Sales"
                            )
                        )
                    }
                }

                if (
                    category == "All" ||
                    category == "Inventory"
                ) {

                    val logs =
                        repository.getInventoryLogs()

                    logs.forEach { log ->

                        entries.add(
                            ActivityEntryUI(
                                id =
                                    log.id ?: "",

                                title =
                                    "Inventori: ${log.keterangan}",

                                details =
                                    listOf(
                                        "Perubahan stok: ${log.perubahan}",
                                        "Stok akhir: ${log.stokAkhir}"
                                    ),

                                timestamp =
                                    log.createdAt ?: "",

                                category =
                                    "Inventory"
                            )
                        )
                    }
                }

                val sorted =
                    entries.sortedByDescending {
                        it.timestamp
                    }

                _state.value =
                    if (sorted.isEmpty()) {
                        ActivityState.Empty
                    } else {
                        ActivityState.Success(
                            sorted
                        )
                    }

            } catch (e: Exception) {

                _state.value =
                    ActivityState.Error(
                        e.message
                            ?: "Gagal memuat aktivitas"
                    )
            }
        }
    }
}