package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.InventoryLog
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.postgrest.from

class ActivityRepository {

    private val client =
        SupabaseClientProvider.client

    suspend fun getSales(): List<Penjualan> {
        return client
            .from("penjualan")
            .select()
            .decodeList()
    }

    suspend fun getInventoryLogs(): List<InventoryLog> {
        return client
            .from("inventory_log")
            .select()
            .decodeList()
    }
}