package com.example.ui.screens

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
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("history_screen")
    ) {
        Text(
            text = "Medication Log & History",
            fontSize = if (isSeniorMode) 28.sp else 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Complete audit record of taken, skipped, and missed doses",
            fontSize = if (isSeniorMode) 16.sp else 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // Adherence Card
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Adherence Rate", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("$takenCount of $totalActionable doses taken on time", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (adherenceRate >= 80) HealthSuccessContainer else HealthWarningContainer
                ) {
                    Text(
                        text = "$adherenceRate%",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (adherenceRate >= 80) HealthSuccess else HealthWarning,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL", "TAKEN", "MISSED", "SKIPPED").forEach { f ->
                FilterChip(
                    selected = selectedFilter == f,
                    onClick = { selectedFilter = f },
                    label = { Text(f) }
                )
            }
        }

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.HistoryEdu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No records for '$selectedFilter'",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
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
