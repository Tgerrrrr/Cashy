package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.viewmodel.ActivityState
import com.example.supabaseauth.viewmodel.ActivityViewModel

@Composable
fun HistoryScreen(
    viewModel: ActivityViewModel = viewModel()
) {

    val state by viewModel.state.collectAsStateWithLifecycle()

    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("All") }

    val entries = when (state) {
        is ActivityState.Success -> (state as ActivityState.Success).entries
        else -> emptyList()
    }

    val filtered = remember(entries, searchQuery, selectedFilter) {
        entries.filter { entry ->

            val categorySafe = entry.category ?: ""

            val matchesFilter =
                selectedFilter == "All" ||
                        categorySafe.equals(selectedFilter, ignoreCase = true)

            val matchesSearch =
                searchQuery.isBlank() ||
                        entry.title.contains(searchQuery, ignoreCase = true) ||
                        entry.details.any { it.contains(searchQuery, ignoreCase = true) }

            matchesFilter && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
    ) {

        Spacer(Modifier.height(20.dp))

        Text(
            text = "Activity History",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(16.dp))

        /* SEARCH */
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(52.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFF2F3F2))
                .padding(horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, null, tint = Color(0xFF7C7C7C))

            Spacer(Modifier.width(10.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Activity") },
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        Spacer(Modifier.height(14.dp))

        /* FILTER */
        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(listOf("All", "Inventory", "Sales", "System")) { option ->
                ActivityFilterChip(
                    text = option,
                    selected = selectedFilter == option,
                    onClick = {
                        selectedFilter = option
                        viewModel.loadByCategory(option)
                    }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        when (state) {

            is ActivityState.Loading -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is ActivityState.Error -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = (state as ActivityState.Error).message,
                        color = Color.Red
                    )
                }
            }

            is ActivityState.Empty -> {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No activity found", color = Color.Gray)
                }
            }

            is ActivityState.Success -> {
                LazyColumn(
                    contentPadding = PaddingValues(
                        horizontal = 16.dp,
                        vertical = 4.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(filtered) { entry ->
                        ActivityCard(entry)
                    }
                }
            }

            else -> Unit
        }
    }
}

/* CARD */
@Composable
private fun ActivityCard(entry: ActivityEntryUI) {

    val isSystem = entry.category.equals("System", true)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {

        // ICON
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(
                    if (isSystem) Color(0xFFFFF3E0)
                    else Color(0xFFE6F3F6)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = if (isSystem) Color(0xFFF57C00) else Color(0xFF006C86)
            )
        }

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {

            // TITLE (clean, single line)
            Text(
                text = entry.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF1E1E1E),
                maxLines = 1
            )

            Spacer(Modifier.height(4.dp))

            // DETAILS (compact instead of messy stack)
            entry.details.take(2).forEach {
                Text(
                    text = it,
                    fontSize = 12.sp,
                    color = Color(0xFF6B6B6B),
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(6.dp))

            // CATEGORY BADGE
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        when (entry.category) {
                            "System" -> Color(0xFFFFF3E0)
                            "Inventory" -> Color(0xFFE8F5E9)
                            "Sales" -> Color(0xFFE3F2FD)
                            else -> Color(0xFFF1F1F1)
                        }
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            ) {
                Text(
                    text = entry.category,
                    fontSize = 10.sp,
                    color = Color.DarkGray
                )
            }
        }

        // TIME (fixed format, not ugly raw timestamp)
        Text(
            text = entry.timestamp,
            fontSize = 10.sp,
            color = Color(0xFF9E9E9E),
            textAlign = TextAlign.End
        )
    }
}

/* CHIP */
@Composable
private fun ActivityFilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))
            .background(if (selected) Color(0xFF00657E) else Color(0xFFE6F3F6))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            color = if (selected) Color.White else Color(0xFF003D4C)
        )
    }
}