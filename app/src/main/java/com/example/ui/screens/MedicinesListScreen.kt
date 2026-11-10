package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
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
                    it.genericName.contains(searchQuery, ignoreCase = true) ||
                    it.medicineType.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedicine,
                containerColor = RoyalBlue,
                contentColor = PureWhite,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(20.dp), spotColor = RoyalBlue.copy(alpha = 0.4f))
                    .testTag("fab_add_medicine")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Medicine")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Medicine", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Screen Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "My Medicines",
                        fontSize = if (isSeniorMode) 28.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "${medicines.size} prescribed medicines active",
                        fontSize = if (isSeniorMode) 16.sp else 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = SoftBlue
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${medicines.size} Total",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Modern Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        "Search medicine name, brand, or form...",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                },
                leadingIcon = {
                    Icon(
                        Icons.Default.Search,
                        contentDescription = null,
                        tint = RoyalBlue
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = RoyalBlue,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .testTag("input_search_medicines")
            )

            if (filteredMedicines.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(36.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftBlue,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Medication,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "No matches found for '$searchQuery'"
                            else
                                "No medicines recorded yet",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isSeniorMode) 20.sp else 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "Try searching by generic name or tablet type."
                            else
                                "Tap 'Add Medicine' to save your prescribed doses, timing, and food instructions.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        if (searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(20.dp))
                            Button(
                                onClick = onAddMedicine,
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Add First Medicine", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(filteredMedicines, key = { it.id }) { med ->
                        PremiumMedicineCard(
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
private fun PremiumMedicineCard(
    medicine: Medicine,
    isSeniorMode: Boolean,
    onDelete: () -> Unit,
    onAdjustStock: (Int) -> Unit
) {
    var showStockDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isLowStock = medicine.isStockTrackingEnabled && medicine.stockQuantity <= medicine.stockThreshold

    val formIcon = when (medicine.medicineType.lowercase()) {
        "tablet", "capsule" -> Icons.Default.Medication
        "syrup", "liquid", "drops" -> Icons.Default.WaterDrop
        "injection" -> Icons.Default.Vaccines
        "inhaler" -> Icons.Default.Air
        else -> Icons.Default.MedicalServices
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            // Header Row: Form Icon + Name + Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = SoftBlue,
                    modifier = Modifier.size(if (isSeniorMode) 54.dp else 46.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = formIcon,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = medicine.name,
                            fontSize = if (isSeniorMode) 22.sp else 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (medicine.strength.isNotBlank()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = SoftBlue
                            ) {
                                Text(
                                    text = medicine.strength,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RoyalBlue,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "${medicine.medicineType} • ${medicine.doseInstruction} (${medicine.foodInstruction})",
                        fontSize = if (isSeniorMode) 15.sp else 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showDeleteConfirmDialog = true }) {
                    Icon(
                        Icons.Outlined.DeleteOutline,
                        contentDescription = "Delete",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                    )
                }
            }

            // Doctor's Notes (if any)
            if (medicine.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Info,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = medicine.notes,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Stock Tracking & Refill Alert
            if (medicine.isStockTrackingEnabled) {
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isLowStock) StatusMissedContainer else StatusTakenContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLowStock) Icons.Default.Warning else Icons.Default.Inventory2,
                                contentDescription = null,
                                tint = if (isLowStock) StatusMissed else StatusTaken,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isLowStock)
                                    "Low Stock: ${medicine.stockQuantity} remaining"
                                else
                                    "${medicine.stockQuantity} remaining in stock",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLowStock) StatusMissed else StatusTaken
                            )
                        }
                    }

                    TextButton(
                        onClick = { showStockDialog = true }
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Refill / Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Refill / Edit Stock Dialog
    if (showStockDialog) {
        var inputStock by remember { mutableStateOf(medicine.stockQuantity.toString()) }
        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = {
                Text(
                    text = "Refill / Update Stock",
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter current units remaining for ${medicine.name}:",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputStock,
                        onValueChange = { inputStock = it },
                        label = { Text("Remaining Units") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val newQty = inputStock.toIntOrNull() ?: medicine.stockQuantity
                        onAdjustStock(newQty)
                        showStockDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save Stock")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Medicine?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to remove ${medicine.name}? Its upcoming schedules will also be cancelled.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDelete()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Keep")
                }
            }
        )
    }
}
