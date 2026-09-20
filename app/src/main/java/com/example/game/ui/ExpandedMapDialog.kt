package com.example.game.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.game.model.CityWorldData
import com.example.game.model.DeliveryState
import com.example.game.state.GameViewModel
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.HoneyYellowCream
import com.example.ui.theme.TealBrand
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun ExpandedMapDialog(
    viewModel: GameViewModel,
    onDismiss: () -> Unit
) {
    val activeJob by viewModel.activeJob.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(0.88f)
                .shadow(24.dp, RoundedCornerShape(28.dp)),
            shape = RoundedCornerShape(28.dp),
            color = HoneyYellowCream
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(DarkSurface)
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Meow City GPS Navigation",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp
                        )
                        Text(
                            "5 Districts • 2 Gas Stations • Delivery Grid",
                            color = Color(0xFF94A3B8),
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = onDismiss,
                        colors = ButtonDefaults.buttonColors(containerColor = HoneyYellow),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("btn_close_map")
                    ) {
                        Text("Close", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }

                // 2D City Map Canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(Color(0xFFE2E8F0), RoundedCornerShape(20.dp))
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasW = size.width
                        val canvasH = size.height
                        val centerX = canvasW * 0.5f
                        val centerY = canvasH * 0.5f
                        val mapScale = (canvasW.coerceAtMost(canvasH) * 0.42f) / 120f

                        // Draw Grass background
                        drawRect(
                            color = Color(0xFFDCFCE7),
                            topLeft = Offset.Zero,
                            size = size
                        )

                        // Draw Roads
                        CityWorldData.roads.forEach { r ->
                            val sx = centerX + r.startX * mapScale
                            val sy = centerY + r.startZ * mapScale
                            val ex = centerX + r.endX * mapScale
                            val ey = centerY + r.endZ * mapScale
                            drawLine(
                                color = Color(0xFF475569),
                                start = Offset(sx, sy),
                                end = Offset(ex, ey),
                                strokeWidth = r.width * mapScale
                            )
                        }

                        // Draw Buildings
                        CityWorldData.buildings.forEach { b ->
                            val bx = centerX + b.x * mapScale
                            val by = centerY + b.z * mapScale
                            val bw = b.width * mapScale
                            val bl = b.length * mapScale
                            drawRect(
                                color = if (b.isGasStation) Color(0xFFF59E0B) else b.wallColor,
                                topLeft = Offset(bx - bw * 0.5f, by - bl * 0.5f),
                                size = Size(bw, bl)
                            )
                        }

                        // Gas Stations Markers
                        CityWorldData.gasStations.forEach { s ->
                            val gx = centerX + s.x * mapScale
                            val gy = centerY + s.z * mapScale
                            drawCircle(color = Color(0xFFF59E0B), radius = 9f, center = Offset(gx, gy))
                            drawCircle(color = Color.White, radius = 5f, center = Offset(gx, gy))
                        }

                        // Delivery Route & Target
                        if (activeJob != null) {
                            val job = activeJob!!
                            if (job.state == DeliveryState.ACCEPTED) {
                                val tx = centerX + job.pickupX * mapScale
                                val ty = centerY + job.pickupZ * mapScale
                                // Route line
                                drawLine(
                                    color = HoneyYellow,
                                    start = Offset(centerX + viewModel.playerX * mapScale, centerY + viewModel.playerZ * mapScale),
                                    end = Offset(tx, ty),
                                    strokeWidth = 3.5f
                                )
                                drawCircle(color = HoneyYellow, radius = 12f, center = Offset(tx, ty))
                                drawCircle(color = Color.White, radius = 6f, center = Offset(tx, ty))
                            } else if (job.state == DeliveryState.PICKED_UP) {
                                val dx = centerX + job.dropoffX * mapScale
                                val dy = centerY + job.dropoffZ * mapScale
                                // Route line
                                drawLine(
                                    color = DustyRose,
                                    start = Offset(centerX + viewModel.playerX * mapScale, centerY + viewModel.playerZ * mapScale),
                                    end = Offset(dx, dy),
                                    strokeWidth = 3.5f
                                )
                                drawCircle(color = DustyRose, radius = 12f, center = Offset(dx, dy))
                                drawCircle(color = Color.White, radius = 6f, center = Offset(dx, dy))
                            }
                        }

                        // Player Marker
                        val px = centerX + viewModel.playerX * mapScale
                        val py = centerY + viewModel.playerZ * mapScale

                        // Heading arrow
                        val radHeading = (viewModel.playerHeadingDeg * PI / 180.0).toFloat()
                        val arrowX = px + sin(radHeading) * 16f
                        val arrowY = py + cos(radHeading) * 16f
                        drawLine(
                            color = TealBrand,
                            start = Offset(px, py),
                            end = Offset(arrowX, arrowY),
                            strokeWidth = 4f
                        )

                        // Player circle
                        drawCircle(color = TealBrand, radius = 8f, center = Offset(px, py))
                        drawCircle(color = Color.White, radius = 4f, center = Offset(px, py))
                    }
                }

                // Map Legend
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    LegendItem("You (Courier)", TealBrand)
                    LegendItem("Gas Station", Color(0xFFF59E0B))
                    LegendItem("Pickup Spot", HoneyYellow)
                    LegendItem("Dropoff Customer", DustyRose)
                }
            }
        }
    }
}

@Composable
fun LegendItem(label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = DarkSurface)
    }
}
