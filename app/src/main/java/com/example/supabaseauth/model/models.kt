package com.example.supabaseauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val id: String,
    val nama: String? = null,
    val email: String? = null,
    val role: String = "admin"
)

@Serializable
data class Kas(
    val id: String? = null,
    val nama: String,
    val saldo: Double,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Serializable
data class Pelanggan(
    val id: String? = null,
    val nama: String,
    val no_telp: String? = null,
    val is_active: Boolean = true,
    val created_at: String? = null
)

@Serializable
data class Product(
    val id: String? = null,
    val nama: String,
    val harga: Double,
    val stok: Double,
    val is_active: Boolean = true,
    val created_at: String? = null,
    val updated_at: String? = null
)

@Serializable
data class Pengeluaran(
    val id: String? = null,
    val kas_id: String,
    val tanggal: String,
    val deskripsi: String,
    val total: Double,
    val is_cancelled: Boolean = false
)

@Serializable

data class Penjualan(
    val id: String? = null,
    val pelanggan_id: String? = null,
    val kas_id: String,
    val waktu: String,
    val total: Double,
    val jumlah_bayar: Double,
    val status: String
)

@Serializable

data class PenjualanItem(
    val id: String? = null,
    val penjualan_id: String,
    val produk_id: String,
    val nama_produk: String,
    val harga_satuan: Double,
    val qty: Double,
    val subtotal: Double
)
data class PenjualanUI(
    val id: String,
    val pelangganNama: String,
    val waktu: String,
    val total: Double,
    val status: String,
    val description: String = ""
)
@Serializable
data class KasLog(
    val id: String = "",
    val kas_id: String = "",
    val perubahan: Double = 0.0,
    val saldo_akhir: Double = 0.0,
    val keterangan: String? = null,
    val sumber: String = "",
    val referensi_id: String? = null,
    val created_at: String = ""
)
@Serializable
data class PelangganLog(
    val id: String = "",
    val pelanggan_id: String = "",
    val aksi: kotlinx.serialization.json.JsonElement,
    val created_at: String = ""
)
