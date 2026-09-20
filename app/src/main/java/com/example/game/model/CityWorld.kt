package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import com.example.ui.theme.TealBrandLight

data class RoadSegment(
    val id: String,
    val startX: Float,
    val startZ: Float,
    val endX: Float,
    val endZ: Float,
    val width: Float = 10f,
    val isBridge: Boolean = false
)

data class CityBuilding(
    val id: String,
    val name: String,
    val district: String,
    val x: Float,
    val z: Float,
    val width: Float,
    val length: Float,
    val height: Float,
    val wallColor: Color,
    val roofColor: Color,
    val awningColor: Color? = null,
    val signText: String? = null,
    val isGasStation: Boolean = false
)

data class GasStation(
    val id: String,
    val name: String,
    val x: Float,
    val z: Float,
    val pumpRadius: Float = 6.5f,
    val pricePerLiter: Int = 2
)

data class CityProp(
    val type: PropType,
    val x: Float,
    val z: Float,
    val scale: Float = 1.0f
)

enum class PropType {
    TREE,
    STREETLAMP,
    FIRE_HYDRANT,
    BENCH,
    FLOWER_POT,
    PUDDLE,
    DELIVERY_PARCEL,
    NEWSPAPER_STAND,
    PARCEL_STACK,
    TRAFFIC_LIGHT
}

data class NpcVehicle(
    val id: String,
    var x: Float,
    var z: Float,
    var headingDeg: Float,
    val speed: Float,
    val color: Color,
    val isMoped: Boolean = false,
    val waypoints: List<Pair<Float, Float>>,
    var currentWaypointIndex: Int = 0
)

data class NpcPedestrian(
    val id: String,
    val animalEmoji: String,
    var x: Float,
    var z: Float,
    var headingDeg: Float,
    val speed: Float,
    val walkRadius: Float,
    val originX: Float,
    val originZ: Float,
    var walkPhase: Float = 0f
)

object CityWorldData {
    val roads = listOf(
        // Central North-South Main Ave
        RoadSegment("main_ns", 0f, -120f, 0f, 120f, width = 11f),
        // Central East-West Broad St
        RoadSegment("broad_ew", -120f, 0f, 120f, 0f, width = 11f),

        // Suburbs Ring Road (North)
        RoadSegment("north_suburb_ew", -75f, -65f, 75f, -65f, width = 9f),
        // South Market Ave
        RoadSegment("south_market_ew", -75f, 65f, 75f, 65f, width = 9f),

        // West Harbor Boulevard
        RoadSegment("west_harbor_ns", -65f, -95f, -65f, 95f, width = 9f),
        // East Park Drive
        RoadSegment("east_park_ns", 65f, -95f, 65f, 95f, width = 9f),

        // Diagonal connecting shortcut alleys
        RoadSegment("alley_nw", -65f, -65f, 0f, -65f, width = 8f),
        RoadSegment("alley_ne", 0f, -65f, 65f, -65f, width = 8f),
        RoadSegment("alley_sw", -65f, 65f, 0f, 65f, width = 8f),
        RoadSegment("alley_se", 0f, 65f, 65f, 65f, width = 8f),

        // Whisker Canal Bridge
        RoadSegment("canal_bridge", -25f, 0f, 25f, 0f, width = 11f, isBridge = true)
    )

    val gasStations = listOf(
        GasStation("gas_east", "Shell-Paws Station East", 22f, -32f),
        GasStation("gas_west", "Meow-bil Petro Harbor", -42f, 38f)
    )

    val buildings = listOf(
        // Central Boulevard (Immediate start area, bustling street shops)
        CityBuilding("b_bakery", "Lily's Artisan Bakery", "Downpaws Market", -16f, 10f, 15f, 13f, 13f, Color(0xFFFFFBEB), HoneyYellow, Color(0xFFF97316), "LILY'S 🥐"),
        CityBuilding("b_ramen", "Neko Miso Ramen", "Downpaws Market", 16f, 10f, 15f, 13f, 14f, Color(0xFFFEF3C7), Color(0xFFE11D48), DustyRose, "RAMEN 🍜"),
        CityBuilding("b_cafe", "Boba Whiskers Cafe", "Downpaws Market", -16f, 32f, 15f, 13f, 14f, Color(0xFFCCFBF1), TealBrand, TealBrandLight, "BOBA 🧋"),
        CityBuilding("b_daily_purr", "The Daily Purr Press", "Downpaws Market", 16f, 32f, 15f, 13f, 15f, Color(0xFFE0E7FF), Color(0xFF3730A3), Color(0xFF6366F1), "DAILY PURR 🗞️"),
        CityBuilding("b_momo", "Momo Law & Consulting", "Downpaws Market", -16f, 54f, 15f, 13f, 16f, Color(0xFFF1F5F9), Color(0xFF1E293B), Color(0xFF0284C7), "MOMO 💼"),
        CityBuilding("b_grandma", "Grandma Whiskers' Cottage", "Downpaws Market", 16f, 54f, 14f, 12f, 11f, Color(0xFFFFEDD5), Color(0xFFEA580C), DustyRose, "GRANDMA 🏡"),

        // South Central Hub & Benny's Gas Station
        CityBuilding("b_post", "Post Paws Courier Hub", "Downpaws Market", -16f, -16f, 16f, 14f, 16f, Color(0xFFFCE7F3), DustyRose, Color(0xFF6366F1), "POST 📮"),
        CityBuilding("b_gas1", "Benny's Shell-Paws Station", "Downpaws Market", 18f, -16f, 15f, 11f, 10f, Color(0xFFFEF08A), Color(0xFFEAB308), HoneyYellow, "BENNY'S ⛽", isGasStation = true),
        CityBuilding("b_gas2", "Meow-bil Petro Harbor", "Harbor District", -42f, 38f, 14f, 10f, 9f, Color(0xFFBAE6FD), Color(0xFF0284C7), TealBrand, "GAS ⛽", isGasStation = true),

        // North Sunny Mew Suburbs
        CityBuilding("b_cottage1", "Madame Fluff's Villa", "Sunny Suburbs", -35f, -80f, 14f, 14f, 11f, Color(0xFFFDE047), DustyRose, null, "FLUFF 🌸"),
        CityBuilding("b_cottage2", "Professor Barnaby's Study", "Sunny Suburbs", 0f, -85f, 18f, 16f, 13f, Color(0xFFE2E8F0), Color(0xFF475569), null, "BOOKS 📚"),
        CityBuilding("b_cottage3", "Grandma Tabitha's House", "Sunny Suburbs", 35f, -80f, 14f, 14f, 11f, Color(0xFFFFEDD5), Color(0xFFEA580C), null, "TABITHA 🧶"),
        CityBuilding("b_greenhouse", "Catnip Botanical Greenhouse", "Sunny Suburbs", -18f, -48f, 14f, 12f, 10f, Color(0xFFD1FAE5), Color(0xFF059669), null, "PLANTS 🌿"),

        // West Fishbone Harbor & Rico's Hangout
        CityBuilding("b_fishmarket", "Old Wharf Fishmonger", "Fishbone Harbor", -82f, -25f, 18f, 16f, 12f, Color(0xFFCFFAFE), Color(0xFF0891B2), TealBrand, "FISH 🐟"),
        CityBuilding("b_lighthouse", "Moonlight Lighthouse", "Fishbone Harbor", -85f, 30f, 10f, 10f, 26f, Color(0xFFFFFFFF), Color(0xFFEF4444), null, "LIGHT 🏮"),
        CityBuilding("b_diner", "Sailor Cat Chowder Diner", "Fishbone Harbor", -48f, -22f, 14f, 12f, 11f, Color(0xFFFEE2E2), Color(0xFFDC2626), DustyRoseLight, "DINER 🦞"),
        CityBuilding("b_rico", "Rico's Speed Garage", "Fishbone Harbor", -50f, 15f, 16f, 14f, 13f, Color(0xFF1E293B), Color(0xFFDC2626), HoneyYellow, "RICO 🦝"),

        // South Neon Alley
        CityBuilding("b_arcade", "Pixel Paws 8-Bit Arcade", "Neon Alley", -25f, 82f, 16f, 15f, 16f, Color(0xFF312E81), Color(0xFFA855F7), Color(0xFFEC4899), "ARCADE 🕹️"),
        CityBuilding("b_karaoke", "Meow Sound Studio", "Neon Alley", 0f, 85f, 18f, 16f, 15f, Color(0xFF1E293B), Color(0xFF06B6D4), TealBrand, "MUSIC 🎵"),
        CityBuilding("b_nightmarket", "Midnight Snack Stalls", "Neon Alley", 28f, 82f, 16f, 14f, 11f, Color(0xFF78350F), Color(0xFFF97316), HoneyYellow, "NIGHT BAZAAR ✨"),

        // East Whisker Woods & Park
        CityBuilding("b_tea", "Whisker Tea Pagoda", "Whisker Woods", 45f, 25f, 14f, 14f, 14f, Color(0xFFDCFCE7), Color(0xFF16A34A), null, "TEA 🍵"),
        CityBuilding("b_library", "Feline Grand Library", "Whisker Woods", 82f, -15f, 20f, 18f, 18f, Color(0xFFF8FAFC), Color(0xFF334155), DustyRose, "LIBRARY 📖"),
        CityBuilding("b_petvet", "Happy Paws Wellness Clinic", "Whisker Woods", 48f, -50f, 16f, 14f, 13f, Color(0xFFE0F2FE), Color(0xFF0284C7), TealBrandLight, "CLINIC 🩺")
    )

    val props = listOf(
        // Trees around park and roads
        CityProp(PropType.TREE, 35f, 12f, 1.2f),
        CityProp(PropType.TREE, 55f, 12f, 1.1f),
        CityProp(PropType.TREE, 75f, 20f, 1.3f),
        CityProp(PropType.TREE, 75f, 45f, 1.2f),
        CityProp(PropType.TREE, 40f, 48f, 1.1f),
        CityProp(PropType.TREE, -35f, -12f, 1.0f),
        CityProp(PropType.TREE, -55f, -12f, 1.1f),
        CityProp(PropType.TREE, -20f, -75f, 1.2f),
        CityProp(PropType.TREE, 20f, -75f, 1.1f),
        CityProp(PropType.TREE, -35f, 75f, 1.0f),
        CityProp(PropType.TREE, 45f, 75f, 1.2f),

        // Streetlamps at key corners
        CityProp(PropType.STREETLAMP, -7f, -7f, 1.0f),
        CityProp(PropType.STREETLAMP, 7f, 7f, 1.0f),
        CityProp(PropType.STREETLAMP, -7f, -60f, 1.0f),
        CityProp(PropType.STREETLAMP, 7f, -60f, 1.0f),
        CityProp(PropType.STREETLAMP, -7f, 60f, 1.0f),
        CityProp(PropType.STREETLAMP, 7f, 60f, 1.0f),
        CityProp(PropType.STREETLAMP, -60f, -7f, 1.0f),
        CityProp(PropType.STREETLAMP, 60f, -7f, 1.0f),

        // Fire hydrants & flower pots
        CityProp(PropType.FIRE_HYDRANT, 6f, 14f, 0.9f),
        CityProp(PropType.FIRE_HYDRANT, -6f, -14f, 0.9f),
        CityProp(PropType.FLOWER_POT, -14f, 8f, 1.0f),
        CityProp(PropType.FLOWER_POT, 14f, -8f, 1.0f),

        // 3D Delivery Parcels & Newspaper Stands placed prominently in the city!
        CityProp(PropType.DELIVERY_PARCEL, -6.8f, 7f, 1.1f), // Outside Lily's Bakery
        CityProp(PropType.DELIVERY_PARCEL, -6.8f, 9.5f, 0.9f), // Beside Bakery doorstep
        CityProp(PropType.NEWSPAPER_STAND, 6.8f, 28f, 1.2f), // Outside The Daily Purr Press
        CityProp(PropType.DELIVERY_PARCEL, 6.8f, 32f, 1.0f), // Daily Purr news package
        CityProp(PropType.PARCEL_STACK, -6.8f, -14f, 1.2f), // Outside Post Paws Courier Hub
        CityProp(PropType.DELIVERY_PARCEL, -6.8f, 50f, 1.0f), // Outside Momo Law
        CityProp(PropType.DELIVERY_PARCEL, 6.8f, 50f, 1.0f), // Outside Grandma Whiskers' Cottage
        CityProp(PropType.TRAFFIC_LIGHT, -6.8f, 0f, 1.1f), // Central Intersection
        CityProp(PropType.TRAFFIC_LIGHT, 6.8f, 0f, 1.1f),

        // Wet puddle reflections for rain
        CityProp(PropType.PUDDLE, 3f, 25f, 1.3f),
        CityProp(PropType.PUDDLE, -18f, -3f, 1.1f),
        CityProp(PropType.PUDDLE, -3f, -42f, 1.4f)
    )

    val initialNpcVehicles = listOf(
        NpcVehicle(
            "car_blue",
            x = 3f,
            z = -80f,
            headingDeg = 180f,
            speed = 10f,
            color = Color(0xFF38BDF8),
            waypoints = listOf(Pair(3f, -100f), Pair(3f, 100f))
        ),
        NpcVehicle(
            "moped_pink",
            x = -3f,
            z = 60f,
            headingDeg = 0f,
            speed = 12f,
            color = DustyRoseLight,
            isMoped = true,
            waypoints = listOf(Pair(-3f, 100f), Pair(-3f, -100f))
        ),
        NpcVehicle(
            "car_yellow",
            x = -80f,
            z = 3f,
            headingDeg = 90f,
            speed = 11f,
            color = HoneyYellowLight,
            waypoints = listOf(Pair(-100f, 3f), Pair(100f, 3f))
        ),
        NpcVehicle(
            "car_green",
            x = 80f,
            z = -3f,
            headingDeg = 270f,
            speed = 9f,
            color = TealBrandLight,
            waypoints = listOf(Pair(100f, -3f), Pair(-100f, -3f))
        )
    )

    val initialPedestrians = listOf(
        NpcPedestrian("ped_bunny", "🐰", 12f, 20f, 90f, 2.5f, 12f, 12f, 20f),
        NpcPedestrian("ped_bear", "🐻", -12f, -20f, 0f, 2.0f, 10f, -12f, -20f),
        NpcPedestrian("ped_duck", "🦆", 20f, -12f, 180f, 2.8f, 14f, 20f, -12f),
        NpcPedestrian("ped_fox", "🦊", -40f, -70f, 270f, 2.2f, 10f, -40f, -70f),
        NpcPedestrian("ped_cat2", "🐱", 50f, 18f, 45f, 2.4f, 15f, 50f, 18f)
    )
}
