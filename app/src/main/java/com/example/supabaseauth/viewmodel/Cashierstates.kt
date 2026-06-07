package com.example.supabaseauth.viewmodel

data class CartItem(
    val produkId: String,
    val namaProduk: String,
    val hargaSatuan: Double,
    val qty: Int,
    val stokTersedia: Int
) {
    val subtotal: Double get() = hargaSatuan * qty
}

sealed class KasirUiState {
    object Idle    : KasirUiState()
    object Loading : KasirUiState()

    data class Success(
        val penjualanId: String,
        val total: Double,
        val jumlahBayar: Double,
        val kembalian: Double,
        val namaMember: String? = null
    ) : KasirUiState()

    data class Error(val message: String) : KasirUiState()
}

sealed interface ListUiState<out T> {
    object Loading : ListUiState<Nothing>
    data class Success<out T>(val data: List<T>) : ListUiState<T>
    data class Error(val message: String) : ListUiState<Nothing>
}

sealed class AddPelangganState {
    object Idle    : AddPelangganState()
    object Loading : AddPelangganState()
    object Success : AddPelangganState()
    data class Error(val message: String) : AddPelangganState()
}

sealed class MemberSearchState {
    object Idle      : MemberSearchState()
    object Searching : MemberSearchState()
    object Done      : MemberSearchState()
    data class Error(val message: String) : MemberSearchState()
}