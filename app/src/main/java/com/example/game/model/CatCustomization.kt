package com.example.game.model

import androidx.compose.ui.graphics.Color
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import com.example.ui.theme.TealBrandLight

enum class FurColorOption(
    val label: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val earInnerColor: Color,
    val eyeColor: Color
) {
    GINGER_TABBY("Ginger Tabby", Color(0xFFF97316), Color(0xFFFED7AA), Color(0xFFF472B6), Color(0xFF10B981)),
    TUXEDO("Tuxedo B&W", Color(0xFF1E293B), Color(0xFFFFFFFF), Color(0xFFF472B6), Color(0xFFFBBF24)),
    CALICO("Calico Trispot", Color(0xFFEA580C), Color(0xFFFFFFFF), Color(0xFFF43F5E), Color(0xFF06B6D4)),
    CREAM_VANILLA("Cream Vanilla", Color(0xFFFEF3C7), Color(0xFFFFFBEB), Color(0xFFFBCFE8), Color(0xFF3B82F6)),
    SILVER_GRAY("Silver Gray", Color(0xFF94A3B8), Color(0xFFF1F5F9), Color(0xFFFDA4AF), Color(0xFF10B981)),
    SIAMESE("Siamese Point", Color(0xFFFDE68A), Color(0xFF451A03), Color(0xFFF472B6), Color(0xFF2563EB))
}

enum class HelmetOption(val label: String, val color: Color, val accentColor: Color, val hasEars: Boolean) {
    NONE_BOW("Cute Ear Bow", DustyRoseLight, Color.White, true),
    BUBBLE_TEAL("Bubble Teal Visor", TealBrand, Color(0xFFCCFBF1), false),
    ROSE_CAT_EAR("Rose Kitty Visor", DustyRose, Color.White, true),
    HONEY_DOME("Honey Safety Dome", HoneyYellow, Color(0xFF1E293B), false),
    AVIATOR_GOGGLES("Aviator & Goggles", Color(0xFF78350F), Color(0xFF38BDF8), false)
}

enum class JacketOption(val label: String, val color: Color, val trimColor: Color) {
    HONEY_VEST("Honey Courier Vest", HoneyYellow, Color(0xFF1E293B)),
    ROSE_WINDBREAKER("Rose Windbreaker", DustyRose, Color.White),
    TEAL_SLICKER("Teal Rain Slicker", TealBrand, HoneyYellowLight),
    COZY_SWEATER("Cozy Knit Sweater", Color(0xFFE2E8F0), DustyRoseLight),
    STREET_HOODIE("Streetwear Hoodie", Color(0xFF334155), TealBrandLight)
}

enum class BackpackOption(val label: String, val color: Color, val iconSymbol: String) {
    THERMAL_BENTO("Thermal Bento Box", HoneyYellow, "🍱"),
    PARCEL_POUCH("Pastel Parcel Pouch", DustyRose, "📦"),
    FISH_COOLER("Fish-Shaped Cooler", TealBrand, "🐟"),
    CATNIP_CRATE("Catnip Wooden Crate", Color(0xFF854D0E), "🌿"),
    CARGO_TRUNK("Heavy Cargo Trunk", Color(0xFF475569), "📮")
}

enum class ScooterModelOption(
    val label: String,
    val baseSpeed: Float,
    val baseAccel: Float,
    val fuelEfficiency: Float,
    val description: String
) {
    PASTEL_50CC("Pastel 50cc Moped", 26f, 18f, 1.2f, "Nimble, adorable, fuel-sipping classic."),
    TURBO_WHISKERS("Turbo Whiskers 110", 34f, 24f, 0.9f, "Sporty acceleration for urgent deliveries!"),
    ELECTRIC_MEW("Electric Mew-E", 30f, 28f, 1.4f, "Whisper quiet, instant torque, eco-friendly."),
    RETRO_SIDECAR("Retro Sidecar Cruiser", 28f, 16f, 1.0f, "Extra stable with maximum cargo pride.")
}

enum class ScooterColorOption(val label: String, val bodyColor: Color, val seatColor: Color) {
    TEAL_MINT("Teal Mint", TealBrand, Color(0xFF78350F)),
    DUSTY_ROSE("Dusty Rose", DustyRose, Color(0xFF1E293B)),
    HONEY_SUNSHINE("Honey Sunshine", HoneyYellow, Color(0xFF475569)),
    LAVENDER_MINT("Lavender Pastels", Color(0xFFA855F7), Color(0xFFFDE047)),
    MIDNIGHT_STEALTH("Midnight Chrome", Color(0xFF0F172A), Color(0xFFDC2626))
}

data class CatCustomization(
    val name: String = "Mochi",
    val furColor: FurColorOption = FurColorOption.GINGER_TABBY,
    val helmet: HelmetOption = HelmetOption.ROSE_CAT_EAR,
    val jacket: JacketOption = JacketOption.HONEY_VEST,
    val backpack: BackpackOption = BackpackOption.THERMAL_BENTO,
    val scooterModel: ScooterModelOption = ScooterModelOption.PASTEL_50CC,
    val scooterColor: ScooterColorOption = ScooterColorOption.TEAL_MINT
)
