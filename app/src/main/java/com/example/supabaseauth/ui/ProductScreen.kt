package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.*
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.viewmodel.ProductViewModel

/* ================= COLORS ================= */

private val Primary = Color(0xFF00657E)
private val Background = Color.White

/* ================= SCREEN ================= */

@Composable
fun ProductScreen(
    navController: NavController,
    productViewModel: ProductViewModel = viewModel()
) {

    val products by productViewModel
        .productList
        .collectAsStateWithLifecycle()

    var searchQuery by remember {
        mutableStateOf("")
    }

    var selectedFilter by remember {
        mutableStateOf("All")
    }

    LaunchedEffect(Unit) {

        productViewModel.loadProducts()
    }

    val filteredProducts = remember(
        products,
        searchQuery,
        selectedFilter
    ) {

        products.filter { product ->

            val matchesSearch =
                product.nama.contains(
                    searchQuery,
                    ignoreCase = true
                )

            val matchesFilter =
                when (selectedFilter) {

                    "All" -> true

                    "Inactive" ->
                        !product.is_active

                    "Out of Stock" ->
                        product.stok <= 0

                    "Low" ->
                        product.stok in 1.0..9.0

                    else -> true
                }

            matchesSearch && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
    ) {

        Spacer(
            modifier = Modifier.height(40.dp)
        )

        /* ================= SEARCH ================= */
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .height(55.dp)
                .clip(RoundedCornerShape(30.dp))
                .background(Color(0xFFF2F3F2))
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Search, contentDescription = null)

            Spacer(Modifier.width(10.dp))

            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search Product") },
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
        Spacer(
            modifier = Modifier.height(16.dp)
        )

        /* ================= STATS CARD ================= */

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(159.dp)
                .clip(RoundedCornerShape(15.dp))
                .background(Color(0xFF00657E))
                .padding(
                    top = 23.dp,
                    bottom = 17.dp,
                    start = 10.dp,
                    end = 10.dp
                )
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),

                verticalArrangement =
                    Arrangement.spacedBy(2.dp),

                horizontalAlignment =
                    Alignment.CenterHorizontally
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),

                    horizontalArrangement =
                        Arrangement.spacedBy(26.dp),

                    verticalAlignment =
                        Alignment.CenterVertically
                ) {

                    /* ================= TOTAL PRODUCT ================= */

                    Column(
                        modifier = Modifier.width(80.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text = "${products.size}",

                            fontSize = 24.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color = Color.White
                        )

                        Text(
                            text = "Total Product",

                            fontSize = 11.sp,

                            color = Color.White
                        )
                    }

                    /* ================= OUT OF STOCK ================= */

                    Column(
                        modifier = Modifier.width(80.dp),

                        horizontalAlignment =
                            Alignment.CenterHorizontally
                    ) {

                        Text(
                            text =
                                "${products.count { it.stok <= 0 }}",

                            fontSize = 24.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color = Color.White
                        )

                        Text(
                            text = "Out of Stock",

                            fontSize = 11.sp,

                            color = Color.White
                        )
                    }

                    /* ================= ADD PRODUCT ================= */

                    Row(
                        modifier = Modifier
                            .width(126.dp)
                            .height(40.dp)
                            .clip(RoundedCornerShape(30.dp))
                            .background(Color.White)
                            .clickable {

                                navController.navigate(
                                    "manage_product/new"
                                )
                            },

                        verticalAlignment =
                            Alignment.CenterVertically
                    ) {

                        Box(
                            modifier = Modifier.size(29.dp),

                            contentAlignment =
                                Alignment.Center
                        ) {

                            Icon(
                                imageVector =
                                    Icons.Default.Add,

                                contentDescription = null,

                                tint = Color(0xFF002F3B),

                                modifier =
                                    Modifier.size(18.dp)
                            )
                        }

                        Text(
                            text = "Add Product",

                            fontSize = 12.sp,

                            fontWeight =
                                FontWeight.SemiBold,

                            color = Color(0xFF002F3B)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(6.dp)
                )

                /* ================= STOCK BAR ================= */

                val totalProducts =
                    products.size

                val outOfStock =
                    products.count {
                        it.stok <= 0
                    }

                val outPercentage =
                    if (totalProducts > 0)
                        outOfStock.toFloat() /
                                totalProducts.toFloat()
                    else
                        0f

                Text(
                    text =
                        "Out of Stock : $outOfStock",

                    color = Color.White,

                    fontSize = 12.sp,

                    fontWeight =
                        FontWeight.Medium,

                    modifier =
                        Modifier.fillMaxWidth(),

                    textAlign = TextAlign.Start
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(29.dp)
                        .clip(RoundedCornerShape(15.dp))
                        .background(Color(0xFFF4F6F8))
                ) {

                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(outPercentage)
                            .clip(RoundedCornerShape(15.dp))
                            .background(Color(0xFF1A3C40))
                    )
                }
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /* ================= FILTER ================= */

        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp),

            horizontalArrangement =
                Arrangement.spacedBy(10.dp)
        ) {

            listOf(
                "All",
                "Inactive",
                "Out of Stock",
                "Low"
            ).forEach { filter ->

                FilterChip(
                    text = filter,

                    selected =
                        selectedFilter == filter,

                    onClick = {
                        selectedFilter = filter
                    }
                )
            }
        }

        Spacer(
            modifier = Modifier.height(12.dp)
        )

        /* ================= PRODUCT LIST ================= */

        LazyColumn(
            modifier = Modifier.fillMaxSize(),

            contentPadding =
                PaddingValues(16.dp),

            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {

            items(filteredProducts) { product ->

                ProductCard(
                    product = product,

                    onDelete = {

                        product.id?.let { id ->

                            productViewModel
                                .deleteProduct(id)
                        }
                    },

                    onManage = {

                        product.id?.let { id ->

                            navController.navigate(
                                "manage_product/$id"
                            )
                        }
                    }
                )
            }
        }
    }
}

/* ================= FILTER CHIP ================= */

@Composable
private fun FilterChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(30.dp))

            .background(
                if (selected)
                    Primary
                else
                    Color(0xFFE6F3F6)
            )

            .clickable {
                onClick()
            }

            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
    ) {

        Text(
            text = text,

            fontSize = 12.sp,

            color =
                if (selected)
                    Color.White
                else
                    Primary
        )
    }
}

/* ================= PRODUCT CARD ================= */

@Composable
private fun ProductCard(
    product: Product,
    onDelete: () -> Unit,
    onManage: () -> Unit
) {

    var showDeleteDialog by remember {

        mutableStateOf(false)
    }

    /* ================= DELETE DIALOG ================= */

    if (showDeleteDialog) {

        AlertDialog(
            onDismissRequest = {

                showDeleteDialog = false
            },

            title = {

                Text("Delete Product")
            },

            text = {

                Text(
                    "Are you sure you want to delete ${product.nama}?"
                )
            },

            confirmButton = {

                TextButton(
                    onClick = {

                        onDelete()

                        showDeleteDialog = false
                    }
                ) {

                    Text(
                        text = "Delete",
                        color = Color.Red
                    )
                }
            },

            dismissButton = {

                TextButton(
                    onClick = {

                        showDeleteDialog = false
                    }
                ) {

                    Text("Cancel")
                }
            }
        )
    }

    /* ================= CARD ================= */

    Card(
        modifier = Modifier.fillMaxWidth(),

        shape = RoundedCornerShape(20.dp),

        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),

        elevation =
            CardDefaults.cardElevation(4.dp)
    ) {

        Column(
            modifier = Modifier.padding(
                top = 18.dp,
                bottom = 6.dp,
                start = 10.dp,
                end = 10.dp
            ),

            verticalArrangement =
                Arrangement.spacedBy(7.dp)
        ) {

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        start = 5.dp,
                        end = 15.dp
                    ),

                horizontalArrangement =
                    Arrangement.SpaceBetween,

                verticalAlignment =
                    Alignment.CenterVertically
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    modifier = Modifier.weight(1f)
                ) {

                    val iconBgColor =
                        when {

                            !product.is_active ->
                                Color(0xFFEAEAEA)

                            product.stok <= 0 ->
                                Color(0xFFFFEBEE)

                            product.stok in 1.0..9.0 ->
                                Color(0xFFFFF3E0)

                            else ->
                                Color(0xFFE8F5E9)
                        }

                    val iconColor =
                        when {

                            !product.is_active ->
                                Color.Black

                            product.stok <= 0 ->
                                Color(0xFFBA2020)

                            product.stok in 1.0..9.0 ->
                                Color(0xFFFF9800)

                            else ->
                                Color(0xFF2A7F13)
                        }

                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(
                                RoundedCornerShape(20.dp)
                            )
                            .background(iconBgColor),

                        contentAlignment =
                            Alignment.Center
                    ) {

                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(
                                    RoundedCornerShape(20.dp)
                                )
                                .background(iconColor)
                        )
                    }

                    Spacer(
                        modifier = Modifier.width(15.dp)
                    )

                    Column {

                        Text(
                            text = product.nama,

                            fontSize = 16.sp,

                            fontWeight =
                                FontWeight.Medium,

                            color = Color.Black
                        )

                        Text(
                            text =
                                "Rp${product.harga}",

                            fontSize = 14.sp,

                            color =
                                Color(0xFF141414)
                        )

                        Text(
                            text =
                                "Stock: ${product.stok.toInt()}",

                            fontSize = 14.sp,

                            color =
                                Color(0xFF979797)
                        )
                    }
                }
            }

            /* ================= MANAGE BUTTON ================= */

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(30.dp)

                    .background(
                        Color(0xFF006C86),
                        RoundedCornerShape(18.dp)
                    )

                    .clickable {

                        onManage()
                    },

                contentAlignment =
                    Alignment.Center
            ) {

                Row(
                    verticalAlignment =
                        Alignment.CenterVertically,

                    horizontalArrangement =
                        Arrangement.spacedBy(11.dp)
                ) {

                    Text(
                        text = "Manage",

                        color = Color.White,

                        fontSize = 14.sp
                    )

                    Icon(
                        imageVector =
                            Icons.Default.Add,

                        contentDescription = null,

                        tint = Color.White,

                        modifier =
                            Modifier.size(19.dp)
                    )
                }
            }
        }
    }
}