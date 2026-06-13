package com.example.supabaseauth.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.BorderStroke
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.viewmodel.CartItem
import com.example.supabaseauth.viewmodel.KasirUiState
import com.example.supabaseauth.viewmodel.KasirViewModel
import com.example.supabaseauth.viewmodel.BarangViewModel
import com.example.supabaseauth.viewmodel.AddPelangganState
import com.example.supabaseauth.viewmodel.CashierPelangganViewModel
import com.example.supabaseauth.viewmodel.ListUiState
import com.example.supabaseauth.viewmodel.MemberSearchState

// ── Kasir color palette (matches admin) ───────────────────────────────────────
private val KasirPrimary   = Color(0xFF1A3C40)
private val KasirTeal      = Color(0xFF00897B)
private val KasirBg        = Color(0xFFF4F6F8)
private val KasirSurface   = Color(0xFFFFFFFF)
private val KasirOnDark    = Color(0xFFFFFFFF)
private val KasirSubText   = Color(0xFF90A4AE)
private val KasirDivider   = Color(0xFFECEFF1)

// =============================================================================
// CASHIER KASIR SCREEN
// =============================================================================

@Composable
fun CashierKasirScreen(onNavigateBack: () -> Unit) {

    val vm: KasirViewModel = viewModel()
    val produkList     by vm.produkList.collectAsStateWithLifecycle()
    val keranjang      by vm.keranjang.collectAsStateWithLifecycle()
    val jumlahBayar    by vm.jumlahBayar.collectAsStateWithLifecycle()
    val uiState        by vm.kasirUiState.collectAsStateWithLifecycle()
    val selectedMember by vm.selectedMember.collectAsStateWithLifecycle()
    val memberQuery    by vm.memberQuery.collectAsStateWithLifecycle()
    val memberResults  by vm.memberResults.collectAsStateWithLifecycle()
    val memberSearchSt by vm.memberSearchState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadAll() }

    // ── Success receipt ───────────────────────────────────────────────────────
    if (uiState is KasirUiState.Success) {
        val s = uiState as KasirUiState.Success
        StrukSukses(
            state           = s,
            keranjang       = keranjang,
            onTransaksiBaru = { vm.resetSemua() },
            onKembali       = onNavigateBack
        )
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirBg)
    ) {
        // ── Top bar ──────────────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirPrimary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Back button
            IconButton(
                onClick  = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = KasirOnDark)
            }
            // Title
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Transaksi",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = KasirOnDark
                )
                if (keranjang.isNotEmpty()) {
                    Text(
                        "${keranjang.size} item • ${formatRupiah(vm.totalBelanja)}",
                        fontSize = 12.sp,
                        color    = KasirOnDark.copy(alpha = 0.75f)
                    )
                }
            }
            // Cart badge
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                if (keranjang.isNotEmpty()) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(KasirTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${keranjang.size}",
                            color      = KasirOnDark,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {

            // ── Pilih Produk ─────────────────────────────────────────────────
            item {
                KasirSectionHeader(
                    title    = "Pilih Produk",
                    subtitle = "${produkList.filter { it.stok > 0 }.size} produk tersedia"
                )
            }
            item {
                if (produkList.isEmpty()) {
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        Alignment.Center
                    ) {
                        CircularProgressIndicator(color = KasirPrimary)
                    }
                } else {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(produkList) { produk ->
                            KasirProdukCard(
                                produk         = produk,
                                qtyDiKeranjang = keranjang.find { it.produkId == produk.id }?.qty ?: 0,
                                onTambah       = { vm.tambahKeKeranjang(produk) }
                            )
                        }
                    }
                }
            }

            // ── Keranjang ────────────────────────────────────────────────────
            item {
                KasirSectionHeader(
                    title    = "Keranjang",
                    subtitle = if (keranjang.isEmpty()) "Kosong" else "${keranjang.sumOf { it.qty }} item"
                )
            }
            if (keranjang.isEmpty()) {
                item {
                    KasirEmptyCart()
                }
            } else {
                items(keranjang) { item ->
                    KasirKeranjangItem(
                        item      = item,
                        onTambah  = { produkList.find { it.id == item.produkId }?.let { vm.tambahKeKeranjang(it) } },
                        onKurangi = { vm.kurangiDariKeranjang(item.produkId) },
                        onHapus   = { vm.hapusDariKeranjang(item.produkId) }
                    )
                }
            }

            // ── Member (Pelanggan) ────────────────────────────────────────────
            item {
                KasirSectionHeader(
                    title    = "Anggota",
                    subtitle = if (selectedMember != null) selectedMember!!.nama else "Opsional"
                )
            }
            item {
                KasirMemberSection(
                    query          = memberQuery,
                    results        = memberResults,
                    selectedMember = selectedMember,
                    searchState    = memberSearchSt,
                    onQueryChange  = { vm.onMemberQueryChange(it) },
                    onSelect       = { vm.selectMember(it) },
                    onClear        = { vm.clearMember() }
                )
            }

            // ── Ringkasan Pembayaran ──────────────────────────────────────────
            item {
                KasirSectionHeader(title = "Pembayaran", subtitle = null)
                KasirRingkasan(
                    jumlahBayar         = jumlahBayar,
                    totalBelanja        = vm.totalBelanja,
                    kembalian           = vm.kembalian,
                    onJumlahBayarChange = { vm.onJumlahBayarChange(it) }
                )
            }

            // ── Error ────────────────────────────────────────────────────────
            if (uiState is KasirUiState.Error) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Row(
                            Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                (uiState as KasirUiState.Error).message,
                                color    = MaterialTheme.colorScheme.onErrorContainer,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }

            // ── Checkout Button ───────────────────────────────────────────────
            item {
                Spacer(Modifier.height(8.dp))
                val inputValid = (jumlahBayar.toDoubleOrNull() ?: 0.0) >= vm.totalBelanja && keranjang.isNotEmpty()
                Button(
                    onClick  = { vm.checkout() },
                    enabled  = inputValid && uiState !is KasirUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(54.dp),
                    shape  = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = KasirTeal)
                ) {
                    if (uiState is KasirUiState.Loading) {
                        CircularProgressIndicator(
                            Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color       = KasirOnDark
                        )
                        Spacer(Modifier.width(10.dp))
                        Text("Memproses...", fontSize = 16.sp, color = KasirOnDark)
                    } else {
                        Icon(
                            Icons.Default.ShoppingCartCheckout,
                            null,
                            tint     = KasirOnDark,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "Bayar  •  ${formatRupiah(vm.totalBelanja)}",
                            fontSize   = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color      = KasirOnDark
                        )
                    }
                }
                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

// =============================================================================
// CASHIER BARANG SCREEN
// =============================================================================

@Composable
fun CashierBarangScreen(onNavigateBack: () -> Unit) {
    val vm: BarangViewModel = viewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirBg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirPrimary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick  = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = KasirOnDark)
            }
            Text(
                "Daftar Produk",
                fontSize   = 18.sp,
                fontWeight = FontWeight.Bold,
                color      = KasirOnDark,
                modifier   = Modifier.align(Alignment.Center)
            )
        }

        when (uiState) {
            is ListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = KasirPrimary)
            }
            is ListUiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                Alignment.Center
            ) {
                Text(
                    (uiState as ListUiState.Error).message,
                    color     = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center
                )
            }
            is ListUiState.Success -> {
                val data = (uiState as ListUiState.Success<*>).data
                @Suppress("UNCHECKED_CAST")
                val produkList = data as List<Product>
                if (produkList.isEmpty()) {
                    Box(Modifier.fillMaxSize(), Alignment.Center) {
                        Text("Belum ada produk.", color = KasirSubText)
                    }
                } else {
                    LazyColumn(
                        contentPadding      = PaddingValues(all = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(produkList) { produk ->
                            Card(
                                shape    = RoundedCornerShape(14.dp),
                                colors   = CardDefaults.cardColors(containerColor = KasirSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(48.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(KasirPrimary.copy(alpha = 0.1f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            produk.nama.take(2).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color      = KasirPrimary,
                                            fontSize   = 16.sp
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            produk.nama,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 15.sp
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Text(
                                            formatRupiah(produk.harga),
                                            fontSize   = 13.sp,
                                            color      = KasirTeal,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            "Stok: ${produk.stok.toInt()}",
                                            fontSize   = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color      = if (produk.stok <= 0) MaterialTheme.colorScheme.error
                                            else KasirPrimary
                                        )
                                        Spacer(Modifier.height(2.dp))
                                        Surface(
                                            shape = RoundedCornerShape(20.dp),
                                            color = if (produk.is_active) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                        ) {
                                            Text(
                                                if (produk.is_active) "Aktif" else "Nonaktif",
                                                fontSize = 11.sp,
                                                color    = if (produk.is_active) Color(0xFF388E3C)
                                                else MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// CASHIER PELANGGAN SCREEN
// =============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CashierPelangganScreen(onNavigateBack: () -> Unit) {
    val vm: CashierPelangganViewModel = viewModel()
    val uiState     by vm.uiState.collectAsStateWithLifecycle()
    val addState    by vm.addState.collectAsStateWithLifecycle()
    val searchQuery by vm.searchQuery.collectAsStateWithLifecycle()

    var showAddDialog by remember { mutableStateOf(false) }
    var namaBaru      by remember { mutableStateOf("") }
    var noTelpBaru    by remember { mutableStateOf("") }

    LaunchedEffect(addState) {
        if (addState is AddPelangganState.Success) {
            showAddDialog = false
            namaBaru      = ""
            noTelpBaru    = ""
            vm.resetAddState()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirBg)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(KasirPrimary)
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            IconButton(
                onClick  = onNavigateBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Default.ArrowBack, null, tint = KasirOnDark)
            }
            Column(
                modifier            = Modifier.align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Pelanggan",
                    fontSize   = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color      = KasirOnDark
                )
                Text(
                    "Daftar & Daftarkan Anggota",
                    fontSize = 12.sp,
                    color    = KasirOnDark.copy(alpha = 0.7f)
                )
            }
            IconButton(
                onClick  = { showAddDialog = true },
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Default.PersonAdd, null, tint = KasirOnDark)
            }
        }

        // Search bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            OutlinedTextField(
                value         = searchQuery,
                onValueChange = { vm.onSearchChange(it) },
                placeholder   = { Text("Cari nama atau nomor HP...") },
                leadingIcon   = { Icon(Icons.Default.Search, null, tint = KasirSubText) },
                trailingIcon  = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { vm.onSearchChange("") }) {
                            Icon(Icons.Default.Close, null, tint = KasirSubText)
                        }
                    }
                },
                modifier   = Modifier.fillMaxWidth(),
                shape      = RoundedCornerShape(12.dp),
                singleLine = true,
                colors     = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor      = KasirPrimary,
                    unfocusedBorderColor    = KasirDivider,
                    focusedContainerColor   = KasirSurface,
                    unfocusedContainerColor = KasirSurface
                )
            )
        }

        // List
        when (uiState) {
            is ListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator(color = KasirPrimary)
            }
            is ListUiState.Error -> Box(
                Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.ErrorOutline,
                        null,
                        tint     = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        (uiState as ListUiState.Error).message,
                        color     = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = { vm.load() }) { Text("Coba Lagi") }
                }
            }
            is ListUiState.Success -> {
                val data = (uiState as ListUiState.Success<*>).data
                @Suppress("UNCHECKED_CAST")
                val list = data as List<Pelanggan>
                if (list.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PersonSearch,
                                null,
                                tint     = KasirSubText,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                if (searchQuery.isNotEmpty()) "Pelanggan tidak ditemukan"
                                else "Belum ada pelanggan terdaftar",
                                color     = KasirSubText,
                                textAlign = TextAlign.Center,
                                fontSize  = 15.sp
                            )
                            if (searchQuery.isEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors  = ButtonDefaults.buttonColors(containerColor = KasirTeal)
                                ) {
                                    Icon(
                                        Icons.Default.PersonAdd,
                                        null,
                                        tint     = KasirOnDark,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(6.dp))
                                    Text("Tambah Pelanggan", color = KasirOnDark)
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        // FIX: replaced invalid PaddingValues(horizontal, bottom) with explicit params
                        contentPadding      = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(list) { pelanggan ->
                            Card(
                                shape    = RoundedCornerShape(14.dp),
                                colors   = CardDefaults.cardColors(containerColor = KasirSurface),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(46.dp)
                                            .clip(CircleShape)
                                            .background(KasirTeal.copy(alpha = 0.12f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            pelanggan.nama.take(1).uppercase(),
                                            fontWeight = FontWeight.Bold,
                                            color      = KasirTeal,
                                            fontSize   = 18.sp
                                        )
                                    }
                                    Spacer(Modifier.width(14.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(
                                            pelanggan.nama,
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize   = 15.sp
                                        )
                                        if (!pelanggan.no_telp.isNullOrBlank()) {
                                            Spacer(Modifier.height(2.dp))
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    Icons.Default.Phone,
                                                    null,
                                                    tint     = KasirSubText,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                                Spacer(Modifier.width(4.dp))
                                                Text(
                                                    pelanggan.no_telp,
                                                    fontSize = 13.sp,
                                                    color    = KasirSubText
                                                )
                                            }
                                        }
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(20.dp),
                                        color = if (pelanggan.is_active) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
                                    ) {
                                        Text(
                                            if (pelanggan.is_active) "Aktif" else "Nonaktif",
                                            fontSize = 11.sp,
                                            color    = if (pelanggan.is_active) Color(0xFF388E3C)
                                            else MaterialTheme.colorScheme.error,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ── Add Pelanggan Dialog ──────────────────────────────────────────────────
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                namaBaru      = ""
                noTelpBaru    = ""
                vm.resetAddState()
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.PersonAdd,
                        null,
                        tint     = KasirTeal,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Tambah Pelanggan", fontWeight = FontWeight.SemiBold)
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = namaBaru,
                        onValueChange = { namaBaru = it },
                        label         = { Text("Nama Pelanggan *") },
                        leadingIcon   = { Icon(Icons.Default.Person, null) },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true
                    )
                    OutlinedTextField(
                        value           = noTelpBaru,
                        onValueChange   = { noTelpBaru = it },
                        label           = { Text("Nomor HP (opsional)") },
                        leadingIcon     = { Icon(Icons.Default.Phone, null) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier        = Modifier.fillMaxWidth(),
                        singleLine      = true
                    )
                    if (addState is AddPelangganState.Error) {
                        Text(
                            (addState as AddPelangganState.Error).message,
                            color    = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick  = { vm.addPelanggan(namaBaru.trim(), noTelpBaru.trim()) },
                    enabled  = namaBaru.isNotBlank() && addState !is AddPelangganState.Loading,
                    colors   = ButtonDefaults.buttonColors(containerColor = KasirTeal)
                ) {
                    if (addState is AddPelangganState.Loading) {
                        CircularProgressIndicator(
                            Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color       = KasirOnDark
                        )
                    } else {
                        Text("Simpan", color = KasirOnDark)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    namaBaru      = ""
                    noTelpBaru    = ""
                    vm.resetAddState()
                }) { Text("Batal") }
            }
        )
    }
}

// =============================================================================
// SHARED SUB-COMPOSABLES
// =============================================================================

@Composable
fun KasirSectionHeader(title: String, subtitle: String? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.Bottom
    ) {
        Text(
            title,
            fontSize   = 14.sp,
            fontWeight = FontWeight.Bold,
            color      = KasirPrimary
        )
        if (subtitle != null) {
            Text(subtitle, fontSize = 12.sp, color = KasirSubText)
        }
    }
}

@Composable
fun KasirEmptyCart() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.ShoppingCart,
                null,
                tint     = KasirSubText.copy(alpha = 0.5f),
                modifier = Modifier.size(40.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text("Belum ada produk dipilih", color = KasirSubText, fontSize = 14.sp)
        }
    }
}

@Composable
fun KasirProdukCard(produk: Product, qtyDiKeranjang: Int, onTambah: () -> Unit) {
    val stokHabis = produk.stok.toInt() <= 0
    val selected  = qtyDiKeranjang > 0

    Card(
        modifier = Modifier
            .width(130.dp)
            .clickable(enabled = !stokHabis) { onTambah() },
        shape     = RoundedCornerShape(14.dp),
        colors    = CardDefaults.cardColors(containerColor = KasirSurface),
        border    = if (selected) BorderStroke(1.5.dp, KasirTeal) else null,
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 2.dp else 0.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(
                        if (selected) KasirTeal.copy(alpha = 0.15f)
                        else          KasirPrimary.copy(alpha = 0.08f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    produk.nama.take(2).uppercase(),
                    fontSize   = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color      = if (selected) KasirTeal else KasirPrimary
                )
                // qty badge
                if (qtyDiKeranjang > 0) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(20.dp)
                            .clip(CircleShape)
                            .background(KasirTeal),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "$qtyDiKeranjang",
                            fontSize   = 10.sp,
                            color      = KasirOnDark,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                produk.nama,
                fontSize   = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines   = 2,
                overflow   = TextOverflow.Ellipsis,
                color      = if (stokHabis) KasirSubText else Color(0xFF263238)
            )
            Spacer(Modifier.height(2.dp))
            Text(
                formatRupiah(produk.harga),
                fontSize   = 12.sp,
                color      = if (selected) KasirTeal else KasirPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                if (stokHabis) "Stok habis" else "Stok: ${produk.stok.toInt()}",
                fontSize = 10.sp,
                color    = if (stokHabis) MaterialTheme.colorScheme.error else KasirSubText
            )
        }
    }
}

@Composable
fun KasirKeranjangItem(
    item: CartItem,
    onTambah: () -> Unit,
    onKurangi: () -> Unit,
    onHapus: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KasirSurface)
    ) {
        Row(
            modifier          = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item avatar
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(KasirPrimary.copy(alpha = 0.08f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    item.namaProduk.take(1).uppercase(),
                    fontWeight = FontWeight.Bold,
                    color      = KasirPrimary,
                    fontSize   = 15.sp
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    item.namaProduk,
                    fontSize  = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines  = 1,
                    overflow  = TextOverflow.Ellipsis
                )
                Text("${formatRupiah(item.hargaSatuan)} / pcs", fontSize = 11.sp, color = KasirSubText)
            }
            Spacer(Modifier.width(8.dp))
            // Qty controls
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onKurangi, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Remove, null, tint = KasirPrimary, modifier = Modifier.size(16.dp))
                }
                Text(
                    "${item.qty}",
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier   = Modifier.widthIn(min = 26.dp),
                    textAlign  = TextAlign.Center
                )
                IconButton(onClick = onTambah, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Add, null, tint = KasirPrimary, modifier = Modifier.size(16.dp))
                }
            }
            Spacer(Modifier.width(6.dp))
            Text(
                formatRupiah(item.subtotal),
                fontSize   = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color      = KasirTeal,
                modifier   = Modifier.widthIn(min = 78.dp),
                textAlign  = TextAlign.End
            )
            IconButton(onClick = onHapus, modifier = Modifier.size(28.dp)) {
                Icon(
                    Icons.Default.Close,
                    null,
                    tint     = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun KasirMemberSection(
    query: String,
    results: List<Pelanggan>,
    selectedMember: Pelanggan?,
    searchState: MemberSearchState,
    onQueryChange: (String) -> Unit,
    onSelect: (Pelanggan) -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KasirSurface)
    ) {
        Column(Modifier.padding(14.dp)) {
            if (selectedMember != null) {
                // Selected state
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(KasirTeal.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Person, null, tint = KasirTeal, modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(selectedMember.nama, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        if (!selectedMember.no_telp.isNullOrBlank()) {
                            Text(selectedMember.no_telp, fontSize = 12.sp, color = KasirSubText)
                        }
                    }
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFFE8F5E9)
                    ) {
                        Text(
                            "Anggota",
                            fontSize = 11.sp,
                            color    = Color(0xFF388E3C),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    IconButton(onClick = onClear, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Close, null, tint = KasirSubText, modifier = Modifier.size(16.dp))
                    }
                }
            } else {
                // Search field
                OutlinedTextField(
                    value         = query,
                    onValueChange = onQueryChange,
                    placeholder   = { Text("Cari anggota (nama / no. HP)...") },
                    leadingIcon   = {
                        if (searchState is MemberSearchState.Searching) {
                            CircularProgressIndicator(
                                Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color       = KasirPrimary
                            )
                        } else {
                            Icon(Icons.Default.PersonSearch, null, tint = KasirSubText)
                        }
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onClear) {
                                Icon(Icons.Default.Close, null, tint = KasirSubText)
                            }
                        }
                    },
                    modifier   = Modifier.fillMaxWidth(),
                    shape      = RoundedCornerShape(10.dp),
                    singleLine = true,
                    colors     = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor   = KasirPrimary,
                        unfocusedBorderColor = KasirDivider
                    )
                )
                // Results dropdown
                if (results.isNotEmpty()) {
                    Spacer(Modifier.height(8.dp))
                    results.forEach { pelanggan ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onSelect(pelanggan) }
                                .padding(horizontal = 8.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(KasirTeal.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    pelanggan.nama.take(1).uppercase(),
                                    color      = KasirTeal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize   = 14.sp
                                )
                            }
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text(pelanggan.nama, fontSize = 14.sp, fontWeight = FontWeight.Medium)
                                if (!pelanggan.no_telp.isNullOrBlank()) {
                                    Text(pelanggan.no_telp, fontSize = 12.sp, color = KasirSubText)
                                }
                            }
                        }
                        if (results.last() != pelanggan) {
                            HorizontalDivider(color = KasirDivider)
                        }
                    }
                } else if (query.isNotBlank() && searchState is MemberSearchState.Done) {
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Anggota tidak ditemukan. Transaksi bisa tetap dilanjutkan.",
                        fontSize = 12.sp,
                        color    = KasirSubText
                    )
                }
                // No member notice
                if (query.isBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Kosongkan jika tidak memiliki kartu anggota",
                        fontSize = 11.sp,
                        color    = KasirSubText
                    )
                }
            }
        }
    }
}

@Composable
fun KasirRingkasan(
    jumlahBayar: String,
    totalBelanja: Double,
    kembalian: Double,
    onJumlahBayarChange: (String) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        shape  = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = KasirSurface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Belanja", fontSize = 14.sp, color = KasirSubText)
                Text(
                    formatRupiah(totalBelanja),
                    fontSize   = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color      = KasirPrimary
                )
            }

            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = KasirDivider)
            Spacer(Modifier.height(14.dp))

            OutlinedTextField(
                value           = jumlahBayar,
                onValueChange   = onJumlahBayarChange,
                label           = { Text("Jumlah Bayar") },
                prefix          = { Text("Rp ") },
                leadingIcon     = { Icon(Icons.Default.Payments, null, tint = KasirTeal) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier        = Modifier.fillMaxWidth(),
                singleLine      = true,
                shape           = RoundedCornerShape(10.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = KasirPrimary,
                    unfocusedBorderColor = KasirDivider
                )
            )

            Spacer(Modifier.height(12.dp))

            val valid = kembalian >= 0
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape    = RoundedCornerShape(10.dp),
                color    = if (valid) Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
            ) {
                Row(
                    modifier              = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        "Kembalian",
                        fontWeight = FontWeight.SemiBold,
                        color      = if (valid) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                    )
                    Text(
                        formatRupiah(kembalian),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = if (valid) Color(0xFF388E3C) else MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

// =============================================================================
// RECEIPT / STRUK SUKSES
// =============================================================================

@Composable
fun StrukSukses(
    state: KasirUiState.Success,
    keranjang: List<CartItem>,
    onTransaksiBaru: () -> Unit,
    onKembali: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KasirBg)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(24.dp))

        // Success icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(Color(0xFFE8F5E9)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Default.CheckCircle,
                null,
                tint     = Color(0xFF388E3C),
                modifier = Modifier.size(44.dp)
            )
        }

        Spacer(Modifier.height(14.dp))
        Text(
            "Transaksi Berhasil!",
            style      = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color      = KasirPrimary
        )
        Text("ID: ${state.penjualanId.take(8)}...", fontSize = 12.sp, color = KasirSubText)
        if (!state.namaMember.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFFE8F5E9)) {
                Text(
                    "Anggota: ${state.namaMember}",
                    fontSize = 12.sp,
                    color    = Color(0xFF388E3C),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        // Receipt card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape    = RoundedCornerShape(16.dp),
            colors   = CardDefaults.cardColors(containerColor = KasirSurface)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    "RINCIAN PEMBELIAN",
                    fontSize      = 11.sp,
                    fontWeight    = FontWeight.Bold,
                    color         = KasirSubText,
                    letterSpacing = 1.sp
                )
                Spacer(Modifier.height(10.dp))

                keranjang.forEach { item ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            "${item.namaProduk}  ×${item.qty}",
                            fontSize = 13.sp,
                            modifier = Modifier.weight(1f)
                        )
                        Text(formatRupiah(item.subtotal), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(Modifier.height(10.dp))
                HorizontalDivider(color = KasirDivider)
                Spacer(Modifier.height(10.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total", color = KasirSubText)
                    Text(formatRupiah(state.total), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(6.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Dibayar", color = KasirSubText)
                    Text(formatRupiah(state.jumlahBayar), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(color = KasirDivider)
                Spacer(Modifier.height(8.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kembalian", fontWeight = FontWeight.Bold, color = KasirPrimary)
                    Text(
                        formatRupiah(state.kembalian),
                        fontWeight = FontWeight.Bold,
                        fontSize   = 16.sp,
                        color      = KasirTeal
                    )
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick  = onTransaksiBaru,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape  = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = KasirTeal)
        ) {
            Icon(Icons.Default.Add, null, tint = KasirOnDark, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Transaksi Baru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = KasirOnDark)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onKembali, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Dashboard", color = KasirPrimary)
        }
    }
}
