package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.repository.CashRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class CashState {

    object Idle : CashState()

    object Loading : CashState()

    object Empty : CashState()

    data class Success(
        val cashList: List<Kas>
    ) : CashState()

    data class Error(
        val message: String
    ) : CashState()
}

class CashViewModel : ViewModel() {

    /* ================= REPOSITORY ================= */

    private val repository = CashRepository()

    /* ================= REALTIME ================= */

//    private val realtime =
//        SupabaseClientProvider.realtimeManager

    /* ================= STATES ================= */

    private val _cashState =
        MutableStateFlow<CashState>(CashState.Idle)

    val cashState: StateFlow<CashState> =
        _cashState

    private val _totalSaldo =
        MutableStateFlow(0.0)

    val totalSaldo: StateFlow<Double> =
        _totalSaldo

    /* ================= INIT ================= */

    init {

        refresh()

        //observeRealtime()
    }

    /* ================= LOAD CASH ================= */

    fun refresh() {

        viewModelScope.launch {

            _cashState.value = CashState.Loading

            try {

                val cashList =
                    repository.getAllCash()

                if (cashList.isEmpty()) {

                    _cashState.value =
                        CashState.Empty

                    _totalSaldo.value = 0.0

                } else {

                    _cashState.value =
                        CashState.Success(cashList)

                    _totalSaldo.value =
                        cashList.sumOf { it.saldo }
                }

            } catch (e: Exception) {

                _cashState.value =
                    CashState.Error(
                        e.message ?: "Terjadi kesalahan"
                    )
            }
        }
    }

    /* ================= REALTIME OBSERVER ================= */

//    private fun observeRealtime() {
//
//        viewModelScope.launch {
//
//            realtime
//                .observeTable("kas")
//                .collect {
//
//                    refresh()
//                }
//        }
//    }
}