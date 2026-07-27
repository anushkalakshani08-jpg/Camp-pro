package com.example.ui.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.AppSettings
import com.example.data.model.RentalBillEntity
import com.example.data.model.RentalStatus
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils
import com.example.util.Localization
import com.example.util.PdfGenerator
import com.example.util.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RentalsListScreen(
    viewModel: MainViewModel,
    settings: AppSettings
) {
    val context = LocalContext.current
    val bills by viewModel.allBills.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf("ACTIVE") } // ALL, ACTIVE, DUE_TODAY, OVERDUE, RETURNED

    val startOfDay = DateUtils.getTodayStartMs()
    val endOfDay = DateUtils.getTodayEndMs()

    val filteredBills = bills.filter { bill ->
        val matchesSearch = bill.billNumber.contains(searchQuery, ignoreCase = true) ||
                bill.customerName.contains(searchQuery, ignoreCase = true) ||
                bill.customerPhone.contains(searchQuery, ignoreCase = true)

        val matchesTab = when (selectedTab) {
            "ACTIVE" -> bill.status == RentalStatus.ACTIVE
            "DUE_TODAY" -> bill.status == RentalStatus.ACTIVE && bill.endDate in startOfDay..endOfDay
            "OVERDUE" -> bill.status == RentalStatus.ACTIVE && bill.endDate < startOfDay
            "RETURNED" -> bill.status == RentalStatus.RETURNED
            else -> true
        }

        matchesSearch && matchesTab
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search by Bill # or Customer...") },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            ),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status Tabs
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            val tabs = listOf(
                "ACTIVE" to "Active",
                "DUE_TODAY" to "Due Today",
                "OVERDUE" to "Overdue",
                "RETURNED" to "Returned",
                "ALL" to "All Bills"
            )
            items(tabs) { (tabKey, tabLabel) ->
                FilterChip(
                    selected = selectedTab == tabKey,
                    onClick = { selectedTab = tabKey },
                    label = { Text(tabLabel) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredBills.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = Localization.getString(StringKey.NO_DATA, settings.language),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 90.dp)
            ) {
                items(filteredBills) { bill ->
                    RentalBillItemCard(
                        bill = bill,
                        settings = settings,
                        context = context,
                        onMarkReturned = { viewModel.markBillReturned(bill.id) },
                        onDelete = { viewModel.deleteBill(bill) },
                        onGeneratePdf = {
                            viewModel.generateBillPdfAndShare(context, bill) { pdfFile ->
                                // Open PDF or share
                                PdfGenerator.sharePdfViaWhatsApp(context, pdfFile, bill.customerPhone)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun RentalBillItemCard(
    bill: RentalBillEntity,
    settings: AppSettings,
    context: Context,
    onMarkReturned: () -> Unit,
    onDelete: () -> Unit,
    onGeneratePdf: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = bill.billNumber,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
                StatusBadge(status = bill.status)
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${bill.customerName} (${bill.customerPhone})",
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
                text = "Period: ${DateUtils.formatDate(bill.startDate)} → ${DateUtils.formatDate(bill.endDate)} (${bill.totalDays} Days)",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = DateUtils.formatCurrency(bill.grandTotal),
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Deposit: ${DateUtils.formatCurrency(bill.totalDeposit)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    // Call Button
                    IconButton(onClick = {
                        val callIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${bill.customerPhone}")
                        }
                        context.startActivity(callIntent)
                    }) {
                        Icon(imageVector = Icons.Default.Call, contentDescription = "Call Customer", tint = MaterialTheme.colorScheme.primary)
                    }

                    // PDF / WhatsApp Share Button
                    IconButton(onClick = onGeneratePdf) {
                        Icon(imageVector = Icons.Default.PictureAsPdf, contentDescription = "PDF Invoice", tint = MaterialTheme.colorScheme.secondary)
                    }

                    if (bill.status == RentalStatus.ACTIVE) {
                        IconButton(onClick = onMarkReturned) {
                            Icon(imageVector = Icons.Default.AssignmentTurnedIn, contentDescription = "Mark Returned", tint = MaterialTheme.colorScheme.tertiary)
                        }
                    }

                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Bill", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
