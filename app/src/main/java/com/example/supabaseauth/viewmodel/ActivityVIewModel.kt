package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.repository.ActivityRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class ActivityState {
    object Idle    : ActivityState()
    object Loading : ActivityState()
    object Empty   : ActivityState()
    data class Success(val entries: List<ActivityEntryUI>) : ActivityState()
    data class Error(val message: String) : ActivityState()
}

class ActivityViewModel : ViewModel() {

    private val repository = ActivityRepository()

    private val _state = MutableStateFlow<ActivityState>(ActivityState.Idle)
    val state: StateFlow<ActivityState> = _state

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _state.value = ActivityState.Loading
            try {
                val entries = repository.getAllActivities()
                _state.value = if (entries.isEmpty()) ActivityState.Empty
                else ActivityState.Success(entries)
            } catch (e: Exception) {
                _state.value = ActivityState.Error(e.message ?: "Failed to load activity")
            }
        }
    }

    fun loadByCategory(category: String) {
        viewModelScope.launch {
            _state.value = ActivityState.Loading
            try {

                val entries = when (category) {

                    "Inventory" -> repository.getInventoryActivities()

                    "Sales" -> repository.getSalesActivities()

                    "System" -> repository.getSystemActivities()

                    else -> repository.getAllActivities()
                }

                _state.value =
                    if (entries.isEmpty())
                        ActivityState.Empty
                    else
                        ActivityState.Success(entries)

            } catch (e: Exception) {
                _state.value = ActivityState.Error(
                    e.message ?: "Failed to load activity"
                )
            }
        }
    }
}