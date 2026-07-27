package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.screens.*
import com.example.ui.theme.CampRentTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.Localization
import com.example.util.StringKey

enum class AppNavRoute(val labelKey: StringKey, val icon: ImageVector) {
    DASHBOARD(StringKey.DASHBOARD, Icons.Default.Dashboard),
    INVENTORY(StringKey.INVENTORY, Icons.Default.Inventory2),
    NEW_BILL(StringKey.NEW_BILL, Icons.Default.ReceiptLong),
    RENTALS(StringKey.RENTALS, Icons.Default.HomeWork),
    QUOTATIONS(StringKey.QUOTATIONS, Icons.Default.RequestQuote),
    CUSTOMERS(StringKey.CUSTOMERS, Icons.Default.People),
    SETTINGS(StringKey.SETTINGS, Icons.Default.Settings)
}

class MainActivity : ComponentActivity() {
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val settings by mainViewModel.settings.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }
            val userMessage by mainViewModel.userMessage.collectAsState()

            LaunchedEffect(userMessage) {
                userMessage?.let { msg ->
                    snackbarHostState.showSnackbar(msg)
                    mainViewModel.clearUserMessage()
                }
            }

            CampRentTheme(
                themeMode = settings.themeMode,
                palette = settings.palette
            ) {
                MainAppScreen(
                    viewModel = mainViewModel,
                    settings = settings,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    snackbarHostState: SnackbarHostState
) {
    var currentRoute by remember { mutableStateOf(AppNavRoute.DASHBOARD) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.primary,
                            shadowElevation = 2.dp
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Landscape,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimary,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = settings.businessName,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "NATURE GREEN THEME",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.2.sp
                                ),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                },
                actions = {
                    Surface(
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(40.dp),
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        IconButton(onClick = { currentRoute = AppNavRoute.SETTINGS }) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        },
        bottomBar = {
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 1.dp)
                NavigationBar(
                    windowInsets = WindowInsets.navigationBars,
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                ) {
                    AppNavRoute.entries.forEach { route ->
                        val label = Localization.getString(route.labelKey, settings.language)
                        val isSelected = currentRoute == route
                        NavigationBarItem(
                            selected = isSelected,
                            onClick = { currentRoute = route },
                            icon = { Icon(imageVector = route.icon, contentDescription = label) },
                            label = {
                                Text(
                                    text = label,
                                    maxLines = 1,
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                    )
                                )
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                selectedTextColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (currentRoute) {
                AppNavRoute.DASHBOARD -> DashboardScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onNavigateToNewBill = { currentRoute = AppNavRoute.NEW_BILL },
                    onNavigateToInventory = { currentRoute = AppNavRoute.INVENTORY },
                    onNavigateToQuotations = { currentRoute = AppNavRoute.QUOTATIONS },
                    onNavigateToRentals = { currentRoute = AppNavRoute.RENTALS }
                )
                AppNavRoute.INVENTORY -> InventoryScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onAddToCart = { item ->
                        viewModel.addToCart(item)
                        currentRoute = AppNavRoute.NEW_BILL
                    }
                )
                AppNavRoute.NEW_BILL -> BillingScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onBillCreated = { billId -> currentRoute = AppNavRoute.RENTALS },
                    onQuotationCreated = { quoteId -> currentRoute = AppNavRoute.QUOTATIONS }
                )
                AppNavRoute.RENTALS -> RentalsListScreen(
                    viewModel = viewModel,
                    settings = settings
                )
                AppNavRoute.QUOTATIONS -> QuotationsScreen(
                    viewModel = viewModel,
                    settings = settings,
                    onNavigateToNewQuote = { currentRoute = AppNavRoute.NEW_BILL }
                )
                AppNavRoute.CUSTOMERS -> CustomerCrmScreen(
                    viewModel = viewModel,
                    settings = settings
                )
                AppNavRoute.SETTINGS -> SettingsScreen(
                    viewModel = viewModel,
                    settings = settings
                )
            }
        }
    }
}

