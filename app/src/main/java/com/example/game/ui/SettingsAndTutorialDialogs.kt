package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.game.model.CameraMode
import com.example.game.model.WeatherCondition
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.TealBrand

@Composable
fun SettingsDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val settings by viewModel.settings.collectAsState()

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyYellowCream),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(16.dp, RoundedCornerShape(28.dp))
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Game Settings",
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = DarkSurface
                    )
                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weather Condition
                Text("Atmosphere & Weather", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    WeatherCondition.values().forEach { w ->
                        val isSelected = settings.weather == w
                        Card(
                            onClick = { viewModel.setWeather(w) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) DustyRose else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(w.icon, fontSize = 18.sp)
                                Text(
                                    w.label.split(" ").first(),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else DarkSurface
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Camera Perspectives
                Text("Camera Perspective", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    CameraMode.values().forEach { mode ->
                        val isSelected = settings.cameraMode == mode
                        Card(
                            onClick = { viewModel.setCameraMode(mode) },
                            shape = RoundedCornerShape(10.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) TealBrand else Color.White
                            ),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                mode.label,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else DarkSurface,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                // Sound & Haptics
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sound Effects & Audio Engine", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    Switch(
                        checked = settings.soundEnabled,
                        onCheckedChange = {
                            viewModel.toggleSound(it)
                        },
                        colors = SwitchDefaults.colors(checkedThumbColor = HoneyYellow)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = {
                        viewModel.showTutorialDialog.value = true
                        onDismiss()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF64748B)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Replay Rider Tutorial", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TutorialOverlay(onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(20.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("🛵🐾", fontSize = 42.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Welcome to Lalameow Rider!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = DarkSurface
                )
                Text(
                    "The adorable miniature city delivery game",
                    fontSize = 12.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(16.dp))

                TutorialBullet("🕹️ Left Thumb", "Virtual Steering Joystick to steer your cute scooter smoothly.")
                TutorialBullet("🛵 Right Pedals", "GAS pedal accelerates, BRAKE reverses, DRIFT slides corners!")
                TutorialBullet("📯 MEEP Button", "Honk horn to clear crosswalks and enjoy delightful meows.")
                TutorialBullet("📱 MeowPhone", "Check chats, orders, GPS routes, and daily coin bounties.")
                TutorialBullet("⛽ Gas Stations", "Watch your fuel gauge! Stop at Shell-Paws to refuel your tank.")

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_start_tutorial")
                ) {
                    Text("START DELIVERING! 🚀", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 15.sp)
                }
            }
        }
    }
}

@Composable
fun TutorialBullet(title: String, desc: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = DustyRose, modifier = Modifier.width(100.dp))
        Text(desc, fontSize = 12.sp, color = Color(0xFF334155), modifier = Modifier.weight(1f))
    }
}
