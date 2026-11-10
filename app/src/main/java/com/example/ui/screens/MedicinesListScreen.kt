package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.Medicine
import com.example.ui.theme.*

@Composable
fun MedicinesListScreen(
    medicines: List<Medicine>,
    isSeniorMode: Boolean,
    onAddMedicine: () -> Unit,
    onDeleteMedicine: (Medicine) -> Unit,
    onAdjustStock: (Medicine, Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredMedicines = remember(medicines, searchQuery) {
        if (searchQuery.isBlank()) medicines
        else medicines.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.brandName.contains(searchQuery, ignoreCase = true) ||
                    it.genericName.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_medicine")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Medicine")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "My Medicines",
                fontSize = if (isSeniorMode) 28.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${medicines.size} active prescribed medicines",
                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 14.dp)
            )

            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search medicine name or type...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_search_medicines")
            )

            if (filteredMedicines.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(72.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Medication, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No medicine found matching '$searchQuery'" else "No medicines added yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap the + button to add your prescribed medicines.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredMedicines, key = { it.id }) { med ->
                        MedicineCard(
                            medicine = med,
                            isSeniorMode = isSeniorMode,
                            onDelete = { onDeleteMedicine(med) },
                            onAdjustStock = { qty -> onAdjustStock(med, qty) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun MedicineCard(
    medicine: Medicine,
    isSeniorMode: Boolean,
    onDelete: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    var showStockDialog by remember { mutableStateOf(false) }

    val isLowStock = medicine.isStockTrackingEnabled && medicine.stockQuantity <= medicine.stockThreshold

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(if (isSeniorMode) 52.dp else 44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Default.Medication,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = medicine.name,
                        fontSize = if (isSeniorMode) 22.sp else 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${medicine.medicineType} • ${medicine.doseInstruction} (${medicine.foodInstruction})",
                        fontSize = if (isSeniorMode) 15.sp else 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }

            if (medicine.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Notes: ${medicine.notes}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            // Stock badge & refill alert
            if (medicine.isStockTrackingEnabled) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isLowStock) HealthWarningContainer else HealthSuccessContainer
                    ) {
                        Text(
                            text = if (isLowStock) "⚠️ Low Stock: ${medicine.stockQuantity} remaining" else "Stock: ${medicine.stockQuantity} remaining",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) HealthWarning else HealthSuccess,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }

                    TextButton(onClick = { showStockDialog = true }) {
                        Text("Refill / Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    if (showStockDialog) {
        var inputStock by remember { mutableStateOf(medicine.stockQuantity.toString()) }
        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { Text("Update Stock for ${medicine.name}") },
            text = {
                OutlinedTextField(
                    value = inputStock,
                    onValueChange = { inputStock = it },
                    label = { Text("Remaining Units") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(onClick = {
                    val newQty = inputStock.toIntOrNull() ?: medicine.stockQuantity
                    onAdjustStock(newQty)
                    showStockDialog = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
