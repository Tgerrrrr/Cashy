package com.example.supabaseauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoUpdate(

    @SerialName("is_done")
    val isDone: Boolean
)