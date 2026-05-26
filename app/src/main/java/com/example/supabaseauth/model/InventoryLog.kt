package com.example.supabaseauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// Maps to the inventory_log table
@Serializable
data class InventoryLog(
    val id: String? = null,
    @SerialName("produk_id") val produkId: String,
    val perubahan: Double,
    @SerialName("stok_akhir") val stokAkhir: Double,
    val keterangan: String,                  // tambah_manual | edit_stok | terjual | pembatalan
    @SerialName("referensi_id") val referensiId: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

// Unified UI model used by HistoryScreen
data class ActivityEntryUI(
    val id: String,
    val title: String,
    val details: List<String>,
    val timestamp: String,
    // Inventory | Sales | System
    val category: String
)