package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entities.UserProfile
import com.example.ui.theme.*

@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    onToggleNightMode: (Boolean) -> Unit,
    onToggleSeniorMode: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onToggleVoice: (Boolean) -> Unit,
    onDeleteAllData: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }

    val isSenior = userProfile?.seniorMode == true
    val isNightMode = userProfile?.nightMode == true

    // Check exact alarm permission status
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() == true
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 12.dp)
            .testTag("settings_screen")
    ) {
        Text(
            text = "Settings & Preferences",
            fontSize = if (isSenior) 28.sp else 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Personalize theme, reminders, legibility, and privacy",
            fontSize = if (isSenior) 16.sp else 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // 1. Theme / Night Mode Card (Requested feature: Default light mode, night mode toggle in settings)
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isNightMode) Color(0xFF1E293B) else MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                            contentDescription = null,
                            tint = if (isNightMode) Color(0xFFFFD54F) else MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🌙 Night Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 20.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (isNightMode) "Dark background for night viewing" else "Light background (Active)",
                        fontSize = if (isSenior) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isNightMode,
                    onCheckedChange = onToggleNightMode,
                    modifier = Modifier.testTag("switch_night_mode")
                )
            }
        }

        // 2. Senior Mode Toggle
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSenior) Color(0xFFE3F2FD) else MaterialTheme.colorScheme.surface
            ),
            border = if (isSenior) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(48.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.AccessibilityNew, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "👴 Senior Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = if (isSenior) 20.sp else 16.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Enlarge buttons and text for maximum readability",
                        fontSize = if (isSenior) 14.sp else 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = isSenior,
                    onCheckedChange = onToggleSeniorMode,
                    modifier = Modifier.testTag("switch_senior_mode")
                )
            }
        }

        // 3. Alert & Sound Preferences
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Sound & Notifications", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Reminder Sound", fontSize = 15.sp)
                    Switch(
                        checked = userProfile?.soundEnabled ?: true,
                        onCheckedChange = onToggleSound
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibration", fontSize = 15.sp)
                    Switch(
                        checked = userProfile?.vibrationEnabled ?: true,
                        onCheckedChange = onToggleVibration
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Spoken Voice Reminders", fontSize = 15.sp)
                        Text("Speaks dose aloud when alarm fires", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Switch(
                        checked = userProfile?.voiceRemindersEnabled ?: true,
                        onCheckedChange = onToggleVoice
                    )
                }
            }
        }

        // 4. Exact Alarm & Device Reliability
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (canScheduleExact) HealthSuccessContainer else HealthWarningContainer
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (canScheduleExact) Icons.Default.CheckCircle else Icons.Default.Warning,
                        contentDescription = null,
                        tint = if (canScheduleExact) HealthSuccess else HealthWarning
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = if (canScheduleExact) "Exact Alarms Active (100% Reliable)" else "Exact Alarm Permission Required",
                        fontWeight = FontWeight.Bold,
                        color = if (canScheduleExact) HealthSuccess else HealthWarning
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (canScheduleExact)
                        "Your device allows MediTime to wake up exactly on time for medicine schedules."
                    else
                        "Without exact alarm permission, alarms might be delayed by Android battery optimization. Tap below to enable.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!canScheduleExact && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                                data = Uri.parse("package:${context.packageName}")
                            }
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = HealthWarning)
                    ) {
                        Text("Grant Exact Alarm Permission")
                    }
                }
            }
        }

        // 5. Privacy & Data
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text("Privacy & Security", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyPolicyDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Security, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Privacy Policy & Local Storage Info", fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("About MediTime", fontSize = 15.sp, modifier = Modifier.weight(1f))
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = Color.Gray)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteConfirmDialog = true }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Delete All Data (Reset App)", fontSize = 15.sp, color = MaterialTheme.colorScheme.error, modifier = Modifier.weight(1f))
                }
            }
        }

        Spacer(modifier = Modifier.height(80.dp))
    }

    // Dialogs
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete All Data?") },
            text = { Text("This will permanently clear all your saved medicines, schedules, and dose history from this device.") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("Privacy Policy") },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(
                        text = "MediTime is designed with an Offline-First, Zero-Knowledge Privacy architecture:\n\n" +
                                "• 100% Local Storage: All medication schedules and logs remain solely on your device in an encrypted local database.\n\n" +
                                "• No Tracking or Ads: MediTime contains no advertising SDKs, no behavioral trackers, and never sells or transmits your health data.\n\n" +
                                "• Medical Disclaimer: MediTime is an organizational reminder utility and is not a substitute for professional medical advice, diagnosis, or prescription guidance.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showPrivacyPolicyDialog = false }) {
                    Text("Close")
                }
            }
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("About MediTime") },
            text = {
                Column {
                    Text("MediTime – Smart Medication Management", fontWeight = FontWeight.Bold)
                    Text("Tagline: \"Never Miss Your Medicine Again.\"", fontSize = 13.sp, color = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Version 1.0.0 (Global Free Launch)", fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Designed for patients, busy families, and seniors with love and accessibility.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(onClick = { showAboutDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
}
