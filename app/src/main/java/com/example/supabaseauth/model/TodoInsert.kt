package com.example.supabaseauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoInsert(

    @SerialName("user_id")
    val userId: String,

    val title: String,

    @SerialName("is_done")
    val isDone: Boolean = false
)