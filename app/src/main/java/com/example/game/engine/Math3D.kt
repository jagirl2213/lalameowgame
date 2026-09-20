package com.example.game.engine

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Vector3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(other: Vector3) = Vector3(x + other.x, y + other.y, z + other.z)
    operator fun minus(other: Vector3) = Vector3(x - other.x, y - other.y, z - other.z)
    operator fun times(scalar: Float) = Vector3(x * scalar, y * scalar, z * scalar)

    fun distanceTo(other: Vector3): Float {
        val dx = x - other.x
        val dy = y - other.y
        val dz = z - other.z
        return kotlin.math.sqrt(dx * dx + dy * dy + dz * dz)
    }

    fun distance2D(otherX: Float, otherZ: Float): Float {
        val dx = x - otherX
        val dz = z - otherZ
        return kotlin.math.sqrt(dx * dx + dz * dz)
    }
}

data class ScreenPoint(
    val x: Float,
    val y: Float,
    val depth: Float,
    val isVisible: Boolean
)

class Camera3D(
    var posX: Float = 0f,
    var posY: Float = 5f,
    var posZ: Float = -7f,
    var yawDeg: Float = 0f,
    var pitchDeg: Float = 22f,
    var fov: Float = 60f
) {
    /**
     * Projects a 3D world coordinate (x, y, z) to screen (screenX, screenY, depth)
     */
    fun project(
        worldX: Float,
        worldY: Float,
        worldZ: Float,
        screenWidth: Float,
        screenHeight: Float
    ): ScreenPoint {
        // 1. Translation relative to camera
        val relX = worldX - posX
        val relY = worldY - posY
        val relZ = worldZ - posZ

        // 2. Rotate around Y axis (Yaw)
        val yawRad = (yawDeg * PI / 180.0).toFloat()
        val cosY = cos(yawRad)
        val sinY = sin(yawRad)

        val rotX = relX * cosY - relZ * sinY
        val rotZ = relX * sinY + relZ * cosY

        // 3. Rotate around X axis (Pitch: looking down by pitchDeg)
        val pitchRad = (pitchDeg * PI / 180.0).toFloat()
        val cosP = cos(pitchRad)
        val sinP = sin(pitchRad)

        val camY = relY * cosP + rotZ * sinP
        val camZ = -relY * sinP + rotZ * cosP

        // If behind camera or too close, mark invisible
        if (camZ <= 0.4f) {
            return ScreenPoint(0f, 0f, camZ, false)
        }

        // 4. Perspective projection
        val focalLength = (screenHeight * 0.5f) / kotlin.math.tan((fov * 0.5f * PI / 180.0).toFloat())
        val screenX = (screenWidth * 0.5f) + (rotX / camZ) * focalLength
        val screenY = (screenHeight * 0.5f) - (camY / camZ) * focalLength

        return ScreenPoint(screenX, screenY, camZ, true)
    }

    fun toCameraSpace(worldX: Float, worldY: Float, worldZ: Float): Vector3 {
        val relX = worldX - posX
        val relY = worldY - posY
        val relZ = worldZ - posZ

        val yawRad = (yawDeg * PI / 180.0).toFloat()
        val cosY = cos(yawRad)
        val sinY = sin(yawRad)

        val rotX = relX * cosY - relZ * sinY
        val rotZ = relX * sinY + relZ * cosY

        val pitchRad = (pitchDeg * PI / 180.0).toFloat()
        val cosP = cos(pitchRad)
        val sinP = sin(pitchRad)

        val camY = relY * cosP + rotZ * sinP
        val camZ = -relY * sinP + rotZ * cosP

        return Vector3(rotX, camY, camZ)
    }

    fun projectCameraPoint(camPoint: Vector3, screenWidth: Float, screenHeight: Float): ScreenPoint {
        if (camPoint.z <= 0.35f) {
            return ScreenPoint(0f, 0f, camPoint.z, false)
        }
        val focalLength = (screenHeight * 0.5f) / kotlin.math.tan((fov * 0.5f * PI / 180.0).toFloat())
        val screenX = (screenWidth * 0.5f) + (camPoint.x / camPoint.z) * focalLength
        val screenY = (screenHeight * 0.5f) - (camPoint.y / camPoint.z) * focalLength
        return ScreenPoint(screenX, screenY, camPoint.z, true)
    }
}
