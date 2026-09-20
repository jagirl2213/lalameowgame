package com.example.game.art

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import com.example.ui.theme.TealBrandLight

/**
 * =============================================================================
 * LALAMEOW RIDER ART BIBLE & DESIGN TOKEN SPECIFICATION
 * =============================================================================
 * Cohesive aesthetic guidelines for Lalameow Rider:
 * - Miniature diorama proportions (tilt-shift feel, rounded chamfers, toybox scale)
 * - Stylized 3D character design with expressive feline features
 * - Soft cinematic palette with clear district identities
 * - Dynamic tactile UI/UX built for responsive touchscreen mobile gaming
 */
object ArtBible {

    // -------------------------------------------------------------------------
    // 1. BRAND & CORE PALETTE TOKENS
    // -------------------------------------------------------------------------
    object Colors {
        // Hero Brand Tones
        val HoneyGold = HoneyYellow // #F59E0B
        val HoneyGoldLight = HoneyYellowLight // #FCD34D
        val HoneyCream = HoneyYellowCream // #FFFBEB
        val TealCourier = TealBrand // #0D9488
        val TealCourierLight = TealBrandLight // #2DD4BF
        val RoseBerry = DustyRose // #E11D48
        val RoseBerryLight = DustyRoseLight // #FDA4AF

        // Miniature City Surfaces
        val SlateAsphalt = Color(0xFF334155)
        val SlateAsphaltWet = Color(0xFF1E293B)
        val NightAsphalt = Color(0xFF0F172A)
        val SidewalkConcrete = Color(0xFFCBD5E1)
        val SidewalkCurbLight = Color(0xFFE2E8F0)
        val RoadLineYellow = Color(0xFFFDE047)
        val CrosswalkWhite = Color(0xFFFFFFFF)

        // Architectural Wall Pastels
        val BakeryPeach = Color(0xFFFFEDD5)
        val CafeMint = Color(0xFFCCFBF1)
        val CottageButter = Color(0xFFFEF3C7)
        val LegalLavender = Color(0xFFEDE9FE)
        val StationNavy = Color(0xFF1E293B)
        val HubTeal = Color(0xFFE0F2FE)
        val MarketCoral = Color(0xFFFFE4E6)

        // Dynamic Lighting Moods
        val SunDaylight = Color(0xFFFEF08A)
        val SunSunset = Color(0xFFFB923C)
        val SkyDay = Color(0xFFBAE6FD)
        val SkySunset = Color(0xFFFED7AA)
        val SkyRain = Color(0xFF94A3B8)
        val SkyNight = Color(0xFF0F172A)
        val WindowGlowNight = Color(0xFFFEF08A)
        val StreetlightCone = Color(0x33FEF08A)
        val HeadlightGlow = Color(0x55FEF08A)
        val NeonCyan = Color(0xFF06B6D4)
        val NeonPink = Color(0xFFF43F5E)
    }

    // -------------------------------------------------------------------------
    // 2. UI/UX SHAPE & SPACING TOKENS
    // -------------------------------------------------------------------------
    object Geometry {
        val CornerSmall: Dp = 8.dp
        val CornerMedium: Dp = 14.dp
        val CornerLarge: Dp = 20.dp
        val CornerXLarge: Dp = 28.dp

        val TouchTargetMin: Dp = 48.dp
        val TouchTargetPedalWidth: Dp = 78.dp
        val TouchTargetPedalHeight: Dp = 88.dp
        val TouchTargetJoystickDiameter: Dp = 130.dp

        val ShadowElevationLow: Dp = 2.dp
        val ShadowElevationMedium: Dp = 6.dp
        val ShadowElevationHigh: Dp = 12.dp
        val ShadowElevationPopup: Dp = 24.dp
    }

    // -------------------------------------------------------------------------
    // 3. DISTRICT IDENTITIES
    // -------------------------------------------------------------------------
    enum class DistrictId(
        val displayName: String,
        val themeEmoji: String,
        val ambientColor: Color,
        val architecturalStyle: String,
        val primarySoundscape: String
    ) {
        CENTRAL_CITY(
            displayName = "Central City",
            themeEmoji = "🏙️",
            ambientColor = Color(0xFF38BDF8),
            architecturalStyle = "Modern multi-tier buildings, large illuminated storefronts, glass canopies",
            primarySoundscape = "City traffic hum, distant crosswalk beeps, brisk chatter"
        ),
        DOWNPAWS_MARKET(
            displayName = "Downpaws Market",
            themeEmoji = "🏪",
            ambientColor = Color(0xFFF59E0B),
            architecturalStyle = "Vibrant fabric awnings, roadside produce crates, newsstands, warm bakeries",
            primarySoundscape = "Market vendor calls, sizzling bakery stoves, scooter horns"
        ),
        SUNNY_SUBURBS(
            displayName = "Sunny Suburbs",
            themeEmoji = "🏡",
            ambientColor = Color(0xFF4ADE80),
            architecturalStyle = "Charming cottages, blooming flower pots, white picket fences, lush trees",
            primarySoundscape = "Chirping birds, rustling leaves, gentle neighborhood breeze"
        ),
        WHISKER_WATERFRONT(
            displayName = "Whisker Waterfront",
            themeEmoji = "⛵",
            ambientColor = Color(0xFF0EA5E9),
            architecturalStyle = "Arched canal bridge, stone quayside promenade, seaside cafes",
            primarySoundscape = "Water sloshing, harbor buoys, cheerful seagulls"
        ),
        NEON_NIGHT_ROW(
            displayName = "Neon Night Row",
            themeEmoji = "🏮",
            ambientColor = Color(0xFFA855F7),
            architecturalStyle = "Glow-trimmed signage, late-night ramen bar, cozy lantern illumination",
            primarySoundscape = "Ambient synth pads, bubbling broth, night crickets"
        )
    }

    // -------------------------------------------------------------------------
    // 4. CHARACTER ANATOMY & RIG PROPORTIONS
    // -------------------------------------------------------------------------
    object CharacterSpecs {
        const val HEAD_TO_BODY_RATIO = 0.85f // Stylized chibi proportions
        const val EAR_TWITCH_INTERVAL_SEC = 3.5f
        const val BLINK_INTERVAL_SEC = 3.2f
        const val BLINK_DURATION_SEC = 0.15f
        const val TAIL_WAG_FREQUENCY = 5.0f
        const val LEAN_MAX_DEGREES = 32f
        const val SUSPENSION_BOUNCE_PX = 4f
    }
}
