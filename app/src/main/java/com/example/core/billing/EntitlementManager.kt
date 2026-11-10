package com.example.core.billing

/**
 * Modular Entitlement & Billing Architecture for Future Premium Expansion.
 *
 * POLICY & ARCHITECTURE GUARANTEE:
 * 1. Initial launch is 100% FREE without subscriptions, paywalls, or fake buttons.
 * 2. Essential medicine reminders, existing schedules, dose history, and tracking
 *    can NEVER be locked behind payment.
 * 3. This module abstracts entitlements so future convenience features (e.g. encrypted
 *    cloud sync, advanced PDF export, multi-device sync) can be granted cleanly without
 *    rewriting business logic or core medicine reminders.
 */
enum class PremiumFeature {
    CLOUD_BACKUP,
    MULTI_DEVICE_SYNC,
    CAREGIVER_SYNC,
    ADVANCED_MEDICATION_REPORTS,
    ADVANCED_PRESCRIPTION_OCR
}

sealed class SubscriptionStatus {
    object Free : SubscriptionStatus()
    data class Active(val expiryTimestamp: Long, val isAutoRenewing: Boolean) : SubscriptionStatus()
    object InGracePeriod : SubscriptionStatus()
    object Expired : SubscriptionStatus()
}

interface EntitlementManager {
    fun isFeatureUnlocked(feature: PremiumFeature): Boolean
    fun getSubscriptionStatus(): SubscriptionStatus
}

class MediTimeEntitlementManager : EntitlementManager {
    override fun isFeatureUnlocked(feature: PremiumFeature): Boolean {
        // Free-first launch: all core functionality is fully available
        return true
    }

    override fun getSubscriptionStatus(): SubscriptionStatus {
        return SubscriptionStatus.Free
    }
}
