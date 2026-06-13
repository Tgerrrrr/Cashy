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

            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("User tidak ditemukan"))

            val profile = client
                .from("profiles")
                .select(Columns.list("id", "nama", "email", "role")) {
                    filter { eq("id", userId) }
                }
                .decodeList<Profile>()
                .firstOrNull()
                ?: return Result.failure(Exception("Profile tidak ditemukan"))

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

            val adminSession = client.auth.currentSessionOrNull()

            val signUpResult = try {
                client.auth.signUpWith(Email) {
                    this.email = email
                    this.password = password
                }
            } catch (e: Exception) {
                if (e.message?.contains("already", ignoreCase = true) == true ||
                    e.message?.contains("registered", ignoreCase = true) == true) {
                    null
                } else {
                    throw e
                }
            }

            val userId = signUpResult?.id
                ?: client.auth.currentUserOrNull()?.id
                ?: return Result.failure(Exception("Register gagal: tidak bisa mendapatkan user id"))

            // Insert profile
            try {
                client.from("profiles").insert(
                    Profile(
                        id    = userId,
                        nama  = nama,
                        email = email,
                        role  = role
                    )
                )
            } catch (e: Exception) {
                if (e.message?.contains("duplicate", ignoreCase = true) == true ||
                    e.message?.contains("already exists", ignoreCase = true) == true) {
                    client.from("profiles")
                        .update(mapOf("nama" to nama, "role" to role)) {
                            filter { eq("id", userId) }
                        }
                } else {
                    throw e
                }
            }

            // Restore admin session
            if (adminSession != null) {
                client.auth.importSession(adminSession)
            } else {
                client.auth.clearSession()
            }

            Result.success(Unit)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    // =========================================
    // UPDATE PROFILE
    // =========================================
    suspend fun updateProfile(userId: String, nama: String): Result<Unit> {
        return try {
            client.from("profiles")
                .update(mapOf("nama" to nama)) {
                    filter { eq("id", userId) }
                }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}