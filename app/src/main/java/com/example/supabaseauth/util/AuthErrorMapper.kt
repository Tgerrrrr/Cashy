package com.example.supabaseauth.util

object AuthErrorMapper {

    fun map(message: String?): String {

        val msg = message?.lowercase() ?: ""

        return when {

            msg.contains("invalid login credentials") ->
                "Incorrect email or password."

            msg.contains("otp") || msg.contains("nonce") || msg.contains("token") ->
                "Verification code is invalid or expired."

            msg.contains("session missing") || msg.contains("no session") || msg.contains("belum login") ->
                "Session expired. Please login again."

            msg.contains("weak password") || msg.contains("password") ->
                message ?: "Password is too weak."

//            msg.contains("user already registered") ->
//                "This email is already registered."

            msg.contains("email not confirmed") ->
                "Please verify your email before logging in."

            msg.contains("network") ->
                "Network error. Check your internet connection."

            msg.contains("jwt") ->
                "Session expired. Please login again."

//            msg.contains("duplicate") ->
//                "Account already exists."

            msg.isBlank() ->
                "Unknown error occurred."

            else ->
                message ?: "Something went wrong. Please try again."
        }
    }
}
