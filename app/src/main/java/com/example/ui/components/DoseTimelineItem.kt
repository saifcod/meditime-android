package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DoseRecord
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DoseTimelineItem(
    dose: DoseRecord,
    isSeniorMode: Boolean,
    onTake: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(dose.scheduledTimeMillis))

    val statusColor = when (dose.status) {
        "TAKEN" -> HealthSuccess
        "MISSED" -> HealthError
        "SKIPPED" -> HealthWarning
        else -> MaterialTheme.colorScheme.primary
    }

    val statusBg = when (dose.status) {
        "TAKEN" -> HealthSuccessContainer
        "MISSED" -> HealthErrorContainer
        "SKIPPED" -> HealthWarningContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .testTag("dose_item_${dose.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSeniorMode) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Status Icon Indicator
            Surface(
                shape = CircleShape,
                color = statusBg,
                modifier = Modifier.size(if (isSeniorMode) 48.dp else 40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (dose.status) {
                            "TAKEN" -> Icons.Default.Check
                            "MISSED" -> Icons.Default.Warning
                            "SKIPPED" -> Icons.Default.Close
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = dose.status,
                        tint = statusColor,
                        modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = formattedTime,
                        fontSize = if (isSeniorMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = dose.status,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = dose.medicineNameSnapshot.ifBlank { "Prescribed Medicine" },
                    fontSize = if (isSeniorMode) 20.sp else 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (dose.doseInstruction.isNotBlank() || dose.foodInstruction.isNotBlank()) {
                    Text(
                        text = listOf(dose.doseInstruction, dose.foodInstruction).filter { it.isNotBlank() }.joinToString(" • "),
                        fontSize = if (isSeniorMode) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions if still SCHEDULED or MISSED
            if (dose.status == "SCHEDULED" || dose.status == "MISSED") {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    IconButton(
                        onClick = onTake,
                        modifier = Modifier
                            .size(if (isSeniorMode) 48.dp else 40.dp)
                            .testTag("btn_take_dose_${dose.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = HealthSuccessContainer,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Check, contentDescription = "Take", tint = HealthSuccess)
                            }
                        }
                    }

                    IconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(if (isSeniorMode) 48.dp else 40.dp)
                            .testTag("btn_skip_dose_${dose.id}")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(Icons.Default.Close, contentDescription = "Skip", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}
