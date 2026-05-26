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

sealed class Screen(
    val route: String
) {

    object Login :
        Screen("login")

    object Register :
        Screen("register")

    object AdminDashboard :
        Screen("admin_dashboard")

    object CashierDashboard :
        Screen("cashier_dashboard")

    object Home :
        Screen("home")

    object Product :
        Screen("product")

    object Customer :
        Screen("customer")

    object Cash :
        Screen("cash")

    object Expense :
        Screen("expense")

    object Sales :
        Screen("sales")

    object Transaction :
        Screen("transaction")

    object AddTransaction :
        Screen("add_transaction")

    object History :
        Screen("history")

    object Profile :
        Screen("profile")

    /* ================= MANAGE PRODUCT ================= */

    object ManageProduct :
        Screen("manage_product?productId={productId}") {

        fun createRoute(
            productId: String? = null
        ): String {

            return if (productId == null) {

                "manage_product"

            } else {

                "manage_product?productId=$productId"
            }
        }
    }
}

// =========================================
// BOTTOM NAVIGATION ITEMS
// =========================================

sealed class BottomNavItem(

    val route: String,
    val label: String,
    val icon: ImageVector

) {

    object Home : BottomNavItem(

        route = Screen.Home.route,
        label = "Home",
        icon = Icons.Default.Home
    )

    object Transaction : BottomNavItem(

        route = Screen.Transaction.route,
        label = "Transaction",
        icon = Icons.Default.PointOfSale
    )

    object Product : BottomNavItem(

        route = Screen.Product.route,
        label = "Product",
        icon = Icons.Default.Inventory2
    )

    object History : BottomNavItem(

        route = Screen.History.route,
        label = "Activity",
        icon = Icons.Default.History
    )

    object Profile : BottomNavItem(

        route = Screen.Profile.route,
        label = "Profile",
        icon = Icons.Default.Person
    )
}