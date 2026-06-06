package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.model.Penjualan
import com.example.supabaseauth.model.PenjualanItem
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// ── Insert models (only what we need to write) ────────────────────────────────

@Serializable
data class PenjualanInsert(
    @SerialName("kas_id")      val kasId: String,
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

    // Your table is "product" (not "produk")
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

    // Uses the Supabase stored procedure — see SQL below
    suspend fun kurangiStokProduk(produkId: String, qtyTerjual: Int) {
        supabase.postgrest.rpc(
            function   = "kurangi_stok_produk",
            parameters = buildJsonObject {
                put("p_produk_id", produkId)
                put("p_qty", qtyTerjual)
            }
        )
    }
}