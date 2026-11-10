package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.local.entities.DoseRecord
import com.example.data.local.entities.Medicine
import com.example.data.local.entities.UserProfile
import com.example.ui.components.DoseTimelineItem
import com.example.ui.components.NextMedicineCard
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    userProfile: UserProfile?,
    medicines: List<Medicine>,
    todayDoses: List<DoseRecord>,
    nextDoseMedicine: Medicine?,
    nextDoseTimeMillis: Long?,
    onTakeNext: () -> Unit,
    onSnoozeNext: () -> Unit,
    onSkipNext: () -> Unit,
    onTakeDose: (DoseRecord) -> Unit,
    onSkipDose: (DoseRecord) -> Unit,
    onNavigateToAddMedicine: () -> Unit,
    onNavigateToMedicines: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToHistory: () -> Unit
) {
    val isSenior = userProfile?.seniorMode == true

    // Compute greeting based on local hour
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hourOfDay < 12 -> stringResource(R.string.greeting_morning)
        hourOfDay < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
    val nameGreeting = if (!userProfile?.name.isNullOrBlank()) "$greeting, ${userProfile?.name}!" else "$greeting!"

    // Statistics calculated directly from real database records
    val totalDoses = todayDoses.size
    val takenDoses = todayDoses.count { it.status == "TAKEN" }
    val missedDoses = todayDoses.count { it.status == "MISSED" }
    val upcomingDoses = todayDoses.count { it.status == "SCHEDULED" || it.status == "SNOOZED" }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp)
    ) {
        // 1. Header Greeting & Date
        item {
            val dateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = dateStr.uppercase(),
                    fontSize = if (isSenior) 14.sp else 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = nameGreeting,
                    fontSize = if (isSenior) 30.sp else 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }

        // 2. Summary Statistics Card Row
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    StatPill("Total", totalDoses.toString(), MaterialTheme.colorScheme.primary, isSenior)
                    StatPill("Taken", takenDoses.toString(), HealthSuccess, isSenior)
                    StatPill("Upcoming", upcomingDoses.toString(), MedicalTealSecondary, isSenior)
                    StatPill("Missed", missedDoses.toString(), if (missedDoses > 0) HealthError else NavyTypographyMuted, isSenior)
                }
            }
        }

        // 3. Next Medicine Card (If available)
        item {
            if (nextDoseMedicine != null && nextDoseTimeMillis != null) {
                NextMedicineCard(
                    medicine = nextDoseMedicine,
                    scheduledTimeMillis = nextDoseTimeMillis,
                    isSeniorMode = isSenior,
                    onTake = onTakeNext,
                    onSnooze = onSnoozeNext,
                    onSkip = onSkipNext,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            } else {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HealthSuccessContainer,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = HealthSuccess)
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text(
                                text = "All Doses Up to Date!",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSenior) 20.sp else 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (medicines.isEmpty()) "Add a medicine below to begin tracking." else "No more doses pending for today.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Action Shortcuts
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAddMedicine,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSenior) 64.dp else 52.dp)
                        .testTag("quick_action_add_medicine"),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Medicine",
                        fontSize = if (isSenior) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onNavigateToSchedule,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSenior) 64.dp else 52.dp)
                        .testTag("quick_action_schedule")
                ) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule",
                        fontSize = if (isSenior) 17.sp else 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // 5. Today's Timeline Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule",
                    fontSize = if (isSenior) 22.sp else 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToHistory) {
                    Text("View History", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 6. Timeline List
        if (todayDoses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Default.EventNote,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "No doses scheduled for today",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 18.sp else 15.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Tap 'Add Medicine' above to create schedules.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(todayDoses, key = { it.id }) { dose ->
                DoseTimelineItem(
                    dose = dose,
                    isSeniorMode = isSenior,
                    onTake = { onTakeDose(dose) },
                    onSkip = { onSkipDose(dose) }
                )
            }
        }

        // Bottom space so FAB or nav bar doesn't cut off
        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun StatPill(
    label: String,
    value: String,
    color: Color,
    isSenior: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = if (isSenior) 26.sp else 22.sp,
            fontWeight = FontWeight.ExtraBold,
            color = color
        )
        Text(
            text = label,
            fontSize = if (isSenior) 14.sp else 11.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
