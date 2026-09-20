package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.TealBrand

enum class DeliveryCategory(val label: String, val icon: String, val badgeColor: Color) {
    FOOD("Warm Food", "🍜", HoneyYellow),
    PARCEL("Express Parcel", "📦", DustyRose),
    GROCERIES("Fresh Groceries", "🥦", TealBrand),
    FLOWERS("Flower Bouquet", "💐", Color(0xFFEC4899)),
    DOCUMENTS("Important Papers", "📑", Color(0xFF6366F1)),
    NEWSPAPER("The Daily Purr", "📰", Color(0xFF0284C7)),
    CAT_CURIOS("Whimsical Item", "✨", Color(0xFF8B5CF6))
}

enum class DeliveryState {
    OFFERED,
    ACCEPTED,
    PICKED_UP,
    DELIVERED,
    EXPIRED
}

data class DeliveryJob(
    val id: String,
    val title: String,
    val description: String,
    val category: DeliveryCategory,
    val pickupVenue: String,
    val pickupDistrict: String,
    val pickupX: Float,
    val pickupZ: Float,
    val dropoffCustomer: String,
    val dropoffDistrict: String,
    val dropoffCustomerAvatar: String,
    val dropoffX: Float,
    val dropoffZ: Float,
    val customerDialogue: String,
    val newspaperHeadline: String? = null,
    val rewardCoins: Int,
    val rewardRep: Int,
    val estimatedDistanceMeters: Int,
    val isUrgent: Boolean = false,
    val timeLimitSeconds: Int = 120,
    var state: DeliveryState = DeliveryState.OFFERED,
    var timeRemainingSeconds: Int = 120,
    val multiStopIndex: Int = 0,
    val totalStops: Int = 1
)
