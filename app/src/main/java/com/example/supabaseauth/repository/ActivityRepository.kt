package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.model.InventoryLog
import com.example.supabaseauth.model.Product
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

class ActivityRepository {

    private val client = SupabaseClientProvider.client

    // ─────────────────────────────────────────
    // Output formatter  →  "19/05/26\n14:30"
    // ─────────────────────────────────────────
    private val inputFmt  = DateTimeFormatter.ISO_OFFSET_DATE_TIME
    private val outputFmt = DateTimeFormatter.ofPattern("d/MM/yy'\n'HH:mm")

    private fun formatTs(raw: String?): String {
        if (raw == null) return "-"
        return try {
            OffsetDateTime.parse(raw, inputFmt).format(outputFmt)
        } catch (_: Exception) { raw }
    }

    // ─────────────────────────────────────────
    // Keterangan  →  human-readable title
    // ─────────────────────────────────────────
    private fun keteranganToTitle(keterangan: String, perubahan: Double): String {
        val sign  = if (perubahan >= 0) "+${perubahan.toInt()}" else "${perubahan.toInt()}"
        return when (keterangan) {
            "tambah_manual" -> "STOCK ADDED ($sign)"
            "edit_stok"     -> "STOCK EDITED ($sign)"
            "terjual"       -> "STOCK SOLD ($sign)"
            "pembatalan"    -> "SALE CANCELLED ($sign)"
            else            -> keterangan.uppercase()
        }
    }

    // ─────────────────────────────────────────
    // FETCH INVENTORY LOGS (category = Inventory)
    // ─────────────────────────────────────────
    suspend fun getInventoryActivities(): List<ActivityEntryUI> {

        val logs: List<InventoryLog> = client
            .from("inventory_log")
            .select {
                order(column = "created_at", order = Order.DESCENDING)
                limit(50)
            }
            .decodeList()

        // Fetch product names in one call, build a lookup map
        val products: List<Product> = client
            .from("product")
            .select()
            .decodeList()
        val productMap = products.associateBy { it.id }

        return logs.map { log ->
            val produk = productMap[log.produkId]

            ActivityEntryUI(
                id        = log.id ?: "",
                title     = keteranganToTitle(log.keterangan, log.perubahan),
                details   = listOfNotNull(
                    produk?.let { "Product: ${it.nama}" },
                    "Stock after: ${log.stokAkhir.toInt()}",
                    log.referensiId?.let { "Ref: ${it.take(8)}" }
                ),
                timestamp = formatTs(log.createdAt),
                category  = "Inventory"
            )
        }
    }

    // ─────────────────────────────────────────
    // FETCH PENJUALAN LOGS (category = Sales)
    // Reuses penjualan as a proxy for system/sales activity
    // ─────────────────────────────────────────
    suspend fun getSalesActivities(): List<ActivityEntryUI> {

        val penjualan = client
            .from("penjualan")
            .select {
                order(column = "waktu", order = Order.DESCENDING)
                limit(50)
            }
            .decodeList<com.example.supabaseauth.model.Penjualan>()

        return penjualan.map { p ->
            val statusLabel = when (p.status) {
                "selesai"  -> "Completed"
                "batal"    -> "Cancelled"
                else       -> p.status
            }

            ActivityEntryUI(
                id        = p.id ?: "",
                title     = "Sale $statusLabel",
                details   = listOf(
                    "Order #${(p.id ?: "").take(8)}",
                    "Total: Rp ${p.total.toLong()}",
                    "Paid: Rp ${p.jumlah_bayar.toLong()}"
                ),
                timestamp = formatTs(p.waktu),
                category  = "Sales"
            )
        }
    }

    // ─────────────────────────────────────────
    // ALL combined + sorted newest first
    // ─────────────────────────────────────────
    suspend fun getAllActivities(): List<ActivityEntryUI> {
        val inventory = getInventoryActivities()
        val sales     = getSalesActivities()
        return (inventory + sales).sortedByDescending { it.id }
    }
    suspend fun getSystemActivities(): List<ActivityEntryUI> {
        return listOf(
            ActivityEntryUI(
                id = "sys-1",
                title = "LOGIN DETECTED",
                details = listOf(
                    "Cashier: Admin",
                    "Device: Android",
                    "Location: Local"
                ),
                timestamp = "now",
                category = "System"
            )
        )
    }
}