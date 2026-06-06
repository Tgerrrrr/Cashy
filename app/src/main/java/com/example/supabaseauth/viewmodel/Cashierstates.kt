package com.example.supabaseauth.viewmodel


// ── Cart item (not serialized, lives only in memory) ─────────────────────────

data class CartItem(
    val produkId: String,
    val namaProduk: String,
    val hargaSatuan: Double,
    val qty: Int,
    val stokTersedia: Int
) {
    val subtotal: Double get() = hargaSatuan * qty
}

// ── KasirUiState ─────────────────────────────────────────────────────────────

sealed class KasirUiState {
    object Idle    : KasirUiState()
    object Loading : KasirUiState()

    data class Success(
        val penjualanId: String,
        val total: Double,
        val jumlahBayar: Double,
        val kembalian: Double
    ) : KasirUiState()

    data class Error(val message: String) : KasirUiState()
}

// ── Generic list state ────────────────────────────────────────────────────────

sealed interface ListUiState<out T> {
    object Loading : ListUiState<Nothing>
    data class Success<out T>(val data: List<T>) : ListUiState<T>
    data class Error(val message: String) : ListUiState<Nothing>
}