package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.MedicationSchedule
import com.example.data.local.entities.Medicine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun ScheduleManagementScreen(
    medicines: List<Medicine>,
    schedules: List<MedicationSchedule>,
    isSeniorMode: Boolean,
    onToggleSchedule: (MedicationSchedule, Boolean) -> Unit,
    onDeleteSchedule: (MedicationSchedule) -> Unit,
    onAddNewSchedule: () -> Unit
) {
    val activeCount = schedules.count { it.isEnabled }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewSchedule,
                containerColor = RoyalBlue,
                contentColor = PureWhite,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .shadow(8.dp, shape = RoundedCornerShape(20.dp), spotColor = RoyalBlue.copy(alpha = 0.4f))
                    .testTag("fab_add_schedule")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.AddAlarm, contentDescription = "Add Schedule")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Add Schedule", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Medication Schedule",
                        fontSize = if (isSeniorMode) 28.sp else 24.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "$activeCount active alarms configured",
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
                            imageVector = Icons.Default.Alarm,
                            contentDescription = null,
                            tint = RoyalBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "$activeCount Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = RoyalBlue
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (schedules.isEmpty()) {
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
                                    Icons.Default.Alarm,
                                    contentDescription = null,
                                    tint = RoyalBlue,
                                    modifier = Modifier.size(38.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(18.dp))
                        Text(
                            text = "No schedules set up yet",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = if (isSeniorMode) 20.sp else 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Create reminder schedules for each dose to receive reliable audible alarms and notifications.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 13.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onAddNewSchedule,
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Create Schedule", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 100.dp)
                ) {
                    items(schedules, key = { it.id }) { schedule ->
                        val matchingMed = medicines.firstOrNull { it.id == schedule.medicineId }
                        val medName = matchingMed?.name ?: "Medicine"
                        val doseInfo = matchingMed?.doseInstruction ?: "1 Dose"

                        val cal = Calendar.getInstance().apply {
                            set(Calendar.HOUR_OF_DAY, schedule.timeHour)
                            set(Calendar.MINUTE, schedule.timeMinute)
                        }
                        val formattedTime = SimpleDateFormat("h:mm a", Locale.getDefault()).format(cal.time)

                        Card(
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(
                                1.dp,
                                if (schedule.isEnabled) RoyalBlue.copy(alpha = 0.35f)
                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (schedule.isEnabled) SoftBlue else CleanSurfaceVariant,
                                    modifier = Modifier.size(if (isSeniorMode) 54.dp else 46.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (schedule.isEnabled) RoyalBlue else NavyTypographyMuted,
                                            modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = if (isSeniorMode) 26.sp else 20.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (schedule.isEnabled) MaterialTheme.colorScheme.onSurface else NavyTypographyMuted
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = "$medName • $doseInfo",
                                        fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = schedule.isEnabled,
                                    onCheckedChange = { onToggleSchedule(schedule, it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = PureWhite,
                                        checkedTrackColor = RoyalBlue
                                    ),
                                    modifier = Modifier.testTag("switch_schedule_${schedule.id}")
                                )

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(onClick = { onDeleteSchedule(schedule) }) {
                                    Icon(
                                        Icons.Outlined.DeleteOutline,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
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
