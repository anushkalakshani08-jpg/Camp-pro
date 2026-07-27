package com.example.ui.screens

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.data.model.ItemEntity
import com.example.ui.viewmodel.MainViewModel
import com.example.util.DateUtils
import com.example.util.Localization
import com.example.util.StringKey

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: MainViewModel,
    settings: AppSettings,
    onAddToCart: (ItemEntity) -> Unit
) {
    val items by viewModel.allItems.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showAddDialog by remember { mutableStateOf(false) }
    var itemToEdit by remember { mutableStateOf<ItemEntity?>(null) }

    val categories = listOf(
        "ALL" to Localization.getString(StringKey.ALL_CATEGORIES, settings.language),
        "Tents & Shelters" to Localization.getString(StringKey.TENTS, settings.language),
        "Cooking & BBQ" to Localization.getString(StringKey.COOKING, settings.language),
        "Sleeping & Mats" to Localization.getString(StringKey.SLEEPING, settings.language),
        "Lighting & Power" to Localization.getString(StringKey.LIGHTING, settings.language),
        "Hiking Gear" to Localization.getString(StringKey.GEAR, settings.language)
    )

    val filteredItems = items.filter { item ->
        val matchesCategory = (selectedCategory == "ALL" || item.category == selectedCategory)
        val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) || item.category.contains(searchQuery, ignoreCase = true)
        matchesCategory && matchesSearch
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    itemToEdit = null
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Equipment Item")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(Localization.getString(StringKey.SEARCH_PLACEHOLDER, settings.language)) },
                leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
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

            // Category Filter Chips
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(categories) { (catKey, catLabel) ->
                    FilterChip(
                        selected = selectedCategory == catKey,
                        onClick = { selectedCategory = catKey },
                        label = { Text(catLabel) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Inventory Item Cards
            if (filteredItems.isEmpty()) {
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
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredItems) { item ->
                        InventoryItemCard(
                            item = item,
                            settings = settings,
                            onEdit = {
                                itemToEdit = item
                                showAddDialog = true
                            },
                            onDelete = { viewModel.deleteItem(item) },
                            onAddToCart = { onAddToCart(item) }
                        )
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        ItemEditorDialog(
            item = itemToEdit,
            settings = settings,
            onDismiss = { showAddDialog = false },
            onSave = { updatedItem ->
                viewModel.saveItem(updatedItem)
                showAddDialog = false
            }
        )
    }
}

@Composable
fun InventoryItemCard(
    item: ItemEntity,
    settings: AppSettings,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onAddToCart: () -> Unit
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = item.category,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit Item", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            if (item.description.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = item.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${DateUtils.formatCurrency(item.dailyPrice)} / day",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Text(
                        text = "Deposit: ${DateUtils.formatCurrency(item.depositAmount)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.Gray
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "Avail: ${item.availableQuantity} / ${item.stockQuantity}",
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            color = if (item.availableQuantity > 0) MaterialTheme.colorScheme.primary else Color.Red
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = onAddToCart,
                        enabled = item.availableQuantity > 0,
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = Color.White
                        )
                    ) {
                        Icon(imageVector = Icons.Default.AddShoppingCart, contentDescription = "Add to Bill Cart")
                    }
                }
            }
        }
    }
}

@Composable
fun ItemEditorDialog(
    item: ItemEntity?,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onSave: (ItemEntity) -> Unit
) {
    var name by remember { mutableStateOf(item?.name ?: "") }
    var category by remember { mutableStateOf(item?.category ?: "Tents & Shelters") }
    var dailyPriceStr by remember { mutableStateOf(item?.dailyPrice?.toString() ?: "") }
    var stockStr by remember { mutableStateOf(item?.stockQuantity?.toString() ?: "") }
    var depositStr by remember { mutableStateOf(item?.depositAmount?.toString() ?: "") }
    var description by remember { mutableStateOf(item?.description ?: "") }

    val categories = listOf(
        "Tents & Shelters",
        "Cooking & BBQ",
        "Sleeping & Mats",
        "Lighting & Power",
        "Hiking Gear",
        "Electronics"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (item == null) "Add Camping Equipment" else "Edit Equipment") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Equipment Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                OutlinedTextField(
                    value = dailyPriceStr,
                    onValueChange = { dailyPriceStr = it },
                    label = { Text("Daily Rental Rate (LKR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = stockStr,
                        onValueChange = { stockStr = it },
                        label = { Text("Stock Quantity") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = depositStr,
                        onValueChange = { depositStr = it },
                        label = { Text("Refundable Deposit") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }

                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description / Specs") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val price = dailyPriceStr.toDoubleOrNull() ?: 0.0
                    val stock = stockStr.toIntOrNull() ?: 1
                    val deposit = depositStr.toDoubleOrNull() ?: 0.0

                    val newItem = item?.copy(
                        name = name.trim(),
                        category = category,
                        dailyPrice = price,
                        stockQuantity = stock,
                        depositAmount = deposit,
                        description = description.trim()
                    ) ?: ItemEntity(
                        name = name.trim(),
                        category = category,
                        dailyPrice = price,
                        stockQuantity = stock,
                        depositAmount = deposit,
                        description = description.trim()
                    )

                    onSave(newItem)
                },
                enabled = name.isNotBlank() && dailyPriceStr.isNotBlank()
            ) {
                Text(Localization.getString(StringKey.ACTION_SAVE, settings.language))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(Localization.getString(StringKey.ACTION_CANCEL, settings.language))
            }
        }
    )
}
