package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.data.SupabaseClientProvider.client
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.SupabaseClient


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