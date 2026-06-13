package com.example.supabaseauth.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.supabaseauth.R
import com.example.supabaseauth.model.ActivityEntryUI
import com.example.supabaseauth.viewmodel.ActivityState
import com.example.supabaseauth.viewmodel.ActivityViewModel
import com.example.supabaseauth.viewmodel.AuthViewModel
import com.example.supabaseauth.viewmodel.CashState
import com.example.supabaseauth.viewmodel.CashViewModel
import com.example.supabaseauth.viewmodel.ProductViewModel

@Composable
fun HomeScreen(
    authViewModel: AuthViewModel,
    navController: NavController,
    onLogout: () -> Unit,
    cashViewModel: CashViewModel = viewModel(),
    activityViewModel: ActivityViewModel = viewModel(),
    productViewModel: ProductViewModel = viewModel()
) {

    /* ================= STATES ================= */

    val cashState by cashViewModel.cashState.collectAsStateWithLifecycle()
    val totalSaldo by cashViewModel.totalSaldo.collectAsStateWithLifecycle()

    val activityState by activityViewModel.state.collectAsStateWithLifecycle()

    val productList by productViewModel.productList.collectAsStateWithLifecycle()

    /* ================= TOTALS ================= */

    val totalTransactions = when (activityState) {

        is ActivityState.Success -> {

            (activityState as ActivityState.Success)
                .entries
                .count {
                    it.category == "Sales"
                }
        }

        else -> 0
    }

    val totalProducts = productList.size

    /* ================= SCREEN ================= */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CashyColors.Background)
            .verticalScroll(rememberScrollState())
    ) {

        /* ================= LOGO ================= */

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {

            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(90.dp)
                    .graphicsLayer {
                        scaleX = 1.4f
                        scaleY = 1.4f
                    }
            )
        }

        /* ================= CASH SUMMARY ================= */

        TransactionSummaryCard(
            cashState = cashState,
            totalSaldo = totalSaldo,
            onAddTransaction = {
                    navController.navigate(Screen.AddTransaction.route)
            },
            onOpenHistory = {
                navController.navigate(Screen.History.route)
            },
            onOpenCash = {
                navController.navigate(Screen.Cash.route)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        /* ================= STATS ================= */

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            /* ================= TRANSACTION CARD ================= */

            FigmaStatCard(
                title = "Total Sales",
                value = "$totalTransactions Sales",
                buttonText = "See all sales",
                onClick = {
                    navController.navigate(Screen.Transaction.route)
                }
            )

            /* ================= PRODUCT CARD ================= */

            FigmaStatCard(
                title = "Products",
                value = "$totalProducts Items",
                buttonText = "See all product",
                onClick = {
                    navController.navigate(Screen.Product.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        /* ================= RECENT ACTIVITY ================= */

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {

            Text(
                text = "Aktivitas Terbaru",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp
            )

            Text(
                text = "See All",
                color = Color(0xFF00657E),
                fontSize = 13.sp,
                modifier = Modifier.clickable {
                    navController.navigate(Screen.History.route)
                }
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        when (activityState) {

            is ActivityState.Loading -> {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    contentAlignment = Alignment.Center
                ) {

                    CircularProgressIndicator()
                }
            }

            is ActivityState.Error -> {

                Text(
                    text = "Failed to load activity",
                    color = Color.Red,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            is ActivityState.Empty -> {

                Text(
                    text = "No recent activity",
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            is ActivityState.Success -> {

                val activities =
                    (activityState as ActivityState.Success)
                        .entries
                        .take(5)

                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {

                    activities.forEach { activity ->

                        val icon = when (activity.category) {

                            "Sales" -> Icons.Default.CheckCircle

                            "Inventory" -> Icons.Default.Warning

                            "System" -> Icons.Default.Settings

                            else -> Icons.Default.Info
                        }

                        val color = when (activity.category) {

                            "Sales" -> CashyColors.Success

                            "Inventory" -> CashyColors.Warning

                            "System" -> CashyColors.Accent

                            else -> CashyColors.Accent
                        }

                        ActivityItem(
                            icon = icon,
                            color = color,
                            activity = activity
                        )
                    }
                }
            }

            else -> Unit
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

/* =========================================================
   FIGMA STAT CARD
========================================================= */

@Composable
private fun FigmaStatCard(
    title: String,
    value: String,
    buttonText: String,
    onClick: () -> Unit
) {

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Color(0xFFE6F3F6),
                RoundedCornerShape(10.dp)
            )
            .padding(14.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {

                Text(
                    text = title,
                    fontSize = 14.sp,
                    color = Color.Black
                )

                Text(
                    text = value,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF002F3B))
                    .clickable {
                        onClick()
                    }
                    .padding(horizontal = 10.dp),
                contentAlignment = Alignment.CenterStart
            ) {

                Text(
                    text = buttonText,
                    fontSize = 12.sp,
                    color = Color.White
                )
            }
        }
    }
}

/* =========================================================
   ACTIVITY ITEM
========================================================= */

@Composable
private fun ActivityItem(
    icon: ImageVector,
    color: Color,
    activity: ActivityEntryUI
) {

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(CashyColors.Surface)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {

            Text(
                text = activity.title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = activity.details.joinToString(", "),
                fontSize = 11.sp,
                color = CashyColors.TextSecondary
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = activity.timestamp,
            fontSize = 10.sp,
            color = CashyColors.TextSecondary
        )
    }
}

/* =========================================================
   TRANSACTION SUMMARY CARD
========================================================= */

@Composable
fun TransactionSummaryCard(
    cashState: CashState,
    totalSaldo: Double,
    onAddTransaction: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenCash: () -> Unit
) {

    Box(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth()
            .background(
                color = Color(0xFF002F3B),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(19.dp)
    ) {

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            Row(
                modifier = Modifier.fillMaxWidth() .clickable { onOpenCash() },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Cash Balance",
                    color = Color.White,
                    fontSize = 14.sp
                )

                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Kelola Kas",
                    tint = Color.White
                )
            }

            Text(
                text = when (cashState) {

                    is CashState.Loading -> "Loading..."

                    else -> formatRupiah(totalSaldo)
                },
                color = Color.White,
                fontSize = 29.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { onOpenCash() }
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                Row(
                    modifier = Modifier
                        .height(43.dp)
                        .width(192.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable {
                            onAddTransaction()
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "Add Transaction",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }

                Row(
                    modifier = Modifier
                        .height(43.dp)
                        .width(125.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color(0xFFF0F0F0))
                        .clickable {
                            onOpenHistory()
                        }
                        .padding(horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = Color.Black
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = "History",
                        fontSize = 14.sp,
                        color = Color.Black,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}