package com.example.supabaseauth.viewmodel

import com.example.supabaseauth.model.TodoWithProfile

sealed class TodoUiState {

    /*
     * Kondisi ketika data sedang dimuat.
     */
    object Loading : TodoUiState()

    /*
     * Kondisi ketika data berhasil dimuat.
     */
    data class Success(
        val todos: List<TodoWithProfile>
    ) : TodoUiState()

    /*
     * Kondisi ketika terjadi error.
     */
    data class Error(
        val message: String
    ) : TodoUiState()
}