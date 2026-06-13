package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Kas
import com.example.supabaseauth.model.KasLog
import com.example.supabaseauth.model.Pelanggan
import com.example.supabaseauth.model.Penjualan
import io.github.jan.supabase.postgrest.from
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class TransactionRepository {

    private val client =
        SupabaseClientProvider.client

    suspend fun getPenjualan(): List<Penjualan> {

        return client
            .from("penjualan")
            .select()
            .decodeList()
    }

    suspend fun getPelanggan(): List<Pelanggan> {

        return client
            .from("pelanggan")
            .select()
            .decodeList()
    }

    suspend fun getKas(): List<Kas> {

        return client
            .from("kas")
            .select()
            .decodeList()
    }

    suspend fun getKasLogs(): List<KasLog> {

        return client
            .from("kas_log")
            .select()
            .decodeList()
    }

    suspend fun addIncome(
        kasId: String,
        saldoAwal: Double,
        amount: Double,
        desc: String
    ) {

        val newSaldo =
            saldoAwal + amount

        client
            .from("kas_log")
            .insert(
                buildJsonObject {

                    put(
                        "kas_id",
                        kasId
                    )

                    put(
                        "perubahan",
                        amount
                    )

                    put(
                        "saldo_akhir",
                        newSaldo
                    )

                    put(
                        "keterangan",
                        if (desc.isBlank())
                            "Manual income"
                        else
                            desc
                    )

                    put(
                        "sumber",
                        "manual"
                    )
                }
            )

        client
            .from("kas")
            .update(
                buildJsonObject {

                    put(
                        "saldo",
                        newSaldo
                    )
                }
            ) {
                filter {
                    eq(
                        "id",
                        kasId
                    )
                }
            }
    }
}