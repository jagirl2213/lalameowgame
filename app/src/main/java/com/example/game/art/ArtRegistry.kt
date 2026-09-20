package com.example.game.art

import androidx.compose.ui.graphics.Color
import com.example.R
import com.example.game.model.DeliveryCategory
import com.example.game.model.PropType

/**
 * =============================================================================
 * LALAMEOW RIDER CENTRALIZED ASSET REGISTRY
 * =============================================================================
 * Centralized catalog for all game assets:
 * - Characters (Player Hero, Shopkeepers, Mechanics, Matriarchs, Pedestrians, Companions)
 * - Vehicles (Player Scooters, NPC Cars, Mopeds, Delivery Vans)
 * - Environment (Buildings, Facades, Districts, Roads, Landmarks)
 * - Props (Parcels, Newspaper Stands, Traffic Lights, Streetlamps, Hydrants, Flora)
 * - UI & Audio Assets
 */

enum class AssetCategory {
    CHARACTERS,
    VEHICLES,
    ENVIRONMENT,
    PROPS,
    UI,
    AUDIO,
    MOTION,
    VFX
}

data class RegistryAssetItem(
    val id: String,
    val name: String,
    val subtitle: String,
    val category: AssetCategory,
    val subCategory: String,
    val drawableRes: Int,
    val fallbackDrawableRes: Int = R.drawable.game_app_icon,
    val description: String,
    val systemTarget: String,
    val tags: List<String> = emptyList()
)

object ArtRegistry {

    val allAssets: List<RegistryAssetItem> = listOf(
        // =====================================================================
        // CHARACTERS
        // =====================================================================
        RegistryAssetItem(
            id = "char_nami",
            name = "Nami the Courier",
            subtitle = "Lead Playable Character",
            category = AssetCategory.CHARACTERS,
            subCategory = "PLAYER CAT",
            drawableRes = R.drawable.char_nami,
            description = "Energetic ginger tabby courier with aerodynamic ears, yellow safety helmet with tinted speed goggles, and official Lalameow delivery uniform.",
            systemTarget = "CatRiderRenderer3D & CharacterCreatorScreen",
            tags = listOf("hero", "playable", "cat", "courier", "ginger")
        ),
        RegistryAssetItem(
            id = "char_lily",
            name = "Lily the Baker",
            subtitle = "Artisan Pastry Chef",
            category = AssetCategory.CHARACTERS,
            subCategory = "SHOPKEEPERS",
            drawableRes = R.drawable.char_lily,
            description = "Master cat pastry chef operating Lily's Artisan Bakery. Regularly orders deliveries of fresh warm strawberry croissants and morning bread.",
            systemTarget = "DeliveryDialogs & CityWorldData",
            tags = listOf("npc", "bakery", "shopkeeper", "cat")
        ),
        RegistryAssetItem(
            id = "char_benny",
            name = "Benny the Mechanic",
            subtitle = "Shell-Paws Station Operator",
            category = AssetCategory.CHARACTERS,
            subCategory = "GAS-STATION WORKERS",
            drawableRes = R.drawable.char_benny,
            description = "Friendly fox mechanic stationed at the Shell-Paws gas depot. Refuels scooters, tunes carburetors, and shares shortcut tips.",
            systemTarget = "OutOfFuelDialog & CityWorldData",
            tags = listOf("npc", "mechanic", "fuel", "fox")
        ),
        RegistryAssetItem(
            id = "char_momo",
            name = "Momo Esq.",
            subtitle = "Corporate Legal Consultant",
            category = AssetCategory.CHARACTERS,
            subCategory = "CUSTOMERS",
            drawableRes = R.drawable.char_momo,
            description = "Dapper Siamese legal cat in sharp collar and tie. Handles VIP courier contracts and urgent document transfers.",
            systemTarget = "DeliveryDialogs & PhoneMessages",
            tags = listOf("npc", "vip", "customer", "siamese")
        ),
        RegistryAssetItem(
            id = "char_grandma",
            name = "Grandma Whiskers",
            subtitle = "Cottage Matriarch",
            category = AssetCategory.CHARACTERS,
            subCategory = "CUSTOMERS",
            drawableRes = R.drawable.char_grandma,
            description = "Beloved sweet grandmother cat residing in the sunny market cottage. Loves morning parcel deliveries of rainbow yarn and tea.",
            systemTarget = "DeliveryDialogs & IntroJob",
            tags = listOf("npc", "customer", "cottage", "generous-tips")
        ),
        RegistryAssetItem(
            id = "char_pigeon",
            name = "Mr. Pickles",
            subtitle = "City Traffic Scout",
            category = AssetCategory.CHARACTERS,
            subCategory = "COMPANIONS",
            drawableRes = R.drawable.char_pigeon,
            description = "Urban carrier pigeon with tiny pilot goggles. Perches on streetlamps to monitor traffic jams, road obstacles, and weather changes.",
            systemTarget = "CityRenderer3D Ambient Props",
            tags = listOf("npc", "bird", "traffic-scout", "companion")
        ),

        // =====================================================================
        // VEHICLES
        // =====================================================================
        RegistryAssetItem(
            id = "veh_scooter_meowped",
            name = "Meow-Ped 50cc Classic",
            subtitle = "Starter Delivery Scooter",
            category = AssetCategory.VEHICLES,
            subCategory = "SCOOTERS",
            drawableRes = R.drawable.img_nami_rider,
            description = "Agile honey-yellow retro scooter with chrome handlebars, round headlamp, rear parcel rack, and cushioned vinyl seat.",
            systemTarget = "CatRiderRenderer3D & GarageCustomizationDialog",
            tags = listOf("vehicle", "scooter", "classic", "starter")
        ),
        RegistryAssetItem(
            id = "veh_npc_blue_sedan",
            name = "Paws-Mobile Sedan",
            subtitle = "City Traffic Car",
            category = AssetCategory.VEHICLES,
            subCategory = "NPC VEHICLES",
            drawableRes = R.drawable.img_city_environment,
            description = "Miniature toy-like urban cruiser sharing the streets and observing traffic signal stops at intersections.",
            systemTarget = "CityRenderer3D NPC Traffic System",
            tags = listOf("vehicle", "npc", "car", "traffic")
        ),

        // =====================================================================
        // ENVIRONMENT & BUILDINGS
        // =====================================================================
        RegistryAssetItem(
            id = "env_lily_bakery",
            name = "Lily's Artisan Bakery",
            subtitle = "Downpaws Market Flagship Store",
            category = AssetCategory.ENVIRONMENT,
            subCategory = "BAKERIES",
            drawableRes = R.drawable.img_city_environment,
            description = "Warm peach facade with pink-and-white striped awning, display windows showcasing croissants, and chimney puffs.",
            systemTarget = "CityWorldData & CityRenderer3D",
            tags = listOf("building", "bakery", "storefront", "market")
        ),
        RegistryAssetItem(
            id = "env_daily_purr_press",
            name = "The Daily Purr Press",
            subtitle = "Newspaper Printing Headquarters",
            category = AssetCategory.ENVIRONMENT,
            subCategory = "OFFICES",
            drawableRes = R.drawable.item_newspaper,
            description = "Stout brick headquarters with printing presses, front headline marquee, and sidewalk distribution rack.",
            systemTarget = "CityWorldData & IntroMission",
            tags = listOf("building", "news", "press", "office")
        ),
        RegistryAssetItem(
            id = "env_shell_paws_station",
            name = "Shell-Paws Gas Depot",
            subtitle = "24/7 Refueling & Tune-Up Station",
            category = AssetCategory.ENVIRONMENT,
            subCategory = "GAS STATIONS",
            drawableRes = R.drawable.char_benny,
            description = "Illuminated yellow-and-red service canopy, twin digital fuel pumps, price signage, and air hose station.",
            systemTarget = "CityWorldData & GasStationFuelSystem",
            tags = listOf("building", "fuel", "gas-station", "service")
        ),

        // =====================================================================
        // PROPS & DELIVERY ITEMS
        // =====================================================================
        RegistryAssetItem(
            id = "prop_delivery_parcel",
            name = "Lalameow Delivery Parcel",
            subtitle = "3D Standard Courier Package",
            category = AssetCategory.PROPS,
            subCategory = "DELIVERY BOXES",
            drawableRes = R.drawable.item_parcel,
            description = "Official craft cardboard parcel box sealed with Lalameow yellow paw-print tape, fragile postage stamp, and barcode label.",
            systemTarget = "CityRenderer3D WaypointBeacon & ScooterLuggageRack",
            tags = listOf("prop", "delivery", "parcel", "box", "3D")
        ),
        RegistryAssetItem(
            id = "prop_daily_purr_newspaper",
            name = "The Daily Purr Newspaper Roll",
            subtitle = "3D Rolled Morning Edition",
            category = AssetCategory.PROPS,
            subCategory = "NEWSPAPER BOXES",
            drawableRes = R.drawable.item_newspaper,
            description = "Crisp rolled daily morning edition newspaper with printed headlines and royal blue twine ribbon.",
            systemTarget = "CityRenderer3D WaypointBeacon & ScooterRack",
            tags = listOf("prop", "delivery", "newspaper", "daily-purr", "3D")
        ),
        RegistryAssetItem(
            id = "prop_croissant_box",
            name = "Artisan Pastry Thermal Box",
            subtitle = "3D Warm Bakery Delivery Box",
            category = AssetCategory.PROPS,
            subCategory = "PACKAGE CRATES",
            drawableRes = R.drawable.item_croissant,
            description = "Pastel insulated pastry box containing freshly baked butter croissants and sweet tarts from Lily's oven.",
            systemTarget = "CityRenderer3D WaypointBeacon",
            tags = listOf("prop", "delivery", "pastry", "bakery", "3D")
        ),
        RegistryAssetItem(
            id = "prop_traffic_light",
            name = "3D Traffic Signal Post",
            subtitle = "Street Intersection Infrastructure",
            category = AssetCategory.PROPS,
            subCategory = "TRAFFIC LIGHTS",
            drawableRes = R.drawable.img_city_environment,
            description = "Full 3D post with cycling Red, Amber, and Green lens illuminations controlling intersection rights of way.",
            systemTarget = "CityRenderer3D PropSystem",
            tags = listOf("prop", "traffic", "signal", "3D")
        ),
        RegistryAssetItem(
            id = "prop_newspaper_stand",
            name = "Sidewalk Newspaper Stand",
            subtitle = "Public News Distribution Rack",
            category = AssetCategory.PROPS,
            subCategory = "STREET VENDORS",
            drawableRes = R.drawable.item_newspaper,
            description = "Curbside metal rack displaying freshly printed headlines with morning editions ready for pick-up.",
            systemTarget = "CityRenderer3D PropSystem",
            tags = listOf("prop", "stand", "news", "3D")
        ),

        // =====================================================================
        // UI & BRANDING
        // =====================================================================
        RegistryAssetItem(
            id = "ui_game_app_icon",
            name = "Lalameow Rider Crest",
            subtitle = "Official Emblem & App Icon",
            category = AssetCategory.UI,
            subCategory = "ICONS",
            drawableRes = R.drawable.game_app_icon,
            description = "Iconic golden brand seal of Lalameow Rider featuring the cheerful courier cat face, goggles, and racing laurels.",
            systemTarget = "Launcher, HUD & AssetLibrary",
            tags = listOf("ui", "branding", "logo", "app-icon")
        )
    )

    fun getByCategory(category: AssetCategory): List<RegistryAssetItem> {
        return allAssets.filter { it.category == category }
    }

    fun getById(id: String): RegistryAssetItem? {
        return allAssets.firstOrNull { it.id == id }
    }
}
