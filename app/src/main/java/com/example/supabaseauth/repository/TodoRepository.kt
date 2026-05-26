package com.example.supabaseauth.repository

import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.model.TodoInsert
import com.example.supabaseauth.model.TodoUpdate
import com.example.supabaseauth.model.TodoWithProfile
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc

class TodoRepository {

    private val supabase = SupabaseClientProvider.client

    /*
     * Mengambil id user yang sedang login.
     * Id ini dibutuhkan saat insert todo baru.
     */
    private fun getCurrentUserId(): String {
        return supabase.auth.currentUserOrNull()?.id
            ?: throw Exception("User belum login")
    }

    /*
     * Mengambil todo menggunakan RPC/function yang sudah dibuat di Supabase.
     * Function yang dipanggil:
     * get_my_todos_with_profile()
     */
    suspend fun getTodosWithProfile(): List<TodoWithProfile> {
        return supabase
            .postgrest
            .rpc("get_my_todos_with_profile")
            .decodeList<TodoWithProfile>()
    }

    /*
     * Menambahkan todo baru ke tabel todos.
     * user_id diambil dari user yang sedang login.
     */
    suspend fun addTodo(title: String) {

        val userId = getCurrentUserId()

        val newTodo = TodoInsert(
            userId = userId,
            title = title,
            isDone = false
        )

        supabase
            .from("todos")
            .insert(newTodo)
    }

    /*
     * Mengubah status todo.
     */
    suspend fun updateTodoStatus(
        todoId: String,
        isDone: Boolean
    ) {

        supabase
            .from("todos")
            .update(
                {
                    set("is_done", isDone)
                }
            ) {
                filter {
                    eq("id", todoId)
                }
            }
    }

    /*
     * Menghapus todo berdasarkan id.
     */
    suspend fun deleteTodo(todoId: String) {

        supabase
            .from("todos")
            .delete {
                filter {
                    eq("id", todoId)
                }
            }
    }
}