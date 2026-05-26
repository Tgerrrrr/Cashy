package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.Profile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns

class AuthRepository {

    private val client = SupabaseClientProvider.client

    // =========================================
    // LOGIN
    // =========================================
    suspend fun login(
        email: String,
        password: String
    ): Result<Profile> {

        return try {

            // LOGIN
            client.auth.signInWith(Email) {

                this.email = email
                this.password = password
            }

            // GET USER ID
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(
                    Exception("User tidak ditemukan")
                )

            // GET PROFILE
            val profile = client
                .from("profiles")
                .select(
                    Columns.list(
                        "id",
                        "nama",
                        "role"
                    )
                ) {

                    filter {

                        eq("id", userId)
                    }
                }
                .decodeSingle<Profile>()

            Result.success(profile)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    // =========================================
    // REGISTER
    // =========================================
    suspend fun register(
        email: String,
        password: String,
        nama: String,
        role: String
    ): Result<Unit> {

        return try {

            client.auth.signUpWith(Email) {

                this.email = email
                this.password = password
            }

            // GET USER ID
            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(
                    Exception("Register gagal")
                )

            // INSERT PROFILE
            client
                .from("profiles")
                .insert(

                    Profile(
                        id = userId,
                        nama = nama,
                        role = role
                    )
                )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
}