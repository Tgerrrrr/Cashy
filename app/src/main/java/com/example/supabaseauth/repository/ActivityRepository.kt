package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.*
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.json.*
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ActivityRepository {

    private val client = SupabaseClientProvider.client

    private val inputFmt = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val outputFmt = DateTimeFormatter.ofPattern("d/MM/yy'\n'HH:mm")

    private fun formatTs(raw: String?): String {
        if (raw == null) return "-"
        return try {
            OffsetDateTime.parse(raw, inputFmt).format(outputFmt)
        } catch (_: Exception) {
            raw
        }
    }

    // ─────────────────────────────────────────
    // INVENTORY LABEL
    // ─────────────────────────────────────────
    private fun keteranganToTitle(keterangan: String, perubahan: Double): String {
        val sign = if (perubahan >= 0) "+${perubahan.toInt()}" else perubahan.toInt().toString()

        return when (keterangan) {
            "tambah_manual" -> "STOCK ADDED ($sign)"
            "edit_stok" -> "STOCK EDITED ($sign)"
            "terjual" -> "STOCK SOLD ($sign)"
            "pembatalan" -> "SALE CANCELLED ($sign)"
            else -> keterangan.uppercase()
        }
    }

    // ─────────────────────────────────────────
    // INVENTORY ACTIVITY
    // ─────────────────────────────────────────
    suspend fun getInventoryActivities(): List<ActivityEntryUI> {

        val logs = client
            .from("inventory_log")
            .select {
                order("created_at", Order.DESCENDING)
                limit(50)
            }
            .decodeList<InventoryLog>()

        val products = client
            .from("product")
            .select()
            .decodeList<Product>()
            .associateBy { it.id }

        return logs.map { log ->
            val produk = products[log.produkId]

            ActivityEntryUI(
                id = log.id ?: "",
                title = keteranganToTitle(log.keterangan, log.perubahan),
                details = listOfNotNull(
                    produk?.nama?.let { "Product: $it" },
                    "Stock after: ${log.stokAkhir.toInt()}",
                    log.referensiId?.let { "Ref: ${it.take(8)}" }
                ),
                timestamp = formatTs(log.createdAt),
                category = "Inventory"
            )
        }
    }

    // ─────────────────────────────────────────
    // SALES ACTIVITY
    // ─────────────────────────────────────────
    suspend fun getSalesActivities(): List<ActivityEntryUI> {

        val penjualan = client
            .from("penjualan")
            .select {
                order("waktu", Order.DESCENDING)
                limit(50)
            }
            .decodeList<Penjualan>()

        return penjualan.map { p ->

            val status = when (p.status) {
                "selesai" -> "Completed"
                "batal" -> "Cancelled"
                else -> p.status
            }

            ActivityEntryUI(
                id = p.id ?: "",
                title = "Sale $status",
                details = listOf(
                    "Order #${(p.id ?: "").take(8)}",
                    "Total: Rp ${p.total.toLong()}",
                    "Paid: Rp ${p.jumlah_bayar.toLong()}"
                ),
                timestamp = formatTs(p.waktu),
                category = "Sales"
            )
        }
    }

    // ─────────────────────────────────────────
    // SYSTEM / CUSTOMER ACTIVITY (FIXED)
    // ─────────────────────────────────────────
    suspend fun getSystemActivities(): List<ActivityEntryUI> {

        val logs = client
            .from("pelanggan_log")
            .select()
            .decodeList<PelangganLog>()

        return logs.map { log ->

            val aksi = log.aksi.jsonObject

            val tipe = aksi["tipe"]?.jsonPrimitive?.content ?: "unknown"
            val keterangan = aksi["keterangan"]?.jsonPrimitive?.content ?: "-"

            // FIX: handle both "data_baru" and typo "data baru"
            val dataBaru =
                aksi["data_baru"]?.jsonObject
                    ?: aksi["data baru"]?.jsonObject

            val dataLama = aksi["data_lama"]?.jsonObject

            val title = when (tipe) {
                "daftar" -> "Customer Registered"
                "ubah_profil" -> "Customer Profile Updated"
                "ubah_status" -> "Customer Status Changed"
                else -> "Customer Activity"
            }

            ActivityEntryUI(
                id = log.id ?: "",
                title = title,
                details = listOfNotNull(
                    keterangan,
                    dataBaru?.get("nama")?.jsonPrimitive?.content?.let { "Name: $it" },
                    dataBaru?.get("no_telp")?.jsonPrimitive?.content?.let { "Phone: $it" },
                    dataBaru?.get("is_active")?.jsonPrimitive?.content?.let { "Active: $it" },
                    dataLama?.get("nama")?.jsonPrimitive?.content?.let { "Old Name: $it" }
                ),
                timestamp = formatTs(log.created_at),
                category = "System"
            )
        }
    }

    // ─────────────────────────────────────────
    // COMBINED FEED
    // ─────────────────────────────────────────
    suspend fun getAllActivities(): List<ActivityEntryUI> {
        return (
                getInventoryActivities() +
                        getSalesActivities() +
                        getSystemActivities()
                ).sortedByDescending { it.timestamp }
    }
}