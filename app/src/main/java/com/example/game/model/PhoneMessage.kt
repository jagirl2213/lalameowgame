package com.example.game.model

enum class MessageType(val icon: String) {
    DELIVERY_ALERT("📦"),
    CUSTOMER_CHAT("💬"),
    WEATHER_ALERT("⛅"),
    FUEL_WARNING("⛽"),
    PROMO_SPAM("🎉"),
    NEWS_FLASH("📰")
}

data class PhoneMessage(
    val id: String,
    val sender: String,
    val senderAvatar: String,
    val title: String,
    val body: String,
    val timeLabel: String,
    val type: MessageType,
    var isRead: Boolean = false
)
