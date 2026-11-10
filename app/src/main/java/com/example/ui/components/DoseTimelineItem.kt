package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
        "TAKEN" -> StatusTaken
        "MISSED" -> StatusMissed
        "SKIPPED" -> StatusSkipped
        else -> StatusUpcoming
    }

    val statusBg = when (dose.status) {
        "TAKEN" -> StatusTakenContainer
        "MISSED" -> StatusMissedContainer
        "SKIPPED" -> StatusSkippedContainer
        else -> StatusUpcomingContainer
    }

    val statusLabel = when (dose.status) {
        "TAKEN" -> "Taken"
        "MISSED" -> "Missed"
        "SKIPPED" -> "Skipped"
        "SNOOZED" -> "Snoozed"
        else -> "Upcoming"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp)
            .testTag("dose_item_${dose.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSeniorMode) 18.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Timeline Status Icon Indicator
            Surface(
                shape = CircleShape,
                color = statusBg,
                modifier = Modifier.size(if (isSeniorMode) 50.dp else 42.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (dose.status) {
                            "TAKEN" -> Icons.Default.CheckCircle
                            "MISSED" -> Icons.Default.ErrorOutline
                            "SKIPPED" -> Icons.Default.Cancel
                            "SNOOZED" -> Icons.Default.Snooze
                            else -> Icons.Default.Schedule
                        },
                        contentDescription = dose.status,
                        tint = statusColor,
                        modifier = Modifier.size(if (isSeniorMode) 28.dp else 22.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Medicine Details & Time
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = formattedTime,
                        fontSize = if (isSeniorMode) 17.sp else 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = statusBg
                    ) {
                        Text(
                            text = statusLabel.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            letterSpacing = 0.5.sp,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Text(
                    text = dose.medicineNameSnapshot.ifBlank { "Prescribed Medicine" },
                    fontSize = if (isSeniorMode) 20.sp else 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val details = listOf(dose.doseInstruction, dose.foodInstruction)
                    .filter { it.isNotBlank() }
                    .joinToString(" • ")

                if (details.isNotBlank()) {
                    Text(
                        text = details,
                        fontSize = if (isSeniorMode) 14.sp else 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Quick Actions (Only shown if pending / scheduled / missed)
            if (dose.status == "SCHEDULED" || dose.status == "MISSED" || dose.status == "SNOOZED") {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Take Button
                    FilledIconButton(
                        onClick = onTake,
                        modifier = Modifier
                            .size(if (isSeniorMode) 48.dp else 40.dp)
                            .testTag("btn_take_dose_${dose.id}"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = StatusTakenContainer,
                            contentColor = StatusTaken
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Take",
                            modifier = Modifier.size(if (isSeniorMode) 26.dp else 20.dp)
                        )
                    }

                    // Skip Button
                    FilledIconButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .size(if (isSeniorMode) 48.dp else 40.dp)
                            .testTag("btn_skip_dose_${dose.id}"),
                        shape = CircleShape,
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Skip",
                            modifier = Modifier.size(if (isSeniorMode) 24.dp else 18.dp)
                        )
                    }
                }
            }
        }
    }
}
