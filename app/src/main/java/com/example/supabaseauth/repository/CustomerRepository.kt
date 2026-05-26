package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Pelanggan
import io.github.jan.supabase.postgrest.from

class CustomerRepository {

    private val client = SupabaseClientProvider.client

    suspend fun getCustomers(): List<Pelanggan> {
        return client
            .from("pelanggan")
            .select()
            .decodeList()
    }

    suspend fun addCustomer(customer: Pelanggan) {
        client.from("pelanggan").insert(customer)
    }

    suspend fun updateCustomer(customer: Pelanggan) {
        client
            .from("pelanggan")
            .update(customer) {
                filter {
                    eq("id", customer.id!!)
                }
            }
    }

    suspend fun deleteCustomer(id: String) {
        client
            .from("pelanggan")
            .delete {
                filter {
                    eq("id", id)
                }
            }
    }
}