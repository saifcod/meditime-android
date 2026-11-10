package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.DoseRecord
import com.example.ui.components.DoseTimelineItem
import com.example.ui.theme.*

@Composable
fun MedicationHistoryScreen(
    historyList: List<DoseRecord>,
    isSeniorMode: Boolean,
    onTakeDose: (DoseRecord) -> Unit,
    onSkipDose: (DoseRecord) -> Unit
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredList = remember(historyList, selectedFilter) {
        when (selectedFilter) {
            "TAKEN" -> historyList.filter { it.status == "TAKEN" }
            "MISSED" -> historyList.filter { it.status == "MISSED" }
            "SKIPPED" -> historyList.filter { it.status == "SKIPPED" }
            else -> historyList
        }
    }

    // Adherence rate calculation
    val takenCount = historyList.count { it.status == "TAKEN" }
    val totalActionable = historyList.count { it.status in listOf("TAKEN", "MISSED", "SKIPPED") }
    val adherenceRate = if (totalActionable > 0) ((takenCount.toFloat() / totalActionable) * 100).toInt() else 100

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("history_screen")
    ) {
        // Screen Header
        Text(
            text = "Medication History",
            fontSize = if (isSeniorMode) 28.sp else 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Complete audit record of taken, skipped, and missed doses",
            fontSize = if (isSeniorMode) 16.sp else 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Adherence Rate Card with visual progress
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = if (adherenceRate >= 80) StatusTakenContainer else StatusUpcomingContainer,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (adherenceRate >= 80) Icons.Default.CheckCircle else Icons.Default.Assessment,
                                        contentDescription = null,
                                        tint = if (adherenceRate >= 80) StatusTaken else StatusUpcoming,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Adherence Rate",
                                fontWeight = FontWeight.Bold,
                                fontSize = if (isSeniorMode) 18.sp else 15.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "$takenCount of $totalActionable actionable doses taken on time",
                            fontSize = if (isSeniorMode) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (adherenceRate >= 80) StatusTakenContainer else StatusUpcomingContainer
                    ) {
                        Text(
                            text = "$adherenceRate%",
                            fontSize = if (isSeniorMode) 26.sp else 22.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (adherenceRate >= 80) StatusTaken else StatusUpcoming,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress Bar
                LinearProgressIndicator(
                    progress = { (adherenceRate / 100f).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = if (adherenceRate >= 80) StatusTaken else StatusUpcoming,
                    trackColor = CleanSurfaceVariant
                )
            }
        }

        // Modern Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            val filters = listOf("ALL", "TAKEN", "MISSED", "SKIPPED")
            filters.forEach { f ->
                val isSelected = selectedFilter == f
                val chipColor = when (f) {
                    "TAKEN" -> StatusTaken
                    "MISSED" -> StatusMissed
                    "SKIPPED" -> StatusSkipped
                    else -> RoyalBlue
                }

                FilterChip(
                    selected = isSelected,
                    onClick = { selectedFilter = f },
                    label = {
                        Text(
                            text = f,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = chipColor,
                        selectedLabelColor = PureWhite
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        if (filteredList.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
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
                        modifier = Modifier.size(68.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Default.HistoryEdu,
                                contentDescription = null,
                                tint = RoyalBlue,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No records for '$selectedFilter'",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = if (isSeniorMode) 18.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Medication events will appear here as scheduled alarms are completed.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(filteredList, key = { it.id }) { record ->
                    DoseTimelineItem(
                        dose = record,
                        isSeniorMode = isSeniorMode,
                        onTake = { onTakeDose(record) },
                        onSkip = { onSkipDose(record) }
                    )
                }
            }
        }
    }
}
