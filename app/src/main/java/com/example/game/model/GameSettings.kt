package com.example.game.model

enum class CameraMode(val label: String, val distance: Float, val pitchDeg: Float, val fov: Float) {
    CHASE_CAM("Driving Cam", 6.5f, 22f, 60f),
    NAVIGATION_CAM("GPS Elevated", 9.5f, 42f, 55f),
    FREE_LOOK("Free Look", 7.0f, 28f, 60f),
    CINEMATIC("Cinematic Celebration", 5.2f, 16f, 50f)
}

enum class WeatherCondition(val label: String, val icon: String, val rainIntensity: Float, val roadSlipperiness: Float) {
    SUNNY("Sunny Breeze", "☀️", 0f, 1.0f),
    GOLDEN_SUNSET("Golden Sunset", "🌅", 0f, 1.0f),
    RAINY("Pastel Rain Shower", "🌧️", 0.75f, 1.25f),
    NEON_NIGHT("Cozy Neon Night", "🌙", 0f, 1.0f)
}

enum class GraphicsQuality {
    HIGH,
    PERFORMANCE
}

data class GameSettings(
    val cameraMode: CameraMode = CameraMode.CHASE_CAM,
    val weather: WeatherCondition = WeatherCondition.SUNNY,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val controlSensitivity: Float = 1.0f,
    val controlScale: Float = 1.0f,
    val graphicsQuality: GraphicsQuality = GraphicsQuality.HIGH
)
