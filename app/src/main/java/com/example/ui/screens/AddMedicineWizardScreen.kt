package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.Medicine
import com.example.ui.theme.*
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMedicineWizardScreen(
    onDismiss: () -> Unit,
    onSaveMedicine: (
        medicine: Medicine,
        times: List<Pair<Int, Int>>, // hour to minute
        selectedDays: List<Int>
    ) -> Unit
) {
    var step by remember { mutableStateOf(1) }

    // Step 1: Info
    var name by remember { mutableStateOf("") }
    var brandName by remember { mutableStateOf("") }
    var genericName by remember { mutableStateOf("") }
    var medicineType by remember { mutableStateOf("Tablet") }
    var strength by remember { mutableStateOf("") }
    var doseInstruction by remember { mutableStateOf("1 Tablet") }
    var notes by remember { mutableStateOf("") }

    // Step 2: Frequency & Schedule
    var frequency by remember { mutableStateOf("Once daily") }
    var reminderTimes by remember {
        mutableStateOf(mutableListOf(Pair(8, 0)))
    }
    var selectedDays by remember {
        mutableStateOf(listOf(1, 2, 3, 4, 5, 6, 7)) // Mon to Sun
    }

    // Step 3: Food & Stock
    var foodInstruction by remember { mutableStateOf("After food") }
    var isStockTrackingEnabled by remember { mutableStateOf(false) }
    var stockQuantityStr by remember { mutableStateOf("30") }
    var stockThresholdStr by remember { mutableStateOf("5") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Add Medicine",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                        Text(
                            text = "Step $step of 3",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) step-- else onDismiss()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Back")
                        }
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.height(56.dp)
                        ) {
                            Text("Cancel")
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 3) {
                                if (name.isBlank()) {
                                    // simple validation
                                    return@Button
                                }
                                step++
                            } else {
                                val stockQty = stockQuantityStr.toIntOrNull() ?: 0
                                val stockThresh = stockThresholdStr.toIntOrNull() ?: 5
                                val med = Medicine(
                                    name = name.trim(),
                                    brandName = brandName.trim(),
                                    genericName = genericName.trim(),
                                    medicineType = medicineType,
                                    strength = strength.trim(),
                                    doseInstruction = doseInstruction.trim(),
                                    foodInstruction = foodInstruction,
                                    notes = notes.trim(),
                                    isStockTrackingEnabled = isStockTrackingEnabled,
                                    stockQuantity = stockQty,
                                    stockThreshold = stockThresh
                                )
                                onSaveMedicine(med, reminderTimes, selectedDays)
                            }
                        },
                        shape = RoundedCornerShape(16.dp),
                        enabled = name.isNotBlank(),
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("btn_wizard_next"),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text(
                            text = if (step == 3) "Save Medicine" else "Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            when (step) {
                1 -> {
                    // STEP 1: Basic Information
                    Text(
                        text = "Medicine Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enter the name and form prescribed by your doctor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name *") },
                        placeholder = { Text("e.g. Paracetamol, Atorvastatin") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_name")
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = strength,
                        onValueChange = { strength = it },
                        label = { Text("Strength / Dosage (Optional)") },
                        placeholder = { Text("e.g. 500mg, 10mg, 2.5ml") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = doseInstruction,
                        onValueChange = { doseInstruction = it },
                        label = { Text("Dose per Intake") },
                        placeholder = { Text("e.g. 1 Tablet, 2 Drops, 1 Puff") },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Medicine Form",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val types = listOf("Tablet", "Capsule", "Syrup", "Injection", "Drops", "Inhaler", "Cream", "Other")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.take(4).forEach { t ->
                            FilterChip(
                                selected = medicineType == t,
                                onClick = { medicineType = t },
                                label = { Text(t, fontSize = 12.sp) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        types.drop(4).forEach { t ->
                            FilterChip(
                                selected = medicineType == t,
                                onClick = { medicineType = t },
                                label = { Text(t, fontSize = 12.sp) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Doctor's Notes (Optional)") },
                        placeholder = { Text("e.g. Prescribed for high blood pressure") },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                2 -> {
                    // STEP 2: Timing & Frequency
                    Text(
                        text = "Reminder Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Choose how often and at what exact time to receive reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "Custom times")
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequencies.take(2).forEach { f ->
                            FilterChip(
                                selected = frequency == f,
                                onClick = {
                                    frequency = f
                                    if (f == "Once daily") reminderTimes = mutableListOf(Pair(8, 0))
                                    if (f == "Twice daily") reminderTimes = mutableListOf(Pair(8, 0), Pair(20, 0))
                                },
                                label = { Text(f) }
                            )
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        frequencies.drop(2).forEach { f ->
                            FilterChip(
                                selected = frequency == f,
                                onClick = {
                                    frequency = f
                                    if (f == "Three times daily") reminderTimes = mutableListOf(Pair(8, 0), Pair(14, 0), Pair(20, 0))
                                },
                                label = { Text(f) }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Reminder Times",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    reminderTimes.forEachIndexed { index, (h, m) ->
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, h)
                                    set(Calendar.MINUTE, m)
                                }
                                val formatted = java.text.SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "Dose ${index + 1}: $formatted",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp
                                    )
                                }

                                Row {
                                    IconButton(onClick = {
                                        TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val copy = reminderTimes.toMutableList()
                                                copy[index] = Pair(hour, minute)
                                                reminderTimes = copy
                                            },
                                            h,
                                            m,
                                            false
                                        ).show()
                                    }) {
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Time", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    if (reminderTimes.size > 1) {
                                        IconButton(onClick = {
                                            val copy = reminderTimes.toMutableList()
                                            copy.removeAt(index)
                                            reminderTimes = copy
                                        }) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete Time", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    TextButton(
                        onClick = {
                            val copy = reminderTimes.toMutableList()
                            copy.add(Pair(12, 0))
                            reminderTimes = copy
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Another Dose Time")
                    }
                }

                3 -> {
                    // STEP 3: Food Instruction & Stock
                    Text(
                        text = "Instructions & Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Set food timing and optionally track tablet stock for refill reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )

                    Text(
                        text = "Food Instructions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val foodOptions = listOf("Before food", "After food", "With food", "Empty stomach", "No specific instruction")
                    foodOptions.forEach { opt ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { foodInstruction = opt }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = foodInstruction == opt,
                                onClick = { foodInstruction = opt }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(opt, fontSize = 16.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Track Stock & Refills", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                    Text("Alert me when tablets run low", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Switch(
                                    checked = isStockTrackingEnabled,
                                    onCheckedChange = { isStockTrackingEnabled = it }
                                )
                            }

                            if (isStockTrackingEnabled) {
                                Spacer(modifier = Modifier.height(14.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = stockQuantityStr,
                                        onValueChange = { stockQuantityStr = it },
                                        label = { Text("Current Stock") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = stockThresholdStr,
                                        onValueChange = { stockThresholdStr = it },
                                        label = { Text("Alert Below") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
