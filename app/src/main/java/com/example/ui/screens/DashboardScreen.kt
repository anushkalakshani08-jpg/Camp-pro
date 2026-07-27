package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.model.AppSettings
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalStatus
import com.example.ui.theme.AccentGold
import com.example.ui.theme.StatusActive
import com.example.ui.theme.StatusOverdue
import com.example.ui.viewmodel.DashboardStats
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils
import com.example.util.Localization
import com.example.util.StringKey

@Composable
fun DashboardScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    onNavigateToNewBill: () -> Unit,
    onNavigateToInventory: () -> Unit,
    onNavigateToQuotations: () -> Unit,
    onNavigateToRentals: () -> Unit
) {
    val stats by viewModel.dashboardStats.collectAsState()
    val bills by viewModel.allBills.collectAsState()
    val activeBills = bills.filter { it.status == RentalStatus.ACTIVE }.take(5)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Hero Banner Header
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(28.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_banner),
                        contentDescription = "Camping Hero Banner",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.85f)
                                    )
                                )
                            )
                    )
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(16.dp)
                    ) {
                        Text(
                            text = settings.businessName,
                            color = Color.White,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp
                            )
                        )
                        Text(
                            text = "Equipment Rental & Billing System",
                            color = com.example.ui.theme.NatureGreenPrimaryContainer,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium)
                        )
                    }
                }
            }
        }

        // Quick Stats Section
        item {
            Column {
                Text(
                    text = Localization.getString(StringKey.DASHBOARD, settings.language).uppercase(),
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Active Rentals Stat Card (Sleek Nature Light Green Card)
                    SleekStatCard(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.ACTIVE_RENTALS, settings.language),
                        value = "${stats.activeRentalsCount}",
                        containerColor = com.example.ui.theme.NatureGreenPrimaryContainer,
                        borderColor = com.example.ui.theme.NatureGreenOutlineVariant,
                        titleColor = com.example.ui.theme.NatureGreenOnPrimaryContainer,
                        valueColor = com.example.ui.theme.NatureGreenPrimary
                    )

                    // Due Today Stat Card (White Card with Red Badge)
                    SleekStatCard(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.DUE_TODAY, settings.language),
                        value = String.format("%02d", stats.dueTodayCount),
                        containerColor = MaterialTheme.colorScheme.surface,
                        borderColor = MaterialTheme.colorScheme.outline,
                        titleColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        valueColor = com.example.ui.theme.SleekRedPrimary,
                        badgeContent = {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(com.example.ui.theme.SleekRedContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Notifications,
                                    contentDescription = null,
                                    tint = com.example.ui.theme.SleekRedOnContainer,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Overdue Stat Card
                    SleekStatCard(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.OVERDUE, settings.language),
                        value = "${stats.overdueCount}",
                        containerColor = com.example.ui.theme.SleekRedContainer,
                        borderColor = MaterialTheme.colorScheme.outline,
                        titleColor = com.example.ui.theme.SleekRedOnContainer,
                        valueColor = com.example.ui.theme.SleekRedPrimary
                    )

                    // Total Revenue Stat Card
                    SleekStatCard(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.TOTAL_REVENUE, settings.language),
                        value = DateUtils.formatCurrency(stats.totalRevenue),
                        containerColor = com.example.ui.theme.NatureGreenSecondaryContainer,
                        borderColor = com.example.ui.theme.NatureGreenOutlineVariant,
                        titleColor = com.example.ui.theme.NatureGreenOnSecondaryContainer,
                        valueColor = com.example.ui.theme.NatureGreenPrimary
                    )
                }
            }
        }

        // Quick Workspace Shortcuts
        item {
            Column {
                Text(
                    text = "QUICK WORKSPACE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SleekWorkspaceButton(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.NEW_BILL, settings.language),
                        icon = Icons.Default.Add,
                        badgeBg = com.example.ui.theme.NatureGreenSecondaryContainer,
                        iconColor = com.example.ui.theme.NatureGreenPrimary,
                        onClick = onNavigateToNewBill
                    )
                    SleekWorkspaceButton(
                        modifier = Modifier.weight(1f),
                        title = Localization.getString(StringKey.QUOTATIONS, settings.language),
                        icon = Icons.Default.Description,
                        badgeBg = com.example.ui.theme.SleekAmberContainer,
                        iconColor = com.example.ui.theme.SleekAmberPrimary,
                        onClick = onNavigateToQuotations
                    )
                    SleekWorkspaceButton(
                        modifier = Modifier.weight(1f),
                        title = "CRM",
                        icon = Icons.Default.People,
                        badgeBg = com.example.ui.theme.SleekBlueContainer,
                        iconColor = com.example.ui.theme.SleekBluePrimary,
                        onClick = onNavigateToInventory
                    )
                }
            }
        }

        // Active Rentals Preview Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = Localization.getString(StringKey.ACTIVE_RENTALS, settings.language),
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                TextButton(onClick = onNavigateToRentals) {
                    Text("View All")
                }
            }
        }

        if (activeBills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = StatusActive
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = Localization.getString(StringKey.NO_DATA, settings.language),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        } else {
            items(activeBills) { bill ->
                ActiveBillCard(
                    bill = bill,
                    settings = settings,
                    onMarkReturned = { viewModel.markBillReturned(bill.id) }
                )
            }
        }
    }
}

@Composable
fun SleekStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    containerColor: Color,
    borderColor: Color,
    titleColor: Color,
    valueColor: Color,
    badgeContent: (@Composable () -> Unit)? = null
) {
    Card(
        modifier = modifier.height(112.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(28.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp
                ),
                color = titleColor
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp
                    ),
                    color = valueColor
                )
                if (badgeContent != null) {
                    badgeContent()
                }
            }
        }
    }
}

@Composable
fun SleekWorkspaceButton(
    modifier: Modifier = Modifier,
    title: String,
    icon: ImageVector,
    badgeBg: Color,
    iconColor: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(badgeBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ActiveBillCard(
    bill: RentalBillEntity,
    settings: AppSettings,
    onMarkReturned: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.DateRange,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = bill.billNumber,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Customer: ${bill.customerName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = DateUtils.formatCurrency(bill.grandTotal),
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                )
                Text(
                    text = "Due: ${DateUtils.formatDate(bill.endDate)}",
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (bill.status == RentalStatus.OVERDUE) com.example.ui.theme.SleekRedPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }
        }
    }
}

@Composable
fun StatusBadge(status: RentalStatus) {
    val (bgColor, textColor, text) = when (status) {
        RentalStatus.ACTIVE -> Triple(StatusActive.copy(alpha = 0.15f), StatusActive, "ACTIVE")
        RentalStatus.RETURNED -> Triple(Color.Gray.copy(alpha = 0.15f), Color.DarkGray, "RETURNED")
        RentalStatus.OVERDUE -> Triple(StatusOverdue.copy(alpha = 0.15f), StatusOverdue, "OVERDUE")
        RentalStatus.CANCELLED -> Triple(Color.Red.copy(alpha = 0.15f), Color.Red, "CANCELLED")
    }

    Surface(
        color = bgColor,
        shape = RoundedCornerShape(8.dp)
    ) {
        Text(
            text = text,
            color = textColor,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}
