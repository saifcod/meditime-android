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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
    val isOverdue = diffMinutes < 0
    val countdownText = when {
        diffMinutes < -15 -> "Overdue by ${-diffMinutes}m"
        diffMinutes < 0 -> "Due right now"
        diffMinutes == 0 -> "Due in < 1m"
        diffMinutes < 60 -> "Due in ${diffMinutes}m"
        else -> "Due in ${diffMinutes / 60}h ${diffMinutes % 60}m"
    }

    // Hero Card Container
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSeniorMode) 4.dp else 10.dp,
                shape = RoundedCornerShape(28.dp),
                spotColor = RoyalBlue.copy(alpha = 0.25f)
            )
            .testTag("card_next_medicine"),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = if (isSeniorMode) HeroCardGradientSenior else HeroCardGradient
                )
                .padding(if (isSeniorMode) 24.dp else 22.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Top Badges Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // "NEXT MEDICINE" Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSeniorMode) RoyalBlue else PureWhite.copy(alpha = 0.18f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (isSeniorMode) PureWhite else MediTeal)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "NEXT MEDICINE",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = if (isSeniorMode) 14.sp else 11.sp,
                                color = if (isSeniorMode) PureWhite else PureWhite,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }

                    // Countdown Badge
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isOverdue) {
                            if (isSeniorMode) StatusMissedContainer else StatusMissed.copy(alpha = 0.85f)
                        } else {
                            if (isSeniorMode) SoftBlue else PureWhite.copy(alpha = 0.2f)
                        }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = if (isOverdue) Icons.Default.Warning else Icons.Default.Schedule,
                                contentDescription = null,
                                tint = if (isSeniorMode) {
                                    if (isOverdue) StatusMissed else RoyalBlue
                                } else PureWhite,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = countdownText,
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSeniorMode) 14.sp else 12.sp,
                                color = if (isSeniorMode) {
                                    if (isOverdue) StatusMissed else DeepNavy
                                } else PureWhite
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Medicine Name & Type Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSeniorMode) RoyalBlue else PureWhite.copy(alpha = 0.2f),
                        modifier = Modifier.size(if (isSeniorMode) 58.dp else 50.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Medication,
                                contentDescription = null,
                                tint = PureWhite,
                                modifier = Modifier.size(if (isSeniorMode) 34.dp else 28.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = medicine.name,
                            fontSize = if (isSeniorMode) 28.sp else 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isSeniorMode) DeepNavy else PureWhite,
                            lineHeight = if (isSeniorMode) 32.sp else 28.sp
                        )
                        val subtitleParts = listOfNotNull(
                            medicine.medicineType.takeIf { it.isNotBlank() },
                            medicine.strength.takeIf { it.isNotBlank() },
                            medicine.doseInstruction.takeIf { it.isNotBlank() }
                        )
                        if (subtitleParts.isNotEmpty()) {
                            Text(
                                text = subtitleParts.joinToString(" • "),
                                fontSize = if (isSeniorMode) 16.sp else 13.sp,
                                fontWeight = FontWeight.Medium,
                                color = if (isSeniorMode) DeepNavy.copy(alpha = 0.75f) else PureWhite.copy(alpha = 0.85f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Detail Chips: Scheduled Time & Food Instruction
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Time Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSeniorMode) PureWhite else PureWhite.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Alarm,
                                contentDescription = null,
                                tint = if (isSeniorMode) RoyalBlue else MediTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "TIME",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSeniorMode) NavyTypographyMuted else PureWhite.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = formattedTime,
                                    fontSize = if (isSeniorMode) 18.sp else 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSeniorMode) DeepNavy else PureWhite
                                )
                            }
                        }
                    }

                    // Food Pill
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSeniorMode) PureWhite else PureWhite.copy(alpha = 0.15f),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Restaurant,
                                contentDescription = null,
                                tint = if (isSeniorMode) MediTeal else MediTeal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = "INSTRUCTION",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSeniorMode) NavyTypographyMuted else PureWhite.copy(alpha = 0.7f),
                                    letterSpacing = 0.5.sp
                                )
                                Text(
                                    text = medicine.foodInstruction.ifBlank { "As Directed" },
                                    fontSize = if (isSeniorMode) 16.sp else 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSeniorMode) DeepNavy else PureWhite,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Big Primary CTA: "TAKE MEDICINE"
                Button(
                    onClick = onTake,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(if (isSeniorMode) 70.dp else 56.dp)
                        .testTag("btn_take_next_medicine"),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = StatusTaken,
                        contentColor = PureWhite
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        modifier = Modifier.size(if (isSeniorMode) 30.dp else 24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "I HAVE TAKEN IT",
                        fontSize = if (isSeniorMode) 20.sp else 16.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Secondary Action Row: Snooze & Skip
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSeniorMode) PureWhite else PureWhite.copy(alpha = 0.12f),
                            contentColor = if (isSeniorMode) DeepNavy else PureWhite
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSeniorMode) BorderSubtle else PureWhite.copy(alpha = 0.35f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Snooze 10m",
                            fontSize = if (isSeniorMode) 15.sp else 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    OutlinedButton(
                        onClick = onSkip,
                        modifier = Modifier
                            .weight(1f)
                            .height(if (isSeniorMode) 52.dp else 44.dp)
                            .testTag("btn_skip_next_medicine"),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSeniorMode) PureWhite else PureWhite.copy(alpha = 0.12f),
                            contentColor = if (isSeniorMode) StatusMissed else Color(0xFFFF8B8E)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSeniorMode) StatusMissed.copy(alpha = 0.5f) else Color(0xFFFF8B8E).copy(alpha = 0.5f)
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Skip",
                            fontSize = if (isSeniorMode) 15.sp else 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
