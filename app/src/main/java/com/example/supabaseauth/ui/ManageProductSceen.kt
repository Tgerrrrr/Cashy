package com.example.supabaseauth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supabaseauth.model.Product
import com.example.supabaseauth.viewmodel.ProductViewModel

private val Background = Color(0xFFFFFFFF)
private val FieldBg = Color(0xFFF7F7F7)
private val Primary = Color(0xFF00657E)

@Composable
fun ManageProductScreen(
    navController: NavController,
    productId: String? = null,
    productViewModel: ProductViewModel = viewModel()
) {

    val products by productViewModel.productList.collectAsStateWithLifecycle()

    val isEditMode = !productId.isNullOrBlank()

    /* ================= FORM STATE ================= */
    var nama by remember { mutableStateOf("") }
    var harga by remember { mutableStateOf("") }
    var stok by remember { mutableStateOf("") }
    var isActive by remember { mutableStateOf(true) }

    var expanded by remember { mutableStateOf(false) }

    /* ================= LOAD DATA  ================= */
    LaunchedEffect(productId, products) {

        if (isEditMode) {

            val product = products.find { it.id == productId }

            if (product != null) {
                nama = product.nama
                harga = product.harga.toInt().toString()
                stok = product.stok.toInt().toString()
                isActive = product.is_active
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 15.dp)
    ) {

        Spacer(Modifier.height(40.dp))

        /* ================= TITLE ================= */

        Text(
            text = if (isEditMode) "Manage Product" else "Add Product",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Black
        )

        Spacer(Modifier.height(35.dp))

        /* ================= NAME ================= */

        Text("Product Name", fontSize = 14.sp, color = Color(0xFF898989))

        OutlinedTextField(
            value = nama,
            onValueChange = { nama = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(5.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = FieldBg,
                unfocusedContainerColor = FieldBg,
                focusedBorderColor = Color.Transparent,
                unfocusedBorderColor = Color.Transparent
            )
        )

        Spacer(Modifier.height(20.dp))

        /* ================= PRICE & STOCK ================= */

        Row(Modifier.fillMaxWidth()) {

            Column(Modifier.weight(1f)) {

                Text("Price", fontSize = 14.sp, color = Color(0xFF898989))

                OutlinedTextField(
                    value = harga,
                    onValueChange = {
                        harga = it.filter { c -> c.isDigit() }
                    },
                    prefix = { Text("Rp.") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    singleLine = true
                )
            }

            Spacer(Modifier.width(10.dp))

            Column(Modifier.weight(1f)) {

                Text("Stock", fontSize = 14.sp, color = Color(0xFF898989))

                OutlinedTextField(
                    value = stok,
                    onValueChange = {
                        stok = it.filter { c -> c.isDigit() }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(5.dp),
                    singleLine = true
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        /* ================= STATUS ================= */

        Text("Status", fontSize = 14.sp, color = Color(0xFF898989))

        Box {

            Row(
                modifier = Modifier
                    .width(150.dp)
                    .height(50.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(
                        if (isActive) Color(0xFFAAED97) else Color(0xFFE0E0E0)
                    )
                    .clickable { expanded = true }
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Text(
                    text = if (isActive) "Active" else "Inactive"
                )

                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {

                DropdownMenuItem(
                    text = { Text("Active") },
                    onClick = {
                        isActive = true
                        expanded = false
                    }
                )

                DropdownMenuItem(
                    text = { Text("Inactive") },
                    onClick = {
                        isActive = false
                        expanded = false
                    }
                )
            }
        }

        Spacer(Modifier.height(40.dp))

        /* ================= SAVE ================= */

        Button(
            onClick = {

                val product = Product(
                    id = productId,
                    nama = nama,
                    harga = harga.toDoubleOrNull() ?: 0.0,
                    stok = stok.toDoubleOrNull() ?: 0.0,
                    is_active = isActive
                )

                if (isEditMode) {
                    productViewModel.updateProduct(product)
                } else {
                    productViewModel.insertProduct(product)
                }

                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(40.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {

            Text(
                text = "Save",
                color = Color.White,
                fontSize = 16.sp
            )
        }

        Spacer(Modifier.height(40.dp))
    }
}