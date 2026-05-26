package com.example.supabaseauth.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.supabaseauth.data.SupabaseClientProvider
import com.example.supabaseauth.repository.TodoRepository
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TodoViewModel : ViewModel() {

    private val repository = TodoRepository()

    private val supabase =
        SupabaseClientProvider.client

    private var realtimeJob: Job? = null

    /*
     * State untuk daftar todo.
     */
    private val _todoUiState =
        MutableStateFlow<TodoUiState>(
            TodoUiState.Loading
        )

    val todoUiState: StateFlow<TodoUiState> =
        _todoUiState

    /*
     * State untuk input judul todo.
     */
    private val _title = MutableStateFlow("")

    val title: StateFlow<String> = _title

    /*
     * Dipanggil ketika user mengetik judul todo.
     */
    fun onTitleChange(value: String) {
        _title.value = value
    }

    /*
     * Mengambil data todo menggunakan RPC.
     */
    fun loadTodos() {

        viewModelScope.launch {

            try {

                _todoUiState.value =
                    TodoUiState.Loading

                val todos =
                    repository.getTodosWithProfile()

                _todoUiState.value =
                    TodoUiState.Success(todos)

            } catch (e: Exception) {

                _todoUiState.value =
                    TodoUiState.Error(
                        e.message
                            ?: "Gagal mengambil data todo"
                    )
            }
        }
    }

    /*
     * Menambahkan todo baru.
     */
    fun addTodo() {

        val currentTitle =
            _title.value.trim()

        if (currentTitle.isEmpty()) {

            _todoUiState.value =
                TodoUiState.Error(
                    "Judul todo tidak boleh kosong"
                )

            return
        }

        viewModelScope.launch {

            try {

                repository.addTodo(currentTitle)

                /*
                 * Kosongkan input setelah berhasil tambah.
                 */
                _title.value = ""

                /*
                 * Refresh data.
                 */
                loadTodos()

            } catch (e: Exception) {

                _todoUiState.value =
                    TodoUiState.Error(
                        e.message
                            ?: "Gagal menambahkan todo"
                    )
            }
        }
    }

    /*
     * Mengubah status todo.
     */
    fun toggleTodo(
        todoId: String,
        currentStatus: Boolean
    ) {

        viewModelScope.launch {

            try {

                repository.updateTodoStatus(
                    todoId = todoId,
                    isDone = !currentStatus
                )

                loadTodos()

            } catch (e: Exception) {

                _todoUiState.value =
                    TodoUiState.Error(
                        e.message
                            ?: "Gagal mengubah status todo"
                    )
            }
        }
    }

    /*
     * Menghapus todo.
     */
    fun deleteTodo(todoId: String) {

        viewModelScope.launch {

            try {

                repository.deleteTodo(todoId)

                loadTodos()

            } catch (e: Exception) {

                _todoUiState.value =
                    TodoUiState.Error(
                        e.message
                            ?: "Gagal menghapus todo"
                    )
            }
        }
    }

    /*
     * Mengaktifkan realtime listener.
     */
    fun startRealtimeTodos() {

        if (realtimeJob != null) return

        realtimeJob = viewModelScope.launch {

            try {

                val channel =
                    supabase.channel("todos-changes")

                val changeFlow =
                    channel.postgresChangeFlow<PostgresAction>(
                        schema = "public"
                    ) {
                        table = "todos"
                    }

                /*
                 * Subscribe realtime.
                 */
                channel.subscribe()

                /*
                 * Reload ketika ada perubahan.
                 */
                changeFlow.collect {

                    loadTodos()
                }

            } catch (e: Exception) {

                _todoUiState.value =
                    TodoUiState.Error(
                        e.message
                            ?: "Gagal menjalankan realtime"
                    )
            }
        }
    }

    /*
     * Hentikan realtime saat ViewModel dihancurkan.
     */
    override fun onCleared() {

        super.onCleared()

        realtimeJob?.cancel()
    }
}