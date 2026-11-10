package com.example.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

enum class Screen {
    ONBOARDING,
    DASHBOARD,
    MEDICINES,
    SCHEDULE,
    HISTORY,
    SETTINGS
}

/**
 * Lightweight, zero-overhead navigation state holder.
 * Completely eliminates Jetpack navigation recomposition lags,
 * providing instant and smooth tab and screen transitions for elderly users.
 */
@Stable
class NavigationState(initialScreen: Screen = Screen.DASHBOARD) {
    var currentScreen by mutableStateOf(initialScreen)
        private set

    var showAddMedicineWizard by mutableStateOf(false)
        private set

    fun navigateTo(screen: Screen) {
        if (currentScreen != screen) {
            showAddMedicineWizard = false
            currentScreen = screen
        }
    }

    fun openAddMedicineWizard() {
        showAddMedicineWizard = true
    }

    fun closeAddMedicineWizard() {
        showAddMedicineWizard = false
    }

    fun handleBack(): Boolean {
        if (showAddMedicineWizard) {
            showAddMedicineWizard = false
            return true
        }
        if (currentScreen != Screen.DASHBOARD) {
            currentScreen = Screen.DASHBOARD
            return true
        }
        return false
    }
}

@Composable
fun rememberNavigationState(initialScreen: Screen = Screen.DASHBOARD): NavigationState {
    return remember { NavigationState(initialScreen) }
}
