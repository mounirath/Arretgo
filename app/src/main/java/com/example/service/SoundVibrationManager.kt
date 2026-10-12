package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import com.example.model.AlarmTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

/**
 * SoundVibrationManager
 *
 * Robust, high-fidelity audio synthesis and haptic vibration engine.
 * Generates custom alarm waveforms (Siren, Urgent, Chime, Sonar, Bell) in stereo
 * with volume boosting on STREAM_MUSIC (ensures loud, unmistakable sound on emulator and devices).
 * Provides seamless fallback to system alarm ringtone if hardware AudioTrack is unavailable.
 */
class SoundVibrationManager(private val context: Context) {

    companion object {
        private const val TAG = "SoundVibrationMgr"
        private const val SAMPLE_RATE = 44100
    }

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var currentAudioTrack: AudioTrack? = null
    private var fallbackRingtone: Ringtone? = null
    private var soundJob: Job? = null
    private var isVibrating = false
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Start playing the selected tone repeatedly until stopped.
     */
    fun startAlarmSound(tone: AlarmTone) {
        stopAlarmSound()

        // 1. Ensure STREAM_MUSIC volume is audible (emulator or devices may have 0 volume)
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            audioManager?.let { am ->
                val currentVol = am.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                if (currentVol < maxVol / 2) {
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.85).toInt(), 0)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Could not adjust audio volume", e)
        }

        soundJob = scope.launch {
            try {
                val stereoSamples = generateToneBuffer(tone, SAMPLE_RATE)
                val bufferSizeBytes = stereoSamples.size * 2

                val track = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_STEREO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSizeBytes)
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                track.write(stereoSamples, 0, stereoSamples.size)
                val numFrames = stereoSamples.size / 2
                track.setLoopPoints(0, numFrames, -1) // Loop continuously
                track.setVolume(1.0f)
                track.play()

                currentAudioTrack = track
                Log.i(TAG, "AudioTrack playing tone ${tone.name} successfully (MODE_STATIC, $numFrames frames)")
            } catch (e: Exception) {
                Log.e(TAG, "AudioTrack failed to initialize, using Ringtone fallback", e)
                playFallbackRingtone()
            }
        }
    }

    private fun playFallbackRingtone() {
        try {
            val alertUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
                ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)

            if (alertUri != null) {
                val ringtone = RingtoneManager.getRingtone(context, alertUri)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    ringtone?.audioAttributes = AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                }
                ringtone?.play()
                fallbackRingtone = ringtone
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fallback ringtone failed", e)
        }
    }

    /**
     * Stop playing alarm sound immediately.
     */
    fun stopAlarmSound() {
        soundJob?.cancel()
        soundJob = null

        try {
            currentAudioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error releasing AudioTrack", e)
        }
        currentAudioTrack = null

        try {
            fallbackRingtone?.stop()
        } catch (e: Exception) {
            Log.w(TAG, "Error stopping fallback ringtone", e)
        }
        fallbackRingtone = null
    }

    /**
     * Start haptic alarm vibration pattern.
     */
    fun startVibration() {
        if (isVibrating || vibrator == null || !vibrator.hasVibrator()) return
        isVibrating = true
        val timings = longArrayOf(0, 450, 150, 450, 150, 700, 300)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, 0)
        }
    }

    /**
     * Stop haptic alarm vibration.
     */
    fun stopVibration() {
        isVibrating = false
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    /**
     * Preview tone for 3 seconds then stop.
     */
    fun testTone(tone: AlarmTone, onFinished: () -> Unit) {
        scope.launch {
            startAlarmSound(tone)
            delay(3200)
            stopAlarmSound()
            onFinished()
        }
    }

    /**
     * Synthesizes 16-bit PCM Stereo buffer for the chosen alarm tone.
     */
    private fun generateToneBuffer(tone: AlarmTone, sampleRate: Int): ShortArray {
        return when (tone) {
            AlarmTone.SIREN -> generateSirenBuffer(sampleRate)
            AlarmTone.URGENT -> generateUrgentBuffer(sampleRate)
            AlarmTone.SOFT -> generateSoftChimeBuffer(sampleRate)
            AlarmTone.RADAR -> generateRadarBuffer(sampleRate)
            AlarmTone.BELL -> generateBellBuffer(sampleRate)
        }
    }

    /**
     * Two-tone emergency siren: 880Hz (400ms) followed by 660Hz (400ms) with rich harmonics.
     */
    private fun generateSirenBuffer(sampleRate: Int): ShortArray {
        val totalDurationMs = 800
        val totalFrames = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalFrames * 2)

        val halfFrames = totalFrames / 2
        for (i in 0 until totalFrames) {
            val t = i.toDouble() / sampleRate
            val freq = if (i < halfFrames) 880.0 else 660.0

            // Base sine + 2nd harmonic for crisp alarm presence
            val base = sin(2.0 * PI * freq * t)
            val harmonic = 0.25 * sin(4.0 * PI * freq * t)
            val raw = (base + harmonic) * 0.8

            // Soft envelope at transitions
            val envelope = when {
                i < 100 -> i / 100.0
                i in (halfFrames - 50)..(halfFrames + 50) -> {
                    val dist = kotlin.math.abs(i - halfFrames)
                    (dist / 50.0).coerceIn(0.5, 1.0)
                }
                i > totalFrames - 100 -> (totalFrames - i) / 100.0
                else -> 1.0
            }

            val sample = (raw * envelope * Short.MAX_VALUE * 0.9).toInt().coerceIn(-32767, 32767).toShort()
            buffer[i * 2] = sample     // Left channel
            buffer[i * 2 + 1] = sample // Right channel
        }
        return buffer
    }

    /**
     * Rapid urgent warning beeps (1200Hz bursts): 3 quick beeps + pause.
     */
    private fun generateUrgentBuffer(sampleRate: Int): ShortArray {
        val totalDurationMs = 700
        val totalFrames = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalFrames * 2)

        for (i in 0 until totalFrames) {
            val tMs = (i.toDouble() / sampleRate) * 1000.0
            val tSec = i.toDouble() / sampleRate

            // Beep pattern: 0-100ms ON, 100-160ms OFF, 160-260ms ON, 260-320ms OFF, 320-420ms ON, 420-700ms OFF
            val isBeeping = (tMs in 0.0..100.0) || (tMs in 160.0..260.0) || (tMs in 320.0..420.0)

            if (isBeeping) {
                val wave = sin(2.0 * PI * 1250.0 * tSec) + 0.2 * sin(2.0 * PI * 2500.0 * tSec)
                val sample = (wave * 0.85 * Short.MAX_VALUE * 0.9).toInt().coerceIn(-32767, 32767).toShort()
                buffer[i * 2] = sample
                buffer[i * 2 + 1] = sample
            } else {
                buffer[i * 2] = 0
                buffer[i * 2 + 1] = 0
            }
        }
        return buffer
    }

    /**
     * Warm harmonic chime: C5 (523Hz) -> E5 (659Hz) -> G5 (784Hz) with natural decay.
     */
    private fun generateSoftChimeBuffer(sampleRate: Int): ShortArray {
        val totalDurationMs = 1300
        val totalFrames = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalFrames * 2)

        for (i in 0 until totalFrames) {
            val tSec = i.toDouble() / sampleRate
            val tMs = tSec * 1000.0

            val (freq, noteStartMs) = when {
                tMs < 280.0 -> Pair(523.25, 0.0)
                tMs < 560.0 -> Pair(659.25, 280.0)
                else -> Pair(783.99, 560.0)
            }

            val noteAgeSec = (tMs - noteStartMs) / 1000.0
            val decay = exp(-3.5 * noteAgeSec)
            val wave = (sin(2.0 * PI * freq * tSec) + 0.3 * sin(2.0 * PI * (freq * 2) * tSec)) * decay

            val sample = (wave * Short.MAX_VALUE * 0.85).toInt().coerceIn(-32767, 32767).toShort()
            buffer[i * 2] = sample
            buffer[i * 2 + 1] = sample
        }
        return buffer
    }

    /**
     * Submarine sonar radar ping: 1480Hz resonant ping with decaying reverberation echo.
     */
    private fun generateRadarBuffer(sampleRate: Int): ShortArray {
        val totalDurationMs = 1100
        val totalFrames = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalFrames * 2)

        for (i in 0 until totalFrames) {
            val tSec = i.toDouble() / sampleRate
            val tMs = tSec * 1000.0

            var sampleVal = 0.0
            if (tMs < 300.0) {
                val decay = exp(-6.0 * (tMs / 1000.0))
                sampleVal = sin(2.0 * PI * 1480.0 * tSec) * decay
            } else if (tMs in 350.0..550.0) {
                val echoAge = (tMs - 350.0) / 1000.0
                val decay = exp(-7.0 * echoAge) * 0.35
                sampleVal = sin(2.0 * PI * 1480.0 * tSec) * decay
            }

            val sample = (sampleVal * Short.MAX_VALUE * 0.95).toInt().coerceIn(-32767, 32767).toShort()
            buffer[i * 2] = sample
            buffer[i * 2 + 1] = sample
        }
        return buffer
    }

    /**
     * European train station transit chime: F#5 (740Hz) followed by D5 (587Hz).
     */
    private fun generateBellBuffer(sampleRate: Int): ShortArray {
        val totalDurationMs = 1350
        val totalFrames = (sampleRate * (totalDurationMs / 1000.0)).toInt()
        val buffer = ShortArray(totalFrames * 2)

        for (i in 0 until totalFrames) {
            val tSec = i.toDouble() / sampleRate
            val tMs = tSec * 1000.0

            val (freq, chimeStartMs) = if (tMs < 350.0) {
                Pair(739.99, 0.0)
            } else {
                Pair(587.33, 350.0)
            }

            val ageSec = (tMs - chimeStartMs) / 1000.0
            val decay = exp(-3.0 * ageSec)
            val bellSound = (sin(2.0 * PI * freq * tSec) + 0.4 * sin(2.0 * PI * (freq * 1.5) * tSec)) * decay

            val sample = (bellSound * Short.MAX_VALUE * 0.9).toInt().coerceIn(-32767, 32767).toShort()
            buffer[i * 2] = sample
            buffer[i * 2 + 1] = sample
        }
        return buffer
    }
}
