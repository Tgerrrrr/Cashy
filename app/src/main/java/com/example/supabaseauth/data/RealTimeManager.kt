//package com.example.supabaseauth.data
//
//import io.github.jan.supabase.SupabaseClient
//import io.github.jan.supabase.realtime.PostgresAction
//import io.github.jan.supabase.realtime.channel
//import io.github.jan.supabase.realtime.postgresChangeFlow
//import kotlinx.coroutines.CoroutineScope
//import kotlinx.coroutines.Dispatchers
//import kotlinx.coroutines.flow.Flow
//import kotlinx.coroutines.launch
//
//class RealtimeManager(
//    private val client: SupabaseClient
//) {
//
//    private val channel = client.channel("global-realtime")
//
//    init {
//
//        CoroutineScope(Dispatchers.IO).launch {
//
//            channel.subscribe()
//        }
//    }
//
//    fun observeTable(
//        tableName: String
//    ): Flow<PostgresAction> {
//
//        return channel.postgresChangeFlow(
//            schema = "public"
//        )
//    }
//}