package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.model.Penjualan
import com.example.supabaseauth.model.PenjualanItem
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ── Insert models ─────────────────────────────────────────────────────────────

@Serializable
data class PenjualanInsert(
    @SerialName("kas_id")       val kasId: String,
    @SerialName("pelanggan_id") val pelangganId: String? = null,
    val total: Double,
    @SerialName("jumlah_bayar") val jumlahBayar: Double,
    val status: String = "selesai"
)

@Serializable
data class PenjualanItemInsert(
    @SerialName("penjualan_id") val penjualanId: String,
    @SerialName("produk_id")    val produkId: String,
    @SerialName("nama_produk")  val namaProduk: String,
    @SerialName("harga_satuan") val hargaSatuan: Double,
    val qty: Double,
    val subtotal: Double
)

@Serializable
data class PenjualanResponse(val id: String)

// =============================================================================
// BARANG (Product) REPOSITORY — cashier read-only
// =============================================================================

class BarangRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun getAllBarang(): List<Product> {
        return supabase
            .from("product")
            .select()
            .decodeList<Product>()
    }
}

// =============================================================================
// KAS REPOSITORY — cashier read-only
// =============================================================================

class KasRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun getAllKas(): List<Kas> {
        return supabase
            .from("kas")
            .select()
            .decodeList<Kas>()
    }
}

// =============================================================================
// KASIR REPOSITORY — checkout / insert transactions
// =============================================================================

class KasirRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun insertPenjualan(data: PenjualanInsert): String {
        val result = supabase
            .from("penjualan")
            .insert(data) { select() }
            .decodeSingle<PenjualanResponse>()
        return result.id
    }

    suspend fun insertPenjualanItems(items: List<PenjualanItemInsert>) {
        supabase.from("penjualan_item").insert(items)
    }

    /**
     * Decrements stock directly via PostgREST update — no RPC required.
     * Reads current stok first, then writes (currentStok - qty) back.
     */
    suspend fun kurangiStokProduk(produkId: String, qtyTerjual: Int) {
        // 1. Read current stock
        val current = supabase
            .from("product")
            .select {
                filter { eq("id", produkId) }
                limit(1)
            }
            .decodeSingleOrNull<Product>() ?: return

        // 2. Write new stock value (never goes below 0)
        val newStok = maxOf(0.0, current.stok - qtyTerjual)
        supabase
            .from("product")
            .update({
                set("stok", newStok)
            }) {
                filter { eq("id", produkId) }
            }
    }
}