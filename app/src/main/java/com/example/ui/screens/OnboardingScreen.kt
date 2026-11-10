package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun OnboardingScreen(
    onFinish: (seniorMode: Boolean, name: String) -> Unit
) {
    var step by remember { mutableStateOf(1) }
    var selectedSeniorMode by remember { mutableStateOf(false) }
    var userName by remember { mutableStateOf("") }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (step > 1) {
                        OutlinedButton(
                            onClick = { step-- },
                            modifier = Modifier
                                .height(56.dp)
                                .testTag("btn_onboarding_back"),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Text("Back", fontSize = 16.sp)
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp))
                    }

                    // Dots indicator
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (i in 1..4) {
                            Box(
                                modifier = Modifier
                                    .size(if (i == step) 12.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i == step) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
                                    )
                            )
                        }
                    }

                    Button(
                        onClick = {
                            if (step < 4) {
                                step++
                            } else {
                                onFinish(selectedSeniorMode, userName)
                            }
                        },
                        modifier = Modifier
                            .height(56.dp)
                            .testTag("btn_onboarding_next"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Text(
                            text = if (step == 4) "Get Started" else "Next",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (step) {
                1 -> StepWelcome()
                2 -> StepHowItWorks()
                3 -> StepChooseMode(
                    isSenior = selectedSeniorMode,
                    onSelect = { selectedSeniorMode = it }
                )
                4 -> StepPreferences(
                    name = userName,
                    onNameChange = { userName = it },
                    isSenior = selectedSeniorMode
                )
            }
        }
    }
}

@Composable
private fun StepWelcome() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxWidth()
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(110.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.MedicalServices,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(56.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Welcome to MediTime",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "\"Never Miss Your Medicine Again.\"",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(20.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "🔒 100% Offline & Private",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "No account needed. Your health data stays safe and secure right on your device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun StepHowItWorks() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How MediTime Works",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Powerful medication tracking made effortlessly simple",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
        )

        val items = listOf(
            Triple(Icons.Default.Medication, "1. Add Prescribed Medicines", "Enter tablet name, dose amount, and food instructions"),
            Triple(Icons.Default.Alarm, "2. Set Reminder Times", "Choose exact daily alarm times with precision timing"),
            Triple(Icons.Default.NotificationsActive, "3. Automatic Notifications", "Clear alerts with quick Taken, Snooze, or Skip buttons"),
            Triple(Icons.Default.Assessment, "4. Track History & Refills", "Keep an accurate log for yourself and your doctor")
        )

        items.forEach { (icon, title, desc) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = MaterialTheme.colorScheme.onBackground)
                    Text(desc, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

@Composable
private fun StepChooseMode(
    isSenior: Boolean,
    onSelect: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Choose Your Experience",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "You can always change this anytime in Settings",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 20.dp, top = 4.dp)
        )

        // Senior Mode Card
        Card(
            onClick = { onSelect(true) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isSenior) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (isSenior) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("mode_senior")
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = isSenior,
                    onClick = { onSelect(true) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "👴 Senior Mode (High Legibility)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Extra-large buttons, high contrast text, simplified screens, and spoken audio reminders.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Standard Mode Card
        Card(
            onClick = { onSelect(false) },
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (!isSenior) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
            ),
            border = if (!isSenior) CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.primary)) else null,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .testTag("mode_standard")
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = !isSenior,
                    onClick = { onSelect(false) }
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "📱 Standard Mode",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "Modern compact cards, detailed statistics, schedule timeline, and comprehensive medicine reports.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StepPreferences(
    name: String,
    onNameChange: (String) -> Unit,
    isSenior: Boolean
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Personalize MediTime",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Optional details to tailor your experience",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 24.dp, top = 4.dp)
        )

        OutlinedTextField(
            value = name,
            onValueChange = onNameChange,
            label = { Text("What is your name? (Optional)") },
            placeholder = { Text("e.g. Grandma Rose, Alex") },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("input_onboarding_name")
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "🔔 Reminder Permissions",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "MediTime uses exact alarms and high-priority notifications to make sure medicine reminders arrive on the exact second, even when your phone is asleep.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
