package com.example.supabaseauth.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class TodoWithProfile(

    val id: String,

    @SerialName("nama_profil")
    val namaProfil: String,

    @SerialName("judul_todo")
    val judulTodo: String,

    @SerialName("is_done")
    val isDone: Boolean,

    val status: String,

    @SerialName("created_at")
    val createdAt: String
)