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
import androidx.compose.material3.MaterialTheme
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

/* =========================================================
   COLORS
========================================================= */

private val NavBg = Color(0xFFFFFFFF)
private val NavPrimary = Color(0xFF1A3C40)
private val NavUnselected = Color(0xFF90A4AE)
private val NavIndicator = Color(0xFFE0F2F1)
private val ScaffoldBg = Color(0xFFF4F6F8)

/* =========================================================
   BOTTOM NAVIGATION ROUTES
========================================================= */

private val bottomNavRoutes = setOf(
    Screen.Home.route,
    Screen.Transaction.route,
    Screen.Product.route,
    Screen.History.route,
    Screen.Profile.route
)

/* =========================================================
   APP NAVIGATION
========================================================= */

@Composable
fun AppNavigation(
    navController: NavHostController,
    authViewModel: AuthViewModel
) {

    /* =====================================================
       STATES
    ===================================================== */

    val authCheckState by authViewModel
        .authCheckState
        .collectAsStateWithLifecycle()

    val authUiState by authViewModel
        .authUiState
        .collectAsStateWithLifecycle()

    val currentEmail by authViewModel
        .currentUserEmail
        .collectAsStateWithLifecycle()

    val currentName by authViewModel
        .currentUserName
        .collectAsStateWithLifecycle()

    val currentUserId by authViewModel
        .currentUserId
        .collectAsStateWithLifecycle()

    val navBackStackEntry by navController
        .currentBackStackEntryAsState()

    val currentRoute =
        navBackStackEntry
            ?.destination
            ?.route

    val showBottomBar =
        currentRoute in bottomNavRoutes

    /* =====================================================
       LOADING SCREEN
    ===================================================== */

    if (authCheckState == AuthCheckState.Checking) {

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {

            CircularProgressIndicator(
                color = NavPrimary
            )
        }

        return
    }

    /* =====================================================
       MAIN SCAFFOLD
    ===================================================== */

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

                                popUpTo(
                                    navController.graph.startDestinationId
                                ) {

                                    saveState = true
                                }
                            }
                        }
                    }
                )
            }
        }

    ) { innerPadding ->

        /* =================================================
           NAV HOST
        ================================================= */

        NavHost(

            navController = navController,

            startDestination = Screen.Login.route,

            modifier = Modifier.padding(innerPadding)

        ) {

            /* =============================================
               LOGIN
            ============================================= */

            composable(Screen.Login.route) {

                LoginScreen(

                    authViewModel = authViewModel,

                    onSignup = {

                        authViewModel.resetUiState()

                        navController.navigate(
                            Screen.Register.route
                        )
                    }
                )
            }

            /* =============================================
               REGISTER
            ============================================= */

            composable(Screen.Register.route) {

                RegisterScreen(

                    authViewModel = authViewModel,

                    authUiState = authUiState,

                    onNavigateToLogin = {

                        authViewModel.resetUiState()

                        navController.popBackStack()
                    }
                )
            }

            /* =============================================
               HOME
            ============================================= */

            composable(Screen.Home.route) {

                HomeScreen(

                    authViewModel = authViewModel,

                    navController = navController,

                    onLogout = {

                        authViewModel.logout()
                    }
                )
            }

            /* =============================================
               TRANSACTION
            ============================================= */

            composable(Screen.Transaction.route) {

                TransactionScreen(

                    onAddTransaction = {

                        navController.navigate(
                            Screen.AddTransaction.route
                        )
                    }
                )
            }

            /* =============================================
               ADD TRANSACTION
            ============================================= */

            composable(Screen.AddTransaction.route) {

                AddTransactionScreen(

                    onBack = {

                        navController.popBackStack()
                    }
                )
            }

            /* =============================================
               PRODUCT
            ============================================= */

            composable(Screen.Product.route) {

                ProductScreen(
                    navController = navController
                )
            }

            /* =============================================
               HISTORY
            ============================================= */

            composable(Screen.History.route) {

                HistoryScreen()
            }

            /* =============================================
               PROFILE
            ============================================= */

            composable(Screen.Profile.route) {

                ProfileScreen(

                    currentUserName = currentName,

                    currentUserEmail = currentEmail,

                    authUiState = authUiState,

                    currentUserId = currentUserId,

                    onUpdateProfile = { userId, nama ->

                        authViewModel.updateProfile(userId, nama)
                    },

                    onLogout = {

                        authViewModel.logout()
                    }
                )
            }

            /* =============================================
               EXTRA SCREENS
            ============================================= */

            composable(Screen.Customer.route) {

                CustomerScreen()
            }

            composable(Screen.Cash.route) {

                CashScreen()
            }

            composable(Screen.Expense.route) {

                ExpenseScreen()
            }

            composable(Screen.Sales.route) {

                SalesScreen()
            }

            /* =============================================
               MANAGE PRODUCT
            ============================================= */

            composable(
                route = "manage_product/{productId}"
            ) { backStackEntry ->

                val productId =
                    backStackEntry
                        .arguments
                        ?.getString("productId")

                ManageProductScreen(

                    navController = navController,

                    productId =
                        if (productId == "new") {
                            null
                        } else {
                            productId
                        }
                )
            }
        }

        /* =================================================
           AUTH NAVIGATION HANDLER
        ================================================= */

        LaunchedEffect(authCheckState) {

            when (authCheckState) {

                AuthCheckState.LoggedIn -> {

                    if (currentRoute != Screen.Home.route) {

                        navController.navigate(
                            Screen.Home.route
                        ) {

                            popUpTo(
                                Screen.Login.route
                            ) {

                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                }

                AuthCheckState.LoggedOut -> {

                    if (currentRoute != Screen.Login.route) {

                        navController.navigate(
                            Screen.Login.route
                        ) {

                            popUpTo(
                                navController.graph.startDestinationId
                            ) {

                                inclusive = true
                            }

                            launchSingleTop = true
                        }
                    }
                }

                else -> Unit
            }
        }
    }
}

/* =========================================================
   BOTTOM NAVIGATION BAR
========================================================= */

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

    NavigationBar(

        containerColor = NavBg,

        tonalElevation = 0.dp

    ) {

        items.forEach { item ->

            val selected =
                currentRoute == item.route

            NavigationBarItem(

                selected = selected,

                onClick = {

                    onNavigate(item.route)
                },

                icon = {

                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },

                label = {

                    Text(
                        text = item.label,
                        fontSize = 10.sp
                    )
                },

                colors = NavigationBarItemDefaults.colors(

                    selectedIconColor = NavPrimary,

                    selectedTextColor = NavPrimary,

                    unselectedIconColor = NavUnselected,

                    unselectedTextColor = NavUnselected,

                    indicatorColor = NavIndicator
                )
            )
        }
    }
}
