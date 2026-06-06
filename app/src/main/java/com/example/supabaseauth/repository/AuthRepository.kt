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

            val profile = try {
                client
                    .from("profiles")
                    .select(Columns.list("id", "nama", "email", "role")) {
                        filter { eq("id", userId) }
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

                client.from("profiles").insert(fallbackProfile)

                fallbackProfile
            }

            Result.success(profile)

        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // =========================================
    // REGISTER
    // Used by admin to create cashier accounts.
    // IMPORTANT: After sign-up Supabase automatically signs in the new user,
    // which would kick the admin out. We save the admin's session, create the
    // account, then restore the admin session.
    // =========================================
    suspend fun register(
        email: String,
        password: String,
        nama: String,
        role: String
    ): Result<Unit> {

        return try {

            // 1. Save admin's current session token before creating new account
            val adminSession = client.auth.currentSessionOrNull()

            // 2. Sign up the new cashier account
            val user = client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }

            val userId = user?.id
                ?: return Result.failure(Exception("Register gagal: user id tidak ditemukan"))

            // 3. Insert profile for the new cashier
            client.from("profiles").insert(
                Profile(
                    id = userId,
                    nama = nama,
                    email = email,
                    role = role
                )
            )

            // 4. Restore admin session so admin stays logged in
            if (adminSession != null) {
                client.auth.importSession(adminSession)
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