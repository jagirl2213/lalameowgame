package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.HapticType
import com.example.game.model.CameraMode
import com.example.game.model.CityWorldData
import com.example.game.model.DeliveryState
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun GameHud(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val customization by viewModel.customization.collectAsState()
    val coins by viewModel.coins.collectAsState()
    val rep by viewModel.reputation.collectAsState()
    val fuel by viewModel.fuelPercent.collectAsState()
    val activeJob by viewModel.activeJob.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val messages by viewModel.phoneMessages.collectAsState()

    val unreadCount = messages.count { !it.isRead }

    val infiniteTransition = rememberInfiniteTransition(label = "hud_pulse")
    val lowFuelAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
        label = "low_fuel_pulse"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 10.dp, start = 12.dp, end = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Top Row: Player Info, Speedometer & Fuel, and Radar/Quick Actions
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Group: Profile & Wallet
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                // Cat Profile Button
                Card(
                    onClick = {
                        viewModel.soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
                        viewModel.openCharacterCreator()
                    },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A)),
                    modifier = Modifier.testTag("btn_hud_profile")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .background(customization.furColor.primaryColor, CircleShape)
                                .border(2.dp, HoneyYellow, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐱", fontSize = 18.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                customization.name,
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 13.sp
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⭐ $rep", color = HoneyYellowLight, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("🪙 $coins", color = Color(0xFFFDE047), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                // Fuel Gauge Card
                val isLowFuel = fuel < 20f
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLowFuel) {
                            Color(0xFFEF4444).copy(alpha = lowFuelAlpha * 0.9f)
                        } else {
                            Color(0xCC0F172A)
                        }
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(if (isLowFuel) "⚠️ ⛽" else "⛽", fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                if (isLowFuel) "FUEL LOW: ${fuel.toInt()}%" else "Fuel ${fuel.toInt()}%",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                            LinearProgressIndicator(
                                progress = { (fuel / 100f).coerceIn(0f, 1f) },
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(2.dp)),
                                color = if (isLowFuel) Color.White else HoneyYellow,
                                trackColor = Color(0x55FFFFFF)
                            )
                        }
                    }
                }
            }

            // Center: Speedometer HUD
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xCC0F172A))
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val speedDisplay = (kotlin.math.abs(viewModel.playerSpeed) * 2.2f).toInt()
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            "$speedDisplay",
                            color = Color.White,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            "km/h",
                            color = HoneyYellowLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }

                    if (viewModel.isDrifting) {
                        Text("DRIFTING ⚡", color = TealBrand, fontWeight = FontWeight.ExtraBold, fontSize = 9.sp)
                    } else {
                        Text(settings.weather.icon + " " + settings.weather.label.split(" ").first(), color = Color(0xFF94A3B8), fontSize = 9.sp)
                    }
                }
            }

            // Right Group: GPS Radar & Quick Buttons
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Mini GPS Radar
                MiniRadarView(
                    viewModel = viewModel,
                    activeJob = activeJob,
                    onClick = {
                        viewModel.soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
                        viewModel.showExpandedMapDialog.value = true
                    }
                )

                // Action Bar: Camera, Settings, MeowPhone
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    // MeowPhone Button with unread badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(6.dp, CircleShape)
                            .background(DustyRose, CircleShape)
                            .clickable { viewModel.togglePhone() }
                            .testTag("btn_meow_phone"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("📱", fontSize = 20.sp)
                        if (unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .size(16.dp)
                                    .background(Color(0xFFEF4444), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    "$unreadCount",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Camera Mode cycle button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .background(Color(0xCC0F172A), CircleShape)
                            .clickable {
                                val allModes = CameraMode.values()
                                val nextIndex = (settings.cameraMode.ordinal + 1) % allModes.size
                                viewModel.setCameraMode(allModes[nextIndex])
                            }
                            .testTag("btn_cycle_cam"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎥", fontSize = 16.sp)
                    }

                    // Asset Library Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .background(Color(0xCC0F172A), CircleShape)
                            .clickable {
                                viewModel.soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
                                viewModel.showAssetLibraryDialog.value = true
                            }
                            .testTag("btn_asset_library"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎨", fontSize = 16.sp)
                    }

                    // Settings Button
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .shadow(6.dp, CircleShape)
                            .background(Color(0xCC0F172A), CircleShape)
                            .clickable {
                                viewModel.soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
                                viewModel.showSettingsDialog.value = true
                            }
                            .testTag("btn_settings"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⚙️", fontSize = 16.sp)
                    }
                }
            }
        }

        // Active Delivery Floating Banner (Breadcrumbs / Directions)
        if (activeJob != null) {
            val job = activeJob!!
            Card(
                onClick = { viewModel.togglePhone() },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xEE0F172A)),
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(8.dp, RoundedCornerShape(16.dp))
                    .testTag("card_active_job_banner")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(job.category.icon, fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            val stateLabel = if (job.state == DeliveryState.ACCEPTED) {
                                "Pickup: ${job.pickupVenue}"
                            } else {
                                "Deliver to: ${job.dropoffCustomerAvatar} ${job.dropoffCustomer}"
                            }
                            Text(
                                stateLabel,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                            Text(
                                "${job.title} • 🪙 ${job.rewardCoins}",
                                color = HoneyYellowLight,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .background(if (job.state == DeliveryState.ACCEPTED) HoneyYellow else DustyRose, RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            if (job.state == DeliveryState.ACCEPTED) "PICKUP" else "DELIVER",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MiniRadarView(
    viewModel: GameViewModel,
    activeJob: com.example.game.model.DeliveryJob?,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .shadow(8.dp, CircleShape)
            .background(Color(0xDD0F172A), CircleShape)
            .border(2.dp, HoneyYellow, CircleShape)
            .clickable { onClick() }
            .testTag("radar_minimap"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(4.dp)) {
            val radarRadius = size.width * 0.5f
            val center = Offset(radarRadius, radarRadius)
            val scale = radarRadius / 75f

            // Radar concentric rings
            drawCircle(color = Color(0x33CBD5E1), radius = radarRadius * 0.5f, center = center, style = Stroke(1f))
            drawCircle(color = Color(0x33CBD5E1), radius = radarRadius * 0.9f, center = center, style = Stroke(1f))

            // Player position in center
            drawCircle(color = Color.White, radius = 4f, center = center)

            // Heading indicator line
            val radHeading = (viewModel.playerHeadingDeg * PI / 180.0).toFloat()
            drawLine(
                color = TealBrand,
                start = center,
                end = Offset(center.x + sin(radHeading) * 12f, center.y + cos(radHeading) * 12f),
                strokeWidth = 2.5f
            )

            // Gas station blips
            CityWorldData.gasStations.forEach { s ->
                val relX = (s.x - viewModel.playerX) * scale
                val relZ = (s.z - viewModel.playerZ) * scale
                if (relX * relX + relZ * relZ < radarRadius * radarRadius) {
                    drawCircle(color = Color(0xFFF59E0B), radius = 3.5f, center = Offset(center.x + relX, center.y + relZ))
                }
            }

            // Target Delivery blip
            if (activeJob != null) {
                val targetX = if (activeJob.state == DeliveryState.ACCEPTED) activeJob.pickupX else activeJob.dropoffX
                val targetZ = if (activeJob.state == DeliveryState.ACCEPTED) activeJob.pickupZ else activeJob.dropoffZ
                val relX = (targetX - viewModel.playerX) * scale
                val relZ = (targetZ - viewModel.playerZ) * scale

                val distSq = relX * relX + relZ * relZ
                val targetColor = if (activeJob.state == DeliveryState.ACCEPTED) HoneyYellow else DustyRose

                if (distSq < radarRadius * radarRadius) {
                    drawCircle(color = targetColor, radius = 5f, center = Offset(center.x + relX, center.y + relZ))
                    drawCircle(color = Color.White, radius = 2.5f, center = Offset(center.x + relX, center.y + relZ))
                } else {
                    // Clamp to radar perimeter
                    val dist = kotlin.math.sqrt(distSq)
                    val clampedX = (relX / dist) * (radarRadius - 6f)
                    val clampedY = (relZ / dist) * (radarRadius - 6f)
                    drawCircle(color = targetColor, radius = 4.5f, center = Offset(center.x + clampedX, center.y + clampedY))
                }
            }
        }
    }
}
