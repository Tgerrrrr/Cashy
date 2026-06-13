package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.Pengeluaran
import com.example.supabaseauth.repository.ExpenseRepository
import com.example.supabaseauth.ui.ExpenseUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExpenseViewModel(
    private val repository: ExpenseRepository = ExpenseRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ExpenseUiState())
    val uiState: StateFlow<ExpenseUiState> = _uiState.asStateFlow()

    init {
        loadExpenses()
    }

    fun loadExpenses() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                val expenses = repository.getExpenses()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    expenses = expenses
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addExpense(expense: Pengeluaran) {
        viewModelScope.launch {
            try {
                repository.addExpense(expense)
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun updateExpense(expense: Pengeluaran) {
        viewModelScope.launch {
            try {
                repository.updateExpense(expense)
                loadExpenses()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message
                )
            }
        }
    }
}