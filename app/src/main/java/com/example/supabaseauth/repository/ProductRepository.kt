package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Product
import io.github.jan.supabase.postgrest.from

class ProductRepository {

    private val client =
        SupabaseClientProvider.client

    /* ================= GET ALL PRODUCTS ================= */

    suspend fun getProducts(): List<Product> {

        return client
            .from("product")
            .select()
            .decodeList<Product>()
    }

    /* ================= INSERT PRODUCT ================= */

    suspend fun addProduct(
        product: Product
    ) {

        client
            .from("product")
            .insert(product)
    }

    /* ================= UPDATE PRODUCT ================= */

    suspend fun updateProduct(
        product: Product
    ) {

        client
            .from("product")
            .update(
                {
                    set("nama", product.nama)
                    set("harga", product.harga)
                    set("stok", product.stok)
                    set("is_active", product.is_active)
                }
            ) {

                filter {

                    eq(
                        "id",
                        product.id!!
                    )
                }
            }
    }

    /* ================= DELETE PRODUCT ================= */

    suspend fun deleteProduct(
        id: String
    ) {

        client
            .from("product")
            .delete {

                filter {

                    eq(
                        "id",
                        id
                    )
                }
            }
    }

    /* ================= GET PRODUCT BY ID ================= */

    suspend fun getProductById(
        id: String
    ): Product? {

        return client
            .from("product")
            .select {

                filter {

                    eq(
                        "id",
                        id
                    )
                }

                limit(1)
            }
            .decodeSingleOrNull<Product>()
    }
}