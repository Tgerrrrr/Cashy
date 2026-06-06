package com.example.supabaseauth.ui

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.ui.graphics.vector.ImageVector

// =========================================
// SCREEN ROUTES
// =========================================

sealed class Screen(val route: String) {

    // ── Auth ──────────────────────────────
    object Login      : Screen("login")
    object Register   : Screen("register")

    // ── Admin ─────────────────────────────
    object Home           : Screen("home")
    object Product        : Screen("product")
    object Customer       : Screen("customer")
    object Cash           : Screen("cash")
    object Expense        : Screen("expense")
    object Sales          : Screen("sales")
    object Transaction    : Screen("transaction")
    object AddTransaction : Screen("add_transaction")
    object History        : Screen("history")
    object Profile        : Screen("profile")

    object ManageProduct : Screen("manage_product?productId={productId}") {
        fun createRoute(productId: String? = null): String =
            if (productId == null) "manage_product" else "manage_product?productId=$productId"
    }

    // ── Cashier ───────────────────────────
    object CashierHome   : Screen("cashier_home")
    object CashierKasir  : Screen("cashier_kasir")
    object CashierBarang : Screen("cashier_barang")
    object CashierKas    : Screen("cashier_kas")
}

// =========================================
// BOTTOM NAV – ADMIN
// =========================================

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home        : BottomNavItem(Screen.Home.route,        "Home",        Icons.Default.Home)
    object Transaction : BottomNavItem(Screen.Transaction.route, "Transaction", Icons.Default.PointOfSale)
    object Product     : BottomNavItem(Screen.Product.route,     "Product",     Icons.Default.Inventory2)
    object History     : BottomNavItem(Screen.History.route,     "Activity",    Icons.Default.History)
    object Profile     : BottomNavItem(Screen.Profile.route,     "Profile",     Icons.Default.Person)
}