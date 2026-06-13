package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.data.SupabaseClientProvider.client
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.SupabaseClient
import com.example.supabaseauth.model.KasLog



class CashRepository {

    // =========================================
    // SUPABASE CLIENT
    // =========================================
    private val client = SupabaseClientProvider.client

    // =========================================
    // GET ALL CASH
    // =========================================
    suspend fun getAllCash(): List<Kas> {

        return client
            .from("kas")
            .select {

                order(
                    column = "created_at",
                    order = Order.ASCENDING
                )
            }
            .decodeList<Kas>()
    }

    // =========================================
    // GET CASH BY ID
    // =========================================
    suspend fun getCashById(
        kasId: String
    ): Kas? {

        return client
            .from("kas")
            .select {

                filter {

                    eq("id", kasId)
                }

                limit(1)
            }
            .decodeList<Kas>()
            .firstOrNull()
    }

    // =========================================
    // ADD CASH
    // =========================================
    suspend fun addCash(
        kas: Kas
    ) {

        client
            .from("kas")
            .insert(kas)
    }

    // =========================================
    // UPDATE CASH
    // =========================================
    suspend fun updateCash(
        kas: Kas
    ) {

        client
            .from("kas")
            .update(kas) {

                filter {

                    eq("id", kas.id!!)
                }
            }
    }

    // =========================================
    // SET ACTIVE / NONAKTIFKAN
    // =========================================
    suspend fun setCashActive(
        id: String,
        isActive: Boolean
    ) {
        client
            .from("kas")
            .update(
                {
                    set("is_active", isActive)
                }
            ) {
                filter {
                    eq("id", id)
                }
            }
    }

    // =========================================
    // DELETE CASH
    // =========================================
    suspend fun deleteCash(
        id: String
    ) {

        client
            .from("kas")
            .delete {

                filter {

                    eq("id", id)
                }
            }
    }

    // =========================================
    // GET KAS LOG BY KAS ID
    // =========================================
    suspend fun getKasLog(
        kasId: String
    ): List<KasLog> {

        return client
            .from("kas_log")
            .select {

                filter {
                    eq("kas_id", kasId)
                }

                order(
                    column = "created_at",
                    order = Order.DESCENDING
                )
            }
            .decodeList<KasLog>()
    }
}
class TransactionRepository(private val client: SupabaseClient) {

    suspend fun getRecentTransactions(): List<Penjualan> {
        return client
            .from("penjualan")
            .select()
            .decodeList<Penjualan>()
            .sortedByDescending { it.waktu }
            .take(10)
    }
}