package com.example.game.engine

import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import com.example.game.model.CityBuilding
import com.example.game.model.CityProp
import com.example.game.model.CityWorldData
import com.example.game.model.DeliveryCategory
import com.example.game.model.DeliveryJob
import com.example.game.model.DeliveryState
import com.example.game.model.NpcPedestrian
import com.example.game.model.NpcVehicle
import com.example.game.model.PropType
import com.example.game.model.RoadSegment
import com.example.game.model.WeatherCondition
import com.example.ui.theme.DustyRose
import com.example.ui.theme.HoneyYellow
import com.example.ui.theme.TealBrand
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * High-performance, robust 3D miniature city renderer for Lalameow Rider.
 * Implements near-plane clipping for geometry, depth-sorted Painter's Algorithm,
 * dynamic sky/atmospheres, interactive delivery waypoints, and animated GPS breadcrumbs.
 */
class CityRenderer3D {

    private var cloudOffset = 0f
    private var rainPhase = 0f
    private var waypointPulse = 0f
    private var breadcrumbPhase = 0f

    fun update(deltaSeconds: Float) {
        cloudOffset = (cloudOffset + deltaSeconds * 2.5f) % 2000f
        rainPhase = (rainPhase + deltaSeconds * 14f) % 100f
        waypointPulse = (waypointPulse + deltaSeconds * 4.0f) % (2f * PI.toFloat())
        breadcrumbPhase = (breadcrumbPhase + deltaSeconds * 3.0f) % 1.0f
    }

    fun renderCity(
        drawScope: DrawScope,
        camera: Camera3D,
        weather: WeatherCondition,
        activeJob: DeliveryJob?,
        npcVehicles: List<NpcVehicle>,
        npcPedestrians: List<NpcPedestrian>
    ) {
        val w = drawScope.size.width
        val h = drawScope.size.height

        // 1. Render Sky & Atmospheric Background
        renderSky(drawScope, camera, weather, w, h)

        // 2. Render Ground & Road Network with Camera Clipping
        renderGroundAndRoads(drawScope, camera, weather, w, h)

        // 3. Render GPS Breadcrumbs towards destination
        if (activeJob != null) {
            renderGpsBreadcrumbs(drawScope, camera, activeJob, w, h)
        }

        // 4. Collect 3D Renderables for Painter's Algorithm (Depth Sorting)
        val renderables = mutableListOf<Renderable3D>()

        // Buildings
        CityWorldData.buildings.forEach { b ->
            val camPt = camera.toCameraSpace(b.x, 0f, b.z)
            if (camPt.z in 1f..170f) {
                renderables.add(BuildingRenderable(b, camPt.z))
            }
        }

        // Props (Trees, Streetlamps, Hydrants, Signs)
        CityWorldData.props.forEach { prop ->
            val camPt = camera.toCameraSpace(prop.x, 0f, prop.z)
            if (camPt.z in 1f..130f) {
                renderables.add(PropRenderable(prop, camPt.z))
            }
        }

        // NPC Vehicles
        npcVehicles.forEach { v ->
            val camPt = camera.toCameraSpace(v.x, 0f, v.z)
            if (camPt.z in 1f..130f) {
                renderables.add(NpcVehicleRenderable(v, camPt.z))
            }
        }

        // NPC Pedestrians
        npcPedestrians.forEach { ped ->
            val camPt = camera.toCameraSpace(ped.x, 0f, ped.z)
            if (camPt.z in 1f..90f) {
                renderables.add(PedestrianRenderable(ped, camPt.z))
            }
        }

        // Active Delivery Holographic Waypoints
        if (activeJob != null) {
            if (activeJob.state == DeliveryState.ACCEPTED) {
                val camPt = camera.toCameraSpace(activeJob.pickupX, 0f, activeJob.pickupZ)
                if (camPt.z > 0.5f) {
                    renderables.add(
                        WaypointRenderable(
                            x = activeJob.pickupX,
                            z = activeJob.pickupZ,
                            color = HoneyYellow,
                            label = "PICKUP: ${activeJob.pickupVenue}",
                            category = activeJob.category,
                            isPickup = true,
                            depth = camPt.z
                        )
                    )
                }
            } else if (activeJob.state == DeliveryState.PICKED_UP) {
                val camPt = camera.toCameraSpace(activeJob.dropoffX, 0f, activeJob.dropoffZ)
                if (camPt.z > 0.5f) {
                    renderables.add(
                        WaypointRenderable(
                            x = activeJob.dropoffX,
                            z = activeJob.dropoffZ,
                            color = DustyRose,
                            label = "DELIVER: ${activeJob.dropoffCustomer}",
                            category = activeJob.category,
                            isPickup = false,
                            depth = camPt.z
                        )
                    )
                }
            }
        }

        // Gas Station Hologram Beacons
        CityWorldData.gasStations.forEach { s ->
            val camPt = camera.toCameraSpace(s.x, 0f, s.z)
            if (camPt.z in 1f..120f) {
                renderables.add(GasStationRenderable(s.x, s.z, s.name, camPt.z))
            }
        }

        // Sort furthest to closest
        renderables.sortByDescending { it.depth }

        // Render all depth-sorted objects
        renderables.forEach { item ->
            item.draw(drawScope, camera, weather, waypointPulse, w, h)
        }

        // 5. Weather Overlays (Raindrops)
        if (weather == WeatherCondition.RAINY) {
            renderRain(drawScope, w, h)
        }
    }

    private fun renderSky(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, w: Float, h: Float) {
        val horizonY = h * 0.38f

        // Atmospheric Sky Color
        val skyColor = when (weather) {
            WeatherCondition.SUNNY -> Color(0xFFBAE6FD)
            WeatherCondition.GOLDEN_SUNSET -> Color(0xFFFED7AA)
            WeatherCondition.RAINY -> Color(0xFF94A3B8)
            WeatherCondition.NEON_NIGHT -> Color(0xFF0F172A)
        }
        drawScope.drawRect(
            color = skyColor,
            topLeft = Offset(0f, 0f),
            size = Size(w, horizonY)
        )

        // Celestial Body: Sun or Moon
        if (weather == WeatherCondition.SUNNY || weather == WeatherCondition.GOLDEN_SUNSET) {
            val sunY = horizonY * 0.45f
            val sunColor = if (weather == WeatherCondition.SUNNY) Color(0xFFFEF08A) else Color(0xFFFB923C)
            drawScope.drawCircle(
                color = sunColor.copy(alpha = 0.35f),
                radius = 54f,
                center = Offset(w * 0.76f, sunY)
            )
            drawScope.drawCircle(
                color = sunColor,
                radius = 32f,
                center = Offset(w * 0.76f, sunY)
            )
        } else if (weather == WeatherCondition.NEON_NIGHT) {
            val moonY = horizonY * 0.4f
            drawScope.drawCircle(
                color = Color(0xFFF1F5F9).copy(alpha = 0.25f),
                radius = 38f,
                center = Offset(w * 0.82f, moonY)
            )
            drawScope.drawCircle(
                color = Color(0xFFF8FAFC),
                radius = 24f,
                center = Offset(w * 0.82f, moonY)
            )
        }

        // Cute stylized puffy clouds
        if (weather != WeatherCondition.NEON_NIGHT) {
            val cloudColor = if (weather == WeatherCondition.RAINY) Color(0xFFCBD5E1) else Color(0xEEFFFFFF)
            for (i in 0..4) {
                val cx = ((i * 260f + cloudOffset) % (w + 200f)) - 100f
                val cy = horizonY * (0.22f + (i % 3) * 0.16f)
                drawScope.drawCircle(color = cloudColor, radius = 38f, center = Offset(cx, cy))
                drawScope.drawCircle(color = cloudColor, radius = 48f, center = Offset(cx + 34f, cy - 6f))
                drawScope.drawCircle(color = cloudColor, radius = 34f, center = Offset(cx + 68f, cy))
            }
        }
    }

    private fun renderGroundAndRoads(
        drawScope: DrawScope,
        camera: Camera3D,
        weather: WeatherCondition,
        w: Float,
        h: Float
    ) {
        val horizonY = h * 0.38f

        // Ground base plane
        val groundColor = when (weather) {
            WeatherCondition.SUNNY -> Color(0xFF86EFAC) // Fresh vibrant lawn
            WeatherCondition.GOLDEN_SUNSET -> Color(0xFFFDE68A) // Warm amber grass
            WeatherCondition.RAINY -> Color(0xFF475569) // Wet slate grass
            WeatherCondition.NEON_NIGHT -> Color(0xFF1E293B) // Dark night lawn
        }
        drawScope.drawRect(
            color = groundColor,
            topLeft = Offset(0f, horizonY),
            size = Size(w, h - horizonY)
        )

        // Road Asphalt Color
        val asphaltColor = when (weather) {
            WeatherCondition.RAINY -> Color(0xFF1E293B)
            WeatherCondition.NEON_NIGHT -> Color(0xFF0F172A)
            else -> Color(0xFF334155)
        }

        // Render all roads in robust subdivided chunks
        CityWorldData.roads.forEach { road ->
            renderSubdividedRoad(drawScope, camera, road, asphaltColor, w, h)
        }
    }

    private fun renderSubdividedRoad(
        drawScope: DrawScope,
        camera: Camera3D,
        road: RoadSegment,
        asphaltColor: Color,
        w: Float,
        h: Float
    ) {
        val dx = road.endX - road.startX
        val dz = road.endZ - road.startZ
        val roadLen = kotlin.math.sqrt(dx * dx + dz * dz)
        if (roadLen <= 0.1f) return

        val halfW = road.width * 0.5f
        val isHorizontal = kotlin.math.abs(dx) > kotlin.math.abs(dz)

        // Subdivide road into 10-unit pieces
        val steps = (roadLen / 10f).toInt().coerceAtLeast(1)

        for (s in 0 until steps) {
            val u0 = s.toFloat() / steps
            val u1 = (s + 1).toFloat() / steps

            val x0 = road.startX + dx * u0
            val z0 = road.startZ + dz * u0
            val x1 = road.startX + dx * u1
            val z1 = road.startZ + dz * u1

            // 4 world corners
            val w1 = if (isHorizontal) Vector3(x0, 0f, z0 - halfW) else Vector3(x0 - halfW, 0f, z0)
            val w2 = if (isHorizontal) Vector3(x0, 0f, z0 + halfW) else Vector3(x0 + halfW, 0f, z0)
            val w3 = if (isHorizontal) Vector3(x1, 0f, z1 + halfW) else Vector3(x1 + halfW, 0f, z1)
            val w4 = if (isHorizontal) Vector3(x1, 0f, z1 - halfW) else Vector3(x1 - halfW, 0f, z1)

            // Convert to camera space
            val c1 = camera.toCameraSpace(w1.x, w1.y, w1.z)
            val c2 = camera.toCameraSpace(w2.x, w2.y, w2.z)
            val c3 = camera.toCameraSpace(w3.x, w3.y, w3.z)
            val c4 = camera.toCameraSpace(w4.x, w4.y, w4.z)

            // If completely behind camera near plane, skip
            if (c1.z < 0.35f && c2.z < 0.35f && c3.z < 0.35f && c4.z < 0.35f) continue
            // If too far away, skip
            if (c1.z > 160f && c3.z > 160f) continue

            // Clamp near z to prevent (0,0) singularities
            val cl1 = if (c1.z < 0.35f) Vector3(c1.x, c1.y, 0.35f) else c1
            val cl2 = if (c2.z < 0.35f) Vector3(c2.x, c2.y, 0.35f) else c2
            val cl3 = if (c3.z < 0.35f) Vector3(c3.x, c3.y, 0.35f) else c3
            val cl4 = if (c4.z < 0.35f) Vector3(c4.x, c4.y, 0.35f) else c4

            val p1 = camera.projectCameraPoint(cl1, w, h)
            val p2 = camera.projectCameraPoint(cl2, w, h)
            val p3 = camera.projectCameraPoint(cl3, w, h)
            val p4 = camera.projectCameraPoint(cl4, w, h)

            if (p1.isVisible || p2.isVisible || p3.isVisible || p4.isVisible) {
                // Draw Asphalt Quad
                val path = Path().apply {
                    moveTo(p1.x, p1.y)
                    lineTo(p2.x, p2.y)
                    lineTo(p3.x, p3.y)
                    lineTo(p4.x, p4.y)
                    close()
                }
                drawScope.drawPath(path, color = asphaltColor)

                // Sidewalk Curbs
                drawScope.drawLine(Color(0xFFCBD5E1), Offset(p1.x, p1.y), Offset(p4.x, p4.y), strokeWidth = 2.5f)
                drawScope.drawLine(Color(0xFFCBD5E1), Offset(p2.x, p2.y), Offset(p3.x, p3.y), strokeWidth = 2.5f)

                // Yellow Centerline Dash
                if (s % 2 == 0) {
                    val midX0 = (x0 + x1) * 0.5f - (dx / steps) * 0.2f
                    val midZ0 = (z0 + z1) * 0.5f - (dz / steps) * 0.2f
                    val midX1 = (x0 + x1) * 0.5f + (dx / steps) * 0.2f
                    val midZ1 = (z0 + z1) * 0.5f + (dz / steps) * 0.2f

                    val cm0 = camera.toCameraSpace(midX0, 0f, midZ0)
                    val cm1 = camera.toCameraSpace(midX1, 0f, midZ1)
                    if (cm0.z >= 0.35f && cm1.z >= 0.35f) {
                        val pm0 = camera.projectCameraPoint(cm0, w, h)
                        val pm1 = camera.projectCameraPoint(cm1, w, h)
                        if (pm0.isVisible && pm1.isVisible) {
                            drawScope.drawLine(
                                color = Color(0xFFFDE047),
                                start = Offset(pm0.x, pm0.y),
                                end = Offset(pm1.x, pm1.y),
                                strokeWidth = 3f
                            )
                        }
                    }
                }
            }
        }
    }

    private fun renderGpsBreadcrumbs(
        drawScope: DrawScope,
        camera: Camera3D,
        job: DeliveryJob,
        w: Float,
        h: Float
    ) {
        val targetX = if (job.state == DeliveryState.ACCEPTED) job.pickupX else job.dropoffX
        val targetZ = if (job.state == DeliveryState.ACCEPTED) job.pickupZ else job.dropoffZ

        val dotsCount = 14
        val color = if (job.state == DeliveryState.ACCEPTED) HoneyYellow else TealBrand

        for (i in 0 until dotsCount) {
            val progress = ((i.toFloat() / dotsCount) + breadcrumbPhase) % 1.0f
            val dotX = camera.posX + (targetX - camera.posX) * progress
            val dotZ = camera.posZ + (targetZ - camera.posZ) * progress

            val cPt = camera.toCameraSpace(dotX, 0.15f, dotZ)
            if (cPt.z in 0.5f..80f) {
                val p = camera.projectCameraPoint(cPt, w, h)
                if (p.isVisible) {
                    val dotRadius = (22f / cPt.z).coerceIn(3f, 10f)
                    drawScope.drawCircle(
                        color = color.copy(alpha = (1f - progress * 0.5f).coerceIn(0.2f, 0.9f)),
                        radius = dotRadius,
                        center = Offset(p.x, p.y)
                    )
                }
            }
        }
    }

    private fun renderRain(drawScope: DrawScope, w: Float, h: Float) {
        val count = 48
        for (i in 0 until count) {
            val rx = ((i * 49f + rainPhase * 45f) % w)
            val ry = ((i * 37f + rainPhase * 115f) % h)
            drawScope.drawLine(
                color = Color(0x77BAE6FD),
                start = Offset(rx, ry),
                end = Offset(rx - 7f, ry + 24f),
                strokeWidth = 2f
            )
        }
    }
}

// 3D Renderable interfaces and items for depth sorting
interface Renderable3D {
    val depth: Float
    fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float)
}

class BuildingRenderable(val b: CityBuilding, override val depth: Float) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val halfW = b.width * 0.5f
        val halfL = b.length * 0.5f

        // Camera-space corners (South: -Z, North: +Z, West: -X, East: +X)
        val c_b0 = camera.toCameraSpace(b.x - halfW, 0f, b.z - halfL) // SW bottom
        val c_b1 = camera.toCameraSpace(b.x + halfW, 0f, b.z - halfL) // SE bottom
        val c_b2 = camera.toCameraSpace(b.x + halfW, 0f, b.z + halfL) // NE bottom
        val c_b3 = camera.toCameraSpace(b.x - halfW, 0f, b.z + halfL) // NW bottom

        val c_t0 = camera.toCameraSpace(b.x - halfW, b.height, b.z - halfL) // SW top
        val c_t1 = camera.toCameraSpace(b.x + halfW, b.height, b.z - halfL) // SE top
        val c_t2 = camera.toCameraSpace(b.x + halfW, b.height, b.z + halfL) // NE top
        val c_t3 = camera.toCameraSpace(b.x - halfW, b.height, b.z + halfL) // NW top

        if (c_b0.z < 0.35f && c_b1.z < 0.35f && c_b2.z < 0.35f && c_b3.z < 0.35f) return

        fun clampPt(v: Vector3) = if (v.z < 0.35f) Vector3(v.x, v.y, 0.35f) else v

        val b0 = camera.projectCameraPoint(clampPt(c_b0), w, h)
        val b1 = camera.projectCameraPoint(clampPt(c_b1), w, h)
        val b2 = camera.projectCameraPoint(clampPt(c_b2), w, h)
        val b3 = camera.projectCameraPoint(clampPt(c_b3), w, h)

        val t0 = camera.projectCameraPoint(clampPt(c_t0), w, h)
        val t1 = camera.projectCameraPoint(clampPt(c_t1), w, h)
        val t2 = camera.projectCameraPoint(clampPt(c_t2), w, h)
        val t3 = camera.projectCameraPoint(clampPt(c_t3), w, h)

        val isNight = weather == WeatherCondition.NEON_NIGHT
        val isSunset = weather == WeatherCondition.GOLDEN_SUNSET

        // Face visibility checks based on camera position relative to building planes
        val isSouthVisible = camera.posZ < (b.z - halfL)
        val isNorthVisible = camera.posZ > (b.z + halfL)
        val isEastVisible = camera.posX > (b.x + halfW)
        val isWestVisible = camera.posX < (b.x - halfW)
        val isRoofVisible = camera.posY > b.height

        // 1. Concrete Sidewalk Plinth around base
        val curbColor = when {
            isNight -> Color(0xFF1E293B)
            isSunset -> Color(0xFFFDE68A)
            else -> Color(0xFFE2E8F0)
        }
        val curbPath = Path().apply {
            moveTo(b0.x, b0.y)
            lineTo(b1.x, b1.y)
            lineTo(b2.x, b2.y)
            lineTo(b3.x, b3.y)
            close()
        }
        drawScope.drawPath(curbPath, color = curbColor)

        // Helper to draw a wall quad
        fun drawWall(p0: ScreenPoint, p1: ScreenPoint, pTop1: ScreenPoint, pTop0: ScreenPoint, faceColor: Color) {
            val wallPath = Path().apply {
                moveTo(p0.x, p0.y)
                lineTo(p1.x, p1.y)
                lineTo(pTop1.x, pTop1.y)
                lineTo(pTop0.x, pTop0.y)
                close()
            }
            drawScope.drawPath(wallPath, color = faceColor)
            drawScope.drawLine(faceColor.copy(alpha = 0.4f), Offset(p0.x, p0.y), Offset(pTop0.x, pTop0.y), strokeWidth = 2f)
            drawScope.drawLine(faceColor.copy(alpha = 0.4f), Offset(p1.x, p1.y), Offset(pTop1.x, pTop1.y), strokeWidth = 2f)
        }

        // Helper to draw windows on a face
        fun drawWindows(p0: ScreenPoint, p1: ScreenPoint, pTop1: ScreenPoint, pTop0: ScreenPoint) {
            val winRows = (b.height / 3.8f).toInt().coerceAtLeast(1)
            val windowColor = if (isNight) Color(0xFFFEF08A) else if (isSunset) Color(0xFFFED7AA) else Color(0x99FFFFFF)
            val frameColor = Color(0x44000000)

            for (r in 1..winRows) {
                val hRatio0 = (r.toFloat() - 0.2f) / (winRows + 1)
                val hRatio1 = (r.toFloat() + 0.35f) / (winRows + 1)

                val w1_b = Offset(p0.x + (p1.x - p0.x) * 0.2f + (pTop0.x - p0.x) * hRatio0, p0.y + (p1.y - p0.y) * 0.2f + (pTop0.y - p0.y) * hRatio0)
                val w1_r = Offset(p0.x + (p1.x - p0.x) * 0.42f + (pTop0.x - p0.x) * hRatio0, p0.y + (p1.y - p0.y) * 0.42f + (pTop0.y - p0.y) * hRatio0)
                val w1_tr = Offset(p0.x + (p1.x - p0.x) * 0.42f + (pTop0.x - p0.x) * hRatio1, p0.y + (p1.y - p0.y) * 0.42f + (pTop0.y - p0.y) * hRatio1)
                val w1_tl = Offset(p0.x + (p1.x - p0.x) * 0.2f + (pTop0.x - p0.x) * hRatio1, p0.y + (p1.y - p0.y) * 0.2f + (pTop0.y - p0.y) * hRatio1)

                val win1Path = Path().apply {
                    moveTo(w1_b.x, w1_b.y)
                    lineTo(w1_r.x, w1_r.y)
                    lineTo(w1_tr.x, w1_tr.y)
                    lineTo(w1_tl.x, w1_tl.y)
                    close()
                }
                drawScope.drawPath(win1Path, color = windowColor)
                drawScope.drawPath(win1Path, color = frameColor, style = Stroke(width = 1.5f))

                val w2_b = Offset(p0.x + (p1.x - p0.x) * 0.58f + (pTop0.x - p0.x) * hRatio0, p0.y + (p1.y - p0.y) * 0.58f + (pTop0.y - p0.y) * hRatio0)
                val w2_r = Offset(p0.x + (p1.x - p0.x) * 0.8f + (pTop0.x - p0.x) * hRatio0, p0.y + (p1.y - p0.y) * 0.8f + (pTop0.y - p0.y) * hRatio0)
                val w2_tr = Offset(p0.x + (p1.x - p0.x) * 0.8f + (pTop0.x - p0.x) * hRatio1, p0.y + (p1.y - p0.y) * 0.8f + (pTop0.y - p0.y) * hRatio1)
                val w2_tl = Offset(p0.x + (p1.x - p0.x) * 0.58f + (pTop0.x - p0.x) * hRatio1, p0.y + (p1.y - p0.y) * 0.58f + (pTop0.y - p0.y) * hRatio1)

                val win2Path = Path().apply {
                    moveTo(w2_b.x, w2_b.y)
                    lineTo(w2_r.x, w2_r.y)
                    lineTo(w2_tr.x, w2_tr.y)
                    lineTo(w2_tl.x, w2_tl.y)
                    close()
                }
                drawScope.drawPath(win2Path, color = windowColor)
                drawScope.drawPath(win2Path, color = frameColor, style = Stroke(width = 1.5f))
            }
        }

        // Draw South Wall (-Z)
        if (isSouthVisible) {
            val faceShade = b.wallColor.copy(alpha = 0.95f)
            drawWall(b0, b1, t1, t0, faceShade)
            drawWindows(b0, b1, t1, t0)
        }

        // Draw North Wall (+Z)
        if (isNorthVisible) {
            val faceShade = b.wallColor.copy(alpha = 0.72f)
            drawWall(b2, b3, t3, t2, faceShade)
            drawWindows(b2, b3, t3, t2)
        }

        // Draw West Wall (-X)
        if (isWestVisible) {
            val faceShade = b.wallColor.copy(alpha = 0.82f)
            drawWall(b3, b0, t0, t3, faceShade)
            drawWindows(b3, b0, t0, t3)
        }

        // Draw East Wall (+X)
        if (isEastVisible) {
            val faceShade = b.wallColor.copy(alpha = 0.88f)
            drawWall(b1, b2, t2, t1, faceShade)
            drawWindows(b1, b2, t2, t1)
        }

        // Draw Roof (+Y)
        if (isRoofVisible) {
            val roofPath = Path().apply {
                moveTo(t0.x, t0.y)
                lineTo(t1.x, t1.y)
                lineTo(t2.x, t2.y)
                lineTo(t3.x, t3.y)
                close()
            }
            drawScope.drawPath(roofPath, color = b.roofColor)
            drawScope.drawPath(roofPath, color = Color(0x33000000), style = Stroke(width = 3f))
        }

        // Storefront Awning & 3D Signboard (for street-facing front face)
        val isFrontVisible = (b.x > 0 && isWestVisible) || (b.x < 0 && isEastVisible) || (b.x == 0f && isSouthVisible)
        if (isFrontVisible) {
            val (f0, f1, ft1, ft0) = when {
                b.x > 0 -> listOf(b3, b0, t0, t3)
                b.x < 0 -> listOf(b1, b2, t2, t1)
                else -> listOf(b0, b1, t1, t0)
            }

            // 3D Striped Awning
            val awningColor = b.awningColor ?: DustyRose
            val aw_h0 = 0.30f
            val aw_h1 = 0.42f
            val a_left_back = Offset(f0.x + (ft0.x - f0.x) * aw_h1, f0.y + (ft0.y - f0.y) * aw_h1)
            val a_right_back = Offset(f1.x + (ft1.x - f1.x) * aw_h1, f1.y + (ft1.y - f1.y) * aw_h1)
            val a_left_front = Offset(f0.x + (ft0.x - f0.x) * aw_h0 - (f1.x - f0.x) * 0.05f, f0.y + (ft0.y - f0.y) * aw_h0 + 10f)
            val a_right_front = Offset(f1.x + (ft1.x - f1.x) * aw_h0 + (f1.x - f0.x) * 0.05f, f1.y + (ft1.y - f1.y) * aw_h0 + 10f)

            val awningPath = Path().apply {
                moveTo(a_left_back.x, a_left_back.y)
                lineTo(a_right_back.x, a_right_back.y)
                lineTo(a_right_front.x, a_right_front.y)
                lineTo(a_left_front.x, a_left_front.y)
                close()
            }
            drawScope.drawPath(awningPath, color = awningColor)

            // Striped panels on awning
            for (s in 0..4 step 2) {
                val u0 = s / 5f
                val u1 = (s + 1) / 5f
                val sp_lb = Offset(a_left_back.x + (a_right_back.x - a_left_back.x) * u0, a_left_back.y + (a_right_back.y - a_left_back.y) * u0)
                val sp_rb = Offset(a_left_back.x + (a_right_back.x - a_left_back.x) * u1, a_left_back.y + (a_right_back.y - a_left_back.y) * u1)
                val sp_rf = Offset(a_left_front.x + (a_right_front.x - a_left_front.x) * u1, a_left_front.y + (a_right_front.y - a_left_front.y) * u1)
                val sp_lf = Offset(a_left_front.x + (a_right_front.x - a_left_front.x) * u0, a_left_front.y + (a_right_front.y - a_left_front.y) * u0)

                val stripePath = Path().apply {
                    moveTo(sp_lb.x, sp_lb.y)
                    lineTo(sp_rb.x, sp_rb.y)
                    lineTo(sp_rf.x, sp_rf.y)
                    lineTo(sp_lf.x, sp_lf.y)
                    close()
                }
                drawScope.drawPath(stripePath, color = Color.White.copy(alpha = 0.85f))
            }

            // Storefront Showcase Window on Ground Floor
            val w_ground = Path().apply {
                val gw_lb = Offset(f0.x + (f1.x - f0.x) * 0.12f, f0.y + (f1.y - f0.y) * 0.12f)
                val gw_rb = Offset(f0.x + (f1.x - f0.x) * 0.62f, f0.y + (f1.y - f0.y) * 0.62f)
                val gw_rt = Offset(f0.x + (f1.x - f0.x) * 0.62f + (ft0.x - f0.x) * 0.26f, f0.y + (f1.y - f0.y) * 0.62f + (ft0.y - f0.y) * 0.26f)
                val gw_lt = Offset(f0.x + (f1.x - f0.x) * 0.12f + (ft0.x - f0.x) * 0.26f, f0.y + (f1.y - f0.y) * 0.12f + (ft0.y - f0.y) * 0.26f)
                moveTo(gw_lb.x, gw_lb.y)
                lineTo(gw_rb.x, gw_rb.y)
                lineTo(gw_rt.x, gw_rt.y)
                lineTo(gw_lt.x, gw_lt.y)
                close()
            }
            drawScope.drawPath(w_ground, color = if (isNight) Color(0xFFFEF08A) else Color(0xBB93C5FD))

            // Shop Entrance Door
            val door = Path().apply {
                val d_lb = Offset(f0.x + (f1.x - f0.x) * 0.72f, f0.y + (f1.y - f0.y) * 0.72f)
                val d_rb = Offset(f0.x + (f1.x - f0.x) * 0.92f, f0.y + (f1.y - f0.y) * 0.92f)
                val d_rt = Offset(f0.x + (f1.x - f0.x) * 0.92f + (ft0.x - f0.x) * 0.30f, f0.y + (f1.y - f0.y) * 0.92f + (ft0.y - f0.y) * 0.30f)
                val d_lt = Offset(f0.x + (f1.x - f0.x) * 0.72f + (ft0.x - f0.x) * 0.30f, f0.y + (f1.y - f0.y) * 0.72f + (ft0.y - f0.y) * 0.30f)
                moveTo(d_lb.x, d_lb.y)
                lineTo(d_rb.x, d_rb.y)
                lineTo(d_rt.x, d_rt.y)
                lineTo(d_lt.x, d_lt.y)
                close()
            }
            drawScope.drawPath(door, color = Color(0xFF92400E))

            // Storefront Signboard Badge
            if (b.signText != null && depth in 2f..90f) {
                val signCenter = Offset(
                    (a_left_back.x + a_right_back.x) * 0.5f,
                    (a_left_back.y + a_right_back.y) * 0.5f - 18f
                )
                val signScale = (h * 0.55f / depth).coerceIn(12f, 90f)

                drawScope.drawRoundRect(
                    color = Color(0xDD0F172A),
                    topLeft = Offset(signCenter.x - signScale * 1.1f, signCenter.y - signScale * 0.22f),
                    size = Size(signScale * 2.2f, signScale * 0.44f),
                    cornerRadius = CornerRadius(signScale * 0.1f, signScale * 0.1f)
                )
                drawScope.drawRoundRect(
                    color = HoneyYellow,
                    topLeft = Offset(signCenter.x - signScale * 1.1f, signCenter.y - signScale * 0.22f),
                    size = Size(signScale * 2.2f, signScale * 0.44f),
                    cornerRadius = CornerRadius(signScale * 0.1f, signScale * 0.1f),
                    style = Stroke(width = 2f)
                )
            }
        }
    }
}

class PropRenderable(val prop: CityProp, override val depth: Float) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val baseScreen = camera.project(prop.x, 0f, prop.z, w, h)
        if (!baseScreen.isVisible) return

        val scale = (h * 0.55f / depth).coerceIn(4f, 80f)

        when (prop.type) {
            PropType.TREE -> {
                // Tree Trunk
                drawScope.drawRoundRect(
                    color = Color(0xFF78350F),
                    topLeft = Offset(baseScreen.x - scale * 0.08f, baseScreen.y - scale * 0.6f),
                    size = Size(scale * 0.16f, scale * 0.6f),
                    cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
                )
                // Fluffy Foliage
                val leafColor = if (weather == WeatherCondition.GOLDEN_SUNSET) Color(0xFFF97316) else Color(0xFF22C55E)
                drawScope.drawCircle(
                    color = leafColor,
                    radius = scale * 0.42f,
                    center = Offset(baseScreen.x, baseScreen.y - scale * 0.85f)
                )
                drawScope.drawCircle(
                    color = leafColor.copy(alpha = 0.8f),
                    radius = scale * 0.32f,
                    center = Offset(baseScreen.x - scale * 0.15f, baseScreen.y - scale * 0.95f)
                )
                drawScope.drawCircle(
                    color = leafColor.copy(alpha = 0.8f),
                    radius = scale * 0.32f,
                    center = Offset(baseScreen.x + scale * 0.15f, baseScreen.y - scale * 0.95f)
                )
            }
            PropType.STREETLAMP -> {
                // Post
                drawScope.drawLine(
                    color = Color(0xFF475569),
                    start = Offset(baseScreen.x, baseScreen.y),
                    end = Offset(baseScreen.x, baseScreen.y - scale * 1.2f),
                    strokeWidth = scale * 0.08f
                )
                // Lamp Fixture
                val isNight = weather == WeatherCondition.NEON_NIGHT
                val lampColor = if (isNight) Color(0xFFFEF08A) else Color(0xFFCBD5E1)
                drawScope.drawCircle(lampColor, radius = scale * 0.16f, center = Offset(baseScreen.x, baseScreen.y - scale * 1.25f))
                if (isNight) {
                    // Soft light cone on ground
                    drawScope.drawOval(
                        color = Color(0x33FEF08A),
                        topLeft = Offset(baseScreen.x - scale * 0.8f, baseScreen.y - scale * 0.25f),
                        size = Size(scale * 1.6f, scale * 0.5f)
                    )
                }
            }
            PropType.FIRE_HYDRANT -> {
                drawScope.drawRoundRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(baseScreen.x - scale * 0.12f, baseScreen.y - scale * 0.45f),
                    size = Size(scale * 0.24f, scale * 0.45f),
                    cornerRadius = CornerRadius(scale * 0.06f, scale * 0.06f)
                )
            }
            PropType.BENCH -> {
                drawScope.drawRoundRect(
                    color = Color(0xFF92400E),
                    topLeft = Offset(baseScreen.x - scale * 0.35f, baseScreen.y - scale * 0.25f),
                    size = Size(scale * 0.7f, scale * 0.16f),
                    cornerRadius = CornerRadius(scale * 0.03f, scale * 0.03f)
                )
            }
            PropType.FLOWER_POT -> {
                drawScope.drawCircle(
                    color = Color(0xFFEA580C),
                    radius = scale * 0.18f,
                    center = Offset(baseScreen.x, baseScreen.y - scale * 0.18f)
                )
                drawScope.drawCircle(
                    color = Color(0xFFEC4899),
                    radius = scale * 0.12f,
                    center = Offset(baseScreen.x, baseScreen.y - scale * 0.32f)
                )
            }
            PropType.PUDDLE -> {
                drawScope.drawOval(
                    color = Color(0x7738BDF8),
                    topLeft = Offset(baseScreen.x - scale * 0.4f, baseScreen.y - scale * 0.12f),
                    size = Size(scale * 0.8f, scale * 0.24f)
                )
            }
            PropType.DELIVERY_PARCEL -> {
                // 3D Cardboard Delivery Parcel on Sidewalk
                val parcelW = scale * 0.52f * prop.scale
                val parcelH = scale * 0.44f * prop.scale

                // Contact ground shadow
                drawScope.drawOval(
                    color = Color(0x66000000),
                    topLeft = Offset(baseScreen.x - parcelW * 0.55f, baseScreen.y - parcelH * 0.18f),
                    size = Size(parcelW * 1.1f, parcelH * 0.36f)
                )

                // Front Kraft Box Face
                drawScope.drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(baseScreen.x - parcelW * 0.5f, baseScreen.y - parcelH * 0.65f),
                    size = Size(parcelW, parcelH * 0.65f),
                    cornerRadius = CornerRadius(scale * 0.03f, scale * 0.03f)
                )
                // Top Flap perspective
                val topFlap = Path().apply {
                    moveTo(baseScreen.x - parcelW * 0.5f, baseScreen.y - parcelH * 0.65f)
                    lineTo(baseScreen.x - parcelW * 0.35f, baseScreen.y - parcelH * 0.95f)
                    lineTo(baseScreen.x + parcelW * 0.35f, baseScreen.y - parcelH * 0.95f)
                    lineTo(baseScreen.x + parcelW * 0.5f, baseScreen.y - parcelH * 0.65f)
                    close()
                }
                drawScope.drawPath(topFlap, color = Color(0xFFF59E0B))

                // Bright Yellow Lalameow Sealing Tape across box
                drawScope.drawRoundRect(
                    color = HoneyYellow,
                    topLeft = Offset(baseScreen.x - scale * 0.06f, baseScreen.y - parcelH * 0.95f),
                    size = Size(scale * 0.12f, parcelH * 0.95f)
                )

                // Red Fragile / Postage Stamp
                drawScope.drawRoundRect(
                    color = Color(0xFFEF4444),
                    topLeft = Offset(baseScreen.x + parcelW * 0.15f, baseScreen.y - parcelH * 0.55f),
                    size = Size(scale * 0.14f, scale * 0.10f),
                    cornerRadius = CornerRadius(scale * 0.01f, scale * 0.01f)
                )

                // Stamped Black Paw Print
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.045f, center = Offset(baseScreen.x - parcelW * 0.15f, baseScreen.y - parcelH * 0.32f))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.018f, center = Offset(baseScreen.x - parcelW * 0.2f, baseScreen.y - parcelH * 0.38f))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.02f, center = Offset(baseScreen.x - parcelW * 0.15f, baseScreen.y - parcelH * 0.41f))
                drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.018f, center = Offset(baseScreen.x - parcelW * 0.1f, baseScreen.y - parcelH * 0.38f))
            }
            PropType.NEWSPAPER_STAND -> {
                // 3D Newspaper Rack Stand holding rolled copies of The Daily Purr
                val standW = scale * 0.6f * prop.scale
                val standH = scale * 0.8f * prop.scale

                // Contact shadow
                drawScope.drawOval(
                    color = Color(0x66000000),
                    topLeft = Offset(baseScreen.x - standW * 0.6f, baseScreen.y - standH * 0.12f),
                    size = Size(standW * 1.2f, standH * 0.25f)
                )

                // Metal Newsstand Legs & Body (Dark Navy)
                drawScope.drawRoundRect(
                    color = Color(0xFF1E293B),
                    topLeft = Offset(baseScreen.x - standW * 0.5f, baseScreen.y - standH * 0.85f),
                    size = Size(standW, standH * 0.85f),
                    cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
                )

                // Headline Display Header ("THE DAILY PURR 🗞️")
                drawScope.drawRoundRect(
                    color = Color(0xFF3B82F6),
                    topLeft = Offset(baseScreen.x - standW * 0.5f, baseScreen.y - standH * 0.98f),
                    size = Size(standW, standH * 0.22f),
                    cornerRadius = CornerRadius(scale * 0.02f, scale * 0.02f)
                )

                // 3 Rolled Newspapers visible on the rack tier
                for (rollIdx in -1..1) {
                    val rX = baseScreen.x + rollIdx * standW * 0.3f
                    val rY = baseScreen.y - standH * 0.45f
                    val rW = standW * 0.25f
                    val rH = standH * 0.18f

                    // White Paper Roll
                    drawScope.drawRoundRect(
                        color = Color(0xFFFFFBEB),
                        topLeft = Offset(rX - rW * 0.5f, rY - rH * 0.5f),
                        size = Size(rW, rH),
                        cornerRadius = CornerRadius(rH * 0.4f, rH * 0.4f)
                    )
                    // Printed news text lines
                    drawScope.drawLine(
                        color = Color(0xFF334155),
                        start = Offset(rX - rW * 0.35f, rY - rH * 0.15f),
                        end = Offset(rX + rW * 0.35f, rY - rH * 0.15f),
                        strokeWidth = scale * 0.02f
                    )
                    drawScope.drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(rX - rW * 0.3f, rY + rH * 0.1f),
                        end = Offset(rX + rW * 0.3f, rY + rH * 0.1f),
                        strokeWidth = scale * 0.015f
                    )
                    // Red Twine String wrapped in middle
                    drawScope.drawRoundRect(
                        color = Color(0xFFEF4444),
                        topLeft = Offset(rX - scale * 0.02f, rY - rH * 0.55f),
                        size = Size(scale * 0.04f, rH * 1.1f)
                    )
                }
            }
            PropType.PARCEL_STACK -> {
                // Stack of 3 parcels of different sizes outside courier depot
                val stackScale = scale * prop.scale

                // Bottom large parcel
                drawScope.drawRoundRect(
                    color = Color(0xFFB45309),
                    topLeft = Offset(baseScreen.x - stackScale * 0.35f, baseScreen.y - stackScale * 0.45f),
                    size = Size(stackScale * 0.7f, stackScale * 0.45f),
                    cornerRadius = CornerRadius(stackScale * 0.03f, stackScale * 0.03f)
                )
                // Yellow tape
                drawScope.drawRoundRect(
                    color = HoneyYellow,
                    topLeft = Offset(baseScreen.x - stackScale * 0.05f, baseScreen.y - stackScale * 0.45f),
                    size = Size(stackScale * 0.1f, stackScale * 0.45f)
                )

                // Middle medium parcel
                drawScope.drawRoundRect(
                    color = Color(0xFFD97706),
                    topLeft = Offset(baseScreen.x - stackScale * 0.26f, baseScreen.y - stackScale * 0.78f),
                    size = Size(stackScale * 0.52f, stackScale * 0.33f),
                    cornerRadius = CornerRadius(stackScale * 0.025f, stackScale * 0.025f)
                )
                // White label
                drawScope.drawRoundRect(
                    color = Color.White,
                    topLeft = Offset(baseScreen.x - stackScale * 0.18f, baseScreen.y - stackScale * 0.72f),
                    size = Size(stackScale * 0.16f, stackScale * 0.12f)
                )

                // Top small bakery / mail parcel
                drawScope.drawRoundRect(
                    color = Color(0xFFF59E0B),
                    topLeft = Offset(baseScreen.x - stackScale * 0.18f, baseScreen.y - stackScale * 1.05f),
                    size = Size(stackScale * 0.36f, stackScale * 0.27f),
                    cornerRadius = CornerRadius(stackScale * 0.02f, stackScale * 0.02f)
                )
                // Teal ribbon
                drawScope.drawRoundRect(
                    color = TealBrand,
                    topLeft = Offset(baseScreen.x - stackScale * 0.04f, baseScreen.y - stackScale * 1.05f),
                    size = Size(stackScale * 0.08f, stackScale * 0.27f)
                )
            }
            PropType.TRAFFIC_LIGHT -> {
                // 3D Traffic Light Post
                val postW = scale * 0.08f * prop.scale
                val postH = scale * 1.4f * prop.scale

                // Black pole
                drawScope.drawLine(
                    color = Color(0xFF334155),
                    start = Offset(baseScreen.x, baseScreen.y),
                    end = Offset(baseScreen.x, baseScreen.y - postH),
                    strokeWidth = postW
                )

                // Signal box housing
                val boxW = scale * 0.28f * prop.scale
                val boxH = scale * 0.68f * prop.scale
                drawScope.drawRoundRect(
                    color = Color(0xFF0F172A),
                    topLeft = Offset(baseScreen.x - boxW * 0.5f, baseScreen.y - postH),
                    size = Size(boxW, boxH),
                    cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
                )

                // Red, Yellow, Green signals
                val lightRadius = boxW * 0.32f
                val lightX = baseScreen.x
                val signalPhase = (System.currentTimeMillis() / 4000L) % 3

                val redColor = if (signalPhase == 0L) Color(0xFFFF1744) else Color(0x33FF1744)
                val yellowColor = if (signalPhase == 1L) Color(0xFFFFEA00) else Color(0x33FFEA00)
                val greenColor = if (signalPhase == 2L) Color(0xFF00E676) else Color(0x3300E676)

                drawScope.drawCircle(redColor, radius = lightRadius, center = Offset(lightX, baseScreen.y - postH + boxH * 0.22f))
                drawScope.drawCircle(yellowColor, radius = lightRadius, center = Offset(lightX, baseScreen.y - postH + boxH * 0.50f))
                drawScope.drawCircle(greenColor, radius = lightRadius, center = Offset(lightX, baseScreen.y - postH + boxH * 0.78f))
            }
        }
    }
}

class NpcVehicleRenderable(val v: NpcVehicle, override val depth: Float) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val baseScreen = camera.project(v.x, 0f, v.z, w, h)
        if (!baseScreen.isVisible) return

        val scale = (h * 0.65f / depth).coerceIn(6f, 110f)

        // Contact Shadow
        drawScope.drawOval(
            color = Color(0x60000000),
            topLeft = Offset(baseScreen.x - scale * 0.45f, baseScreen.y - scale * 0.1f),
            size = Size(scale * 0.9f, scale * 0.25f)
        )

        // Vehicle Chassis
        drawScope.drawRoundRect(
            color = v.color,
            topLeft = Offset(baseScreen.x - scale * 0.38f, baseScreen.y - scale * 0.42f),
            size = Size(scale * 0.76f, scale * 0.34f),
            cornerRadius = CornerRadius(scale * 0.08f, scale * 0.08f)
        )

        // Roof / Cabin
        drawScope.drawRoundRect(
            color = v.color.copy(alpha = 0.85f),
            topLeft = Offset(baseScreen.x - scale * 0.24f, baseScreen.y - scale * 0.68f),
            size = Size(scale * 0.48f, scale * 0.3f),
            cornerRadius = CornerRadius(scale * 0.06f, scale * 0.06f)
        )

        // Windshield
        drawScope.drawRoundRect(
            color = Color(0xAA93C5FD),
            topLeft = Offset(baseScreen.x - scale * 0.18f, baseScreen.y - scale * 0.64f),
            size = Size(scale * 0.36f, scale * 0.18f),
            cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
        )

        // Wheels
        drawScope.drawCircle(Color(0xFF1E293B), radius = scale * 0.12f, center = Offset(baseScreen.x - scale * 0.24f, baseScreen.y - scale * 0.1f))
        drawScope.drawCircle(Color(0xFF1E293B), radius = scale * 0.12f, center = Offset(baseScreen.x + scale * 0.24f, baseScreen.y - scale * 0.1f))
    }
}

class PedestrianRenderable(val ped: NpcPedestrian, override val depth: Float) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val baseScreen = camera.project(ped.x, 0f, ped.z, w, h)
        if (!baseScreen.isVisible) return

        val scale = (h * 0.45f / depth).coerceIn(4f, 60f)

        // Little shadow
        drawScope.drawOval(
            Color(0x40000000),
            topLeft = Offset(baseScreen.x - scale * 0.18f, baseScreen.y - scale * 0.06f),
            size = Size(scale * 0.36f, scale * 0.12f)
        )

        // Cute Animal Body
        val hopOffset = kotlin.math.abs(sin(ped.walkPhase)) * scale * 0.15f
        drawScope.drawCircle(
            color = Color(0xFFFDBA74),
            radius = scale * 0.22f,
            center = Offset(baseScreen.x, baseScreen.y - scale * 0.4f - hopOffset)
        )
        // Little ears
        drawScope.drawCircle(
            color = Color(0xFFEA580C),
            radius = scale * 0.08f,
            center = Offset(baseScreen.x - scale * 0.14f, baseScreen.y - scale * 0.58f - hopOffset)
        )
        drawScope.drawCircle(
            color = Color(0xFFEA580C),
            radius = scale * 0.08f,
            center = Offset(baseScreen.x + scale * 0.14f, baseScreen.y - scale * 0.58f - hopOffset)
        )
    }
}

class WaypointRenderable(
    val x: Float,
    val z: Float,
    val color: Color,
    val label: String,
    val category: DeliveryCategory = DeliveryCategory.PARCEL,
    val isPickup: Boolean = true,
    override val depth: Float
) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val baseScreen = camera.project(x, 0f, z, w, h)
        if (!baseScreen.isVisible) return

        val topScreen = camera.project(x, 16f, z, w, h)
        val scale = (h * 0.65f / depth).coerceIn(10f, 160f)

        // 1. Concentric Hologram Pulse Rings on Asphalt
        val pulseRingRadius = scale * (0.5f + sin(pulse) * 0.25f)
        drawScope.drawCircle(
            color = color.copy(alpha = 0.45f),
            radius = pulseRingRadius,
            center = Offset(baseScreen.x, baseScreen.y),
            style = Stroke(width = scale * 0.08f)
        )
        drawScope.drawCircle(
            color = color.copy(alpha = 0.85f),
            radius = scale * 0.22f,
            center = Offset(baseScreen.x, baseScreen.y)
        )
        // Golden Paw Stencil Emblem in center of waypoint
        drawScope.drawCircle(color = Color.White, radius = scale * 0.08f, center = Offset(baseScreen.x, baseScreen.y))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.03f, center = Offset(baseScreen.x - scale * 0.07f, baseScreen.y - scale * 0.06f))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.035f, center = Offset(baseScreen.x, baseScreen.y - scale * 0.09f))
        drawScope.drawCircle(color = Color.White, radius = scale * 0.03f, center = Offset(baseScreen.x + scale * 0.07f, baseScreen.y - scale * 0.06f))

        // 2. Translucent 3D Light Column
        if (topScreen.isVisible) {
            val beamPath = Path().apply {
                moveTo(baseScreen.x - scale * 0.35f, baseScreen.y)
                lineTo(baseScreen.x + scale * 0.35f, baseScreen.y)
                lineTo(topScreen.x + scale * 0.15f, topScreen.y)
                lineTo(topScreen.x - scale * 0.15f, topScreen.y)
                close()
            }
            drawScope.drawPath(beamPath, color = color.copy(alpha = 0.24f))
        }

        // 3. Floating 3D Delivery Item Model (Bobbing & Rotating)
        val bobOffset = sin(pulse * 2.5f) * scale * 0.15f
        val itemCenterY = baseScreen.y - scale * 1.3f + bobOffset
        val itemCenterX = baseScreen.x
        val rotPhase = pulse * 1.8f

        if (category == DeliveryCategory.NEWSPAPER) {
            // 3D ROLLED NEWSPAPER BUNDLE ("The Daily Purr")
            val rollW = scale * 0.75f
            val rollH = scale * 0.35f

            // Main Paper Roll Body (Cream/White)
            drawScope.drawRoundRect(
                color = Color(0xFFFFFBEB),
                topLeft = Offset(itemCenterX - rollW * 0.5f, itemCenterY - rollH * 0.5f),
                size = Size(rollW, rollH),
                cornerRadius = CornerRadius(rollH * 0.5f, rollH * 0.5f)
            )
            // Roll underside shadow
            drawScope.drawRoundRect(
                color = Color(0xFFE2E8F0),
                topLeft = Offset(itemCenterX - rollW * 0.5f, itemCenterY),
                size = Size(rollW, rollH * 0.5f),
                cornerRadius = CornerRadius(rollH * 0.5f, rollH * 0.5f)
            )
            // Printed headline stripes
            drawScope.drawLine(
                color = Color(0xFF1E293B),
                start = Offset(itemCenterX - rollW * 0.38f, itemCenterY - rollH * 0.18f),
                end = Offset(itemCenterX + rollW * 0.38f, itemCenterY - rollH * 0.18f),
                strokeWidth = scale * 0.04f
            )
            drawScope.drawLine(
                color = Color(0xFF64748B),
                start = Offset(itemCenterX - rollW * 0.32f, itemCenterY - rollH * 0.05f),
                end = Offset(itemCenterX + rollW * 0.32f, itemCenterY - rollH * 0.05f),
                strokeWidth = scale * 0.025f
            )
            // Royal Blue Tied Ribbon Band wrapped around center
            val ribbonColor = Color(0xFF2563EB)
            drawScope.drawRoundRect(
                color = ribbonColor,
                topLeft = Offset(itemCenterX - scale * 0.08f, itemCenterY - rollH * 0.55f),
                size = Size(scale * 0.16f, rollH * 1.1f),
                cornerRadius = CornerRadius(scale * 0.02f, scale * 0.02f)
            )
            // Ribbon Bow Knot & Ears
            drawScope.drawCircle(ribbonColor, radius = scale * 0.08f, center = Offset(itemCenterX - scale * 0.09f, itemCenterY - rollH * 0.6f))
            drawScope.drawCircle(ribbonColor, radius = scale * 0.08f, center = Offset(itemCenterX + scale * 0.09f, itemCenterY - rollH * 0.6f))
            drawScope.drawCircle(HoneyYellow, radius = scale * 0.04f, center = Offset(itemCenterX, itemCenterY - rollH * 0.6f))
        } else {
            // 3D CARDBOARD PARCEL BOX
            val boxW = scale * 0.7f
            val boxH = scale * 0.6f

            // Front/Center Box Face (Kraft Cardboard)
            drawScope.drawRoundRect(
                color = Color(0xFFD97706),
                topLeft = Offset(itemCenterX - boxW * 0.5f, itemCenterY - boxH * 0.4f),
                size = Size(boxW, boxH * 0.8f),
                cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
            )
            // Top Flap perspective trapezoid
            val topFlap = Path().apply {
                moveTo(itemCenterX - boxW * 0.5f, itemCenterY - boxH * 0.4f)
                lineTo(itemCenterX - boxW * 0.35f, itemCenterY - boxH * 0.75f)
                lineTo(itemCenterX + boxW * 0.35f, itemCenterY - boxH * 0.75f)
                lineTo(itemCenterX + boxW * 0.5f, itemCenterY - boxH * 0.4f)
                close()
            }
            drawScope.drawPath(topFlap, color = Color(0xFFF59E0B))

            // Bright Yellow Fragile / Courier Packing Tape across box
            drawScope.drawRoundRect(
                color = HoneyYellow,
                topLeft = Offset(itemCenterX - scale * 0.08f, itemCenterY - boxH * 0.75f),
                size = Size(scale * 0.16f, boxH * 1.15f)
            )

            // Stamped Black Cat Paw Print on Front Face
            drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.07f, center = Offset(itemCenterX, itemCenterY))
            drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.025f, center = Offset(itemCenterX - scale * 0.06f, itemCenterY - scale * 0.07f))
            drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.03f, center = Offset(itemCenterX, itemCenterY - scale * 0.09f))
            drawScope.drawCircle(color = Color(0xFF1E293B), radius = scale * 0.025f, center = Offset(itemCenterX + scale * 0.06f, itemCenterY - scale * 0.07f))

            // White Postage Shipping Label with Barcode
            drawScope.drawRoundRect(
                color = Color.White,
                topLeft = Offset(itemCenterX - boxW * 0.45f, itemCenterY - scale * 0.15f),
                size = Size(scale * 0.22f, scale * 0.28f),
                cornerRadius = CornerRadius(scale * 0.02f, scale * 0.02f)
            )
            drawScope.drawLine(Color.Black, Offset(itemCenterX - boxW * 0.42f, itemCenterY - scale * 0.08f), Offset(itemCenterX - boxW * 0.26f, itemCenterY - scale * 0.08f), strokeWidth = 1.5f)
            drawScope.drawLine(Color.Black, Offset(itemCenterX - boxW * 0.42f, itemCenterY - scale * 0.02f), Offset(itemCenterX - boxW * 0.26f, itemCenterY - scale * 0.02f), strokeWidth = 1.5f)
        }

        // 4. Orbiting Sparkle Star Particles
        for (i in 0..3) {
            val starAngle = rotPhase + i * (PI.toFloat() / 2f)
            val starX = itemCenterX + cos(starAngle) * scale * 0.58f
            val starY = itemCenterY + sin(starAngle) * scale * 0.35f
            drawScope.drawCircle(color = HoneyYellow, radius = scale * 0.045f, center = Offset(starX, starY))
            drawScope.drawCircle(color = Color.White, radius = scale * 0.02f, center = Offset(starX, starY))
        }

        // 5. Floating Destination Marker Badge
        val badgeY = itemCenterY - scale * 0.85f
        drawScope.drawRoundRect(
            color = Color(0xEE0F172A),
            topLeft = Offset(itemCenterX - scale * 1.1f, badgeY - scale * 0.26f),
            size = Size(scale * 2.2f, scale * 0.52f),
            cornerRadius = CornerRadius(scale * 0.12f, scale * 0.12f)
        )
        drawScope.drawRoundRect(
            color = color,
            topLeft = Offset(itemCenterX - scale * 1.1f, badgeY - scale * 0.26f),
            size = Size(scale * 2.2f, scale * 0.52f),
            cornerRadius = CornerRadius(scale * 0.12f, scale * 0.12f),
            style = Stroke(width = scale * 0.04f)
        )
    }
}

class GasStationRenderable(
    val x: Float,
    val z: Float,
    val name: String,
    override val depth: Float
) : Renderable3D {
    override fun draw(drawScope: DrawScope, camera: Camera3D, weather: WeatherCondition, pulse: Float, w: Float, h: Float) {
        val baseScreen = camera.project(x, 0f, z, w, h)
        if (!baseScreen.isVisible) return

        val scale = (h * 0.65f / depth).coerceIn(8f, 120f)

        // Gas Pump Canopy
        drawScope.drawRoundRect(
            color = Color(0xFFF59E0B),
            topLeft = Offset(baseScreen.x - scale * 0.6f, baseScreen.y - scale * 1.1f),
            size = Size(scale * 1.2f, scale * 0.25f),
            cornerRadius = CornerRadius(scale * 0.06f, scale * 0.06f)
        )
        // Canopy Posts
        drawScope.drawLine(Color(0xFFCBD5E1), Offset(baseScreen.x - scale * 0.45f, baseScreen.y), Offset(baseScreen.x - scale * 0.45f, baseScreen.y - scale * 0.9f), strokeWidth = scale * 0.08f)
        drawScope.drawLine(Color(0xFFCBD5E1), Offset(baseScreen.x + scale * 0.45f, baseScreen.y), Offset(baseScreen.x + scale * 0.45f, baseScreen.y - scale * 0.9f), strokeWidth = scale * 0.08f)

        // Fuel Pump Unit
        drawScope.drawRoundRect(
            color = Color(0xFFEF4444),
            topLeft = Offset(baseScreen.x - scale * 0.18f, baseScreen.y - scale * 0.5f),
            size = Size(scale * 0.36f, scale * 0.5f),
            cornerRadius = CornerRadius(scale * 0.04f, scale * 0.04f)
        )
        // Fuel Icon Box
        drawScope.drawCircle(Color.White, radius = scale * 0.08f, center = Offset(baseScreen.x, baseScreen.y - scale * 0.32f))
    }
}
