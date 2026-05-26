package com.example.supabaseauth.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.supabaseauth.model.TodoWithProfile
import com.example.supabaseauth.viewmodel.TodoUiState

@Composable
fun TodoScreen(
    title: String,
    todoUiState: TodoUiState,
    onTitleChange: (String) -> Unit,
    onAddClick: () -> Unit,
    onToggleClick: (TodoWithProfile) -> Unit,
    onDeleteClick: (TodoWithProfile) -> Unit,
    onLogoutClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "TodoList",
                style = MaterialTheme.typography.headlineMedium
            )

            TextButton(
                onClick = onLogoutClick
            ) {
                Text("Logout")
            }
        }

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        when (todoUiState) {

            is TodoUiState.Success -> {

                Text(
                    text = "Halo, ${
                        todoUiState
                            .todos
                            .firstOrNull()
                            ?.namaProfil
                            ?: "User"
                    }"
                )
            }

            else -> {

                Text(
                    text = "Todo milik user login"
                )
            }
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = {
                Text("Tambah todo")
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(
            modifier = Modifier.height(8.dp)
        )

        Button(
            onClick = onAddClick,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text("Tambah Todo")
        }

        Spacer(
            modifier = Modifier.height(16.dp)
        )

        when (todoUiState) {

            is TodoUiState.Loading -> {

                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }

            is TodoUiState.Error -> {

                Text(
                    text = todoUiState.message,
                    color = MaterialTheme.colorScheme.error
                )
            }

            is TodoUiState.Success -> {

                if (todoUiState.todos.isEmpty()) {

                    Text(
                        text = "Belum ada todo."
                    )

                } else {

                    LazyColumn {

                        items(todoUiState.todos) { todo ->

                            TodoItem(
                                todo = todo,
                                onToggleClick = {
                                    onToggleClick(todo)
                                },
                                onDeleteClick = {
                                    onDeleteClick(todo)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TodoItem(
    todo: TodoWithProfile,
    onToggleClick: () -> Unit,
    onDeleteClick: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = todo.isDone,
                onCheckedChange = {
                    onToggleClick()
                }
            )

            Spacer(
                modifier = Modifier.width(8.dp)
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {

                Text(
                    text = todo.judulTodo,
                    textDecoration =
                        if (todo.isDone) {
                            TextDecoration.LineThrough
                        } else {
                            TextDecoration.None
                        }
                )

                Text(
                    text = todo.status,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            TextButton(
                onClick = onDeleteClick
            ) {

                Text("Hapus")
            }
        }
    }
}