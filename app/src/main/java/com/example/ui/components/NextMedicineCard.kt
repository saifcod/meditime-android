package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import com.example.data.local.entities.Medicine
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun NextMedicineCard(
    medicine: Medicine,
    scheduledTimeMillis: Long,
    isSeniorMode: Boolean,
    onTake: () -> Unit,
    onSnooze: () -> Unit,
    onSkip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
    val formattedTime = timeFormat.format(Date(scheduledTimeMillis))

    val now = System.currentTimeMillis()
    val diffMinutes = ((scheduledTimeMillis - now) / 60000).toInt()
    val countdownText = when {
        diffMinutes < -15 -> "Overdue by ${-diffMinutes} mins"
        diffMinutes < 0 -> "Due right now"
        diffMinutes == 0 -> "Due in < 1 min"
        diffMinutes < 60 -> "In $diffMinutes mins"
        else -> "In ${diffMinutes / 60}h ${diffMinutes % 60}m"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("card_next_medicine"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSeniorMode) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.primaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(if (isSeniorMode) 24.dp else 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        text = "NEXT MEDICINE",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isSeniorMode) 14.sp else 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (diffMinutes < 0) HealthWarningContainer else MaterialTheme.colorScheme.surface,
                    contentColor = if (diffMinutes < 0) HealthWarning else MaterialTheme.colorScheme.onSurface
                ) {
                    Text(
                        text = countdownText,
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSeniorMode) 14.sp else 12.sp,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Medicine Name & Type
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(if (isSeniorMode) 52.dp else 44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Medication,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(if (isSeniorMode) 32.dp else 26.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = medicine.name,
                        fontSize = if (isSeniorMode) 26.sp else 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    if (medicine.strength.isNotBlank()) {
                        Text(
                            text = "${medicine.medicineType} • ${medicine.strength}",
                            fontSize = if (isSeniorMode) 16.sp else 14.sp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dose Details Pill Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Time", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(formattedTime, fontSize = if (isSeniorMode) 18.sp else 15.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Restaurant,
                            contentDescription = null,
                            tint = MedicalTealSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Food", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                text = medicine.foodInstruction.ifBlank { "As Directed" },
                                fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Primary Big Action: "I HAVE TAKEN IT"
            Button(
                onClick = onTake,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (isSeniorMode) 68.dp else 56.dp)
                    .testTag("btn_take_next_medicine"),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HealthSuccess,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "I HAVE TAKEN IT",
                    fontSize = if (isSeniorMode) 20.sp else 17.sp,
                    fontWeight = FontWeight.ExtraBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Secondary Actions: Snooze / Skip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onSnooze,
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSeniorMode) 52.dp else 44.dp)
                        .testTag("btn_snooze_next_medicine"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Snooze, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Snooze 10m", fontSize = if (isSeniorMode) 15.sp else 13.sp)
                }

                OutlinedButton(
                    onClick = onSkip,
                    modifier = Modifier
                        .weight(1f)
                        .height(if (isSeniorMode) 52.dp else 44.dp)
                        .testTag("btn_skip_next_medicine"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Skip", fontSize = if (isSeniorMode) 15.sp else 13.sp)
                }
            }
        }
    }
}
