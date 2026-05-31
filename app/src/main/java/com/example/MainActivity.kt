package com.example

import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.*
import com.example.ui.theme.*
import com.example.viewmodel.PurManViewModel
import java.util.Date

class MainActivity : ComponentActivity() {
    private val viewModel: PurManViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainContainer(viewModel)
            }
        }
    }
}

// Helper badge tag
@Composable
fun StatusBadge(text: String, color: Color) {
    Text(
        text = text,
        color = color,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .background(color.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    )
}

@Composable
fun MainContainer(viewModel: PurManViewModel) {
    val context = LocalContext.current
    var selectedScreen by remember { mutableStateOf("Dashboard") }

    // Navigation lists
    val navItems = listOf(
        Pair("Dashboard", Icons.Default.Home),
        Pair("Sales/POS", Icons.Default.ShoppingCart),
        Pair("Purchases", Icons.Default.CheckCircle),
        Pair("Inventory", Icons.Default.Build),
        Pair("Finance", Icons.Default.Star),
        Pair("CRM Log", Icons.Default.Email),
        Pair("Staff Hub", Icons.Default.Person),
        Pair("ERP System", Icons.Default.Settings)
    )

    // Collect all database states as Composable values
    val currentRole by viewModel.currentUserRole.collectAsState()
    val branchName by viewModel.selectedBranch.collectAsState()
    val baseSymbol = viewModel.currentCurrencySymbol()

    Scaffold(
        bottomBar = {
            // Adaptive Navigation: Bottom Row for Mobile/Tablet layout preview
            Surface(
                color = PremiumDarkCharcoal,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(width = 1.dp, color = PremiumBorderGray, shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items(navItems) { item ->
                        val isSelected = selectedScreen == item.first
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .clickable { selectedScreen = item.first }
                                .padding(horizontal = 14.dp, vertical = 4.dp)
                        ) {
                            Icon(
                                imageVector = item.second,
                                contentDescription = item.first,
                                tint = if (isSelected) NeonGreen else MutedSlate,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = item.first,
                                color = if (isSelected) CleanWhite else MutedSlate,
                                fontSize = 10.sp,
                                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        },
        containerColor = PremiumBlack
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(PremiumBlack)
        ) {
            // Custom Header Banner with quick Role and Branch selectors
            HeaderToolbar(viewModel = viewModel)

            // Animated Screen transition based on selection
            Box(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp)
            ) {
                when (selectedScreen) {
                    "Dashboard" -> DashboardView(viewModel)
                    "Sales/POS" -> SalesPosView(viewModel)
                    "Purchases" -> PurchasesView(viewModel)
                    "Inventory" -> InventoryView(viewModel)
                    "Finance" -> FinanceView(viewModel)
                    "CRM Log" -> CrmView(viewModel)
                    "Staff Hub" -> StaffView(viewModel)
                    "ERP System" -> SettingsView(viewModel)
                }
            }
        }
    }
}

@Composable
fun HeaderToolbar(viewModel: PurManViewModel) {
    val currentRole by viewModel.currentUserRole.collectAsState()
    val branchName by viewModel.selectedBranch.collectAsState()
    val currentUserName by viewModel.currentUserName.collectAsState()

    var showRoleDialog by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(PremiumDarkCharcoal)
            .border(width = 1.dp, color = PremiumBorderGray)
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "PurMAN",
                    color = CleanWhite,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "AI",
                    color = NeonGreen,
                    fontWeight = FontWeight.Black,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.SansSerif
                )
            }
            Text(
                text = "$branchName • $currentUserName",
                color = MutedSlate,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium
            )
        }

        // Tactile Clickable tag to demonstrate Role-Based Access Control
        Row(
            modifier = Modifier
                .background(DeepNeonBg, shape = RoundedCornerShape(10.dp))
                .border(width = 1.dp, color = NeonGreen.copy(alpha = 0.5f), shape = RoundedCornerShape(10.dp))
                .clickable { showRoleDialog = true }
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(NeonGreen, shape = CircleShape)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = currentRole.uppercase(),
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Change Role",
                tint = NeonGreen,
                modifier = Modifier.size(12.dp)
            )
        }
    }

    if (showRoleDialog) {
        Dialog(onDismissRequest = { showRoleDialog = false }) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = PremiumCardGray,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Change Active Role (RBAC Simulator)",
                        color = CleanWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf("Admin", "Manager", "Cashier", "Storekeeper").forEach { role ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    viewModel.currentUserRole.value = role
                                    viewModel.currentUserName.value = when(role) {
                                        "Admin" -> "Alexander Wright"
                                        "Manager" -> "Elena Rostova"
                                        "Cashier" -> "Chen Wei"
                                        else -> "Marcus Finch"
                                    }
                                    viewModel.logAction("ROLE_SWITCHED", "Switched role simulator to ${role}")
                                    showRoleDialog = false
                                }
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = (currentRole == role),
                                onClick = null,
                                colors = RadioButtonDefaults.colors(selectedColor = NeonGreen)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(text = role, color = SoftWhite, fontSize = 14.sp)
                        }
                    }
                }
            }
        }
    }
}

// ---------------------- 1. DASHBOARD VIEW ----------------------
@Composable
fun DashboardView(viewModel: PurManViewModel) {
    val productsList by viewModel.products.collectAsState()
    val salesList by viewModel.salesOrders.collectAsState()
    val purchasesList by viewModel.purchaseOrders.collectAsState()
    val expensesList by viewModel.expenses.collectAsState()
    val auditLogList by viewModel.auditLogs.collectAsState()
    val announcementsList by viewModel.announcements.collectAsState()

    val currency = viewModel.currentCurrencySymbol()
    
    val totalRevenue = salesList.filter { it.status == "Invoiced" }.sumOf { it.netAmount }
    val totalPurchasing = purchasesList.sumOf { it.totalAmount }
    val totalExpenseVal = expensesList.sumOf { it.amount }
    val netProfit = totalRevenue - totalPurchasing - totalExpenseVal

    val alertCount = productsList.count { it.stock <= it.minStockLevel }

    val aiResponse by viewModel.aiInsightText.collectAsState()
    val isAiLoading by viewModel.isAiLoading.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Quick Metrics Rows
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Total revenue card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "NET REVENUE", color = MutedSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency${String.format("%.1f", totalRevenue)}",
                            color = CleanWhite,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(text = "+2.4% VAT Inc.", color = NeonGreen)
                    }
                }

                // Total expenses card
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "TOTAL COST", color = MutedSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency${String.format("%.1f", totalPurchasing + totalExpenseVal)}",
                            color = ErrorRed,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        StatusBadge(text = "Purchases + Exp", color = WarningOrange)
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(
                            1.dp,
                            if (netProfit >= 0) NeonGreen.copy(0.4f) else ErrorRed.copy(0.4f)
                        ), RoundedCornerShape(12.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "OPERATIONAL MARGIN (EBITDA)",
                            color = MutedSlate,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$currency${String.format("%.2f", netProfit)}",
                            color = if (netProfit >= 0) NeonGreen else ErrorRed,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    if (alertCount > 0) {
                        Column(horizontalAlignment = Alignment.End) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = "Alert",
                                    tint = WarningOrange,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(text = "$alertCount STOCKS LOW", color = WarningOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Interactive Natively Drawn KPI Custom Charts
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "ERP FINANCIAL KPI PROFILE (REVENUE VS PROCUREMENT VS UTILITIES)",
                        color = CleanWhite,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        // Max ratio helper
                        val maxVal = maxOf(totalRevenue, totalPurchasing, totalExpenseVal, 100.0)

                        // Bar 1: Revenue
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Sales Revenue", color = SoftWhite, fontSize = 11.sp)
                                Text(text = "$currency${String.format("%.2f", totalRevenue)}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val ratio = (totalRevenue / maxVal).toFloat().coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(PremiumBorderGray, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(8.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(NeonGreenSecondary, NeonGreen)
                                            ), RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }

                        // Bar 2: Purchasing
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Vendor Procurements", color = SoftWhite, fontSize = 11.sp)
                                Text(text = "$currency${String.format("%.2f", totalPurchasing)}", color = InfoBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val ratio = (totalPurchasing / maxVal).toFloat().coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(PremiumBorderGray, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(8.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(InfoBlue, Color(0xFF00D2FF))
                                            ), RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }

                        // Bar 3: Expenses
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = "Indirect Expenses", color = SoftWhite, fontSize = 11.sp)
                                Text(text = "$currency${String.format("%.2f", totalExpenseVal)}", color = ErrorRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            val ratio = (totalExpenseVal / maxVal).toFloat().coerceIn(0.01f, 1f)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .background(PremiumBorderGray, RoundedCornerShape(4.dp))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth(ratio)
                                        .height(8.dp)
                                        .background(
                                            Brush.horizontalGradient(
                                                listOf(ErrorRed, Color(0xFFFF5E62))
                                            ), RoundedCornerShape(4.dp)
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // PurMAN Generative AI Insights Panel
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        BorderStroke(1.dp, Brush.radialGradient(listOf(NeonGreen, PremiumBorderGray))),
                        RoundedCornerShape(16.dp)
                    ),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "AI Action",
                            tint = NeonGreen,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "PurMAN AI Business Intelligence",
                            color = CleanWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "GEMINI 3.5",
                            color = MutedSlate,
                            fontWeight = FontWeight.Bold,
                            fontSize = 9.sp,
                            modifier = Modifier
                                .border(1.dp, PremiumBorderGray, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = aiResponse,
                        color = SoftWhite,
                        fontSize = 13.sp,
                        lineHeight = 20.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(PremiumBlack, RoundedCornerShape(8.dp))
                            .padding(12.dp)
                            .heightIn(max = 280.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = { viewModel.triggerAiInsightsReport() },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isAiLoading
                    ) {
                        if (isAiLoading) {
                            CircularProgressIndicator(color = PremiumBlack, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Invoking Gemini REST API...")
                        } else {
                            Icon(imageVector = Icons.Default.Share, contentDescription = "Run Query", tint = PremiumBlack)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "Generate Live AI Audit Report",
                                color = PremiumBlack,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Live Alerts Log / Activity announcements
        item {
            Text(
                text = "System Notifications & Broadcasts",
                color = CleanWhite,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }

        if (announcementsList.isEmpty()) {
            item {
                Text(text = "No announcements.", color = MutedSlate, fontSize = 11.sp)
            }
        } else {
            items(announcementsList.take(3)) { note ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                    border = BorderStroke(1.dp, PremiumBorderGray)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text(text = note.title, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = note.dateStr, color = MutedSlate, fontSize = 10.sp)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = note.content, color = SoftWhite, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Notifications, contentDescription = "Chan", tint = NeonGreenSecondary, modifier = Modifier.size(10.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Alert Type: ${note.type}", color = MutedSlate, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 2. SALES & POS VIEW ----------------------
@Composable
fun SalesPosView(viewModel: PurManViewModel) {
    val productsList by viewModel.products.collectAsState()
    val ordersList by viewModel.salesOrders.collectAsState()
    val customersList by viewModel.customers.collectAsState()

    val currentCart by viewModel.posCart.collectAsState()
    val currentCust by viewModel.posCustomerSelection.collectAsState()
    val discountPercent by viewModel.posDiscountPercentage.collectAsState()

    val currencySymbol = viewModel.currentCurrencySymbol()

    var activeTab by remember { mutableStateOf("POS TILL") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (activeTab == "POS TILL") 0 else 1,
            containerColor = PremiumBlack,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "POS TILL") 0 else 1]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(selected = (activeTab == "POS TILL"), onClick = { activeTab = "POS TILL" }) {
                Text("POS TILL SYSTEM", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeTab == "POS TILL") NeonGreen else MutedSlate)
            }
            Tab(selected = (activeTab == "ORDERS / INVOICES"), onClick = { activeTab = "ORDERS / INVOICES" }) {
                Text("ORDER LIST & RETURNS", modifier = Modifier.padding(12.dp), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (activeTab == "ORDERS / INVOICES") NeonGreen else MutedSlate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == "POS TILL") {
            // Split layout: top grid is available products, bottom pane is current cart
            Column(modifier = Modifier.weight(1f)) {
                // Products Grid Selection row
                Text("Select Catalog Items to Add to Bill:", color = SoftWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(productsList) { item ->
                        Card(
                            modifier = Modifier
                                .width(140.dp)
                                .clickable { viewModel.addProductToCart(item) }
                                .border(1.dp, PremiumBorderGray, RoundedCornerShape(8.dp)),
                            colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(text = item.name, color = CleanWhite, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, fontWeight = FontWeight.Bold)
                                Text(text = "SKU: ${item.sku}", color = MutedSlate, fontSize = 9.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text(text = "$currencySymbol${item.price}", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Stock: ${item.stock}", color = if (item.stock <= item.minStockLevel) ErrorRed else MutedSlate, fontSize = 9.sp)
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Cart Pane
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(text = "CURRENT POS CHECKOUT TICKET", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            if (currentCart.isNotEmpty()) {
                                Text(
                                    text = "CLR ALL",
                                    color = ErrorRed,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.clickable { viewModel.clearCart() }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Customer Assignment Dropdown simulation
                        var showCustomerSelector by remember { mutableStateOf(false) }
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(PremiumBlack, RoundedCornerShape(6.dp))
                                .clickable { showCustomerSelector = true }
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Customer: ${currentCust?.name ?: "Assign Loyalty Profile Profile"}",
                                color = if (currentCust != null) NeonGreen else SoftWhite,
                                fontSize = 11.sp
                            )
                            Icon(imageVector = Icons.Default.Person, contentDescription = "Add Customer", tint = NeonGreen, modifier = Modifier.size(14.dp))
                        }

                        if (showCustomerSelector) {
                            Dialog(onDismissRequest = { showCustomerSelector = false }) {
                                Surface(shape = RoundedCornerShape(12.dp), color = PremiumCardGray, modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.padding(16.dp)) {
                                        Text("Assign Customer to Sale (Points Multiplier)", color = CleanWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        customersList.forEach { cust ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        viewModel.posCustomerSelection.value = cust
                                                        showCustomerSelector = false
                                                    }
                                                    .padding(vertical = 10.dp)
                                            ) {
                                                Text(text = "${cust.name} (Pts: ${cust.loyaltyPoints})", color = SoftWhite, fontSize = 12.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Cart item list
                        if (currentCart.isEmpty()) {
                            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                                Text(text = "Till Is Empty. Touch products above to add to bill.", color = MutedSlate, fontSize = 11.sp)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.weight(1f)) {
                                items(currentCart.entries.toList()) { entry ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = entry.key.name, color = SoftWhite, fontSize = 12.sp, maxLines = 1)
                                            Text(text = "$currencySymbol${entry.key.price} x ${entry.value}", color = MutedSlate, fontSize = 10.sp)
                                        }
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            IconButton(onClick = { viewModel.removeProductFromCart(entry.key) }, modifier = Modifier.size(24.dp)) {
                                                Icon(imageVector = Icons.Default.Delete, contentDescription = "Del", tint = ErrorRed, modifier = Modifier.size(16.dp))
                                            }
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "${entry.value}", color = CleanWhite, fontSize = 12.sp)
                                            Spacer(modifier = Modifier.width(4.dp))
                                            IconButton(onClick = { viewModel.addProductToCart(entry.key) }, modifier = Modifier.size(24.dp)) {
                                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add", tint = NeonGreen, modifier = Modifier.size(16.dp))
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Pricing calculation block
                        val totalRawPrice = currentCart.entries.sumOf { it.key.price * it.value }
                        val discountAmount = totalRawPrice * (discountPercent / 100.0)
                        val netFinal = totalRawPrice - discountAmount

                        Spacer(modifier = Modifier.height(8.dp))
                        Divider(color = PremiumBorderGray, thickness = 1.dp)
                        Spacer(modifier = Modifier.height(6.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Subtotal", color = MutedSlate, fontSize = 11.sp)
                            Text("$currencySymbol${String.format("%.2f", totalRawPrice)}", color = SoftWhite, fontSize = 11.sp)
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("Promotion Discount", color = MutedSlate, fontSize = 11.sp)
                            // Clickable quick discount choices
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                listOf(0.0, 5.0, 15.0).forEach { disc ->
                                    Text(
                                        text = "${disc.toInt()}%",
                                        color = if (discountPercent == disc) NeonGreen else MutedSlate,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier
                                            .background(if (discountPercent == disc) DeepNeonBg else Color.Transparent, RoundedCornerShape(4.dp))
                                            .clickable { viewModel.posDiscountPercentage.value = disc }
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text("Net Billing Total", color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            Text("$currencySymbol${String.format("%.2f", netFinal)}", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Black)
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { viewModel.checkoutPosCart("Cash / Card Instant") },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp),
                            enabled = currentCart.isNotEmpty()
                        ) {
                            Icon(imageVector = Icons.Default.ShoppingCart, contentDescription = "Checkout", tint = PremiumBlack)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("INSTANT CHECKOUT", color = PremiumBlack, fontWeight = FontWeight.ExtraBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        } else {
            // TAB 2: Orders list, Return, Refund processing
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (ordersList.isEmpty()) {
                    item {
                        Text(text = "No recorded transactions yet.", color = MutedSlate, fontSize = 11.sp)
                    }
                } else {
                    items(ordersList) { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                            border = BorderStroke(1.dp, PremiumBorderGray)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(text = "Order #${order.id} • ${order.customerName}", color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(text = Date(order.timestamp).toString().take(16), color = MutedSlate, fontSize = 11.sp)
                                    }
                                    StatusBadge(
                                        text = order.status.uppercase(),
                                        color = when(order.status) {
                                            "Invoiced" -> NeonGreen
                                            "Quotation" -> InfoBlue
                                            "Returned" -> ErrorRed
                                            else -> WarningOrange
                                        }
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Column {
                                        Text(text = "Amount Paid: $currencySymbol${String.format("%.2f", order.netAmount)}", color = SoftWhite, fontSize = 12.sp)
                                        Text(text = "Payment: ${order.paymentMethod}", color = MutedSlate, fontSize = 10.sp)
                                    }

                                    if (order.status != "Returned") {
                                        Button(
                                            onClick = { viewModel.processSalesReturn(order) },
                                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                                            shape = RoundedCornerShape(6.dp),
                                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                                            modifier = Modifier.height(26.dp)
                                        ) {
                                            Text("EXECUTE REFUND", color = CleanWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 3. PURCHASES VIEW ----------------------
@Composable
fun PurchasesView(viewModel: PurManViewModel) {
    val suppliersList by viewModel.suppliers.collectAsState()
    val purchaseList by viewModel.purchaseOrders.collectAsState()

    val currency = viewModel.currentCurrencySymbol()

    var supplierInput by remember { mutableStateOf("") }
    var amountInput by remember { mutableStateOf("") }
    var descInput by remember { mutableStateOf("") }

    var activeTab by remember { mutableStateOf("REQUISITIONS") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (activeTab == "REQUISITIONS") 0 else 1,
            containerColor = PremiumBlack,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeTab == "REQUISITIONS") 0 else 1]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(selected = (activeTab == "REQUISITIONS"), onClick = { activeTab = "REQUISITIONS" }) {
                Text("REQUISITION & WORKFLOW", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeTab == "REQUISITIONS") NeonGreen else MutedSlate)
            }
            Tab(selected = (activeTab == "SUPPLIERS"), onClick = { activeTab = "SUPPLIERS" }) {
                Text("VENDORS & EVALUATION", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeTab == "SUPPLIERS") NeonGreen else MutedSlate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeTab == "REQUISITIONS") {
            // Requisition filing form
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(text = "SUBMIT NEW PROCUREMENT REQUISITION", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = supplierInput,
                        onValueChange = { supplierInput = it },
                        placeholder = { Text("Search or input Supplier name...", fontSize = 12.sp) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = PremiumBlack,
                            unfocusedContainerColor = PremiumBlack,
                            focusedTextColor = CleanWhite,
                            unfocusedTextColor = CleanWhite,
                            focusedIndicatorColor = NeonGreen
                        ),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = amountInput,
                            onValueChange = { amountInput = it },
                            placeholder = { Text("Cost Amount...", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = PremiumBlack,
                                unfocusedContainerColor = PremiumBlack,
                                focusedTextColor = CleanWhite,
                                unfocusedTextColor = CleanWhite,
                                focusedIndicatorColor = NeonGreen
                            ),
                            singleLine = true
                        )

                        TextField(
                            value = descInput,
                            onValueChange = { descInput = it },
                            placeholder = { Text("Fulfill details (e.g. 5x Xeon...)", fontSize = 11.sp) },
                            modifier = Modifier
                                .weight(1.5f)
                                .height(48.dp),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = PremiumBlack,
                                unfocusedContainerColor = PremiumBlack,
                                focusedTextColor = CleanWhite,
                                unfocusedTextColor = CleanWhite,
                                focusedIndicatorColor = NeonGreen
                            ),
                            singleLine = true
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val amt = amountInput.toDoubleOrNull() ?: 0.0
                            if (supplierInput.isNotBlank() && amt > 0.0) {
                                viewModel.submitPurchaseRequisition(supplierInput, amt, descInput)
                                supplierInput = ""
                                amountInput = ""
                                descInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(40.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("POST FOR ADMINISTRATIVE APPROVAL", color = PremiumBlack, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(text = "Procurement Pipeline Pipeline", color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(purchaseList) { po ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PremiumBorderGray, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Column {
                                    Text(text = "PO #${po.id} • ${po.supplierName}", color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = po.details, color = MutedSlate, fontSize = 11.sp)
                                }
                                StatusBadge(
                                    text = po.status.uppercase(),
                                    color = when(po.status) {
                                        "Goods Received" -> NeonGreen
                                        "Approved" -> InfoBlue
                                        "Paid" -> NeonGreenSecondary
                                        else -> ErrorRed
                                    }
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "Total Value: $currency${String.format("%.2f", po.totalAmount)}", color = SoftWhite, fontSize = 12.sp)
                                
                                // Direct pipeline visual flow states
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (po.status == "Pending Requisition") {
                                        Button(
                                            onClick = { viewModel.approvePurchaseRequisition(po) },
                                            colors = ButtonDefaults.buttonColors(containerColor = InfoBlue),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("APPROVE PO", color = CleanWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (po.status == "Approved") {
                                        Button(
                                            onClick = { viewModel.receiveGoodsPurchase(po) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("RECEIVE & RESTOCK", color = PremiumBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    } else if (po.status == "Goods Received") {
                                        Button(
                                            onClick = { viewModel.paySupplierOrder(po) },
                                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreenSecondary),
                                            shape = RoundedCornerShape(4.dp),
                                            contentPadding = PaddingValues(horizontal = 6.dp),
                                            modifier = Modifier.height(24.dp)
                                        ) {
                                            Text("Settle Due Pay", color = CleanWhite, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // TAB 2: Suppliers Performance evaluation List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(suppliersList) { vendor ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                        border = BorderStroke(1.dp, PremiumBorderGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(text = vendor.name, color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Contact: ${vendor.contactPerson} (${vendor.phone})", color = MutedSlate, fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .background(DeepNeonBg, RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(text = "Rating: ${vendor.ratingString}", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(text = "Accounts Payable Due: $currency${String.format("%.2f", vendor.outstandingPayment)}", color = SoftWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 4. INVENTORY VIEW ----------------------
@Composable
fun InventoryView(viewModel: PurManViewModel) {
    val productsList by viewModel.products.collectAsState()
    val transfersList by viewModel.transfers.collectAsState()

    val currencySymbol = viewModel.currentCurrencySymbol()

    var activeViewTab by remember { mutableStateOf("STOCK") }
    var searchQuery by remember { mutableStateOf("") }

    var showAdjustDialog by remember { mutableStateOf<Product?>(null) }
    var damageCountInput by remember { mutableStateOf("") }
    var damageReasonInput by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (activeViewTab == "STOCK") 0 else 1,
            containerColor = PremiumBlack,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (activeViewTab == "STOCK") 0 else 1]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(selected = (activeViewTab == "STOCK"), onClick = { activeViewTab = "STOCK" }) {
                Text("STOCK & BATCH MANAGER", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeViewTab == "STOCK") NeonGreen else MutedSlate)
            }
            Tab(selected = (activeViewTab == "TRANSFERS"), onClick = { activeViewTab = "TRANSFERS" }) {
                Text("LOCATIONS & TRANSFERS", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (activeViewTab == "TRANSFERS") NeonGreen else MutedSlate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (activeViewTab == "STOCK") {
            // Search & Barcode scanning simulation Bar
            TextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search by SKU, Batch, Barcode...", fontSize = 12.sp) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = "S", tint = MutedSlate) },
                trailingIcon = {
                    IconButton(onClick = { searchQuery = "890123456789" /* Simulates scanning Xeon */ }) {
                        Icon(imageVector = Icons.Default.Check, contentDescription = "Scan", tint = NeonGreen)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(8.dp)),
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = PremiumDarkCharcoal,
                    unfocusedContainerColor = PremiumDarkCharcoal,
                    focusedTextColor = CleanWhite,
                    unfocusedTextColor = CleanWhite,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                val filteredList = productsList.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.sku.contains(searchQuery, ignoreCase = true) ||
                            it.barcode.contains(searchQuery, ignoreCase = true)
                }

                items(filteredList) { product ->
                    val isLow = product.stock <= product.minStockLevel
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(
                                1.dp,
                                if (isLow) ErrorRed.copy(alpha = 0.5f) else PremiumBorderGray,
                                RoundedCornerShape(10.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(text = product.name, color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "SKU: ${product.sku} • Location: ${product.warehouseLocation}", color = MutedSlate, fontSize = 11.sp)
                                }
                                StatusBadge(
                                    text = if (isLow) "LOW STOCK" else "STABLE",
                                    color = if (isLow) ErrorRed else NeonGreen
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column {
                                    Text(text = "Batch: ${product.batchNumber} • Expiry: ${product.expiryDate}", color = MutedSlate, fontSize = 10.sp)
                                    Text(text = "Retail: $currencySymbol${product.price} • Cost: $currencySymbol${product.costPrice}", color = SoftWhite, fontSize = 11.sp)
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(text = "QTY: ${product.stock}", color = if (isLow) ErrorRed else NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "Write Off",
                                        color = ErrorRed,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.clickable { showAdjustDialog = product }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Transfers tab
            var transferProductInput by remember { mutableStateOf("") }
            var transferQtyInput by remember { mutableStateOf("") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("TRIGGER STOCK TRANSFER", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = transferProductInput,
                        onValueChange = { transferProductInput = it },
                        placeholder = { Text("Product name...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite, unfocusedTextColor = CleanWhite),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row {
                        TextField(
                            value = transferQtyInput,
                            onValueChange = { transferQtyInput = it },
                            placeholder = { Text("Quantity...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite, unfocusedTextColor = CleanWhite),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val qt = transferQtyInput.toIntOrNull() ?: 0
                                if (transferProductInput.isNotBlank() && qt > 0) {
                                    viewModel.submitWarehouseTransfer(transferProductInput, "Main Warehouse", "Battersea storage", qt)
                                    transferProductInput = ""
                                    transferQtyInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.height(46.dp)
                        ) {
                            Text("INIT TRANSFER", color = PremiumBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 60.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(transfersList) { trans ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, PremiumBorderGray, RoundedCornerShape(8.dp)),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = trans.productName, color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${trans.fromWarehouse} → ${trans.toWarehouse}", color = MutedSlate, fontSize = 10.sp)
                                Text(text = "Units: ${trans.quantity}", color = SoftWhite, fontSize = 11.sp)
                            }
                            if (trans.status == "Pending") {
                                Button(
                                    onClick = { viewModel.executeWarehouseTransfer(trans) },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(24.dp),
                                    contentPadding = PaddingValues(horizontal = 8.dp)
                                ) {
                                    Text("MARK COMPLETED", color = PremiumBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            } else {
                                StatusBadge(text = "COMPLETED", color = NeonGreen)
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAdjustDialog != null) {
        val selectedProduct = showAdjustDialog!!
        Dialog(onDismissRequest = { showAdjustDialog = null }) {
            Surface(shape = RoundedCornerShape(12.dp), color = PremiumCardGray, modifier = Modifier.padding(16.dp)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Inventory Adjustment / Write-Off", color = CleanWhite, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "Product: ${selectedProduct.name} (Stock: ${selectedProduct.stock})", color = MutedSlate, fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = damageCountInput,
                        onValueChange = { damageCountInput = it },
                        placeholder = { Text("Count to deduct...", fontSize = 11.sp) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = damageReasonInput,
                        onValueChange = { damageReasonInput = it },
                        placeholder = { Text("Reason (e.g., Water Damage)", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAdjustDialog = null }) {
                            Text("CANCEL", color = SoftWhite)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val cnt = damageCountInput.toIntOrNull() ?: 0
                                if (cnt > 0) {
                                    viewModel.adjustDamageStock(selectedProduct, cnt, damageReasonInput)
                                    showAdjustDialog = null
                                    damageCountInput = ""
                                    damageReasonInput = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed)
                        ) {
                            Text("DEDUCT DAMAGE", color = CleanWhite)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 5. FINANCE VIEW ----------------------
@Composable
fun FinanceView(viewModel: PurManViewModel) {
    val expensesList by viewModel.expenses.collectAsState()
    val rawArAmt = viewModel.getAccountsReceivable()
    val rawApAmt = viewModel.getAccountsPayable()

    val currencySymbol = viewModel.currentCurrencySymbol()

    var expenseAmt by remember { mutableStateOf("") }
    var expenseCat by remember { mutableStateOf("Utilities") }
    var expenseDesc by remember { mutableStateOf("") }

    val categories = listOf("Utilities", "Logistics", "Office Rentals", "Marketing", "Fines & Damaged")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 60.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Accounts Receivables and Payables dashboard
        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Card(
                    modifier = Modifier.weight(1f).border(1.dp, PremiumBorderGray, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACCOUNTS RECEIVABLE", color = MutedSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$currencySymbol${String.format("%.2f", rawArAmt)}", color = NeonGreen, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("Expected Customers Dues", color = MutedSlate, fontSize = 9.sp)
                    }
                }

                Card(
                    modifier = Modifier.weight(1f).border(1.dp, PremiumBorderGray, RoundedCornerShape(10.dp)),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("ACCOUNTS PAYABLE", color = MutedSlate, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "$currencySymbol${String.format("%.2f", rawApAmt)}", color = ErrorRed, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("Supplier Unpaid Invoices", color = MutedSlate, fontSize = 9.sp)
                    }
                }
            }
        }

        // Add expense log
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("ADD BUSINESS OPERATION EXPENSE", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = expenseAmt,
                            onValueChange = { expenseAmt = it },
                            placeholder = { Text("Expense value...", fontSize = 11.sp) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                        )

                        // Dropdown choice simulation
                        var showCats by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1.2f)
                                .height(46.dp)
                                .background(PremiumBlack, RoundedCornerShape(4.dp))
                                .clickable { showCats = true }
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(text = "Cat: $expenseCat", color = NeonGreen, fontSize = 11.sp)
                        }

                        if (showCats) {
                            Dialog(onDismissRequest = { showCats = false }) {
                                Surface(shape = RoundedCornerShape(12.dp), color = PremiumCardGray, modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        categories.forEach { cat ->
                                            Text(
                                                text = cat,
                                                color = SoftWhite,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        expenseCat = cat
                                                        showCats = false
                                                    }
                                                    .padding(vertical = 10.dp),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextField(
                        value = expenseDesc,
                        onValueChange = { expenseDesc = it },
                        placeholder = { Text("Expense brief description...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            val amt = expenseAmt.toDoubleOrNull() ?: 0.0
                            if (amt > 0.0) {
                                viewModel.addExpense(amt, expenseCat, expenseDesc)
                                expenseAmt = ""
                                expenseDesc = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("SUBMIT EXPENSE OUTFLOW", color = PremiumBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }
        }

        // List expense histories
        item {
            val netOut = expensesList.sumOf { it.amount }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Operational Incurred Expenses (${expensesList.size})", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(text = "Sum: $currencySymbol${String.format("%.2f", netOut)}", color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        if (expensesList.isEmpty()) {
            item { Text("No logged utility payments.", color = MutedSlate, fontSize = 11.sp) }
        } else {
            items(expensesList) { exp ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                    border = BorderStroke(1.dp, PremiumBorderGray)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(text = exp.category, color = ErrorRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text(text = exp.description, color = SoftWhite, fontSize = 11.sp)
                            Text(text = "Logged by: ${exp.registeredBy}", color = MutedSlate, fontSize = 9.sp)
                        }
                        Text(text = "$currencySymbol${String.format("%.2f", exp.amount)}", color = CleanWhite, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


// ---------------------- 6. CRM LOG VIEW ----------------------
@Composable
fun CrmView(viewModel: PurManViewModel) {
    val ticketsList by viewModel.tickets.collectAsState()
    val announcementsList by viewModel.announcements.collectAsState()

    var customerNameInput by remember { mutableStateOf("") }
    var priorityInput by remember { mutableStateOf("Medium") }
    var complaintInput by remember { mutableStateOf("") }

    var selectedTab by remember { mutableStateOf("TICKETS") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedTab == "TICKETS") 0 else 1,
            containerColor = PremiumBlack,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedTab == "TICKETS") 0 else 1]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(selected = (selectedTab == "TICKETS"), onClick = { selectedTab = "TICKETS" }) {
                Text("SUPPORT TICKETS COMPLAINT", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == "TICKETS") NeonGreen else MutedSlate)
            }
            Tab(selected = (selectedTab == "BROADCASTLOGS"), onClick = { selectedTab = "BROADCASTLOGS" }) {
                Text("SMS / EMAIL REMINDERS", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedTab == "BROADCASTLOGS") NeonGreen else MutedSlate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedTab == "TICKETS") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("FILE CUSTOMER CRM TICKETING COMPLAINT", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        TextField(
                            value = customerNameInput,
                            onValueChange = { customerNameInput = it },
                            placeholder = { Text("Customer Profile...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                        )

                        // Quick Priority setting
                        var showPrios by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .background(PremiumBlack, RoundedCornerShape(4.dp))
                                .clickable { showPrios = true }
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(text = "Prio: $priorityInput", color = NeonGreen, fontSize = 11.sp)
                        }

                        if (showPrios) {
                            Dialog(onDismissRequest = { showPrios = false }) {
                                Surface(shape = RoundedCornerShape(12.dp), color = PremiumCardGray, modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.padding(14.dp)) {
                                        listOf("High", "Medium", "Low").forEach { p ->
                                            Text(
                                                text = p,
                                                color = SoftWhite,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        priorityInput = p
                                                        showPrios = false
                                                    }
                                                    .padding(vertical = 10.dp),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    TextField(
                        value = complaintInput,
                        onValueChange = { complaintInput = it },
                        placeholder = { Text("Details of issue...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (customerNameInput.isNotBlank() && complaintInput.isNotBlank()) {
                                viewModel.createSupportTicket(customerNameInput, complaintInput, priorityInput)
                                customerNameInput = ""
                                complaintInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("DISPATCH TICKETING TICKET", color = PremiumBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(ticketsList) { t ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                        border = BorderStroke(1.dp, PremiumBorderGray)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Column {
                                    Text(text = "Cust: ${t.customerName}", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text(text = "Logged: ${t.dateStr}", color = MutedSlate, fontSize = 9.sp)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    StatusBadge(text = t.priority.uppercase(), color = if (t.priority == "High") ErrorRed else WarningOrange)
                                    StatusBadge(text = t.status.uppercase(), color = if (t.status == "Resolved") NeonGreenSecondary else InfoBlue)
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = t.complaint, color = SoftWhite, fontSize = 11.sp)
                            
                            if (t.status != "Resolved") {
                                Spacer(modifier = Modifier.height(6.dp))
                                Button(
                                    onClick = { viewModel.updateTicketStatus(t, "Resolved") },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                                    shape = RoundedCornerShape(4.dp),
                                    modifier = Modifier.height(22.dp),
                                    contentPadding = PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("MARK AS RESOLVED", color = PremiumBlack, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Reminders / Alerts blasting
            var broadcastTitle by remember { mutableStateOf("") }
            var broadcastBody by remember { mutableStateOf("") }
            var deliveryMthd by remember { mutableStateOf("SMS Alerts") }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("MANUAL ACCOUNTS REMINDER BLAST", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = broadcastTitle,
                        onValueChange = { broadcastTitle = it },
                        placeholder = { Text("Title...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    TextField(
                        value = broadcastBody,
                        onValueChange = { broadcastBody = it },
                        placeholder = { Text("SMS / Email text context...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            listOf("SMS Alerts", "Email Logs", "Notice").forEach { choice ->
                                val isChosen = deliveryMthd == choice
                                Text(
                                    text = choice,
                                    color = if (isChosen) NeonGreen else MutedSlate,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .background(if (isChosen) DeepNeonBg else Color.Transparent, RoundedCornerShape(4.dp))
                                        .clickable { deliveryMthd = choice }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Button(
                            onClick = {
                                if (broadcastTitle.isNotBlank() && broadcastBody.isNotBlank()) {
                                    viewModel.dispatchAnnouncementAlert(broadcastTitle, broadcastBody, deliveryMthd)
                                    broadcastTitle = ""
                                    broadcastBody = ""
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("SEND BLAST", color = PremiumBlack, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(announcementsList) { ann ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                        border = BorderStroke(1.dp, PremiumBorderGray)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(text = ann.title, color = NeonGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = ann.dateStr, color = MutedSlate, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = ann.content, color = SoftWhite, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Deliver Chan: ${ann.type}", color = MutedSlate, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 7. STAFF HUB VIEW ----------------------
@Composable
fun StaffView(viewModel: PurManViewModel) {
    val staffList by viewModel.staff.collectAsState()
    val auditLogList by viewModel.auditLogs.collectAsState()

    var staffNameInput by remember { mutableStateOf("") }
    var staffRoleInput by remember { mutableStateOf("Cashier") }
    var staffShiftInput by remember { mutableStateOf("Morning (08:00 - 16:00)") }
    var staffDeptInput by remember { mutableStateOf("Sales") }

    var selectedSection by remember { mutableStateOf("ROSTER") }

    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = if (selectedSection == "ROSTER") 0 else 1,
            containerColor = PremiumBlack,
            contentColor = NeonGreen,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[if (selectedSection == "ROSTER") 0 else 1]),
                    color = NeonGreen
                )
            }
        ) {
            Tab(selected = (selectedSection == "ROSTER"), onClick = { selectedSection = "ROSTER" }) {
                Text("STAFF & ATTENDANCE", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedSection == "ROSTER") NeonGreen else MutedSlate)
            }
            Tab(selected = (selectedSection == "AUDIT"), onClick = { selectedSection = "AUDIT" }) {
                Text("SECURITY AUDIT TRAIL LOGS", modifier = Modifier.padding(12.dp), fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (selectedSection == "AUDIT") NeonGreen else MutedSlate)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (selectedSection == "ROSTER") {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("REGISTER TEAM MEMBER & SHIFT", color = CleanWhite, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = staffNameInput,
                        onValueChange = { staffNameInput = it },
                        placeholder = { Text("Employee Name...", fontSize = 11.sp) },
                        modifier = Modifier.fillMaxWidth().height(46.dp),
                        colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Quick Role selection drop
                        var showRoleDrops by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp)
                                .background(PremiumBlack, RoundedCornerShape(4.dp))
                                .clickable { showRoleDrops = true }
                                .padding(horizontal = 8.dp, vertical = 12.dp)
                        ) {
                            Text(text = "Role: $staffRoleInput", color = NeonGreen, fontSize = 11.sp)
                        }

                        if (showRoleDrops) {
                            Dialog(onDismissRequest = { showRoleDrops = false }) {
                                Surface(shape = RoundedCornerShape(12.dp), color = PremiumCardGray, modifier = Modifier.padding(16.dp)) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        listOf("Admin", "Manager", "Cashier", "Storekeeper").forEach { r ->
                                            Text(
                                                text = r,
                                                color = SoftWhite,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        staffRoleInput = r
                                                        showRoleDrops = false
                                                    }
                                                    .padding(vertical = 10.dp),
                                                fontSize = 12.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        TextField(
                            value = staffDeptInput,
                            onValueChange = { staffDeptInput = it },
                            placeholder = { Text("Dept (e.g. Sales)", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f).height(46.dp),
                            colors = TextFieldDefaults.colors(focusedContainerColor = PremiumBlack, unfocusedContainerColor = PremiumBlack, focusedTextColor = CleanWhite)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (staffNameInput.isNotBlank()) {
                                viewModel.registerStaff(staffNameInput, staffRoleInput, staffShiftInput, staffDeptInput)
                                staffNameInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen),
                        modifier = Modifier.fillMaxWidth().height(36.dp),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text("ADD TEAM MEMBER TO BRANCH", color = PremiumBlack, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 60.dp)
            ) {
                items(staffList) { employee ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                        border = BorderStroke(1.dp, PremiumBorderGray)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(text = employee.name, color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                Text(text = "${employee.roleStr}  •  Department: ${employee.department}", color = NeonGreen, fontSize = 11.sp)
                                Text(text = "Attendance: ${employee.statusStr}  •  Shift: ${employee.shiftInfoStr}", color = MutedSlate, fontSize = 10.sp)
                            }
                            IconButton(onClick = { viewModel.demoteOrDismissStaff(employee) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Delete, contentDescription = "D", tint = ErrorRed)
                            }
                        }
                    }
                }
            }
        } else {
            // Security Logs Audit Trail log
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 60.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(auditLogList) { log ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal),
                        border = BorderStroke(1.dp, PremiumBorderGray)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text(text = "[${log.actionStr}]", color = NeonGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(text = log.timestampStr, color = MutedSlate, fontSize = 10.sp)
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = log.details, color = SoftWhite, fontSize = 11.sp)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = "Executed by: ${log.username} (${log.roleStr})", color = MutedSlate, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}


// ---------------------- 8. SETTINGS VIEW ----------------------
@Composable
fun SettingsView(viewModel: PurManViewModel) {
    val context = LocalContext.current
    var companyName by remember { mutableStateOf("PurMAN Global Corp") }

    val activeRole by viewModel.currentUserRole.collectAsState()
    val activeCurrency by viewModel.selectedCurrency.collectAsState()
    val activeTax by viewModel.valTaxRate.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 60.dp, top = 10.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "ERP PROFILE & BRANDING", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    TextField(
                        value = companyName,
                        onValueChange = { companyName = it },
                        label = { Text("Display Company Name") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(focusedTextColor = CleanWhite, focusedContainerColor = PremiumBlack)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        listOf("USD ($)", "EUR (€)", "GBP (£)", "JPY (¥)").forEach { item ->
                            val sCode = item.take(3)
                            val isSelected = activeCurrency == sCode
                            Text(
                                text = item,
                                color = if (isSelected) NeonGreen else MutedSlate,
                                modifier = Modifier
                                    .border(1.dp, if (isSelected) NeonGreen else PremiumBorderGray, RoundedCornerShape(4.dp))
                                    .clickable {
                                        viewModel.selectedCurrency.value = sCode
                                        viewModel.logAction("CURRENCY_CHANGED", "Set core currency display rate to $sCode")
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "TAXATION ENGINE (VAT SYSTEM)", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text(text = "Custom Sales VAT / Tax (%):", color = SoftWhite, fontSize = 12.sp)
                        Text(text = "${activeTax}%", color = NeonGreen, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }

                    Slider(
                        value = activeTax.toFloat(),
                        onValueChange = { viewModel.valTaxRate.value = it.toDouble().coerceIn(0.0, 30.0) },
                        valueRange = 0f..30f,
                        colors = SliderDefaults.colors(thumbColor = NeonGreen, activeTrackColor = NeonGreen)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, PremiumBorderGray), RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "ERP INTEGRATION & API", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generative model REST connections are fully integrated. Ensure your GEMINI_API_KEY is configured in the AI Studio Settings panel.",
                        color = MutedSlate,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row {
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported Ledger Snapshot to CSV (Simulated)", Toast.LENGTH_SHORT).show()
                                viewModel.logAction("EXCEL_EXPORTED", "Successfully formatted financial ledgers for spreadsheet processing.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreenSecondary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("EXPORT CSV", color = CleanWhite, fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                Toast.makeText(context, "Exported Audit Logs to PDF (Simulated)", Toast.LENGTH_SHORT).show()
                                viewModel.logAction("PDF_EXPORTED", "Printed beautiful accounting statement for tax backup.")
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NeonGreenSecondary),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text("PRINT PDF", color = CleanWhite, fontSize = 11.sp)
                        }
                    }
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth().border(1.dp, PremiumBorderGray, RoundedCornerShape(12.dp)),
                colors = CardDefaults.cardColors(containerColor = PremiumDarkCharcoal)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "BACKUP & RECOVERY", color = CleanWhite, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                viewModel.triggerBackupSystem()
                                Toast.makeText(context, "Backup snapshot saved!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = PremiumBorderGray),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("BACKUP SNAPSHOT", color = CleanWhite, fontSize = 10.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.triggerRestoreSystem()
                                Toast.makeText(context, "Database integrity restored!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("RESTORE DATABASE", color = CleanWhite, fontSize = 10.sp)
                        }
                    }
                }
            }
        }
    }
}
