package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.foundation.BorderStroke
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.viewmodel.CartItem
import com.example.supabaseauth.viewmodel.CashierKasViewModel
import com.example.supabaseauth.viewmodel.KasirUiState
import com.example.supabaseauth.viewmodel.KasirViewModel
import com.example.supabaseauth.viewmodel.BarangViewModel
import com.example.supabaseauth.viewmodel.ListUiState
import java.text.NumberFormat
import java.util.Locale


// =============================================================================
// CASHIER KASIR SCREEN
// =============================================================================

@Composable
fun CashierKasirScreen(onNavigateBack: () -> Unit) {

    val vm: KasirViewModel = viewModel()
    val produkList  by vm.produkList.collectAsStateWithLifecycle()
    val keranjang   by vm.keranjang.collectAsStateWithLifecycle()
    val kasList     by vm.kasList.collectAsStateWithLifecycle()
    val jumlahBayar by vm.jumlahBayar.collectAsStateWithLifecycle()
    val kasId       by vm.kasId.collectAsStateWithLifecycle()
    val uiState     by vm.kasirUiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { vm.loadAll() }

    // ── Success receipt ───────────────────────────────────────────────────────
    if (uiState is KasirUiState.Success) {
        val s = uiState as KasirUiState.Success
        StrukSukses(
            state         = s,
            keranjang     = keranjang,
            kasList       = kasList,
            kasId         = kasId,
            onTransaksiBaru = { vm.resetSemua() },
            onKembali       = onNavigateBack
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onNavigateBack) {
                Text("←", color = MaterialTheme.colorScheme.onPrimary, fontSize = 18.sp)
            }
            Text(
                "Kasir",
                style    = MaterialTheme.typography.titleLarge,
                color    = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center
            )
            Text(
                "${keranjang.size} item",
                color    = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                fontSize = 13.sp
            )
        }

        LazyColumn(modifier = Modifier.weight(1f)) {

            // Pilih produk
            item { KasirSectionHeader("Pilih Produk") }
            item {
                if (produkList.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), Alignment.Center) {
                        CircularProgressIndicator()
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(produkList) { produk ->
                            KasirProdukCard(
                                produk           = produk,
                                qtyDiKeranjang   = keranjang.find { it.produkId == produk.id }?.qty ?: 0,
                                onTambah         = { vm.tambahKeKeranjang(produk) }
                            )
                        }
                    }
                }
            }

            // Keranjang
            item { KasirSectionHeader("Keranjang Belanja") }
            if (keranjang.isEmpty()) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), Alignment.Center) {
                        Text(
                            "Belum ada produk dipilih",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                        )
                    }
                }
            } else {
                items(keranjang) { item ->
                    KasirKeranjangItem(
                        item     = item,
                        onTambah = { produkList.find { it.id == item.produkId }?.let { vm.tambahKeKeranjang(it) } },
                        onKurangi = { vm.kurangiDariKeranjang(item.produkId) },
                        onHapus  = { vm.hapusDariKeranjang(item.produkId) }
                    )
                }
            }

            // Ringkasan
            item {
                KasirSectionHeader("Ringkasan Pembayaran")
                KasirRingkasan(
                    kasList         = kasList,
                    kasId           = kasId,
                    jumlahBayar     = jumlahBayar,
                    totalBelanja    = vm.totalBelanja,
                    kembalian       = vm.kembalian,
                    onKasIdChange   = { vm.onKasIdChange(it) },
                    onJumlahBayarChange = { vm.onJumlahBayarChange(it) }
                )
            }

            // Error
            if (uiState is KasirUiState.Error) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                    ) {
                        Text(
                            (uiState as KasirUiState.Error).message,
                            color    = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Checkout button
            item {
                Spacer(Modifier.height(8.dp))
                val inputValid = (jumlahBayar.toDoubleOrNull() ?: 0.0) >= vm.totalBelanja
                Button(
                    onClick  = { vm.checkout() },
                    enabled  = keranjang.isNotEmpty() && inputValid && uiState !is KasirUiState.Loading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is KasirUiState.Loading) {
                        CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
                        Spacer(Modifier.width(8.dp))
                        Text("Memproses...", fontSize = 16.sp)
                    } else {
                        Text(
                            "Proses Pembayaran  •  ${formatRupiah(vm.totalBelanja)}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
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

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onNavigateBack) { Text("← Kembali") }
            Spacer(Modifier.width(8.dp))
            Text("Daftar Produk", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(12.dp))

        when (uiState) {
            is ListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is ListUiState.Error   -> Text((uiState as ListUiState.Error).message, color = MaterialTheme.colorScheme.error)
            is ListUiState.Success -> {
                val data = (uiState as ListUiState.Success<Product>).data
                if (data.isEmpty()) {
                    Text("Belum ada produk.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(data) { produk ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(produk.nama, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Harga: ${formatRupiah(produk.harga)}", style = MaterialTheme.typography.bodyMedium)
                                    Text("Stok: ${produk.stok.toInt()} unit", style = MaterialTheme.typography.bodySmall)
                                    Text(
                                        text  = if (produk.is_active) "Aktif" else "Tidak Aktif",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (produk.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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

// =============================================================================
// CASHIER KAS SCREEN
// =============================================================================

@Composable
fun CashierKasScreen(onNavigateBack: () -> Unit) {
    val vm: CashierKasViewModel = viewModel()
    val uiState by vm.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onNavigateBack) { Text("← Kembali") }
            Spacer(Modifier.width(8.dp))
            Text("Daftar Kas", style = MaterialTheme.typography.headlineSmall)
        }
        Spacer(Modifier.height(12.dp))

        when (uiState) {
            is ListUiState.Loading -> Box(Modifier.fillMaxSize(), Alignment.Center) { CircularProgressIndicator() }
            is ListUiState.Error   -> Text((uiState as ListUiState.Error).message, color = MaterialTheme.colorScheme.error)
            is ListUiState.Success -> {
                val data = (uiState as ListUiState.Success<Kas>).data
                if (data.isEmpty()) {
                    Text("Belum ada kas.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(data) { kas ->
                            Card(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(kas.nama, style = MaterialTheme.typography.titleMedium)
                                    Spacer(Modifier.height(4.dp))
                                    Text("Saldo: ${formatRupiah(kas.saldo)}", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text  = if (kas.is_active) "Aktif" else "Tidak Aktif",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = if (kas.is_active) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
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

// =============================================================================
// SHARED SUB-COMPOSABLES  (used inside CashierKasirScreen)
// =============================================================================

@Composable
fun KasirSectionHeader(title: String) {
    Text(
        text     = title,
        style    = MaterialTheme.typography.labelLarge,
        color    = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 4.dp)
    )
}

@Composable
fun KasirProdukCard(produk: Product, qtyDiKeranjang: Int, onTambah: () -> Unit) {
    val stokHabis = produk.stok.toInt() <= 0
    Card(
        modifier = Modifier.width(140.dp).clickable(enabled = !stokHabis) { onTambah() },
        colors   = CardDefaults.cardColors(
            containerColor = if (stokHabis) MaterialTheme.colorScheme.surfaceVariant
            else MaterialTheme.colorScheme.surface
        ),
        border = if (qtyDiKeranjang > 0) BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth().height(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    produk.nama.take(2).uppercase(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
            Spacer(Modifier.height(8.dp))
            Text(
                produk.nama,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = if (stokHabis) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.onSurface
            )
            Text(
                formatRupiah(produk.harga),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Medium
            )
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (stokHabis) "Habis" else "Stok: ${produk.stok.toInt()}",
                    fontSize = 11.sp,
                    color = if (stokHabis) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
                if (qtyDiKeranjang > 0) {
                    Box(
                        modifier = Modifier.size(20.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("$qtyDiKeranjang", fontSize = 11.sp, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun KasirKeranjangItem(item: CartItem, onTambah: () -> Unit, onKurangi: () -> Unit, onHapus: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(10.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.namaProduk, fontSize = 14.sp, fontWeight = FontWeight.Medium)
            Text("${formatRupiah(item.hargaSatuan)} / pcs", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onKurangi, modifier = Modifier.size(32.dp)) {
                Text("−", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
            Text("${item.qty}", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.widthIn(min = 28.dp), textAlign = TextAlign.Center)
            IconButton(onClick = onTambah, modifier = Modifier.size(32.dp)) {
                Text("+", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
            }
            Spacer(Modifier.width(8.dp))
            Text(formatRupiah(item.subtotal), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.primary, modifier = Modifier.widthIn(min = 80.dp), textAlign = TextAlign.End)
            IconButton(onClick = onHapus, modifier = Modifier.size(32.dp)) {
                Text("×", fontSize = 18.sp, color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
fun KasirRingkasan(
    kasList: List<Kas>, kasId: String,
    jumlahBayar: String, totalBelanja: Double, kembalian: Double,
    onKasIdChange: (String) -> Unit, onJumlahBayarChange: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {

            if (kasList.isNotEmpty()) {
                Text("Pilih Kas", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(kasList) { kas ->
                        val selected = kas.id == kasId
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { kas.id?.let { onKasIdChange(it) } }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(kas.nama, fontSize = 13.sp, color = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            HorizontalDivider()
            Spacer(Modifier.height(10.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Total Belanja", fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f))
                Text(formatRupiah(totalBelanja), fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value         = jumlahBayar,
                onValueChange = onJumlahBayarChange,
                label         = { Text("Jumlah Bayar") },
                prefix        = { Text("Rp ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )
            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (kembalian >= 0) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kembalian", fontWeight = FontWeight.Medium, color = if (kembalian >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer)
                Text(formatRupiah(kembalian), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = if (kembalian >= 0) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onErrorContainer)
            }
        }
    }
}

@Composable
fun StrukSukses(
    state: KasirUiState.Success,
    keranjang: List<CartItem>,
    kasList: List<Kas>,
    kasId: String,
    onTransaksiBaru: () -> Unit,
    onKembali: () -> Unit
) {
    val kasTerpilih = kasList.find { it.id == kasId }?.nama ?: "Umum"

    Column(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(72.dp).clip(RoundedCornerShape(36.dp)).background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) { Text("✓", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary) }

        Spacer(Modifier.height(16.dp))
        Text("Transaksi Berhasil!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("ID: ${state.penjualanId}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), textAlign = TextAlign.Center)
        Text("Kas: $kasTerpilih", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(20.dp))
        Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("RINCIAN ITEM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                Spacer(Modifier.height(8.dp))
                keranjang.forEach { item ->
                    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${item.namaProduk} x${item.qty}", fontSize = 13.sp, modifier = Modifier.weight(1f))
                        Text(formatRupiah(item.subtotal), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
                Spacer(Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(Modifier.height(12.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Total Belanja")
                    Text(formatRupiah(state.total), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Jumlah Bayar")
                    Text(formatRupiah(state.jumlahBayar), fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(8.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Kembalian", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text(formatRupiah(state.kembalian), fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
        Button(onClick = onTransaksiBaru, modifier = Modifier.fillMaxWidth().height(48.dp), shape = RoundedCornerShape(12.dp)) {
            Text("Transaksi Baru", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(Modifier.height(8.dp))
        TextButton(onClick = onKembali, modifier = Modifier.fillMaxWidth()) {
            Text("Kembali ke Dashboard")
        }
    }
}