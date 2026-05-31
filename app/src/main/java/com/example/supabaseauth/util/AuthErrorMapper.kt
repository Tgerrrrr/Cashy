package com.example.supabaseauth.util

object AuthErrorMapper {

    fun map(message: String?): String {

        val msg = message?.lowercase() ?: ""

        return when {

            msg.contains("invalid login credentials") ->
                "Incorrect email or password."

            msg.contains("user already registered") ->
                "This email is already registered."

            msg.contains("email not confirmed") ->
                "Please verify your email before logging in."

            msg.contains("network") ->
                "Network error. Check your internet connection."

            msg.contains("jwt") ->
                "Session expired. Please login again."

            msg.contains("duplicate") ->
                "Account already exists."

            msg.isBlank() ->
                "Unknown error occurred."

            else ->
                "Something went wrong. Please try again."
        }
    }
}