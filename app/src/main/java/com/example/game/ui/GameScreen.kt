package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.game.engine.CatRiderRenderer3D
import com.example.game.engine.CityRenderer3D
import com.example.game.model.WeatherCondition
import com.example.game.state.GameViewModel

@Composable
fun GameScreen(
    viewModel: GameViewModel = viewModel()
) {
    val isCharacterCreatorOpen by viewModel.isCharacterCreatorOpen.collectAsState()
    val offeredJob by viewModel.offeredJob.collectAsState()
    val isPhoneOpen by viewModel.isPhoneOpen.collectAsState()
    val isOutOfFuel by viewModel.isOutOfFuel.collectAsState()
    val customerDialogue by viewModel.customerDialogue.collectAsState()

    val showOfferDialog by viewModel.showDeliveryOfferDialog.collectAsState()
    val showCustomerDialog by viewModel.showCustomerDialog.collectAsState()
    val showGarageDialog by viewModel.showGarageDialog.collectAsState()
    val showSettingsDialog by viewModel.showSettingsDialog.collectAsState()
    val showExpandedMapDialog by viewModel.showExpandedMapDialog.collectAsState()
    val showAssetLibraryDialog by viewModel.showAssetLibraryDialog.collectAsState()
    val showOutOfFuelDialog by viewModel.showOutOfFuelDialog.collectAsState()
    val showTutorialDialog by viewModel.showTutorialDialog.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        // 1. Isolated 3D Game World Canvas
        GameWorldCanvas(viewModel = viewModel)

        // 2. Persistent Game HUD (Speed, Fuel, Profile, Radar, Controls)
        GameHud(viewModel = viewModel)

        // 3. Tactile Touch Controls (Virtual Steering, Gas, Brake, Drift, Horn, Context)
        TouchControls(viewModel = viewModel)

        // 4. Modals & Interactive Overlays
        // Delivery Request Popup
        if (showOfferDialog && offeredJob != null) {
            DeliveryRequestDialog(
                job = offeredJob!!,
                onAccept = { viewModel.acceptOfferedJob() },
                onDecline = { viewModel.declineOfferedJob() }
            )
        }

        // Customer Dropoff Interaction
        if (showCustomerDialog && customerDialogue != null) {
            CustomerInteractionDialog(
                dialogueText = customerDialogue!!,
                onContinue = { viewModel.showCustomerDialog.value = false }
            )
        }

        // MeowPhone In-Game Smartphone
        if (isPhoneOpen) {
            MeowPhoneDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.togglePhone() }
            )
        }

        // Garage Customization
        if (showGarageDialog) {
            GarageCustomizationDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showGarageDialog.value = false }
            )
        }

        // Settings Dialog
        if (showSettingsDialog) {
            SettingsDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showSettingsDialog.value = false }
            )
        }

        // Expanded 2D City Map
        if (showExpandedMapDialog) {
            ExpandedMapDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showExpandedMapDialog.value = false }
            )
        }

        // Asset Library & 3D Model Showcase
        if (showAssetLibraryDialog) {
            AssetLibraryDialog(
                viewModel = viewModel,
                onDismiss = { viewModel.showAssetLibraryDialog.value = false }
            )
        }

        // Out Of Fuel Dialog
        if (showOutOfFuelDialog && isOutOfFuel) {
            OutOfFuelDialog(viewModel = viewModel)
        }

        // Onboarding Tutorial
        if (showTutorialDialog) {
            TutorialOverlay(onDismiss = { viewModel.showTutorialDialog.value = false })
        }

        // Character & Scooter Creator Screen (First Launch & Re-customization)
        if (isCharacterCreatorOpen) {
            CharacterCreatorScreen(viewModel = viewModel)
        }
    }
}

@Composable
private fun GameWorldCanvas(viewModel: GameViewModel) {
    val customization by viewModel.customization.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val isCelebrating by viewModel.isCelebrating.collectAsState()
    val npcVehicles by viewModel.npcVehicles.collectAsState()
    val npcPedestrians by viewModel.npcPedestrians.collectAsState()

    // 3D Renderers
    val cityRenderer = remember { CityRenderer3D() }
    val catRiderRenderer = remember { CatRiderRenderer3D() }

    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        var lastNanos = System.nanoTime()
        while (true) {
            withFrameNanos { nanos ->
                val deltaSec = ((nanos - lastNanos) / 1_000_000_000f).coerceIn(0.001f, 0.1f)
                lastNanos = nanos
                cityRenderer.update(deltaSec)
                catRiderRenderer.updateAnimations(deltaSec)
                frameTick = nanos
            }
        }
    }

    Canvas(modifier = Modifier.fillMaxSize()) {
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        // Render 3D City, Roads, Buildings, Props, NPC traffic, and Delivery Beacons
        cityRenderer.renderCity(
            drawScope = this,
            camera = viewModel.camera,
            weather = settings.weather,
            activeJob = activeJob,
            npcVehicles = npcVehicles,
            npcPedestrians = npcPedestrians
        )

        // Render 3D Cat Rider, Scooter, Ears, Tail, Lean Physics
        val isHeadlightOn = settings.weather == WeatherCondition.NEON_NIGHT || settings.weather == WeatherCondition.GOLDEN_SUNSET
        catRiderRenderer.renderCatRider(
            drawScope = this,
            camera = viewModel.camera,
            worldX = viewModel.playerX,
            worldY = viewModel.playerY,
            worldZ = viewModel.playerZ,
            headingDeg = viewModel.playerHeadingDeg,
            steeringAngleDeg = viewModel.steeringInput * 25f,
            speed = viewModel.playerSpeed,
            leanAngleDeg = viewModel.playerLeanDeg,
            customization = customization,
            isCelebrating = isCelebrating,
            isHeadlightOn = isHeadlightOn,
            activeJob = activeJob
        )
    }
}
