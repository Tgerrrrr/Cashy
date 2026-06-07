package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.repository.BarangRepository
import com.example.supabaseauth.repository.KasirRepository
import com.example.supabaseauth.repository.KasRepository
import com.example.supabaseauth.repository.PelangganInsert
import com.example.supabaseauth.repository.PelangganRepository
import com.example.supabaseauth.repository.PenjualanInsert
import com.example.supabaseauth.repository.PenjualanItemInsert
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// =============================================================================
// KASIR VIEWMODEL
// =============================================================================

class KasirViewModel : ViewModel() {

    private val barangRepo    = BarangRepository()
    private val kasRepo       = KasRepository()
    private val kasirRepo     = KasirRepository()
    private val pelangganRepo = PelangganRepository()

    private val _produkList   = MutableStateFlow<List<Product>>(emptyList())
    val produkList: StateFlow<List<Product>> = _produkList.asStateFlow()

    private val _keranjang    = MutableStateFlow<List<CartItem>>(emptyList())
    val keranjang: StateFlow<List<CartItem>> = _keranjang.asStateFlow()

    // Auto-selected, never shown to cashier
    private var autoKasId: String = ""

    // Kept for receipt display only
    private val _kasList = MutableStateFlow<List<Kas>>(emptyList())
    val kasList: StateFlow<List<Kas>> = _kasList.asStateFlow()

    private val _jumlahBayar  = MutableStateFlow("")
    val jumlahBayar: StateFlow<String> = _jumlahBayar.asStateFlow()

    // Member search
    private val _memberQuery   = MutableStateFlow("")
    val memberQuery: StateFlow<String> = _memberQuery.asStateFlow()

    private val _memberResults = MutableStateFlow<List<Pelanggan>>(emptyList())
    val memberResults: StateFlow<List<Pelanggan>> = _memberResults.asStateFlow()

    private val _selectedMember = MutableStateFlow<Pelanggan?>(null)
    val selectedMember: StateFlow<Pelanggan?> = _selectedMember.asStateFlow()

    private val _memberSearchState = MutableStateFlow<MemberSearchState>(MemberSearchState.Idle)
    val memberSearchState: StateFlow<MemberSearchState> = _memberSearchState.asStateFlow()

    private val _kasirUiState = MutableStateFlow<KasirUiState>(KasirUiState.Idle)
    val kasirUiState: StateFlow<KasirUiState> = _kasirUiState.asStateFlow()

    val totalBelanja: Double get() = _keranjang.value.sumOf { it.subtotal }
    val kembalian: Double    get() = (_jumlahBayar.value.toDoubleOrNull() ?: 0.0) - totalBelanja

    fun loadAll() {
        viewModelScope.launch {
            try {
                _produkList.value = barangRepo.getAllBarang().filter { it.is_active }
                _kasList.value    = kasRepo.getAllKas()
                // Auto-pick first active kas — cashier never sees this
                autoKasId = _kasList.value
                    .firstOrNull { it.nama.equals("Kas Toko", ignoreCase = true) && it.is_active }?.id
                    ?: _kasList.value.firstOrNull { it.is_active }?.id
                            ?: ""
            } catch (e: Exception) {
                _kasirUiState.value = KasirUiState.Error(e.message ?: "Gagal memuat data")
            }
        }
    }

    fun tambahKeKeranjang(produk: Product) {
        val current = _keranjang.value.toMutableList()
        val idx = current.indexOfFirst { it.produkId == produk.id }
        if (idx >= 0) {
            val item = current[idx]
            if (item.qty < item.stokTersedia) current[idx] = item.copy(qty = item.qty + 1)
        } else {
            if (produk.stok.toInt() > 0) {
                current.add(
                    CartItem(
                        produkId     = produk.id ?: return,
                        namaProduk   = produk.nama,
                        hargaSatuan  = produk.harga,
                        qty          = 1,
                        stokTersedia = produk.stok.toInt()
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

    fun onJumlahBayarChange(nilai: String) { _jumlahBayar.value = nilai }

    // Member search with debounce
    private var searchJob: Job? = null

    fun onMemberQueryChange(query: String) {
        _memberQuery.value = query
        if (query.isBlank()) {
            _memberResults.value = emptyList()
            _memberSearchState.value = MemberSearchState.Idle
            return
        }
        _memberSearchState.value = MemberSearchState.Searching
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(400)
            try {
                val results = pelangganRepo.searchPelanggan(query)
                _memberResults.value = results
                _memberSearchState.value = MemberSearchState.Done
            } catch (e: Exception) {
                _memberSearchState.value = MemberSearchState.Error(e.message ?: "Gagal mencari")
            }
        }
    }

    fun selectMember(pelanggan: Pelanggan) {
        _selectedMember.value = pelanggan
        _memberQuery.value = pelanggan.nama
        _memberResults.value = emptyList()
        _memberSearchState.value = MemberSearchState.Idle
    }

    fun clearMember() {
        _selectedMember.value = null
        _memberQuery.value = ""
        _memberResults.value = emptyList()
        _memberSearchState.value = MemberSearchState.Idle
    }

    fun checkout() {
        viewModelScope.launch {
            _kasirUiState.value = KasirUiState.Loading
            try {
                val total = totalBelanja
                val bayar = _jumlahBayar.value.toDoubleOrNull() ?: 0.0

                val penjualanId = kasirRepo.insertPenjualan(
                    PenjualanInsert(
                        kasId       = autoKasId,
                        pelangganId = _selectedMember.value?.id,
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
                    kembalian   = bayar - total,
                    namaMember  = _selectedMember.value?.nama
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
        clearMember()
    }
}

// =============================================================================
// BARANG VIEWMODEL
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
// CASHIER PELANGGAN VIEWMODEL
// =============================================================================

class CashierPelangganViewModel : ViewModel() {

    private val repo = PelangganRepository()

    private val _uiState = MutableStateFlow<ListUiState<Pelanggan>>(ListUiState.Loading)
    val uiState: StateFlow<ListUiState<Pelanggan>> = _uiState.asStateFlow()

    private val _addState = MutableStateFlow<AddPelangganState>(AddPelangganState.Idle)
    val addState: StateFlow<AddPelangganState> = _addState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _allData = MutableStateFlow<List<Pelanggan>>(emptyList())

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ListUiState.Loading
            try {
                val data = repo.getAllPelanggan()
                _allData.value = data
                _uiState.value = ListUiState.Success(data)
            } catch (e: Exception) {
                _uiState.value = ListUiState.Error(e.message ?: "Gagal memuat pelanggan")
            }
        }
    }

    fun onSearchChange(q: String) {
        _searchQuery.value = q
        val filtered = if (q.isBlank()) _allData.value
        else _allData.value.filter {
            it.nama.contains(q, ignoreCase = true) ||
                    it.no_telp?.contains(q, ignoreCase = true) == true
        }
        _uiState.value = ListUiState.Success(filtered)
    }

    fun addPelanggan(nama: String, noTelp: String?) {
        viewModelScope.launch {
            _addState.value = AddPelangganState.Loading
            try {
                repo.addPelanggan(PelangganInsert(nama = nama, noTelp = noTelp?.ifBlank { null }))
                _addState.value = AddPelangganState.Success
                load()
            } catch (e: Exception) {
                _addState.value = AddPelangganState.Error(e.message ?: "Gagal menambah pelanggan")
            }
        }
    }

    fun resetAddState() { _addState.value = AddPelangganState.Idle }
}