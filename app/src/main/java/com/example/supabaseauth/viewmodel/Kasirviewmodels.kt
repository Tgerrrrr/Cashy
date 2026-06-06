package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.repository.BarangRepository
import com.example.supabaseauth.repository.KasirRepository
import com.example.supabaseauth.repository.KasRepository
import com.example.supabaseauth.repository.PenjualanInsert
import com.example.supabaseauth.repository.PenjualanItemInsert
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================================================
// KASIR VIEWMODEL  (full POS — cart, checkout, kas selection)
// =============================================================================

class KasirViewModel : ViewModel() {

    private val barangRepo = BarangRepository()
    private val kasRepo    = KasRepository()
    private val kasirRepo  = KasirRepository()

    // ── State flows ───────────────────────────────────────────────────────────

    private val _produkList  = MutableStateFlow<List<Product>>(emptyList())
    val produkList: StateFlow<List<Product>> = _produkList.asStateFlow()

    private val _keranjang   = MutableStateFlow<List<CartItem>>(emptyList())
    val keranjang: StateFlow<List<CartItem>> = _keranjang.asStateFlow()

    private val _kasList     = MutableStateFlow<List<Kas>>(emptyList())
    val kasList: StateFlow<List<Kas>> = _kasList.asStateFlow()

    private val _jumlahBayar = MutableStateFlow("")
    val jumlahBayar: StateFlow<String> = _jumlahBayar.asStateFlow()

    private val _kasId       = MutableStateFlow("")
    val kasId: StateFlow<String> = _kasId.asStateFlow()

    private val _kasirUiState = MutableStateFlow<KasirUiState>(KasirUiState.Idle)
    val kasirUiState: StateFlow<KasirUiState> = _kasirUiState.asStateFlow()

    // ── Derived values ────────────────────────────────────────────────────────

    val totalBelanja: Double
        get() = _keranjang.value.sumOf { it.subtotal }

    val kembalian: Double
        get() = (_jumlahBayar.value.toDoubleOrNull() ?: 0.0) - totalBelanja

    // ── Load data ─────────────────────────────────────────────────────────────

    fun loadAll() {
        viewModelScope.launch {
            try {
                _produkList.value = barangRepo.getAllBarang()
                _kasList.value    = kasRepo.getAllKas()
                // Default to first active kas
                if (_kasId.value.isEmpty()) {
                    _kasList.value.firstOrNull { it.is_active }?.id?.let { _kasId.value = it }
                }
            } catch (e: Exception) {
                _kasirUiState.value = KasirUiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    // ── Cart operations ───────────────────────────────────────────────────────

    fun tambahKeKeranjang(produk: Product) {
        val current = _keranjang.value.toMutableList()
        val idx = current.indexOfFirst { it.produkId == produk.id }
        if (idx >= 0) {
            val item = current[idx]
            if (item.qty < item.stokTersedia) {
                current[idx] = item.copy(qty = item.qty + 1)
            }
        } else {
            if (produk.stok.toInt() > 0) {
                current.add(
                    CartItem(
                        produkId      = produk.id ?: return,
                        namaProduk    = produk.nama,
                        hargaSatuan   = produk.harga,
                        qty           = 1,
                        stokTersedia  = produk.stok.toInt()
                    )
                )
            }
        }
        _keranjang.value = current
    }

    fun kurangiDariKeranjang(produkId: String) {
        val current = _keranjang.value.toMutableList()
        val idx = current.indexOfFirst { it.produkId == produkId }
        if (idx >= 0) {
            val item = current[idx]
            if (item.qty > 1) current[idx] = item.copy(qty = item.qty - 1)
            else current.removeAt(idx)
            _keranjang.value = current
        }
    }

    fun hapusDariKeranjang(produkId: String) {
        _keranjang.value = _keranjang.value.filter { it.produkId != produkId }
    }

    fun onKasIdChange(id: String)          { _kasId.value = id }
    fun onJumlahBayarChange(nilai: String) { _jumlahBayar.value = nilai }

    // ── Checkout ──────────────────────────────────────────────────────────────

    fun checkout() {
        viewModelScope.launch {
            _kasirUiState.value = KasirUiState.Loading
            try {
                val total  = totalBelanja
                val bayar  = _jumlahBayar.value.toDoubleOrNull() ?: 0.0
                val kasId  = _kasId.value

                val penjualanId = kasirRepo.insertPenjualan(
                    PenjualanInsert(
                        kasId       = kasId,
                        total       = total,
                        jumlahBayar = bayar
                    )
                )

                val items = _keranjang.value.map { item ->
                    PenjualanItemInsert(
                        penjualanId = penjualanId,
                        produkId    = item.produkId,
                        namaProduk  = item.namaProduk,
                        hargaSatuan = item.hargaSatuan,
                        qty         = item.qty.toDouble(),
                        subtotal    = item.subtotal
                    )
                }
                kasirRepo.insertPenjualanItems(items)

                _keranjang.value.forEach { item ->
                    kasirRepo.kurangiStokProduk(item.produkId, item.qty)
                }

                _kasirUiState.value = KasirUiState.Success(
                    penjualanId = penjualanId,
                    total       = total,
                    jumlahBayar = bayar,
                    kembalian   = bayar - total
                )
            } catch (e: Exception) {
                _kasirUiState.value = KasirUiState.Error(e.message ?: "Checkout gagal")
            }
        }
    }

    fun resetSemua() {
        _keranjang.value    = emptyList()
        _jumlahBayar.value  = ""
        _kasirUiState.value = KasirUiState.Idle
    }
}

// =============================================================================
// BARANG VIEWMODEL  (product list for cashier, read-only)
// =============================================================================

class BarangViewModel : ViewModel() {

    private val repo = BarangRepository()

    private val _uiState = MutableStateFlow<ListUiState<Product>>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState<Product>> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            try {
                _uiState.value = ListUiState.Success(repo.getAllBarang())
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message ?: "Gagal memuat produk")
            }
        }
    }
}

// =============================================================================
// CASHIER KAS VIEWMODEL  (kas list for cashier, read-only)
// =============================================================================

class CashierKasViewModel : ViewModel() {

    private val repo = KasRepository()

    private val _uiState = MutableStateFlow<ListUiState<Kas>>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState<Kas>> = _uiState.asStateFlow()

    init { load() }

    private fun load() {
        viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            try {
                _uiState.value = ListUiState.Success(repo.getAllKas())
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message ?: "Gagal memuat kas")
            }
        }
    }
}