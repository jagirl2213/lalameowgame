package com.example.game.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

/**
 * Procedural low-latency real-time audio & haptic engine for Lalameow Rider.
 * Generates custom synthesized sounds without requiring external audio assets.
 */
class GameSoundEngine(private val context: Context) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private var engineJob: Job? = null
    private var isMuted = false
    private var isHapticsEnabled = true

    private val vibrator: Vibrator? by lazy {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            manager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    }

    // Engine sound state
    @Volatile private var targetEngineFreq = 70f
    @Volatile private var engineThrottle = 0f
    @Volatile private var isEngineRunning = false

    var isSoundEnabled: Boolean
        get() = !isMuted
        set(value) {
            isMuted = !value
        }

    fun setMuted(muted: Boolean) {
        isMuted = muted
    }

    fun setHapticsEnabled(enabled: Boolean) {
        isHapticsEnabled = enabled
    }

    fun triggerHaptic(type: HapticType) {
        if (!isHapticsEnabled) return
        try {
            when (type) {
                HapticType.LIGHT_CLICK -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(18)
                    }
                }
                HapticType.THROTTLE_RUMBLE -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(30, 80))
                    }
                }
                HapticType.NOTIFICATION -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 40, 60, 50), intArrayOf(0, 180, 0, 240), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(100)
                    }
                }
                HapticType.SUCCESS -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 30, 40, 50, 40, 80), -1))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(150)
                    }
                }
                HapticType.BUMP -> {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(60, 220))
                    }
                }
            }
        } catch (_: Exception) {
            // Ignore haptic failures
        }
    }

    /**
     * Start procedural continuous engine purr loop
     */
    fun startEngine() {
        if (engineJob != null && engineJob?.isActive == true) return
        isEngineRunning = true
        engineJob = scope.launch {
            val sampleRate = 22050
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )
            val bufferSize = (minBufferSize * 2).coerceAtLeast(1024)
            var audioTrack: AudioTrack? = null

            try {
                audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()

                audioTrack.play()

                val buffer = ShortArray(512)
                var phase = 0.0
                var currentFreq = 70.0

                while (isActive && isEngineRunning) {
                    if (isMuted) {
                        buffer.fill(0)
                    } else {
                        // Smoothly lerp towards target frequency
                        currentFreq += (targetEngineFreq.toDouble() - currentFreq) * 0.1
                        val amp = (1200.0 + (engineThrottle.toDouble() * 3200.0)).coerceIn(800.0, 4500.0)

                        for (i in buffer.indices) {
                            // Cute playful 2-stroke scooter harmonic timbre
                            val wave1 = sin(phase)
                            val wave2 = 0.45 * sin(phase * 2.0)
                            val wave3 = 0.25 * sin(phase * 3.0)
                            buffer[i] = ((wave1 + wave2 + wave3) * amp).toInt().toShort()

                            phase += 2.0 * PI * currentFreq / sampleRate
                            if (phase >= 2.0 * PI) phase -= 2.0 * PI
                        }
                    }
                    audioTrack.write(buffer, 0, buffer.size)
                }
            } catch (_: Exception) {
                // Audio track fallback
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun updateEnginePitch(speedRatio: Float, isAccelerating: Boolean) {
        engineThrottle = if (isAccelerating) 1f else 0.2f
        targetEngineFreq = 70f + (speedRatio.coerceIn(0f, 1f) * 140f)
    }

    fun stopEngine() {
        isEngineRunning = false
        engineJob?.cancel()
        engineJob = null
    }

    /**
     * Play cute dual-tone scooter horn ("Beep-Beep!")
     */
    fun playHorn() {
        if (isMuted) return
        triggerHaptic(HapticType.LIGHT_CLICK)
        scope.launch {
            playToneSequence(
                listOf(
                    Tone(620f, 90, 0.6f),
                    Tone(0f, 30, 0f),
                    Tone(780f, 130, 0.7f)
                )
            )
        }
    }

    /**
     * Play coin reward chime
     */
    fun playCoinReward() {
        if (isMuted) return
        triggerHaptic(HapticType.SUCCESS)
        scope.launch {
            playToneSequence(
                listOf(
                    Tone(523.25f, 60, 0.4f), // C5
                    Tone(659.25f, 60, 0.5f), // E5
                    Tone(783.99f, 60, 0.6f), // G5
                    Tone(1046.50f, 180, 0.8f) // C6
                )
            )
        }
    }

    /**
     * Play cute cat meow / chirp
     */
    fun playCatMeow() {
        if (isMuted) return
        triggerHaptic(HapticType.LIGHT_CLICK)
        scope.launch {
            val sampleRate = 22050
            val durationMs = 280
            val totalSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val pcm = ShortArray(totalSamples)

            var phase = 0.0
            for (i in 0 until totalSamples) {
                val t = i.toDouble() / totalSamples
                // Pitch rises then dips: 650Hz -> 880Hz -> 540Hz
                val freq = 620.0 + (320.0 * sin(t * PI))
                val env = sin(t * PI).coerceIn(0.0, 1.0)
                val sample = sin(phase) * 5000.0 * env
                pcm[i] = sample.toInt().toShort()

                phase += 2.0 * PI * freq / sampleRate
                if (phase >= 2.0 * PI) phase -= 2.0 * PI
            }
            playPcmBuffer(pcm, sampleRate)
        }
    }

    /**
     * Delivery complete celebration fanfare
     */
    fun playDeliveryComplete() {
        if (isMuted) return
        triggerHaptic(HapticType.SUCCESS)
        scope.launch {
            playToneSequence(
                listOf(
                    Tone(523.25f, 100, 0.5f), // C5
                    Tone(659.25f, 100, 0.6f), // E5
                    Tone(783.99f, 100, 0.7f), // G5
                    Tone(1046.50f, 250, 0.85f), // C6
                    Tone(880.00f, 120, 0.65f), // A5
                    Tone(1174.66f, 380, 0.95f)  // D6!
                )
            )
        }
    }

    /**
     * Play fuel pump tick / bell ding
     */
    fun playFuelDing() {
        if (isMuted) return
        triggerHaptic(HapticType.LIGHT_CLICK)
        scope.launch {
            playToneSequence(
                listOf(
                    Tone(1200f, 40, 0.3f),
                    Tone(1600f, 120, 0.5f)
                )
            )
        }
    }

    /**
     * Phone notification chime
     */
    fun playNotification() {
        if (isMuted) return
        triggerHaptic(HapticType.NOTIFICATION)
        scope.launch {
            playToneSequence(
                listOf(
                    Tone(880f, 70, 0.4f),
                    Tone(1320f, 110, 0.6f)
                )
            )
        }
    }

    private fun playToneSequence(tones: List<Tone>) {
        val sampleRate = 22050
        var totalSamples = 0
        tones.forEach { totalSamples += (sampleRate * (it.durationMs / 1000.0)).toInt() }
        val pcm = ShortArray(totalSamples)

        var offset = 0
        tones.forEach { tone ->
            val count = (sampleRate * (tone.durationMs / 1000.0)).toInt()
            if (tone.freq > 0f) {
                var phase = 0.0
                for (i in 0 until count) {
                    val progress = i.toDouble() / count
                    // Attack & decay envelope
                    val envelope = if (progress < 0.1) progress / 0.1 else (1.0 - progress).coerceAtLeast(0.0)
                    val sample = sin(phase) * 12000.0 * tone.volume * envelope
                    pcm[offset + i] = sample.toInt().toShort()

                    phase += 2.0 * PI * tone.freq / sampleRate
                    if (phase >= 2.0 * PI) phase -= 2.0 * PI
                }
            }
            offset += count
        }
        playPcmBuffer(pcm, sampleRate)
    }

    private fun playPcmBuffer(pcm: ShortArray, sampleRate: Int) {
        if (pcm.isEmpty()) return
        scope.launch {
            var track: AudioTrack? = null
            try {
                track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_GAME)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(sampleRate)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(pcm.size * 2)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                if (track.state == AudioTrack.STATE_INITIALIZED) {
                    track.write(pcm, 0, pcm.size)
                    track.play()
                    val durationMs = (pcm.size * 1000L / sampleRate) + 60
                    kotlinx.coroutines.delay(durationMs)
                    track.stop()
                }
            } catch (_: Exception) {
                // Audio error catch
            } finally {
                try {
                    track?.release()
                } catch (_: Exception) {}
            }
        }
    }

    fun release() {
        stopEngine()
    }
}

data class Tone(val freq: Float, val durationMs: Int, val volume: Float)

enum class HapticType {
    LIGHT_CLICK,
    THROTTLE_RUMBLE,
    NOTIFICATION,
    SUCCESS,
    BUMP
}
