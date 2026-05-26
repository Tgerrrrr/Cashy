package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Penjualan
import com.example.supabaseauth.model.PenjualanItem
import io.github.jan.supabase.postgrest.from

class SalesRepository {

    private val client = SupabaseClientProvider.client

    suspend fun createSale(sale: Penjualan) {
        client.from("penjualan").insert(sale)
    }

    suspend fun addSaleItem(item: PenjualanItem) {
        client.from("penjualan_item").insert(item)
    }

    suspend fun getSales(): List<Penjualan> {
        return client
            .from("penjualan")
            .select()
            .decodeList()
    }
}