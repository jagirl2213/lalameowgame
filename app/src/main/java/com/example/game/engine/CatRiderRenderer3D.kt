package com.example.game.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.CatCustomization
import com.example.game.model.DeliveryCategory
import com.example.game.model.DeliveryJob
import com.example.game.model.DeliveryState
import com.example.game.model.HelmetOption
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.TealBrand
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * True third-person 3D Cat Rider and Scooter renderer for Lalameow Rider.
 * Renders the cat courier and scooter from rear/3/4 chase perspective with
 * dynamic lean physics, steering, tail physics, blinking, twitching ears,
 * exhaust smoke particles, and reactive celebration animations.
 */
class CatRiderRenderer3D {

    private var blinkTimer = 0f
    private var isBlinking = false
    private var earTwitchTimer = 0f
    private var tailAnimTime = 0f
    private var smokeParticles = mutableListOf<SmokePuff>()

    data class SmokePuff(var x: Float, var y: Float, var alpha: Float, var radius: Float)

    fun updateAnimations(deltaSeconds: Float) {
        blinkTimer += deltaSeconds
        if (blinkTimer > 3.2f) {
            isBlinking = true
            if (blinkTimer > 3.35f) {
                isBlinking = false
                blinkTimer = 0f
            }
        }

        earTwitchTimer += deltaSeconds
        if (earTwitchTimer > 4.2f) {
            earTwitchTimer = 0f
        }

        tailAnimTime += deltaSeconds * 5f

        // Update smoke puffs
        val iter = smokeParticles.iterator()
        while (iter.hasNext()) {
            val p = iter.next()
            p.y -= deltaSeconds * 30f
            p.x += deltaSeconds * 12f
            p.radius += deltaSeconds * 18f
            p.alpha -= deltaSeconds * 1.5f
            if (p.alpha <= 0f) iter.remove()
        }
    }

    fun renderCatRider(
        drawScope: DrawScope,
        camera: Camera3D,
        worldX: Float,
        worldY: Float,
        worldZ: Float,
        headingDeg: Float,
        steeringAngleDeg: Float,
        speed: Float,
        leanAngleDeg: Float,
        customization: CatCustomization,
        isCelebrating: Boolean,
        isHeadlightOn: Boolean,
        activeJob: DeliveryJob? = null
    ) {
        val screenW = drawScope.size.width
        val screenH = drawScope.size.height

        // Project center base of scooter
        val baseScreen = camera.project(worldX, worldY, worldZ, screenW, screenH)
        if (!baseScreen.isVisible) return

        val depth = baseScreen.depth
        // High-presence hero character scaling
        val scale = (screenH * 1.25f / depth).coerceIn(50f, 380f)

        // Lean physics offset (left/right tilt)
        val leanFactor = (leanAngleDeg / 30f).coerceIn(-1.2f, 1.2f)
        val leanOffset = leanFactor * scale * 0.32f

        val bodyColor = customization.scooterColor.bodyColor
        val seatColor = customization.scooterColor.seatColor
        val fur = customization.furColor
        val jacket = customization.jacket
        val bagColor = customization.backpack.color

        val centerX = baseScreen.x
        val centerY = baseScreen.y

        // 1. Soft Contact Shadow on Ground
        drawScope.drawOval(
            color = Color(0x70000000),
            topLeft = Offset(centerX - scale * 0.45f + leanOffset * 0.3f, centerY - scale * 0.12f),
            size = Size(scale * 0.9f, scale * 0.28f)
        )

        // 2. Spawn Exhaust Smoke when moving
        if (speed > 1.5f && smokeParticles.size < 6 && (System.currentTimeMillis() % 4 == 0L)) {
            smokeParticles.add(
                SmokePuff(
                    x = centerX + scale * 0.22f + leanOffset,
                    y = centerY - scale * 0.15f,
                    alpha = 0.65f,
                    radius = scale * 0.04f
                )
            )
        }
        // Render smoke puffs
        smokeParticles.forEach { p ->
            drawScope.drawCircle(
                color = Color.White.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                radius = p.radius,
                center = Offset(p.x, p.y)
            )
        }

        // 3. Scooter Rear Wheel (viewed from behind)
        val wheelCenterX = centerX + leanOffset * 0.5f
        val wheelCenterY = centerY - scale * 0.16f
        val wheelW = scale * 0.18f
        val wheelH = scale * 0.26f

        // Black Rubber Tire
        drawScope.drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(wheelCenterX - wheelW * 0.5f, wheelCenterY - wheelH * 0.5f),
            size = Size(wheelW, wheelH),
            cornerRadius = CornerRadius(scale * 0.08f, scale * 0.08f)
        )
        // Silver Center Rim
        drawScope.drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(wheelCenterX - wheelW * 0.25f, wheelCenterY - wheelH * 0.3f),
            size = Size(wheelW * 0.5f, wheelH * 0.6f),
            cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
        )

        // 4. Scooter Rear Mudguard / Fender
        drawScope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(wheelCenterX - scale * 0.16f, wheelCenterY - scale * 0.22f),
            size = Size(scale * 0.32f, scale * 0.18f),
            cornerRadius = CornerRadius(scale * 0.06f, scale * 0.06f)
        )

        // Cute Mini License Plate "MEOW-01"
        drawScope.drawRoundRect(
            color = Color.White,
            topLeft = Offset(wheelCenterX - scale * 0.1f, wheelCenterY - scale * 0.06f),
            size = Size(scale * 0.2f, scale * 0.09f),
            cornerRadius = CornerRadius(scale * 0.02f, scale * 0.02f)
        )
        drawScope.drawRoundRect(
            color = HoneyYellow,
            topLeft = Offset(wheelCenterX - scale * 0.08f, wheelCenterY - scale * 0.04f),
            size = Size(scale * 0.16f, scale * 0.045f),
            cornerRadius = CornerRadius(scale * 0.01f, scale * 0.01f)
        )

        // Ruby Red Taillight (Glows brighter when braking)
        val isBraking = speed < 0f || (speed > 0f && leanAngleDeg == 0f && (speed < 5f))
        val tailLightColor = if (isBraking) Color(0xFFFF1744) else Color(0xFFDC2626)
        drawScope.drawRoundRect(
            color = tailLightColor,
            topLeft = Offset(wheelCenterX - scale * 0.14f, wheelCenterY - scale * 0.26f),
            size = Size(scale * 0.28f, scale * 0.07f),
            cornerRadius = CornerRadius(scale * 0.035f, scale * 0.035f)
        )
        // Taillight glow halo
        drawScope.drawRoundRect(
            color = tailLightColor.copy(alpha = 0.4f),
            topLeft = Offset(wheelCenterX - scale * 0.18f, wheelCenterY - scale * 0.29f),
            size = Size(scale * 0.36f, scale * 0.13f),
            cornerRadius = CornerRadius(scale * 0.05f, scale * 0.05f)
        )

        // 5. Chrome Exhaust Pipe (Right Side)
        drawScope.drawRoundRect(
            color = Color(0xFFE2E8F0),
            topLeft = Offset(wheelCenterX + scale * 0.12f, wheelCenterY - scale * 0.12f),
            size = Size(scale * 0.14f, scale * 0.08f),
            cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
        )
        drawScope.drawCircle(
            color = Color(0xFF0F172A),
            radius = scale * 0.025f,
            center = Offset(wheelCenterX + scale * 0.24f, wheelCenterY - scale * 0.08f)
        )

        // 6. Scooter Footboards & Chassis Flanks
        val chassisY = centerY - scale * 0.35f
        drawScope.drawRoundRect(
            color = bodyColor,
            topLeft = Offset(centerX - scale * 0.34f + leanOffset * 0.7f, chassisY),
            size = Size(scale * 0.68f, scale * 0.16f),
            cornerRadius = CornerRadius(scale * 0.08f, scale * 0.08f)
        )
        // Scooter Saddle / Seat
        drawScope.drawRoundRect(
            color = seatColor,
            topLeft = Offset(centerX - scale * 0.22f + leanOffset * 0.75f, chassisY - scale * 0.08f),
            size = Size(scale * 0.44f, scale * 0.14f),
            cornerRadius = CornerRadius(scale * 0.07f, scale * 0.07f)
        )

        // 7. Handlebars & Rear-View Mirrors (Wide horizontal bar in front)
        val handleY = chassisY - scale * 0.32f
        val handleCenterX = centerX + leanOffset * 0.9f
        val steerOffset = (steeringAngleDeg / 25f) * scale * 0.1f

        // Chrome Handlebar bar
        drawScope.drawLine(
            color = Color(0xFFCBD5E1),
            start = Offset(handleCenterX - scale * 0.38f + steerOffset, handleY),
            end = Offset(handleCenterX + scale * 0.38f + steerOffset, handleY),
            strokeWidth = scale * 0.045f
        )
        // Rubber Grips
        drawScope.drawCircle(
            color = Color(0xFF0F172A),
            radius = scale * 0.045f,
            center = Offset(handleCenterX - scale * 0.38f + steerOffset, handleY)
        )
        drawScope.drawCircle(
            color = Color(0xFF0F172A),
            radius = scale * 0.045f,
            center = Offset(handleCenterX + scale * 0.38f + steerOffset, handleY)
        )
        // Left & Right Rear-view Mirrors
        drawScope.drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(handleCenterX - scale * 0.32f + steerOffset, handleY),
            end = Offset(handleCenterX - scale * 0.36f + steerOffset, handleY - scale * 0.14f),
            strokeWidth = scale * 0.02f
        )
        drawScope.drawCircle(
            color = Color(0xFFE2E8F0),
            radius = scale * 0.05f,
            center = Offset(handleCenterX - scale * 0.36f + steerOffset, handleY - scale * 0.14f)
        )
        drawScope.drawLine(
            color = Color(0xFF94A3B8),
            start = Offset(handleCenterX + scale * 0.32f + steerOffset, handleY),
            end = Offset(handleCenterX + scale * 0.36f + steerOffset, handleY - scale * 0.14f),
            strokeWidth = scale * 0.02f
        )
        drawScope.drawCircle(
            color = Color(0xFFE2E8F0),
            radius = scale * 0.05f,
            center = Offset(handleCenterX + scale * 0.36f + steerOffset, handleY - scale * 0.14f)
        )

        // Turn Signal Indicators (Amber)
        val isSteeringLeft = steeringAngleDeg < -5f
        val isSteeringRight = steeringAngleDeg > 5f
        val blinkOn = (System.currentTimeMillis() % 400) < 200
        val leftSignalColor = if (isSteeringLeft && blinkOn) Color(0xFFF59E0B) else Color(0x66F59E0B)
        val rightSignalColor = if (isSteeringRight && blinkOn) Color(0xFFF59E0B) else Color(0x66F59E0B)

        drawScope.drawCircle(leftSignalColor, radius = scale * 0.035f, center = Offset(handleCenterX - scale * 0.28f, handleY + scale * 0.05f))
        drawScope.drawCircle(rightSignalColor, radius = scale * 0.035f, center = Offset(handleCenterX + scale * 0.28f, handleY + scale * 0.05f))

        // 8. Fluffy Cat Tail (Waving with speed and physics)
        val tailSway = sin(tailAnimTime) * scale * 0.08f + leanOffset * 0.3f
        val tailBaseX = centerX + leanOffset * 0.8f
        val tailBaseY = chassisY - scale * 0.05f

        val tailPath = Path().apply {
            moveTo(tailBaseX, tailBaseY)
            quadraticTo(
                tailBaseX + scale * 0.22f + tailSway,
                tailBaseY - scale * 0.15f,
                tailBaseX + scale * 0.18f + tailSway * 1.5f,
                tailBaseY - scale * 0.38f
            )
        }
        drawScope.drawPath(
            path = tailPath,
            color = fur.primaryColor,
            style = Stroke(width = scale * 0.075f)
        )
        // Tail tip color
        drawScope.drawCircle(
            color = fur.secondaryColor,
            radius = scale * 0.045f,
            center = Offset(tailBaseX + scale * 0.18f + tailSway * 1.5f, tailBaseY - scale * 0.38f)
        )

        // 9. Delivery Backpack Mounted on Rear Rack (Front and center in rear view)
        val bagCenterX = centerX + leanOffset * 0.85f
        val bagCenterY = chassisY - scale * 0.28f
        val bagW = scale * 0.44f
        val bagH = scale * 0.36f

        // Insulated Courier Box
        drawScope.drawRoundRect(
            color = bagColor,
            topLeft = Offset(bagCenterX - bagW * 0.5f, bagCenterY - bagH * 0.5f),
            size = Size(bagW, bagH),
            cornerRadius = CornerRadius(scale * 0.06f, scale * 0.06f)
        )
        // Reflective Silver Safety Chevron / Stripe across bag
        drawScope.drawRoundRect(
            color = Color.White.copy(alpha = 0.9f),
            topLeft = Offset(bagCenterX - bagW * 0.5f, bagCenterY - scale * 0.04f),
            size = Size(bagW, scale * 0.06f)
        )
        // Lalameow Golden Paw Emblem on Backpack
        drawScope.drawCircle(
            color = HoneyYellow,
            radius = scale * 0.07f,
            center = Offset(bagCenterX, bagCenterY - scale * 0.05f)
        )
        // Paw pads
        drawScope.drawCircle(color = Color.White, radius = scale * 0.03f, center = Offset(bagCenterX, bagCenterY - scale * 0.045f))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.012f, center = Offset(bagCenterX - scale * 0.035f, bagCenterY - scale * 0.08f))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.014f, center = Offset(bagCenterX, bagCenterY - scale * 0.095f))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.012f, center = Offset(bagCenterX + scale * 0.035f, bagCenterY - scale * 0.08f))

        // 9b. Loaded Delivery Item on Scooter Cargo Rack (When actively carrying a job!)
        if (activeJob != null && activeJob.state == DeliveryState.PICKED_UP) {
            val cargoCenterY = bagCenterY - bagH * 0.5f - scale * 0.06f
            val cargoCenterX = bagCenterX

            if (activeJob.category == DeliveryCategory.NEWSPAPER) {
                // Rolled Newspaper bundles strapped on the left & right luggage racks
                val npW = scale * 0.32f
                val npH = scale * 0.14f
                // Left Roll
                drawScope.drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(bagCenterX - bagW * 0.52f - npW * 0.5f, bagCenterY - npH * 0.5f),
                    size = Size(npW, npH),
                    cornerRadius = CornerRadius(npH * 0.5f, npH * 0.5f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF2563EB),
                    topLeft = Offset(bagCenterX - bagW * 0.52f - scale * 0.03f, bagCenterY - npH * 0.55f),
                    size = Size(scale * 0.06f, npH * 1.1f)
                )
                // Right Roll
                drawScope.drawRoundRect(
                    color = Color(0xFFFFFBEB),
                    topLeft = Offset(bagCenterX + bagW * 0.52f - npW * 0.5f, bagCenterY - npH * 0.5f),
                    size = Size(npW, npH),
                    cornerRadius = CornerRadius(npH * 0.5f, npH * 0.5f)
                )
                drawScope.drawRoundRect(
                    color = Color(0xFF2563EB),
                    topLeft = Offset(bagCenterX + bagW * 0.52f - scale * 0.03f, bagCenterY - npH * 0.55f),
                    size = Size(scale * 0.06f, npH * 1.1f)
                )
            } else {
                // 3D Cardboard Parcel strapped to the luggage rack
                val pBoxW = scale * 0.36f
                val pBoxH = scale * 0.24f
                drawScope.drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(cargoCenterX - pBoxW * 0.5f, cargoCenterY - pBoxH * 0.5f),
                    size = Size(pBoxW, pBoxH),
                    cornerRadius = CornerRadius(scale * 0.03f, scale * 0.03f)
                )
                // Yellow Fragile Tape
                drawScope.drawRoundRect(
                    color = HoneyYellow,
                    topLeft = Offset(cargoCenterX - scale * 0.045f, cargoCenterY - pBoxH * 0.55f),
                    size = Size(scale * 0.09f, pBoxH * 1.1f)
                )
                // Black Paw Stamp
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.03f, center = Offset(cargoCenterX, cargoCenterY))
                // Blue Bungee Straps holding parcel securely
                drawScope.drawLine(Color(0xFF0284C7), Offset(cargoCenterX - pBoxW * 0.55f, bagCenterY), Offset(cargoCenterX - pBoxW * 0.2f, cargoCenterY - pBoxH * 0.5f), strokeWidth = 2.5f)
                drawScope.drawLine(Color(0xFF0284C7), Offset(cargoCenterX + pBoxW * 0.55f, bagCenterY), Offset(cargoCenterX + pBoxW * 0.2f, cargoCenterY - pBoxH * 0.5f), strokeWidth = 2.5f)
            }
        }

        // 10. Cat Rider Torso & Jacket (Sitting in front of backpack)
        val catTorsoX = centerX + leanOffset * 0.9f
        val catTorsoY = bagCenterY - scale * 0.18f

        // Jacket back
        drawScope.drawRoundRect(
            color = jacket.color,
            topLeft = Offset(catTorsoX - scale * 0.2f, catTorsoY - scale * 0.14f),
            size = Size(scale * 0.4f, scale * 0.28f),
            cornerRadius = CornerRadius(scale * 0.09f, scale * 0.09f)
        )
        // Jacket safety striping / collar
        drawScope.drawRoundRect(
            color = jacket.trimColor,
            topLeft = Offset(catTorsoX - scale * 0.16f, catTorsoY - scale * 0.05f),
            size = Size(scale * 0.32f, scale * 0.04f)
        )

        // Cat Paws holding handlebars
        val leftPawX = handleCenterX - scale * 0.32f + steerOffset * 0.8f
        val rightPawX = handleCenterX + scale * 0.32f + steerOffset * 0.8f
        val pawY = handleY

        drawScope.drawCircle(fur.secondaryColor, radius = scale * 0.055f, center = Offset(leftPawX, pawY))

        if (isCelebrating) {
            // Raised celebrating paw!
            val celebY = catTorsoY - scale * 0.45f
            drawScope.drawCircle(fur.secondaryColor, radius = scale * 0.07f, center = Offset(catTorsoX + scale * 0.22f, celebY))
            // Joy hearts / star sparkles above head
            drawScope.drawCircle(DustyRose, radius = scale * 0.035f, center = Offset(catTorsoX + scale * 0.28f, celebY - scale * 0.1f))
            drawScope.drawCircle(HoneyYellow, radius = scale * 0.04f, center = Offset(catTorsoX - scale * 0.24f, celebY - scale * 0.05f))
        } else {
            drawScope.drawCircle(fur.secondaryColor, radius = scale * 0.055f, center = Offset(rightPawX, pawY))
        }

        // 11. Cat Head & Ears
        val headCenterX = catTorsoX
        val headCenterY = catTorsoY - scale * 0.28f
        val headRadius = scale * 0.21f

        // Round Fluffy Head
        drawScope.drawCircle(
            color = fur.primaryColor,
            radius = headRadius,
            center = Offset(headCenterX, headCenterY)
        )

        // Animated Ear Twitches & Wind Fold
        val earTwitch = if (earTwitchTimer < 0.25f) sin(earTwitchTimer * 25f) * scale * 0.035f else 0f
        val speedWind = (speed / 35f).coerceIn(0f, 1f) * scale * 0.03f

        // Left Ear
        val leftEar = Path().apply {
            moveTo(headCenterX - headRadius * 0.85f, headCenterY - headRadius * 0.15f)
            lineTo(headCenterX - headRadius * 0.65f - speedWind, headCenterY - headRadius * 1.55f + earTwitch)
            lineTo(headCenterX - headRadius * 0.08f, headCenterY - headRadius * 0.75f)
            close()
        }
        drawScope.drawPath(leftEar, color = fur.primaryColor)
        val leftEarInner = Path().apply {
            moveTo(headCenterX - headRadius * 0.75f, headCenterY - headRadius * 0.25f)
            lineTo(headCenterX - headRadius * 0.6f - speedWind, headCenterY - headRadius * 1.35f + earTwitch)
            lineTo(headCenterX - headRadius * 0.18f, headCenterY - headRadius * 0.7f)
            close()
        }
        drawScope.drawPath(leftEarInner, color = fur.earInnerColor)

        // Right Ear
        val rightEar = Path().apply {
            moveTo(headCenterX + headRadius * 0.08f, headCenterY - headRadius * 0.75f)
            lineTo(headCenterX + headRadius * 0.65f - speedWind, headCenterY - headRadius * 1.55f - earTwitch)
            lineTo(headCenterX + headRadius * 0.85f, headCenterY - headRadius * 0.15f)
            close()
        }
        drawScope.drawPath(rightEar, color = fur.primaryColor)
        val rightEarInner = Path().apply {
            moveTo(headCenterX + headRadius * 0.18f, headCenterY - headRadius * 0.7f)
            lineTo(headCenterX + headRadius * 0.6f - speedWind, headCenterY - headRadius * 1.35f - earTwitch)
            lineTo(headCenterX + headRadius * 0.75f, headCenterY - headRadius * 0.25f)
            close()
        }
        drawScope.drawPath(rightEarInner, color = fur.earInnerColor)

        // 12. Helmet or Cute Bow
        val helmet = customization.helmet
        if (helmet != HelmetOption.NONE_BOW) {
            // Helm Dome
            drawScope.drawArc(
                color = helmet.color,
                startAngle = 170f,
                sweepAngle = 200f,
                useCenter = true,
                topLeft = Offset(headCenterX - headRadius * 1.16f, headCenterY - headRadius * 1.35f),
                size = Size(headRadius * 2.32f, headRadius * 2.2f)
            )
            // Helmet Rear Accent / Stripe
            drawScope.drawArc(
                color = helmet.accentColor,
                startAngle = 195f,
                sweepAngle = 150f,
                useCenter = false,
                topLeft = Offset(headCenterX - headRadius * 1.05f, headCenterY - headRadius * 1.25f),
                size = Size(headRadius * 2.1f, headRadius * 1.9f),
                style = Stroke(width = headRadius * 0.26f)
            )
        } else {
            // Cute Pink Ribbon Bow behind right ear
            drawScope.drawCircle(
                color = DustyRose,
                radius = headRadius * 0.38f,
                center = Offset(headCenterX + headRadius * 0.65f, headCenterY - headRadius * 0.75f)
            )
            drawScope.drawCircle(
                color = Color.White,
                radius = headRadius * 0.14f,
                center = Offset(headCenterX + headRadius * 0.65f, headCenterY - headRadius * 0.75f)
            )
        }

        // Golden Bell Collar
        drawScope.drawCircle(
            color = HoneyYellow,
            radius = scale * 0.042f,
            center = Offset(headCenterX, catTorsoY - scale * 0.14f)
        )

        // 13. Adorable Cat Face Features (Rendered when camera is viewing rider from front/3/4 angle)
        val angleDiff = ((camera.yawDeg - headingDeg + 540f) % 360f) - 180f
        val isFacingCamera = kotlin.math.abs(angleDiff) > 75f

        if (isFacingCamera) {
            // White fluffy muzzle patch
            drawScope.drawOval(
                color = Color.White,
                topLeft = Offset(headCenterX - headRadius * 0.55f, headCenterY + headRadius * 0.05f),
                size = Size(headRadius * 1.1f, headRadius * 0.75f)
            )

            // Cute Pink Cheeks / Rosy Blush
            drawScope.drawCircle(
                color = DustyRose.copy(alpha = 0.65f),
                radius = headRadius * 0.22f,
                center = Offset(headCenterX - headRadius * 0.55f, headCenterY + headRadius * 0.28f)
            )
            drawScope.drawCircle(
                color = DustyRose.copy(alpha = 0.65f),
                radius = headRadius * 0.22f,
                center = Offset(headCenterX + headRadius * 0.55f, headCenterY + headRadius * 0.28f)
            )

            // Expressive Cat Eyes
            val eyeOffsetY = headCenterY - headRadius * 0.12f
            val leftEyeX = headCenterX - headRadius * 0.35f
            val rightEyeX = headCenterX + headRadius * 0.35f

            if (isBlinking) {
                // Closed happy curved eye arcs
                drawScope.drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(leftEyeX - headRadius * 0.18f, eyeOffsetY - headRadius * 0.1f),
                    size = Size(headRadius * 0.36f, headRadius * 0.2f),
                    style = Stroke(width = 3f)
                )
                drawScope.drawArc(
                    color = Color(0xFF1E293B),
                    startAngle = 190f,
                    sweepAngle = 160f,
                    useCenter = false,
                    topLeft = Offset(rightEyeX - headRadius * 0.18f, eyeOffsetY - headRadius * 0.1f),
                    size = Size(headRadius * 0.36f, headRadius * 0.2f),
                    style = Stroke(width = 3f)
                )
            } else {
                // Big Anime Cat Eyes
                drawScope.drawCircle(Color(0xFF1E293B), radius = headRadius * 0.22f, center = Offset(leftEyeX, eyeOffsetY))
                drawScope.drawCircle(Color(0xFF1E293B), radius = headRadius * 0.22f, center = Offset(rightEyeX, eyeOffsetY))
                // Iris (Emerald green)
                drawScope.drawCircle(Color(0xFF059669), radius = headRadius * 0.16f, center = Offset(leftEyeX, eyeOffsetY))
                drawScope.drawCircle(Color(0xFF059669), radius = headRadius * 0.16f, center = Offset(rightEyeX, eyeOffsetY))
                // Pupil
                drawScope.drawCircle(Color(0xFF0F172A), radius = headRadius * 0.1f, center = Offset(leftEyeX, eyeOffsetY))
                drawScope.drawCircle(Color(0xFF0F172A), radius = headRadius * 0.1f, center = Offset(rightEyeX, eyeOffsetY))
                // Catchlight sparkles
                drawScope.drawCircle(Color.White, radius = headRadius * 0.06f, center = Offset(leftEyeX - headRadius * 0.05f, eyeOffsetY - headRadius * 0.05f))
                drawScope.drawCircle(Color.White, radius = headRadius * 0.06f, center = Offset(rightEyeX - headRadius * 0.05f, eyeOffsetY - headRadius * 0.05f))
            }

            // Cute Little Pink Nose
            val noseY = headCenterY + headRadius * 0.22f
            val nosePath = Path().apply {
                moveTo(headCenterX - headRadius * 0.1f, noseY - headRadius * 0.05f)
                lineTo(headCenterX + headRadius * 0.1f, noseY - headRadius * 0.05f)
                lineTo(headCenterX, noseY + headRadius * 0.07f)
                close()
            }
            drawScope.drawPath(nosePath, color = DustyRose)

            // Cat Smile ( :3 )
            drawScope.drawArc(
                color = Color(0xFF1E293B),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(headCenterX - headRadius * 0.16f, noseY + headRadius * 0.02f),
                size = Size(headRadius * 0.16f, headRadius * 0.14f),
                style = Stroke(width = 2.5f)
            )
            drawScope.drawArc(
                color = Color(0xFF1E293B),
                startAngle = 20f,
                sweepAngle = 140f,
                useCenter = false,
                topLeft = Offset(headCenterX, noseY + headRadius * 0.02f),
                size = Size(headRadius * 0.16f, headRadius * 0.14f),
                style = Stroke(width = 2.5f)
            )

            // Whiskers (3 on each cheek)
            for (w in -1..1) {
                val wy = noseY + w * headRadius * 0.08f
                drawScope.drawLine(
                    color = Color(0xAA1E293B),
                    start = Offset(headCenterX - headRadius * 0.35f, wy),
                    end = Offset(headCenterX - headRadius * 0.95f, wy + w * headRadius * 0.06f),
                    strokeWidth = 2f
                )
                drawScope.drawLine(
                    color = Color(0xAA1E293B),
                    start = Offset(headCenterX + headRadius * 0.35f, wy),
                    end = Offset(headCenterX + headRadius * 0.95f, wy + w * headRadius * 0.06f),
                    strokeWidth = 2f
                )
            }
        }
    }
}
