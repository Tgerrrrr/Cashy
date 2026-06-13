package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Pengeluaran
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from

class ExpenseRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getExpenses(): List<Pengeluaran> {
        return client
            .from("pengeluaran")
            .select()
            .decodeList()
    }

    suspend fun addExpense(expense: Pengeluaran) {
        client.from("pengeluaran").insert(expense)
    }

    suspend fun updateExpense(expense: Pengeluaran) {
        client
            .from("pengeluaran")
            .update(expense) {
                filter {
                    eq("id", expense.id!!)
                }
            }
    }
}