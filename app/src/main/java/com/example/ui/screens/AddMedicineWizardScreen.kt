package com.example.ui.screens

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.Medicine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
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
                    Text(
                        text = "Add Medicine",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (step > 1) step-- else onDismiss()
                    }) {
                        Icon(
                            imageVector = if (step > 1) Icons.Default.ArrowBack else Icons.Default.Close,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 6.dp,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                        ) {
                            Icon(Icons.Default.ArrowBack, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Back", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    } else {
                        OutlinedButton(
                            onClick = onDismiss,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .weight(1f)
                        ) {
                            Text("Cancel", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                    }

                    Button(
                        onClick = {
                            if (step < 3) {
                                if (name.isBlank()) return@Button
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
                            .height(52.dp)
                            .weight(1.5f)
                            .testTag("btn_wizard_next"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = RoyalBlue,
                            contentColor = PureWhite
                        )
                    ) {
                        Text(
                            text = if (step == 3) "Save Medicine" else "Continue",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (step == 3) Icons.Default.Check else Icons.Default.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
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
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            // Modern Step Progress Stepper
            WizardStepIndicator(currentStep = step)

            Spacer(modifier = Modifier.height(24.dp))

            when (step) {
                1 -> {
                    // STEP 1: Medicine Details
                    Text(
                        text = "Medicine Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Enter medicine name and form as prescribed by your doctor.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Medicine Name *") },
                        placeholder = { Text("e.g. Paracetamol, Atorvastatin") },
                        leadingIcon = { Icon(Icons.Default.Medication, contentDescription = null, tint = RoyalBlue) },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_medicine_name")
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = strength,
                            onValueChange = { strength = it },
                            label = { Text("Strength") },
                            placeholder = { Text("e.g. 500mg, 10mg") },
                            leadingIcon = { Icon(Icons.Default.Scale, contentDescription = null, tint = RoyalBlue) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = doseInstruction,
                            onValueChange = { doseInstruction = it },
                            label = { Text("Dose per Intake") },
                            placeholder = { Text("e.g. 1 Tablet") },
                            leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, tint = StatusTaken) },
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    Text(
                        text = "Medicine Form",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    val formTypes = listOf(
                        Triple("Tablet", Icons.Default.Medication, "Pill/Tab"),
                        Triple("Capsule", Icons.Default.LocalPharmacy, "Gel cap"),
                        Triple("Syrup", Icons.Default.WaterDrop, "Liquid"),
                        Triple("Injection", Icons.Default.Vaccines, "Needle"),
                        Triple("Drops", Icons.Default.Opacity, "Eye/Ear"),
                        Triple("Inhaler", Icons.Default.Air, "Breath"),
                        Triple("Cream", Icons.Default.Spa, "Ointment"),
                        Triple("Other", Icons.Default.MedicalServices, "Custom")
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 0 until formTypes.size step 2) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                FormTypeCard(
                                    name = formTypes[i].first,
                                    icon = formTypes[i].second,
                                    desc = formTypes[i].third,
                                    isSelected = medicineType == formTypes[i].first,
                                    onSelect = { medicineType = formTypes[i].first },
                                    modifier = Modifier.weight(1f)
                                )
                                if (i + 1 < formTypes.size) {
                                    FormTypeCard(
                                        name = formTypes[i + 1].first,
                                        icon = formTypes[i + 1].second,
                                        desc = formTypes[i + 1].third,
                                        isSelected = medicineType == formTypes[i + 1].first,
                                        onSelect = { medicineType = formTypes[i + 1].first },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    OutlinedTextField(
                        value = notes,
                        onValueChange = { notes = it },
                        label = { Text("Doctor's Notes (Optional)") },
                        placeholder = { Text("e.g. Prescribed for 14 days, take with plenty of water") },
                        leadingIcon = { Icon(Icons.Default.NoteAlt, contentDescription = null, tint = RoyalBlue) },
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }

                2 -> {
                    // STEP 2: Reminder Schedule
                    Text(
                        text = "Reminder Schedule",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Configure exact reminder times with smart audible alarms.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    val frequencies = listOf("Once daily", "Twice daily", "Three times daily", "Custom times")
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        frequencies.chunked(2).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                chunk.forEach { f ->
                                    val isSelected = frequency == f
                                    Card(
                                        shape = RoundedCornerShape(16.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isSelected) SoftBlue else MaterialTheme.colorScheme.surface
                                        ),
                                        border = BorderStroke(
                                            if (isSelected) 1.5.dp else 1.dp,
                                            if (isSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                        ),
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                frequency = f
                                                when (f) {
                                                    "Once daily" -> reminderTimes = mutableListOf(Pair(8, 0))
                                                    "Twice daily" -> reminderTimes = mutableListOf(Pair(8, 0), Pair(20, 0))
                                                    "Three times daily" -> reminderTimes = mutableListOf(Pair(8, 0), Pair(14, 0), Pair(20, 0))
                                                }
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(14.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = null,
                                                colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = f,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                fontSize = 14.sp,
                                                color = if (isSelected) DeepNavy else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    Text(
                        text = "Scheduled Alarm Times",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    reminderTimes.forEachIndexed { index, (h, m) ->
                        Card(
                            shape = RoundedCornerShape(18.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val cal = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, h)
                                    set(Calendar.MINUTE, m)
                                }
                                val formatted = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        shape = CircleShape,
                                        color = SoftBlue,
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Alarm,
                                                contentDescription = null,
                                                tint = RoyalBlue,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Dose ${index + 1}",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = formatted,
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 17.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
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
                                        Icon(Icons.Default.Edit, contentDescription = "Edit Time", tint = RoyalBlue)
                                    }
                                    if (reminderTimes.size > 1) {
                                        IconButton(onClick = {
                                            val copy = reminderTimes.toMutableList()
                                            copy.removeAt(index)
                                            reminderTimes = copy
                                        }) {
                                            Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Time", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    TextButton(
                        onClick = {
                            val copy = reminderTimes.toMutableList()
                            copy.add(Pair(12, 0))
                            reminderTimes = copy
                        },
                        colors = ButtonDefaults.textButtonColors(contentColor = RoyalBlue)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add Another Dose Time", fontWeight = FontWeight.Bold)
                    }
                }

                3 -> {
                    // STEP 3: Instructions & Inventory
                    Text(
                        text = "Instructions & Inventory",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Specify meal instructions and optional stock refill tracking.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 18.dp)
                    )

                    Text(
                        text = "Food Instructions",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onBackground,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    val foodOptions = listOf(
                        Pair("Before food", Icons.Default.Restaurant),
                        Pair("After food", Icons.Default.RestaurantMenu),
                        Pair("With food", Icons.Default.DinnerDining),
                        Pair("Empty stomach", Icons.Default.WaterDrop),
                        Pair("No specific instruction", Icons.Default.CheckCircle)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        foodOptions.forEach { (opt, icon) ->
                            val isSelected = foodInstruction == opt
                            Card(
                                shape = RoundedCornerShape(16.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) SoftBlue else MaterialTheme.colorScheme.surface
                                ),
                                border = BorderStroke(
                                    if (isSelected) 1.5.dp else 1.dp,
                                    if (isSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { foodInstruction = opt }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    RadioButton(
                                        selected = isSelected,
                                        onClick = { foodInstruction = opt },
                                        colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = if (isSelected) RoyalBlue else NavyTypographyMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = opt,
                                        fontSize = 15.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) DeepNavy else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(22.dp))

                    // Inventory Tracking Card
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = if (isStockTrackingEnabled) StatusTakenContainer else CleanSurfaceVariant,
                                        modifier = Modifier.size(42.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                Icons.Default.Inventory2,
                                                contentDescription = null,
                                                tint = if (isStockTrackingEnabled) StatusTaken else NavyTypographyMuted,
                                                modifier = Modifier.size(22.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Track Stock & Refills",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "Get notified before pills run out",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = isStockTrackingEnabled,
                                    onCheckedChange = { isStockTrackingEnabled = it },
                                    colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue)
                                )
                            }

                            if (isStockTrackingEnabled) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = stockQuantityStr,
                                        onValueChange = { stockQuantityStr = it },
                                        label = { Text("Current Stock") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = stockThresholdStr,
                                        onValueChange = { stockThresholdStr = it },
                                        label = { Text("Alert Below") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(14.dp),
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

@Composable
private fun WizardStepIndicator(currentStep: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val steps = listOf("Details", "Schedule", "Instructions")
        steps.forEachIndexed { index, title ->
            val stepNumber = index + 1
            val isCompleted = stepNumber < currentStep
            val isActive = stepNumber == currentStep

            val circleColor by animateColorAsState(
                targetValue = when {
                    isCompleted -> StatusTaken
                    isActive -> RoyalBlue
                    else -> CleanSurfaceVariant
                },
                label = "circleColor"
            )

            val textColor = when {
                isCompleted || isActive -> PureWhite
                else -> NavyTypographyMuted
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = circleColor,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(16.dp)
                            )
                        } else {
                            Text(
                                text = "$stepNumber",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = textColor
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = title,
                    fontSize = 11.sp,
                    fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium,
                    color = if (isActive) RoyalBlue else NavyTypographyMuted
                )
            }

            if (index < steps.size - 1) {
                Box(
                    modifier = Modifier
                        .weight(0.8f)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(if (stepNumber < currentStep) StatusTaken else CleanSurfaceVariant)
                )
            }
        }
    }
}

@Composable
private fun FormTypeCard(
    name: String,
    icon: ImageVector,
    desc: String,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SoftBlue else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            if (isSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        ),
        modifier = modifier.clickable { onSelect() }
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = if (isSelected) RoyalBlue else CleanSurfaceVariant,
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) PureWhite else RoyalBlue,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = name,
                    fontSize = 14.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) DeepNavy else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = desc,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
