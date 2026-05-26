package com.example.supabaseauth.model

import kotlinx.serialization.Serializable

@Serializable
data class Produk(

    val id: String? = null,

    val nama: String = "",

    val harga: Double = 0.0,

    val stok: Double = 0.0,

    val is_active: Boolean = true
)