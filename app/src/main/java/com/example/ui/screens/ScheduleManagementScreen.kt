package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddNewSchedule,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .padding(bottom = 72.dp)
                    .testTag("fab_add_schedule")
            ) {
                Icon(Icons.Default.AddAlarm, contentDescription = "Add Schedule")
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
                text = "Reminder Schedule",
                fontSize = if (isSeniorMode) 28.sp else 24.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "${schedules.count { it.isEnabled }} active reminders scheduled",
                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            if (schedules.isEmpty()) {
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
                                Icon(Icons.Default.Alarm, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No schedules created yet",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add a medicine or tap + to create custom alarm schedules.",
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
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (schedule.isEnabled) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.size(if (isSeniorMode) 52.dp else 44.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.Alarm,
                                            contentDescription = null,
                                            tint = if (schedule.isEnabled) MaterialTheme.colorScheme.primary else Color.Gray,
                                            modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.width(14.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = formattedTime,
                                        fontSize = if (isSeniorMode) 24.sp else 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (schedule.isEnabled) MaterialTheme.colorScheme.onSurface else Color.Gray
                                    )
                                    Text(
                                        text = "$medName • $doseInfo",
                                        fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = schedule.isEnabled,
                                    onCheckedChange = { onToggleSchedule(schedule, it) },
                                    modifier = Modifier.testTag("switch_schedule_${schedule.id}")
                                )

                                IconButton(onClick = { onDeleteSchedule(schedule) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
