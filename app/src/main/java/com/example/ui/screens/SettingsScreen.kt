package com.example.ui.screens

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.core.sound.AlarmPreferences
import com.example.core.sound.AlarmTone
import com.example.data.local.entities.UserProfile
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userProfile: UserProfile?,
    alarmPreferences: AlarmPreferences,
    onToggleNightMode: (Boolean) -> Unit,
    onToggleSeniorMode: (Boolean) -> Unit,
    onToggleAudibleAlarm: (Boolean) -> Unit,
    onSelectAlarmTone: (AlarmTone) -> Unit,
    onSetCustomAudio: (uri: String, title: String) -> Unit,
    onPreviewAlarmTone: (AlarmTone, String?) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSetSnoozeDuration: (Int) -> Unit,
    onSetMaxAlarmDuration: (Int) -> Unit,
    onTriggerTestAlarm: () -> Unit,
    onToggleVoice: (Boolean) -> Unit,
    onDeleteAllData: () -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showPrivacyPolicyDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showCustomSnoozeDialog by remember { mutableStateOf(false) }
    var showCustomMaxDurationDialog by remember { mutableStateOf(false) }

    val isSenior = userProfile?.seniorMode == true
    val isNightMode = userProfile?.nightMode == true

    // Audio file picker contract using GetContent (zero-permission storage access)
    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                try {
                    context.contentResolver.takePersistableUriPermission(
                        uri,
                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (ignored: Exception) {
                    // Safe fallback
                }

                var displayName = "Custom Music / Audio"
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex != -1 && cursor.moveToFirst()) {
                        displayName = cursor.getString(nameIndex) ?: displayName
                    }
                }

                onSetCustomAudio(uri.toString(), displayName)
            } catch (e: Exception) {
                onSetCustomAudio(uri.toString(), "Custom Audio File")
            }
        }
    }

    // Check exact alarm permission status
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager
    val canScheduleExact = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        alarmManager?.canScheduleExactAlarms() == true
    } else true

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 14.dp)
            .testTag("settings_screen")
    ) {
        // Screen Header
        Text(
            text = "Settings",
            fontSize = if (isSenior) 28.sp else 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Preferences, alarm sound, snooze, accessibility, and privacy",
            fontSize = if (isSenior) 16.sp else 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        // SECTION 1: APPEARANCE & THEME
        SettingsSectionHeader(title = "Appearance & Display", icon = Icons.Default.Palette)

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
                // Night Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isNightMode) Color(0xFF1E293B) else SoftBlue,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = if (isNightMode) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = null,
                                tint = if (isNightMode) Color(0xFFFFD54F) else RoyalBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Night Mode (Dark Theme)",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (isNightMode) "Dark theme enabled for low-light use" else "Default clean light theme active",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isNightMode,
                        onCheckedChange = onToggleNightMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue),
                        modifier = Modifier.testTag("switch_night_mode")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Senior Care Mode Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (isSenior) StatusTakenContainer else SoftBlue,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Elderly,
                                contentDescription = null,
                                tint = if (isSenior) StatusTaken else RoyalBlue
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Senior Care Mode",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Larger fonts, high contrast buttons, simplified layout",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = isSenior,
                        onCheckedChange = onToggleSeniorMode,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue),
                        modifier = Modifier.testTag("switch_senior_mode")
                    )
                }
            }
        }

        // SECTION 2: SMART MEDICATION ALARM
        SettingsSectionHeader(title = "Smart Medication Alarm", icon = Icons.Default.Alarm)

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
                // Audible Alarm Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (alarmPreferences.audibleAlarmEnabled) SoftBlue else CleanSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.VolumeUp,
                                contentDescription = null,
                                tint = if (alarmPreferences.audibleAlarmEnabled) RoyalBlue else NavyTypographyMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Audible Alarm Sound",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Plays loud reminder chime until taken or snoozed",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = alarmPreferences.audibleAlarmEnabled,
                        onCheckedChange = onToggleAudibleAlarm,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue),
                        modifier = Modifier.testTag("switch_audible_alarm")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Vibration Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (alarmPreferences.vibrationEnabled) SoftBlue else CleanSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Vibration,
                                contentDescription = null,
                                tint = if (alarmPreferences.vibrationEnabled) RoyalBlue else NavyTypographyMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Vibration Pattern",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Haptic feedback alongside reminder chime",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = alarmPreferences.vibrationEnabled,
                        onCheckedChange = onToggleVibration,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue),
                        modifier = Modifier.testTag("switch_alarm_vibration")
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Spoken Voice Announcements
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (userProfile?.voiceRemindersEnabled == true) SoftBlue else CleanSurfaceVariant,
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = if (userProfile?.voiceRemindersEnabled == true) RoyalBlue else NavyTypographyMuted
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Spoken Voice Reminders",
                            fontWeight = FontWeight.Bold,
                            fontSize = if (isSenior) 19.sp else 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Speaks medicine name out loud (great for seniors)",
                            fontSize = if (isSenior) 14.sp else 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = userProfile?.voiceRemindersEnabled == true,
                        onCheckedChange = onToggleVoice,
                        colors = SwitchDefaults.colors(checkedThumbColor = PureWhite, checkedTrackColor = RoyalBlue)
                    )
                }
            }
        }

        // SECTION 3: ALARM SOUND SELECTION & CUSTOM MUSIC
        SettingsSectionHeader(title = "Medication Alarm Tone", icon = Icons.Default.MusicNote)

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
                // Pre-configured Tones
                val tones = listOf(
                    Triple(AlarmTone.CHIME, "Gentle Chime", "Soft morning bell"),
                    Triple(AlarmTone.PULSE, "Heartbeat Pulse", "Rhythmic clinical alert"),
                    Triple(AlarmTone.BELL, "Pleasant Bell", "Peaceful harmonic notes"),
                    Triple(AlarmTone.DIGITAL, "Digital Alert", "High audibility chime"),
                    Triple(AlarmTone.SYSTEM_ALARM, "Device Default", "Standard phone alarm")
                )

                tones.forEach { (tone, name, desc) ->
                    val isSelected = alarmPreferences.selectedTone == tone
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectAlarmTone(tone) }
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = isSelected,
                            onClick = { onSelectAlarmTone(tone) },
                            colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = name,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = if (isSenior) 18.sp else 15.sp,
                                color = if (isSelected) RoyalBlue else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = desc,
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onPreviewAlarmTone(tone, null) }) {
                            Icon(
                                Icons.Default.PlayArrow,
                                contentDescription = "Preview Tone",
                                tint = RoyalBlue
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Custom Tone Option (From Phone / Gallery)
                val isCustomSelected = alarmPreferences.selectedTone == AlarmTone.CUSTOM_FILE
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isCustomSelected) SoftBlue else MaterialTheme.colorScheme.surfaceVariant
                    ),
                    border = BorderStroke(
                        if (isCustomSelected) 1.5.dp else 1.dp,
                        if (isCustomSelected) RoyalBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectAlarmTone(AlarmTone.CUSTOM_FILE) }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = isCustomSelected,
                                onClick = { onSelectAlarmTone(AlarmTone.CUSTOM_FILE) },
                                colors = RadioButtonDefaults.colors(selectedColor = RoyalBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Custom Audio (From Phone / Gallery)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isCustomSelected) DeepNavy else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (!alarmPreferences.customAudioTitle.isNullOrBlank())
                                        "Selected: ${alarmPreferences.customAudioTitle}"
                                    else
                                        "Pick any song or audio file from device storage",
                                    fontSize = 12.sp,
                                    color = if (isCustomSelected) RoyalBlue else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (!alarmPreferences.customAudioUri.isNullOrBlank()) {
                                IconButton(onClick = {
                                    onPreviewAlarmTone(AlarmTone.CUSTOM_FILE, alarmPreferences.customAudioUri)
                                }) {
                                    Icon(Icons.Default.PlayArrow, contentDescription = "Preview Custom", tint = RoyalBlue)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = { audioPickerLauncher.launch("audio/*") },
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.AudioFile, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (alarmPreferences.customAudioTitle.isNullOrBlank())
                                    "Choose Audio / Music from Device"
                                else
                                    "Change Audio File",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }

        // SECTION 4: SNOOZE & ALARM TIMEOUT
        SettingsSectionHeader(title = "Snooze & Duration", icon = Icons.Default.Snooze)

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
                // Snooze Minutes
                Text(
                    text = "Snooze Interval: ${alarmPreferences.snoozeDurationMinutes} Minutes",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 17.sp else 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(5, 10, 15, 20).forEach { mins ->
                        val isSelected = alarmPreferences.snoozeDurationMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetSnoozeDuration(mins) },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                    val isCustomSnooze = alarmPreferences.snoozeDurationMinutes !in listOf(5, 10, 15, 20)
                    FilterChip(
                        selected = isCustomSnooze,
                        onClick = { showCustomSnoozeDialog = true },
                        label = { Text("Custom...") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Max Alarm Duration
                val maxMinutes = alarmPreferences.maxAlarmDurationSeconds / 60
                Text(
                    text = "Auto-Stop Alarm After: $maxMinutes Minutes",
                    fontWeight = FontWeight.Bold,
                    fontSize = if (isSenior) 17.sp else 15.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(1, 2, 3, 5).forEach { mins ->
                        val isSelected = maxMinutes == mins
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSetMaxAlarmDuration(mins * 60) },
                            label = { Text("${mins}m") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = RoyalBlue,
                                selectedLabelColor = PureWhite
                            )
                        )
                    }
                    val isCustomMax = maxMinutes !in listOf(1, 2, 3, 5)
                    FilterChip(
                        selected = isCustomMax,
                        onClick = { showCustomMaxDurationDialog = true },
                        label = { Text("Custom...") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = RoyalBlue,
                            selectedLabelColor = PureWhite
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Test Alarm Trigger Button
                OutlinedButton(
                    onClick = onTriggerTestAlarm,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.AlarmOn, contentDescription = null, tint = RoyalBlue)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Trigger 5-Second Test Alarm", fontWeight = FontWeight.Bold, color = RoyalBlue)
                }
            }
        }

        // SECTION 5: PRIVACY & SYSTEM
        SettingsSectionHeader(title = "Privacy & Data", icon = Icons.Default.Security)

        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                // Privacy Notice Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showPrivacyPolicyDialog = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StatusTakenContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = StatusTaken, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("100% Offline & Private", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Your medical records stay strictly on your phone", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyTypographyMuted)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // About Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAboutDialog = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SoftBlue,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = RoyalBlue, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("About MediTime", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Text("Version 1.0 • Smart Medication Management", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = NavyTypographyMuted)
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                // Delete All Data Item
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDeleteConfirmDialog = true }
                        .padding(vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = StatusMissedContainer,
                        modifier = Modifier.size(40.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.DeleteForever, contentDescription = null, tint = StatusMissed, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Clear All Data", fontWeight = FontWeight.Bold, fontSize = 15.sp, color = StatusMissed)
                        Text("Reset medicines, schedules, and history", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = StatusMissed)
                }
            }
        }

        Spacer(modifier = Modifier.height(36.dp))
    }

    // Custom Snooze Dialog
    if (showCustomSnoozeDialog) {
        var snoozeInput by remember { mutableStateOf(alarmPreferences.snoozeDurationMinutes.toString()) }
        AlertDialog(
            onDismissRequest = { showCustomSnoozeDialog = false },
            title = { Text("Custom Snooze Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Enter snooze interval in minutes (1 to 60):", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = snoozeInput,
                        onValueChange = { snoozeInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = snoozeInput.toIntOrNull()?.coerceIn(1, 60) ?: 10
                        onSetSnoozeDuration(mins)
                        showCustomSnoozeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomSnoozeDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Custom Max Alarm Duration Dialog
    if (showCustomMaxDurationDialog) {
        var maxInput by remember { mutableStateOf((alarmPreferences.maxAlarmDurationSeconds / 60).toString()) }
        AlertDialog(
            onDismissRequest = { showCustomMaxDurationDialog = false },
            title = { Text("Custom Alarm Duration", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Auto-stop alarm ringing after minutes (1 to 15):", fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = maxInput,
                        onValueChange = { maxInput = it },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val mins = maxInput.toIntOrNull()?.coerceIn(1, 15) ?: 3
                        onSetMaxAlarmDuration(mins * 60)
                        showCustomMaxDurationDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCustomMaxDurationDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyPolicyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyPolicyDialog = false },
            title = { Text("MediTime Privacy Commitment", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "MediTime is designed with an offline-first architecture. Your medication list, reminder times, and dose audit history are stored entirely locally on this device in an encrypted Room SQLite database. We do not transmit or sell personal health telemetry to any third party."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showPrivacyPolicyDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Understood")
                }
            }
        )
    }

    // About Dialog
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("MediTime – Smart Medication Management", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "Tagline: \"Never Miss Your Medicine Again.\"\n\nMediTime helps patients organize their prescriptions, receive automatic audible alarms, and keep accurate logs for themselves and their physicians.\n\nBuilt natively with Kotlin & Jetpack Compose."
                )
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = RoyalBlue)
                ) {
                    Text("Close")
                }
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Erase All Data?", fontWeight = FontWeight.Bold, color = StatusMissed) },
            text = {
                Text("This will permanently delete all added medicines, schedules, and dose records from your phone. This action cannot be undone.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteAllData()
                        showDeleteConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusMissed)
                ) {
                    Text("Erase All")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = RoyalBlue,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Bold,
            color = RoyalBlue,
            letterSpacing = 0.5.sp
        )
    }
}
