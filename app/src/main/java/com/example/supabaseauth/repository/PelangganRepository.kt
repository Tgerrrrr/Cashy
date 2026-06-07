package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Pelanggan
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PelangganInsert(
    val nama: String,
    @SerialName("no_telp") val noTelp: String? = null,
    val is_active: Boolean = true
)

@Serializable
data class PelangganResponse(val id: String)

class PelangganRepository {
    private val supabase = SupabaseClientProvider.client

    suspend fun getAllPelanggan(): List<Pelanggan> {
        return supabase
            .from("pelanggan")
            .select {
                order(column = "nama", order = Order.ASCENDING)
            }
            .decodeList<Pelanggan>()
    }

    suspend fun searchPelanggan(query: String): List<Pelanggan> {
        return supabase
            .from("pelanggan")
            .select {
                filter {
                    or {
                        ilike("nama", "%$query%")
                        ilike("no_telp", "%$query%")
                    }
                }
            }
            .decodeList<Pelanggan>()
    }

    suspend fun addPelanggan(data: PelangganInsert): String {
        val result = supabase
            .from("pelanggan")
            .insert(data) { select() }
            .decodeSingle<PelangganResponse>()
        return result.id
    }
}