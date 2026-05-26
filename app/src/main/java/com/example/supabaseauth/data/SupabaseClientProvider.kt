package com.example.supabaseauth.data


import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime


object SupabaseClientProvider {
    val client = createSupabaseClient(
        supabaseUrl = "https://pajgjncyvsbkwwmajtaf.supabase.co",
        supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InBhamdqbmN5dnNia3d3bWFqdGFmIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NzI2MDM2NTIsImV4cCI6MjA4ODE3OTY1Mn0.eEq-17OW50Rc_FEMH7uPw98-ttEWcJTN4ZenGjsFR0c"
    ) {
        install(Auth)

        install(Postgrest)

        install(Realtime)
    }

    //val realtimeManager = RealtimeManager(client)
}