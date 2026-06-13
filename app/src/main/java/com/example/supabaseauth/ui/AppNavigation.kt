package com.example.supabaseauth.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.supabaseauth.viewmodel.AuthCheckState
import com.example.supabaseauth.viewmodel.AuthViewModel

private val NavBg        = Color(0xFFFFFFFF)
private val NavPrimary   = Color(0xFF1A3C40)
private val NavUnselected = Color(0xFF90A4AE)
private val NavIndicator = Color(0xFFE0F2F1)
private val ScaffoldBg   = Color(0xFFF4F6F8)

private val adminBottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Transaction.route,
    Screen.Product.route,
    Screen.History.route,
    Screen.Profile.route
)

// =============================================================================
// ROOT COMPOSABLE
// =============================================================================

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {
    val authCheckState by authViewModel.authCheckState.collectAsStateWithLifecycle()
    val authUiState    by authViewModel.authUiState.collectAsStateWithLifecycle()
    val currentEmail   by authViewModel.currentUserEmail.collectAsStateWithLifecycle()
    val currentName    by authViewModel.currentUserName.collectAsStateWithLifecycle()
    val currentUserId  by authViewModel.currentUserId.collectAsStateWithLifecycle()

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val showBottomBar = currentRoute in adminBottomNavRoutes

    // ── loading splash ────────────────────────────────────────────────────────
    if (authCheckState == AuthCheckState.Checking) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = NavPrimary)
        }
        return
    }

    Scaffold(
        containerColor = ScaffoldBg,
        bottomBar = {
            if (showBottomBar) {
                CashyBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                launchSingleTop = true
                                restoreState = true
                                popUpTo(navController.graph.startDestinationId) {
                                    saveState = true
                                }
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->

        NavHost(
            navController   = navController,
            startDestination = Screen.Login.route,
            modifier         = Modifier.padding(innerPadding)
        ) {

            // ── LOGIN ─────────────────────────────────────────────────────────
            composable(Screen.Login.route) {
                LoginScreen(
                    authViewModel = authViewModel,
                    onSignup = {}
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    authViewModel  = authViewModel,
                    authUiState    = authUiState,
                    onNavigateBack = {
                        authViewModel.resetUiState()
                        navController.popBackStack()
                    }
                )
            }

            // ══════════════════════════════════════════════════════════════════
            // ADMIN SCREENS
            // ══════════════════════════════════════════════════════════════════

            composable(Screen.Home.route) {
                HomeScreen(
                    authViewModel = authViewModel,
                    navController = navController,
                    onLogout      = { authViewModel.logout() }
                )
            }

            composable(Screen.Transaction.route) {
                TransactionScreen(
                    onAddTransaction = { navController.navigate(Screen.AddTransaction.route) }
                )
            }

            composable(Screen.AddTransaction.route) {
                AddTransactionScreen(
                    onBack = { navController.popBackStack() }
                )
            }

            composable(Screen.Product.route) {
                ProductScreen(navController = navController)
            }

            composable(Screen.History.route) {
                HistoryScreen()
            }

            composable(Screen.Profile.route) {
                ProfileScreen(
                    currentUserName  = currentName,
                    currentUserEmail = currentEmail,
                    authUiState      = authUiState,
                    currentUserId    = currentUserId,
                    onUpdateProfile  = { userId, nama -> authViewModel.updateProfile(userId, nama) },
                    onLogout         = { authViewModel.logout() },
                    onCreateCashier  = {
                        authViewModel.resetUiState()
                        navController.navigate(Screen.Register.route)
                    }
                )
            }

            composable(Screen.Customer.route)  { CustomerScreen() }
            composable(Screen.Cash.route)      { CashScreen() }
            composable(Screen.Expense.route)   { ExpenseScreen() }
            composable(Screen.Sales.route)     { SalesScreen() }

            composable(
                route = "manage_product/{productId}"
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId")
                ManageProductScreen(
                    navController = navController,
                    productId     = if (productId == "new") null else productId
                )
            }

            // ══════════════════════════════════════════════════════════════════
            // CASHIER SCREENS
            // ══════════════════════════════════════════════════════════════════

            composable(Screen.CashierHome.route) {
                CashierDashboardScreen(
                    onNavigateToKasir     = { navController.navigate(Screen.CashierKasir.route) },
                    onNavigateToBarang    = { navController.navigate(Screen.CashierBarang.route) },
                    onNavigateToPelanggan = { navController.navigate(Screen.CashierPelanggan.route) },
                    onLogout              = { authViewModel.logout() }
                )
            }

            composable(Screen.CashierKasir.route) {
                CashierKasirScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CashierBarang.route) {
                CashierBarangScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(Screen.CashierPelanggan.route) {
                CashierPelangganScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }

        // ── auth navigation handler ───────────────────────────────────────────
        LaunchedEffect(authCheckState) {
            when (val state = authCheckState) {

                is AuthCheckState.LoggedIn -> {
                    val role = state.role
                    val destination = if (role == "cashier") {
                        Screen.CashierHome.route
                    } else {
                        Screen.Home.route
                    }

                    if (currentRoute != destination) {
                        navController.navigate(destination) {
                            popUpTo(Screen.Login.route) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                is AuthCheckState.LoggedOut -> {
                    if (currentRoute != Screen.Login.route) {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

// =============================================================================
// ADMIN BOTTOM NAV
// =============================================================================

@Composable
fun CashyBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Transaction,
        BottomNavItem.Product,
        BottomNavItem.History,
        BottomNavItem.Profile
    )

    NavigationBar(containerColor = NavBg, tonalElevation = 0.dp) {
        items.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick  = { onNavigate(item.route) },
                icon     = { Icon(imageVector = item.icon, contentDescription = item.label) },
                label    = { Text(text = item.label, fontSize = 10.sp) },
                colors   = NavigationBarItemDefaults.colors(
                    selectedIconColor   = NavPrimary,
                    selectedTextColor   = NavPrimary,
                    unselectedIconColor = NavUnselected,
                    unselectedTextColor = NavUnselected,
                    indicatorColor      = NavIndicator
                )
            )
        }
    }
}