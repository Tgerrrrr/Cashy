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

            client.auth.clearSession()

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
            val profile = try {
                client
                    .from("profiles")
                    .select(
                        Columns.list(
                            "id",
                            "nama",
                            "email",
                            "role"
                        )
                    ) {

                        filter {

                            eq("id", userId)
                        }
                    }
                    .decodeSingle<Profile>()
            } catch (e: Exception) {
                val user = client.auth.currentUserOrNull()
                    ?: return Result.failure(Exception("User tidak ditemukan"))

                val fallbackProfile = Profile(
                    id = userId,
                    nama = user.email?.substringBefore("@"),
                    email = user.email ?: email,
                    role = "cashier"
                )

                client
                    .from("profiles")
                    .insert(fallbackProfile)

                fallbackProfile
            }

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

            client.auth.clearSession()

            val user = client.auth.signUpWith(Email) {

                this.email = email
                this.password = password
            }

            // GET USER ID
            val userId = user?.id ?: client.auth.currentUserOrNull()?.id
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
                        email = email,
                        role = role
                    )
                )

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }
    suspend fun updateProfile(userId: String, nama: String): Result<Unit> {
        return try {

            client.from("profiles")
                .update(
                    mapOf("nama" to nama)
                ) {
                    filter {
                        eq("id", userId)
                    }
                }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
