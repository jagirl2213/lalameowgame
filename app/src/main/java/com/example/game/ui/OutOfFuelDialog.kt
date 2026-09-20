package com.example.game.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedButton
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
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.TealBrand

@Composable
fun OutOfFuelDialog(
    viewModel: GameViewModel
) {
    val fuel by viewModel.fuelPercent.collectAsState()
    val coins by viewModel.coins.collectAsState()

    Dialog(onDismissRequest = { /* Must choose action */ }) {
        Card(
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(containerColor = HoneyYellowCream),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .shadow(24.dp, RoundedCornerShape(28.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFEF4444).copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⛽", fontSize = 32.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Scooter Ran Out of Fuel!",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 20.sp,
                    color = DarkSurface
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    "Your tank is completely dry. Choose how you want to get back on the road:",
                    fontSize = 13.sp,
                    color = Color(0xFF64748B),
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Option 1: Call Roadside Paw-sistance
                Button(
                    onClick = { viewModel.callRoadsideAssistance() },
                    colors = ButtonDefaults.buttonColors(containerColor = TealBrand),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_roadside_assist")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛟 Call Roadside Paw-sistance", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            if (coins >= 25) "Cost: 25 CatCoins (Instant 50% Tank)" else "Emergency Rescue (Free courtesy refill)",
                            fontSize = 11.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Option 2: Push Scooter Mini-game
                OutlinedButton(
                    onClick = { viewModel.pushScooterMiniGame() },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().testTag("btn_push_scooter")
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🐾 Push Scooter to Station (+4% per tap)", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = DarkSurface)
                        Text("Current Tank: ${fuel.toInt()}% (Needs 15% to start engine)", fontSize = 11.sp, color = DustyRose)
                    }
                }
            }
        }
    }
}
