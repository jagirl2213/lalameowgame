package com.example.game.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.game.audio.HapticType
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.DustyRoseLight
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowLight
import com.example.ui.theme.TealBrand
import com.example.ui.theme.TealBrandLight
import kotlin.math.roundToInt

@Composable
fun TouchControls(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Left Side: Virtual Steering Joystick
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(bottom = 12.dp)
        ) {
            VirtualJoystick(
                onSteer = { steer -> viewModel.steeringInput = steer },
                onRelease = { viewModel.steeringInput = 0f }
            )
        }

        // Center Bottom: Context Action Button (Pickup / Deliver / Refuel)
        val contextPrompt = viewModel.contextActionPrompt.value
        AnimatedVisibility(
            visible = contextPrompt != null,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 18.dp)
        ) {
            if (contextPrompt != null) {
                Card(
                    onClick = {
                        viewModel.soundEngine.triggerHaptic(HapticType.SUCCESS)
                        viewModel.handleContextAction()
                    },
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (contextPrompt.contains("REFUEL")) TealBrand else HoneyYellow
                    ),
                    modifier = Modifier
                        .shadow(12.dp, RoundedCornerShape(24.dp))
                        .testTag("btn_context_action")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = contextPrompt,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                    }
                }
            }
        }

        // Right Side: Throttle, Brake, Drift, and Horn Controls
        Row(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            // Secondary buttons: Horn & Drift
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Horn Button ("Meep Meep!")
                RoundTactileButton(
                    label = "MEEP!",
                    subLabel = "📯",
                    sizeDp = 58,
                    backgroundBrush = Brush.verticalGradient(listOf(DustyRoseLight, DustyRose)),
                    onPress = {
                        viewModel.playHorn()
                    },
                    onRelease = {},
                    testTag = "btn_horn"
                )

                // Drift / Handbrake Button
                RoundTactileButton(
                    label = "DRIFT",
                    subLabel = "⚡",
                    sizeDp = 58,
                    backgroundBrush = Brush.verticalGradient(listOf(TealBrandLight, TealBrand)),
                    onPress = {
                        viewModel.isDrifting = true
                        viewModel.soundEngine.triggerHaptic(HapticType.BUMP)
                    },
                    onRelease = {
                        viewModel.isDrifting = false
                    },
                    testTag = "btn_drift"
                )
            }

            // Primary pedals: Brake and Gas
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Brake / Reverse Pedal
                PedalButton(
                    label = "BRAKE",
                    icon = "🛑",
                    widthDp = 74,
                    heightDp = 64,
                    brush = Brush.verticalGradient(listOf(Color(0xFFF87171), Color(0xFFDC2626))),
                    onPress = {
                        viewModel.brakeInput = 1f
                        viewModel.soundEngine.triggerHaptic(HapticType.LIGHT_CLICK)
                    },
                    onRelease = {
                        viewModel.brakeInput = 0f
                    },
                    testTag = "btn_brake"
                )

                // Gas / Throttle Pedal
                PedalButton(
                    label = "GAS",
                    icon = "🛵",
                    widthDp = 78,
                    heightDp = 88,
                    brush = Brush.verticalGradient(listOf(HoneyYellowLight, HoneyYellow)),
                    onPress = {
                        viewModel.throttleInput = 1f
                        viewModel.soundEngine.triggerHaptic(HapticType.THROTTLE_RUMBLE)
                    },
                    onRelease = {
                        viewModel.throttleInput = 0f
                    },
                    testTag = "btn_throttle"
                )
            }
        }
    }
}

@Composable
fun VirtualJoystick(
    onSteer: (Float) -> Unit,
    onRelease: () -> Unit
) {
    var thumbOffset by remember { mutableStateOf(Offset.Zero) }
    val maxRadius = 55f

    Box(
        modifier = Modifier
            .size(130.dp)
            .shadow(10.dp, CircleShape)
            .background(Color(0x771E293B), CircleShape)
            .pointerInput(Unit) {
                detectDragGestures(
                    onDragStart = { offset ->
                        val local = offset - Offset(size.width * 0.5f, size.height * 0.5f)
                        val steer = (local.x / maxRadius).coerceIn(-1f, 1f)
                        thumbOffset = Offset(steer * maxRadius, 0f)
                        onSteer(steer)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        val local = change.position - Offset(size.width * 0.5f, size.height * 0.5f)
                        val steer = (local.x / maxRadius).coerceIn(-1f, 1f)
                        thumbOffset = Offset(steer * maxRadius, 0f)
                        onSteer(steer)
                    },
                    onDragEnd = {
                        thumbOffset = Offset.Zero
                        onRelease()
                    },
                    onDragCancel = {
                        thumbOffset = Offset.Zero
                        onRelease()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        // Subtle steer guidelines
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("◀", color = Color(0x99FFFFFF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Text("▶", color = Color(0x99FFFFFF), fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }

        // Draggable thumb knob
        Box(
            modifier = Modifier
                .offset { IntOffset(thumbOffset.x.roundToInt(), 0) }
                .size(54.dp)
                .shadow(8.dp, CircleShape)
                .background(
                    Brush.verticalGradient(listOf(HoneyYellowLight, HoneyYellow)),
                    CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Text("🐾", fontSize = 22.sp)
        }
    }
}

@Composable
fun RoundTactileButton(
    label: String,
    subLabel: String,
    sizeDp: Int,
    backgroundBrush: Brush,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    testTag: String
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(sizeDp.dp)
            .shadow(if (isPressed) 2.dp else 8.dp, CircleShape)
            .background(backgroundBrush, CircleShape)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(subLabel, fontSize = 16.sp)
            Text(label, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
        }
    }
}

@Composable
fun PedalButton(
    label: String,
    icon: String,
    widthDp: Int,
    heightDp: Int,
    brush: Brush,
    onPress: () -> Unit,
    onRelease: () -> Unit,
    testTag: String
) {
    var isPressed by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(width = widthDp.dp, height = heightDp.dp)
            .shadow(if (isPressed) 2.dp else 10.dp, RoundedCornerShape(18.dp))
            .background(brush, RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        onPress()
                        tryAwaitRelease()
                        isPressed = false
                        onRelease()
                    }
                )
            }
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 13.sp
            )
        }
    }
}
