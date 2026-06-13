package com.example.supabaseauth.ui

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.viewmodel.ExpenseViewModel

@Composable
fun ExpenseScreen(
    viewModel: ExpenseViewModel = viewModel()
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    when {

        state.isLoading -> {
            CircularProgressIndicator()
        }

        state.error != null -> {
            Text(
                text = "Error: ${state.error}"
            )
        }

        else -> {

            LazyColumn {

                items(state.expenses) { expense ->

                    Text(
                        text = expense.toString()
                    )

                }
            }
        }
    }
}