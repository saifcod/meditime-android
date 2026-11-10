package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: (() -> Unit)? = null
) {
    val isSenior = userProfile?.seniorMode == true

    // Compute greeting based on local hour
    val hourOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val greeting = when {
        hourOfDay < 12 -> stringResource(R.string.greeting_morning)
        hourOfDay < 17 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
    val nameGreeting = if (!userProfile?.name.isNullOrBlank()) "$greeting, ${userProfile?.name}" else greeting

    // Statistics calculated directly from real database records
    val totalDoses = todayDoses.size
    val takenDoses = todayDoses.count { it.status == "TAKEN" }
    val missedDoses = todayDoses.count { it.status == "MISSED" }
    val upcomingDoses = todayDoses.count { it.status == "SCHEDULED" || it.status == "SNOOZED" }
    val adherencePercent = if (totalDoses > 0) ((takenDoses.toFloat() / totalDoses) * 100).toInt() else 100

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen"),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp)
    ) {
        // 1. Beautiful Welcome Header
        item {
            val dateStr = SimpleDateFormat("EEEE, MMMM d", Locale.getDefault()).format(Date())
            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 18.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = dateStr.uppercase(),
                                fontSize = if (isSenior) 13.sp else 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = nameGreeting,
                            fontSize = if (isSenior) 26.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onBackground,
                            lineHeight = if (isSenior) 30.sp else 26.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isSenior) "Senior Care Mode Active" else "Smart Medication Reminder",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Avatar / Settings Shortcut
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier
                            .size(if (isSenior) 54.dp else 48.dp)
                            .clickable(enabled = onNavigateToSettings != null) {
                                onNavigateToSettings?.invoke()
                            }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (!userProfile?.name.isNullOrBlank()) {
                                Text(
                                    text = userProfile?.name?.take(1)?.uppercase() ?: "M",
                                    fontSize = if (isSenior) 22.sp else 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.HealthAndSafety,
                                    contentDescription = "Profile",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(if (isSenior) 30.dp else 24.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Medication Statistics Cards Grid (4 Compact Cards)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Daily Overview",
                        fontSize = if (isSenior) 18.sp else 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    if (totalDoses > 0) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (adherencePercent >= 80) StatusTakenContainer else StatusUpcomingContainer
                        ) {
                            Text(
                                text = "$adherencePercent% Complete",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (adherencePercent >= 80) StatusTaken else StatusUpcoming,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ModernStatCard(
                        title = "Total",
                        count = totalDoses.toString(),
                        icon = Icons.Default.Medication,
                        color = RoyalBlue,
                        bgColor = SoftBlue,
                        isSenior = isSenior,
                        modifier = Modifier.weight(1f)
                    )
                    ModernStatCard(
                        title = "Taken",
                        count = takenDoses.toString(),
                        icon = Icons.Default.CheckCircle,
                        color = StatusTaken,
                        bgColor = StatusTakenContainer,
                        isSenior = isSenior,
                        modifier = Modifier.weight(1f)
                    )
                    ModernStatCard(
                        title = "Upcoming",
                        count = upcomingDoses.toString(),
                        icon = Icons.Default.Schedule,
                        color = StatusUpcoming,
                        bgColor = StatusUpcomingContainer,
                        isSenior = isSenior,
                        modifier = Modifier.weight(1f)
                    )
                    ModernStatCard(
                        title = "Missed",
                        count = missedDoses.toString(),
                        icon = Icons.Default.ErrorOutline,
                        color = if (missedDoses > 0) StatusMissed else NavyTypographyMuted,
                        bgColor = if (missedDoses > 0) StatusMissedContainer else CleanSurfaceVariant,
                        isSenior = isSenior,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 3. Next Medication Card (Visual Highlight)
        item {
            if (nextDoseMedicine != null && nextDoseTimeMillis != null) {
                NextMedicineCard(
                    medicine = nextDoseMedicine,
                    scheduledTimeMillis = nextDoseTimeMillis,
                    isSeniorMode = isSenior,
                    onTake = onTakeNext,
                    onSnooze = onSnoozeNext,
                    onSkip = onSkipNext,
                    modifier = Modifier.padding(bottom = 22.dp)
                )
            } else {
                // All Doses Completed or No Pending
                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, StatusTaken.copy(alpha = 0.25f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 22.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = StatusTakenContainer,
                            modifier = Modifier.size(52.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.DoneAll,
                                    contentDescription = null,
                                    tint = StatusTaken,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (medicines.isEmpty()) "Ready to Organize Medicines" else "All Doses Up to Date!",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isSenior) 20.sp else 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (medicines.isEmpty())
                                    "Tap 'Add Medicine' below to create your schedule."
                                else
                                    "Great job! You have no more pending doses for today.",
                                fontSize = if (isSenior) 15.sp else 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // 4. Quick Action Shortcuts (Add Medicine & Schedule)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 22.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onNavigateToAddMedicine,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSenior) 64.dp else 52.dp)
                        .shadow(elevation = 4.dp, shape = RoundedCornerShape(18.dp), spotColor = RoyalBlue.copy(alpha = 0.3f))
                        .testTag("quick_action_add_medicine"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = RoyalBlue,
                        contentColor = PureWhite
                    )
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
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSenior) 64.dp else 52.dp)
                        .testTag("quick_action_schedule"),
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline)
                ) {
                    Icon(
                        Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Schedule",
                        fontSize = if (isSenior) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                TextButton(onClick = onNavigateToHistory) {
                    Text(
                        text = "View History",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 16.sp else 13.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        // 6. Timeline List or Empty State
        if (todayDoses.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = SoftBlue,
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.EventNote,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No doses scheduled for today",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add prescribed medicines with reminder times to automatically track doses.",
                            fontSize = if (isSenior) 15.sp else 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Button(
                            onClick = onNavigateToAddMedicine,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create First Schedule", fontWeight = FontWeight.Bold)
                        }
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

        // Bottom space so nav bar doesn't cut off
        item {
            Spacer(modifier = Modifier.height(36.dp))
        }
    }
}

@Composable
private fun ModernStatCard(
    title: String,
    count: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    isSenior: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                shape = CircleShape,
                color = bgColor,
                modifier = Modifier.size(if (isSenior) 36.dp else 30.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(if (isSenior) 20.dp else 16.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = count,
                fontSize = if (isSenior) 24.sp else 20.sp,
                fontWeight = FontWeight.ExtraBold,
                color = color
            )
            Text(
                text = title,
                fontSize = if (isSenior) 13.sp else 11.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
