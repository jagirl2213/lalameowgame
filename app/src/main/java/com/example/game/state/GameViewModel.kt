package com.example.game.state

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.game.audio.GameSoundEngine
import com.example.game.audio.HapticType
import com.example.game.engine.Camera3D
import com.example.game.model.BackpackOption
import com.example.game.model.CameraMode
import com.example.game.model.CatCustomization
import com.example.game.model.CityWorldData
import com.example.game.model.DeliveryCategory
import com.example.game.model.DeliveryJob
import com.example.game.model.DeliveryState
import com.example.game.model.FurColorOption
import com.example.game.model.GameSettings
import com.example.game.model.GasStation
import com.example.game.model.HelmetOption
import com.example.game.model.JacketOption
import com.example.game.model.MessageType
import com.example.game.model.NpcPedestrian
import com.example.game.model.NpcVehicle
import com.example.game.model.PhoneMessage
import com.example.game.model.ScooterColorOption
import com.example.game.model.WeatherCondition
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

data class DailyChallenge(
    val id: String,
    val title: String,
    val rewardCoins: Int,
    val currentProgress: Int,
    val targetGoal: Int,
    val isCompleted: Boolean = false
)

class GameViewModel(application: Application) : AndroidViewModel(application) {

    val soundEngine = GameSoundEngine(application.applicationContext)

    // Player Physics State (Reactive Compose State)
    var playerX by mutableFloatStateOf(0f)
    var playerY by mutableFloatStateOf(0f)
    var playerZ by mutableFloatStateOf(0f)
    var playerSpeed by mutableFloatStateOf(0f)
    var playerHeadingDeg by mutableFloatStateOf(0f)
    var playerLeanDeg by mutableFloatStateOf(0f)
    var steeringInput by mutableFloatStateOf(0f)
    var throttleInput by mutableFloatStateOf(0f)
    var brakeInput by mutableFloatStateOf(0f)
    var isDrifting by mutableStateOf(false)

    // Camera
    val camera = Camera3D()
    private var freeLookYawOffset = 0f

    // Reactive State Flows
    private val _customization = MutableStateFlow(CatCustomization())
    val customization = _customization.asStateFlow()

    private val _settings = MutableStateFlow(GameSettings())
    val settings = _settings.asStateFlow()

    private val _fuelPercent = MutableStateFlow(85f)
    val fuelPercent = _fuelPercent.asStateFlow()

    private val _isOutOfFuel = MutableStateFlow(false)
    val isOutOfFuel = _isOutOfFuel.asStateFlow()

    private val _coins = MutableStateFlow(120)
    val coins = _coins.asStateFlow()

    private val _reputation = MutableStateFlow(35)
    val reputation = _reputation.asStateFlow()

    private val _activeJob = MutableStateFlow<DeliveryJob?>(null)
    val activeJob = _activeJob.asStateFlow()

    private val _offeredJob = MutableStateFlow<DeliveryJob?>(null)
    val offeredJob = _offeredJob.asStateFlow()

    private val _phoneMessages = MutableStateFlow<List<PhoneMessage>>(emptyList())
    val phoneMessages = _phoneMessages.asStateFlow()

    private val _isPhoneOpen = MutableStateFlow(false)
    val isPhoneOpen = _isPhoneOpen.asStateFlow()

    private val _nearestGasStation = MutableStateFlow<GasStation?>(null)
    val nearestGasStation = _nearestGasStation.asStateFlow()

    private val _isNearGasPump = MutableStateFlow(false)
    val isNearGasPump = _isNearGasPump.asStateFlow()

    private val _isRefueling = MutableStateFlow(false)
    val isRefueling = _isRefueling.asStateFlow()

    private val _isCelebrating = MutableStateFlow(false)
    val isCelebrating = _isCelebrating.asStateFlow()

    private val _contextActionPrompt = MutableStateFlow<String?>(null)
    val contextActionPrompt = _contextActionPrompt.asStateFlow()

    private val _customerDialogue = MutableStateFlow<String?>(null)
    val customerDialogue = _customerDialogue.asStateFlow()

    // Upgrades
    private val _speedUpgradeLevel = MutableStateFlow(1)
    val speedUpgradeLevel = _speedUpgradeLevel.asStateFlow()

    private val _accelUpgradeLevel = MutableStateFlow(1)
    val accelUpgradeLevel = _accelUpgradeLevel.asStateFlow()

    private val _tankUpgradeLevel = MutableStateFlow(1)
    val tankUpgradeLevel = _tankUpgradeLevel.asStateFlow()

    // Daily Challenges
    private val _dailyChallenges = MutableStateFlow(
        listOf(
            DailyChallenge("c1", "Complete 3 deliveries", 80, 0, 3),
            DailyChallenge("c2", "Refuel at a gas station", 50, 0, 1),
            DailyChallenge("c3", "Deliver The Daily Purr newspaper", 100, 0, 1)
        )
    )
    val dailyChallenges = _dailyChallenges.asStateFlow()

    // UI Dialogs
    val isCharacterCreatorOpen = MutableStateFlow(false)
    val showAssetLibraryDialog = MutableStateFlow(false)
    val showDeliveryOfferDialog = MutableStateFlow(false)
    val showCustomerDialog = MutableStateFlow(false)
    val showGarageDialog = MutableStateFlow(false)
    val showSettingsDialog = MutableStateFlow(false)
    val showExpandedMapDialog = MutableStateFlow(false)
    val showOutOfFuelDialog = MutableStateFlow(false)
    val showTutorialDialog = MutableStateFlow(false)

    // Dynamic NPCs
    val npcVehicles = MutableStateFlow(CityWorldData.initialNpcVehicles.map { it.copy() })
    val npcPedestrians = MutableStateFlow(CityWorldData.initialPedestrians.map { it.copy() })

    // Weather & Day/Night
    val timeOfDayHours = MutableStateFlow(14.0f) // 2:00 PM

    private var loopJob: Job? = null

    init {
        // Add welcome phone messages
        addPhoneMessage(
            PhoneMessage(
                id = "m_welcome",
                sender = "Chief Whiskers",
                senderAvatar = "🐱",
                title = "Welcome to Lalameow Rider!",
                body = "Welcome aboard courier! Keep that scooter fueled and get ready for orders on your MeowPhone. Purr-fect deliveries await!",
                timeLabel = "Now",
                type = MessageType.DELIVERY_ALERT
            )
        )
        addPhoneMessage(
            PhoneMessage(
                id = "m_promo",
                sender = "Neko Fish Market",
                senderAvatar = "🐟",
                title = "Hot Tuna Sashimi Bento Ready!",
                body = "Need quick paws for lunchtime orders. Big tips for swift delivery!",
                timeLabel = "5m ago",
                type = MessageType.PROMO_SPAM
            )
        )

        soundEngine.startEngine()
        startSimulationLoop()

        // Set intro delivery job active immediately so player has parcels/newspapers to pick up and deliver right away!
        val introJob = createIntroDeliveryJob()
        _activeJob.value = introJob
        addPhoneMessage(
            PhoneMessage(
                id = "m_intro_job",
                sender = introJob.pickupVenue,
                senderAvatar = "🗞️",
                title = "Delivery Ready: ${introJob.title}",
                body = "Head straight ahead down Downpaws Market road to collect today's fresh papers from ${introJob.pickupVenue}!",
                timeLabel = "Now",
                type = MessageType.DELIVERY_ALERT
            )
        )
    }

    private fun createIntroDeliveryJob(): DeliveryJob {
        return DeliveryJob(
            id = "job_newspaper_intro",
            title = "The Daily Purr (Special Edition)",
            description = "Express Morning Delivery! Collect today's hot newspaper rolls from The Daily Purr Press and deliver to Grandma Whiskers!",
            category = DeliveryCategory.NEWSPAPER,
            pickupVenue = "The Daily Purr Press",
            pickupDistrict = "Downpaws Market",
            pickupX = 16f,
            pickupZ = 32f,
            dropoffCustomer = "Grandma Whiskers",
            dropoffDistrict = "Downpaws Market",
            dropoffCustomerAvatar = "👵🐱",
            dropoffX = 16f,
            dropoffZ = 68f,
            customerDialogue = "\"Oh bless you, Nami! Fresh morning news and warm croissants right at my doorstep! Purr-fect service!\"",
            newspaperHeadline = "LALAMEOW COURIER NAMI HITS THE STREETS IN STYLE!",
            rewardCoins = 75,
            rewardRep = 25,
            estimatedDistanceMeters = 36,
            isUrgent = false,
            state = DeliveryState.ACCEPTED
        )
    }

    private fun startSimulationLoop() {
        loopJob?.cancel()
        loopJob = viewModelScope.launch {
            var lastTime = System.currentTimeMillis()
            while (isActive) {
                val now = System.currentTimeMillis()
                val delta = ((now - lastTime) / 1000f).coerceIn(0.001f, 0.1f)
                lastTime = now

                updatePhysics(delta)
                updateCamera(delta)
                updateNpcs(delta)
                updateNavigationAndContext()

                delay(16) // ~60 FPS
            }
        }
    }

    private fun updatePhysics(delta: Float) {
        if (_isOutOfFuel.value) {
            throttleInput = 0f
            playerSpeed = (playerSpeed - delta * 12f).coerceAtLeast(0f)
            soundEngine.updateEnginePitch(0f, false)
            return
        }

        // Handle Refueling
        if (_isRefueling.value) {
            val newFuel = (_fuelPercent.value + delta * 25f).coerceAtMost(100f)
            _fuelPercent.value = newFuel
            soundEngine.playFuelDing()
            if (newFuel >= 100f) {
                _isRefueling.value = false
            }
            return
        }

        val baseMaxSpeed = _customization.value.scooterModel.baseSpeed + (_speedUpgradeLevel.value - 1) * 3.5f
        val baseAccel = _customization.value.scooterModel.baseAccel + (_accelUpgradeLevel.value - 1) * 3f
        val maxSpeed = if (isDrifting) baseMaxSpeed * 1.15f else baseMaxSpeed

        // Acceleration and braking
        if (throttleInput > 0f) {
            val effAccel = baseAccel * throttleInput
            playerSpeed = (playerSpeed + effAccel * delta).coerceAtMost(maxSpeed)

            // Fuel depletion
            val tankMult = 1.0f / (1.0f + (_tankUpgradeLevel.value - 1) * 0.25f)
            val fuelBurn = delta * 0.45f * tankMult
            val remainingFuel = (_fuelPercent.value - fuelBurn).coerceAtLeast(0f)
            _fuelPercent.value = remainingFuel

            if (remainingFuel <= 0f) {
                _isOutOfFuel.value = true
                showOutOfFuelDialog.value = true
                soundEngine.playNotification()
                addPhoneMessage(
                    PhoneMessage(
                        id = "m_fuel_empty_${System.currentTimeMillis()}",
                        sender = "Roadside Paw-sistance",
                        senderAvatar = "⛽",
                        title = "Tank Empty!",
                        body = "Your scooter ran dry! Call Roadside Paw-sistance or push to the nearest Shell-Paws pump!",
                        timeLabel = "Now",
                        type = MessageType.FUEL_WARNING
                    )
                )
            } else if (remainingFuel < 20f && (_fuelPercent.value + fuelBurn) >= 20f) {
                // Low fuel alert
                soundEngine.playNotification()
                addPhoneMessage(
                    PhoneMessage(
                        id = "m_fuel_low_${System.currentTimeMillis()}",
                        sender = "MeowPhone Alert",
                        senderAvatar = "⚠️",
                        title = "Fuel Low Warning",
                        body = "Tank under 20%! Check your GPS radar for the nearest gas station.",
                        timeLabel = "Now",
                        type = MessageType.FUEL_WARNING
                    )
                )
            }
        } else if (brakeInput > 0f) {
            playerSpeed = (playerSpeed - baseAccel * 1.8f * brakeInput * delta).coerceAtLeast(-8f)
        } else {
            // Natural friction coasting
            val friction = 8.5f
            if (playerSpeed > 0f) {
                playerSpeed = (playerSpeed - friction * delta).coerceAtLeast(0f)
            } else if (playerSpeed < 0f) {
                playerSpeed = (playerSpeed + friction * delta).coerceAtMost(0f)
            }
        }

        // Steering & Yaw
        val turnRate = (if (isDrifting) 85f else 55f) * _settings.value.controlSensitivity
        val speedFactor = (playerSpeed / baseMaxSpeed).coerceIn(-1f, 1f)
        playerHeadingDeg = (playerHeadingDeg + steeringInput * turnRate * speedFactor * delta + 360f) % 360f

        // Dynamic Lean Angle
        val targetLean = (steeringInput * (if (isDrifting) 32f else 20f) * speedFactor.coerceAtLeast(0.2f)).coerceIn(-35f, 35f)
        playerLeanDeg += (targetLean - playerLeanDeg) * 12f * delta

        // Update 3D World Position
        val radHeading = (playerHeadingDeg * PI / 180.0).toFloat()
        val forwardX = sin(radHeading)
        val forwardZ = cos(radHeading)

        playerX = (playerX + forwardX * playerSpeed * delta).coerceIn(-115f, 115f)
        playerZ = (playerZ + forwardZ * playerSpeed * delta).coerceIn(-115f, 115f)

        // Audio Engine update
        soundEngine.updateEnginePitch(
            (playerSpeed / baseMaxSpeed).coerceIn(0f, 1f),
            throttleInput > 0.1f
        )
    }

    private fun updateCamera(delta: Float) {
        val radH = (playerHeadingDeg * PI / 180.0).toFloat()
        val camMode = _settings.value.cameraMode

        val camDist = camMode.distance + (playerSpeed / 15f)
        val camHeight = if (camMode == CameraMode.NAVIGATION_CAM) 8.5f else 4.5f

        val targetCamX = playerX - sin(radH) * camDist
        val targetCamZ = playerZ - cos(radH) * camDist
        val targetCamY = camHeight

        // Smooth camera spring
        camera.posX += (targetCamX - camera.posX) * 10f * delta
        camera.posY += (targetCamY - camera.posY) * 8f * delta
        camera.posZ += (targetCamZ - camera.posZ) * 10f * delta

        var targetYaw = playerHeadingDeg
        if (camMode == CameraMode.FREE_LOOK) {
            targetYaw += freeLookYawOffset
        } else if (camMode == CameraMode.CINEMATIC) {
            freeLookYawOffset = (freeLookYawOffset + delta * 25f) % 360f
            targetYaw += freeLookYawOffset
        }
        val diffYaw = ((targetYaw - camera.yawDeg + 540f) % 360f) - 180f
        camera.yawDeg = (camera.yawDeg + diffYaw * (10f * delta).coerceAtMost(1f)) % 360f
        if (camera.yawDeg < 0f) camera.yawDeg += 360f
        camera.pitchDeg = camMode.pitchDeg
        camera.fov = camMode.fov
    }

    private fun updateNpcs(delta: Float) {
        val vehicles = npcVehicles.value.map { v ->
            val targetWp = v.waypoints[v.currentWaypointIndex]
            val dx = targetWp.first - v.x
            val dz = targetWp.second - v.z
            val dist = sqrt(dx * dx + dz * dz)

            if (dist < 3f) {
                v.currentWaypointIndex = (v.currentWaypointIndex + 1) % v.waypoints.size
            } else {
                val angle = (atan2(dx, dz) * 180f / PI.toFloat() + 360f) % 360f
                v.headingDeg = angle
                val rad = angle * PI.toFloat() / 180f
                v.x += sin(rad) * v.speed * delta
                v.z += cos(rad) * v.speed * delta
            }
            v
        }
        npcVehicles.value = vehicles

        val peds = npcPedestrians.value.map { p ->
            p.walkPhase += delta * p.speed * 2.5f
            val rad = p.headingDeg * PI.toFloat() / 180f
            p.x += sin(rad) * p.speed * delta
            p.z += cos(rad) * p.speed * delta

            val distFromOrigin = sqrt((p.x - p.originX) * (p.x - p.originX) + (p.z - p.originZ) * (p.z - p.originZ))
            if (distFromOrigin > p.walkRadius) {
                p.headingDeg = (p.headingDeg + 180f) % 360f
            }
            p
        }
        npcPedestrians.value = peds
    }

    private fun updateNavigationAndContext() {
        // 1. Gas Station proximity
        var nearestStation: GasStation? = null
        var minDist = Float.MAX_VALUE
        CityWorldData.gasStations.forEach { s ->
            val dx = s.x - playerX
            val dz = s.z - playerZ
            val d = sqrt(dx * dx + dz * dz)
            if (d < minDist) {
                minDist = d
                nearestStation = s
            }
        }
        _nearestGasStation.value = nearestStation
        _isNearGasPump.value = minDist <= 8.5f

        // 2. Context Action Prompt (Pickup, Deliver, Refuel)
        val job = _activeJob.value
        if (_isNearGasPump.value) {
            _contextActionPrompt.value = "⛽ REFUEL SCOOTER"
        } else if (job != null && job.state == DeliveryState.ACCEPTED) {
            val distToPickup = sqrt((job.pickupX - playerX) * (job.pickupX - playerX) + (job.pickupZ - playerZ) * (job.pickupZ - playerZ))
            if (distToPickup < 12.0f) {
                val icon = if (job.category == DeliveryCategory.NEWSPAPER) "🗞️" else "📦"
                _contextActionPrompt.value = "$icon COLLECT ${job.category.label.uppercase()}"
            } else {
                _contextActionPrompt.value = null
            }
        } else if (job != null && job.state == DeliveryState.PICKED_UP) {
            val distToDropoff = sqrt((job.dropoffX - playerX) * (job.dropoffX - playerX) + (job.dropoffZ - playerZ) * (job.dropoffZ - playerZ))
            if (distToDropoff < 12.0f) {
                _contextActionPrompt.value = "🐾 DELIVER TO ${job.dropoffCustomer.uppercase()}"
            } else {
                _contextActionPrompt.value = null
            }
        } else {
            _contextActionPrompt.value = null
        }
    }

    fun handleContextAction() {
        val prompt = _contextActionPrompt.value ?: return

        if (prompt.startsWith("⛽")) {
            // Refuel action
            startRefueling()
        } else if (prompt.contains("COLLECT") || prompt.contains("PICKUP")) {
            // Pickup package or newspapers
            val job = _activeJob.value ?: return
            _activeJob.value = job.copy(state = DeliveryState.PICKED_UP)
            soundEngine.playCoinReward()
            soundEngine.triggerHaptic(HapticType.SUCCESS)
            val icon = if (job.category == DeliveryCategory.NEWSPAPER) "🗞️" else "📦"
            addPhoneMessage(
                PhoneMessage(
                    id = "m_picked_${System.currentTimeMillis()}",
                    sender = "Delivery Hub",
                    senderAvatar = icon,
                    title = "Delivery Collected!",
                    body = "Loaded '${job.title}' from ${job.pickupVenue}. Follow the luminous GPS route to ${job.dropoffCustomer} in ${job.dropoffDistrict}!",
                    timeLabel = "Now",
                    type = MessageType.DELIVERY_ALERT
                )
            )
        } else if (prompt.contains("DELIVER")) {
            // Complete Delivery
            completeDelivery()
        }
    }

    private fun startRefueling() {
        if (_coins.value < 10 && _fuelPercent.value < 100f) {
            soundEngine.playNotification()
            return
        }
        _isRefueling.value = true
        _coins.value = (_coins.value - 12).coerceAtLeast(0)
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)

        // Mark daily challenge if present
        val challenges = _dailyChallenges.value.map { c ->
            if (c.id == "c2") c.copy(currentProgress = 1, isCompleted = true) else c
        }
        _dailyChallenges.value = challenges
    }

    private fun completeDelivery() {
        val job = _activeJob.value ?: return
        _isCelebrating.value = true
        soundEngine.playDeliveryComplete()

        // Calculate earnings + tip
        val tip = (12..28).random()
        val totalCoinsEarned = job.rewardCoins + tip
        val totalRepEarned = job.rewardRep

        _coins.value += totalCoinsEarned
        _reputation.value += totalRepEarned
        _customerDialogue.value = "${job.customerDialogue}\n\n⭐ 5 Stars! Payout: +$totalCoinsEarned CatCoins (including +$tip coin tip!)"
        showCustomerDialog.value = true

        // Add to phone log
        addPhoneMessage(
            PhoneMessage(
                id = "m_done_${System.currentTimeMillis()}",
                sender = job.dropoffCustomer,
                senderAvatar = job.dropoffCustomerAvatar,
                title = "Delivery Complete! ⭐⭐⭐⭐⭐",
                body = "Thanks ${customization.value.name}! Loved the ${job.title}. Left you a generous tip!",
                timeLabel = "Just now",
                type = MessageType.CUSTOMER_CHAT
            )
        )

        // Challenge progress
        val updatedChallenges = _dailyChallenges.value.map { c ->
            if (c.id == "c1") {
                val newP = (c.currentProgress + 1).coerceAtMost(c.targetGoal)
                c.copy(currentProgress = newP, isCompleted = newP >= c.targetGoal)
            } else if (c.id == "c3" && job.category == DeliveryCategory.NEWSPAPER) {
                c.copy(currentProgress = 1, isCompleted = true)
            } else {
                c
            }
        }
        _dailyChallenges.value = updatedChallenges

        _activeJob.value = null

        viewModelScope.launch {
            delay(4000)
            _isCelebrating.value = false
            scheduleNextJobOffer(6000)
        }
    }

    private fun scheduleNextJobOffer(delayMs: Long) {
        viewModelScope.launch {
            delay(delayMs)
            if (_activeJob.value == null && _offeredJob.value == null) {
                generateRandomDeliveryJob()
            }
        }
    }

    fun generateRandomDeliveryJob() {
        val sampleJobs = listOf(
            DeliveryJob(
                id = "job_ramen_${System.currentTimeMillis()}",
                title = "Steaming Neko Miso Ramen",
                description = "Piping hot broth with fish cakes! Deliver quickly before it gets chilly.",
                category = DeliveryCategory.FOOD,
                pickupVenue = "Neko Miso Ramen",
                pickupDistrict = "Downpaws Market",
                pickupX = 16f,
                pickupZ = 16f,
                dropoffCustomer = "Madame Fluff",
                dropoffDistrict = "Sunny Suburbs",
                dropoffCustomerAvatar = "🐰",
                dropoffX = -35f,
                dropoffZ = -80f,
                customerDialogue = "\"Oh purr-fect! The miso broth is still bubbling! You are the speediest kitten in town!\"",
                rewardCoins = 45,
                rewardRep = 15,
                estimatedDistanceMeters = 110,
                isUrgent = true
            ),
            DeliveryJob(
                id = "job_newspaper_${System.currentTimeMillis()}",
                title = "The Daily Purr (Morning Edition)",
                description = "Deliver the morning papers! Headline: 'Cat Council Declares Nap Breaks Mandatory.'",
                category = DeliveryCategory.NEWSPAPER,
                pickupVenue = "Post Paws Courier Hub",
                pickupDistrict = "Downpaws Market",
                pickupX = 16f,
                pickupZ = -16f,
                dropoffCustomer = "Professor Barnaby",
                dropoffDistrict = "Sunny Suburbs",
                dropoffCustomerAvatar = "🐻",
                dropoffX = 0f,
                dropoffZ = -85f,
                customerDialogue = "\"Ah, the morning news! Let's see what the mischievous city mice have been up to!\"",
                newspaperHeadline = "MAYOR PAWS DECLARES 2 PM MANDATORY SNOOZE TIME",
                rewardCoins = 40,
                rewardRep = 12,
                estimatedDistanceMeters = 95
            ),
            DeliveryJob(
                id = "job_fish_${System.currentTimeMillis()}",
                title = "Fresh Tuna Sashimi Platter",
                description = "Direct catch from the wharf! Keep it fresh and cool in your thermal bag.",
                category = DeliveryCategory.FOOD,
                pickupVenue = "Old Wharf Fishmonger",
                pickupDistrict = "Fishbone Harbor",
                pickupX = -82f,
                pickupZ = -25f,
                dropoffCustomer = "Grandma Tabitha",
                dropoffDistrict = "Sunny Suburbs",
                dropoffCustomerAvatar = "🐱",
                dropoffX = 35f,
                dropoffZ = -80f,
                customerDialogue = "\"Bless your sweet whiskers! This will make a delightful dinner for my eleven kittens!\"",
                rewardCoins = 60,
                rewardRep = 20,
                estimatedDistanceMeters = 140
            ),
            DeliveryJob(
                id = "job_yarn_${System.currentTimeMillis()}",
                title = "Jumbo Rainbow Yarn Bundle",
                description = "Critical knitting emergency! Soft pastel merino wool for cozy sweaters.",
                category = DeliveryCategory.PARCEL,
                pickupVenue = "Purr Patisserie Bakery",
                pickupDistrict = "Downpaws Market",
                pickupX = -16f,
                pickupZ = 16f,
                dropoffCustomer = "DJ Scratch",
                dropoffDistrict = "Neon Alley",
                dropoffCustomerAvatar = "🦊",
                dropoffX = -25f,
                dropoffZ = 82f,
                customerDialogue = "\"Yo, this yarn is straight fire! Matching beanies for the whole feline crew tonight!\"",
                rewardCoins = 50,
                rewardRep = 18,
                estimatedDistanceMeters = 120
            ),
            DeliveryJob(
                id = "job_catnip_${System.currentTimeMillis()}",
                title = "Organic Catnip Tea Leaves",
                description = "Hand-picked high-potency aromatic tea. Fragile glass jar, drive with care!",
                category = DeliveryCategory.CAT_CURIOS,
                pickupVenue = "Catnip Greenhouse",
                pickupDistrict = "Sunny Suburbs",
                pickupX = -18f,
                pickupZ = -48f,
                dropoffCustomer = "Sailor Cat Barnacle",
                dropoffDistrict = "Fishbone Harbor",
                dropoffCustomerAvatar = "🦆",
                dropoffX = -48f,
                dropoffZ = -22f,
                customerDialogue = "\"Ahoy matey! One sniff of this and I feel like a feisty young deck-paw again!\"",
                rewardCoins = 55,
                rewardRep = 16,
                estimatedDistanceMeters = 85
            )
        )

        val newJob = sampleJobs.random()
        _offeredJob.value = newJob
        showDeliveryOfferDialog.value = true
        soundEngine.playNotification()

        addPhoneMessage(
            PhoneMessage(
                id = "m_offer_${newJob.id}",
                sender = newJob.pickupVenue,
                senderAvatar = "🔔",
                title = "New Delivery Available: ${newJob.title}",
                body = "Pickup in ${newJob.pickupDistrict} for ${newJob.rewardCoins} CatCoins. Tap to inspect!",
                timeLabel = "Now",
                type = MessageType.DELIVERY_ALERT
            )
        )
    }

    fun acceptOfferedJob() {
        val job = _offeredJob.value ?: return
        _activeJob.value = job.copy(state = DeliveryState.ACCEPTED)
        _offeredJob.value = null
        showDeliveryOfferDialog.value = false
        soundEngine.playCoinReward()
    }

    fun declineOfferedJob() {
        _offeredJob.value = null
        showDeliveryOfferDialog.value = false
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
        scheduleNextJobOffer(10000)
    }

    fun callRoadsideAssistance() {
        if (_coins.value >= 25) {
            _coins.value -= 25
        }
        _fuelPercent.value = 50f
        _isOutOfFuel.value = false
        showOutOfFuelDialog.value = false
        soundEngine.playFuelDing()
        soundEngine.startEngine()
        addPhoneMessage(
            PhoneMessage(
                id = "m_assist_${System.currentTimeMillis()}",
                sender = "Roadside Paw-sistance",
                senderAvatar = "🛟",
                title = "Emergency Fuel Delivered!",
                body = "Roadside team delivered 50% tank fuel directly to your scooter. Drive safe out there!",
                timeLabel = "Just now",
                type = MessageType.FUEL_WARNING
            )
        )
    }

    fun pushScooterMiniGame() {
        val newFuel = (_fuelPercent.value + 4f).coerceAtMost(25f)
        _fuelPercent.value = newFuel
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
        if (newFuel >= 15f) {
            _isOutOfFuel.value = false
            showOutOfFuelDialog.value = false
            soundEngine.startEngine()
        }
    }

    fun addPhoneMessage(msg: PhoneMessage) {
        _phoneMessages.value = listOf(msg) + _phoneMessages.value
    }

    fun togglePhone() {
        _isPhoneOpen.value = !_isPhoneOpen.value
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
    }

    fun playHorn() {
        soundEngine.playHorn()
    }

    fun setCameraMode(mode: CameraMode) {
        _settings.value = _settings.value.copy(cameraMode = mode)
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
    }

    fun setWeather(weather: WeatherCondition) {
        _settings.value = _settings.value.copy(weather = weather)
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
    }

    fun updateCustomization(newCustom: CatCustomization) {
        _customization.value = newCustom
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
    }

    fun openCharacterCreator() {
        isCharacterCreatorOpen.value = true
        soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
    }

    fun closeCharacterCreator() {
        isCharacterCreatorOpen.value = false
        soundEngine.playCoinReward()
        scheduleNextJobOffer(1500)
    }

    fun upgradeSpeed() {
        val cost = _speedUpgradeLevel.value * 50
        if (_coins.value >= cost && _speedUpgradeLevel.value < 5) {
            _coins.value -= cost
            _speedUpgradeLevel.value += 1
            soundEngine.playCoinReward()
        }
    }

    fun upgradeAccel() {
        val cost = _accelUpgradeLevel.value * 45
        if (_coins.value >= cost && _accelUpgradeLevel.value < 5) {
            _coins.value -= cost
            _accelUpgradeLevel.value += 1
            soundEngine.playCoinReward()
        }
    }

    fun upgradeTank() {
        val cost = _tankUpgradeLevel.value * 40
        if (_coins.value >= cost && _tankUpgradeLevel.value < 5) {
            _coins.value -= cost
            _tankUpgradeLevel.value += 1
            soundEngine.playCoinReward()
        }
    }

    fun toggleSound(enabled: Boolean) {
        soundEngine.isSoundEnabled = enabled
        _settings.value = _settings.value.copy(soundEnabled = enabled)
    }

    override fun onCleared() {
        super.onCleared()
        loopJob?.cancel()
        soundEngine.release()
    }
}
