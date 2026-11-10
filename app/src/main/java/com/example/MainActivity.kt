package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.navigation.NavigationState
import com.example.ui.navigation.Screen
import com.example.ui.navigation.rememberNavigationState
import com.example.ui.screens.*
import com.example.ui.theme.*

class MainActivity : ComponentActivity() {

    private val viewModel: MediTimeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val userProfile by viewModel.userProfile.collectAsStateWithLifecycle()
            val medicines by viewModel.medicines.collectAsStateWithLifecycle()
            val schedules by viewModel.allSchedules.collectAsStateWithLifecycle()
            val todayDoses by viewModel.todayDoses.collectAsStateWithLifecycle()
            val historyRecords by viewModel.historyRecords.collectAsStateWithLifecycle()

            val isSenior = userProfile?.seniorMode == true
            // Default to Light Mode; nightMode activated only if user toggles it in Settings
            val isNightMode = userProfile?.nightMode ?: false

            // Use lightweight NavigationState for instant, zero-delay switching
            val navState = rememberNavigationState(Screen.DASHBOARD)

            // Hardware Back button support for elderly users
            BackHandler(enabled = navState.currentScreen != Screen.DASHBOARD || navState.showAddMedicineWizard) {
                navState.handleBack()
            }

            // Runtime notification permission launcher for Android 13+
            val context = LocalContext.current
            val notificationPermissionLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { /* permission handled */ }

            LaunchedEffect(Unit) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.POST_NOTIFICATIONS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        try {
                            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } catch (e: Exception) {
                            // Ignored safely
                        }
                    }
                }
            }

            // Decide if onboarding should be shown first (only if user profile is explicitly not onboarded)
            val showOnboarding = userProfile != null && !userProfile!!.isOnboarded

            MediTimeTheme(darkTheme = isNightMode) {
                if (showOnboarding) {
                    OnboardingScreen(
                        onFinish = { senior, name ->
                            viewModel.saveOnboarding(senior, name)
                            navState.navigateTo(Screen.DASHBOARD)
                        }
                    )
                } else if (navState.showAddMedicineWizard) {
                    AddMedicineWizardScreen(
                        onDismiss = { navState.closeAddMedicineWizard() },
                        onSaveMedicine = { med, times, days ->
                            viewModel.addMedicineWithSchedules(med, times, days)
                            navState.closeAddMedicineWizard()
                        }
                    )
                } else {
                    // Compute next upcoming dose for dashboard using cached state
                    val upcomingDoses = remember(todayDoses) {
                        todayDoses
                            .filter { it.status == "SCHEDULED" || it.status == "SNOOZED" }
                            .sortedBy { it.scheduledTimeMillis }
                    }
                    val nextDose = upcomingDoses.firstOrNull()
                    val nextDoseMed = remember(nextDose, medicines) {
                        nextDose?.let { nd -> medicines.firstOrNull { it.id == nd.medicineId } }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = {
                            NavigationBar(
                                modifier = Modifier
                                    .testTag("bottom_nav_bar")
                                    .windowInsetsPadding(WindowInsets.navigationBars),
                                containerColor = MaterialTheme.colorScheme.surface,
                                tonalElevation = 6.dp
                            ) {
                                val itemColors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = RoyalBlue,
                                    selectedTextColor = RoyalBlue,
                                    indicatorColor = SoftBlue,
                                    unselectedIconColor = NavyTypographyMuted,
                                    unselectedTextColor = NavyTypographyMuted
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (navState.currentScreen == Screen.DASHBOARD) Icons.Filled.Home else Icons.Outlined.Home,
                                            contentDescription = "Home"
                                        )
                                    },
                                    label = { Text("Home", fontSize = if (isSenior) 15.sp else 12.sp, fontWeight = FontWeight.Bold) },
                                    selected = navState.currentScreen == Screen.DASHBOARD,
                                    onClick = { navState.navigateTo(Screen.DASHBOARD) },
                                    colors = itemColors,
                                    modifier = Modifier.testTag("nav_item_home")
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (navState.currentScreen == Screen.MEDICINES) Icons.Filled.Medication else Icons.Outlined.Medication,
                                            contentDescription = "Medicines"
                                        )
                                    },
                                    label = { Text("Medicines", fontSize = if (isSenior) 15.sp else 12.sp, fontWeight = FontWeight.Bold) },
                                    selected = navState.currentScreen == Screen.MEDICINES,
                                    onClick = { navState.navigateTo(Screen.MEDICINES) },
                                    colors = itemColors,
                                    modifier = Modifier.testTag("nav_item_medicines")
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (navState.currentScreen == Screen.SCHEDULE) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                                            contentDescription = "Schedule"
                                        )
                                    },
                                    label = { Text("Schedule", fontSize = if (isSenior) 15.sp else 12.sp, fontWeight = FontWeight.Bold) },
                                    selected = navState.currentScreen == Screen.SCHEDULE,
                                    onClick = { navState.navigateTo(Screen.SCHEDULE) },
                                    colors = itemColors,
                                    modifier = Modifier.testTag("nav_item_schedule")
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (navState.currentScreen == Screen.HISTORY) Icons.Filled.HistoryEdu else Icons.Outlined.HistoryEdu,
                                            contentDescription = "History"
                                        )
                                    },
                                    label = { Text("History", fontSize = if (isSenior) 15.sp else 12.sp, fontWeight = FontWeight.Bold) },
                                    selected = navState.currentScreen == Screen.HISTORY,
                                    onClick = { navState.navigateTo(Screen.HISTORY) },
                                    colors = itemColors,
                                    modifier = Modifier.testTag("nav_item_history")
                                )

                                NavigationBarItem(
                                    icon = {
                                        Icon(
                                            if (navState.currentScreen == Screen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                                            contentDescription = "Settings"
                                        )
                                    },
                                    label = { Text("Settings", fontSize = if (isSenior) 15.sp else 12.sp, fontWeight = FontWeight.Bold) },
                                    selected = navState.currentScreen == Screen.SETTINGS,
                                    onClick = { navState.navigateTo(Screen.SETTINGS) },
                                    colors = itemColors,
                                    modifier = Modifier.testTag("nav_item_settings")
                                )
                            }
                        }
                    ) { innerPadding ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                                .statusBarsPadding()
                        ) {
                            Crossfade(
                                targetState = navState.currentScreen,
                                label = "screen_transition"
                            ) { screen ->
                                when (screen) {
                                    Screen.DASHBOARD -> {
                                        DashboardScreen(
                                            userProfile = userProfile,
                                            medicines = medicines,
                                            todayDoses = todayDoses,
                                            nextDoseMedicine = nextDoseMed,
                                            nextDoseTimeMillis = nextDose?.scheduledTimeMillis,
                                            onTakeNext = { nextDose?.let { viewModel.markDoseTaken(it) } },
                                            onSnoozeNext = { nextDose?.let { viewModel.snoozeDose(it) } },
                                            onSkipNext = { nextDose?.let { viewModel.markDoseSkipped(it) } },
                                            onTakeDose = { dose -> viewModel.markDoseTaken(dose) },
                                            onSkipDose = { dose -> viewModel.markDoseSkipped(dose) },
                                            onNavigateToAddMedicine = { navState.openAddMedicineWizard() },
                                            onNavigateToMedicines = { navState.navigateTo(Screen.MEDICINES) },
                                            onNavigateToSchedule = { navState.navigateTo(Screen.SCHEDULE) },
                                            onNavigateToHistory = { navState.navigateTo(Screen.HISTORY) },
                                            onNavigateToSettings = { navState.navigateTo(Screen.SETTINGS) }
                                        )
                                    }
                                    Screen.MEDICINES -> {
                                        MedicinesListScreen(
                                            medicines = medicines,
                                            isSeniorMode = isSenior,
                                            onAddMedicine = { navState.openAddMedicineWizard() },
                                            onDeleteMedicine = { med -> viewModel.deleteMedicine(med) },
                                            onAdjustStock = { med, qty -> viewModel.updateMedicineStock(med, qty) }
                                        )
                                    }
                                    Screen.SCHEDULE -> {
                                        ScheduleManagementScreen(
                                            medicines = medicines,
                                            schedules = schedules,
                                            isSeniorMode = isSenior,
                                            onToggleSchedule = { sched, active -> viewModel.toggleSchedule(sched, active) },
                                            onDeleteSchedule = { sched -> viewModel.deleteSchedule(sched) },
                                            onAddNewSchedule = { navState.openAddMedicineWizard() }
                                        )
                                    }
                                    Screen.HISTORY -> {
                                        MedicationHistoryScreen(
                                            historyList = historyRecords,
                                            isSeniorMode = isSenior,
                                            onTakeDose = { dose -> viewModel.markDoseTaken(dose) },
                                            onSkipDose = { dose -> viewModel.markDoseSkipped(dose) }
                                        )
                                    }
                                    Screen.SETTINGS -> {
                                        val alarmPrefs by viewModel.alarmPreferences.collectAsStateWithLifecycle()
                                        SettingsScreen(
                                            userProfile = userProfile,
                                            alarmPreferences = alarmPrefs,
                                            onToggleNightMode = { viewModel.toggleNightMode(it) },
                                            onToggleSeniorMode = { viewModel.toggleSeniorMode(it) },
                                            onToggleAudibleAlarm = { viewModel.setAudibleAlarmEnabled(it) },
                                            onSelectAlarmTone = { viewModel.setSelectedAlarmTone(it) },
                                            onSetCustomAudio = { uri, title -> viewModel.setCustomAlarmAudio(uri, title) },
                                            onPreviewAlarmTone = { tone, uri -> viewModel.previewAlarmTone(tone, uri) },
                                            onToggleVibration = { viewModel.setAlarmVibrationEnabled(it) },
                                            onSetSnoozeDuration = { viewModel.setSnoozeDurationMinutes(it) },
                                            onSetMaxAlarmDuration = { viewModel.setMaxAlarmDurationSeconds(it) },
                                            onTriggerTestAlarm = { viewModel.triggerTestAlarm() },
                                            onToggleVoice = { viewModel.toggleVoice(it) },
                                            onDeleteAllData = { viewModel.deleteAllData() }
                                        )
                                    }
                                    Screen.ONBOARDING -> {
                                        OnboardingScreen(
                                            onFinish = { senior, name ->
                                                viewModel.saveOnboarding(senior, name)
                                                navState.navigateTo(Screen.DASHBOARD)
                                            }
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
}
