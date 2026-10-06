package com.example.service

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.example.model.AlarmTone
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundVibrationManager(private val context: Context) {

    @Suppress("DEPRECATION")
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator ?: (context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator)
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private var soundJob: Job? = null
    private var isVibrating = false
    private val scope = CoroutineScope(Dispatchers.Default)

    /**
     * Start playing the selected tone repeatedly until stopped.
     */
    fun startAlarmSound(tone: AlarmTone) {
        stopAlarmSound()
        soundJob = scope.launch {
            val sampleRate = 44100
            val minBufferSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            ).coerceAtLeast(sampleRate / 4)

            val audioTrack = try {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
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
                    .setBufferSizeInBytes(minBufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } catch (e: Exception) {
                null
            }

            try {
                audioTrack?.play()
                while (isActive) {
                    when (tone) {
                        AlarmTone.SIREN -> {
                            // Two-tone high-low siren (880Hz and 660Hz)
                            playSine(audioTrack, sampleRate, 880.0, 350)
                            if (!isActive) break
                            playSine(audioTrack, sampleRate, 660.0, 350)
                            if (!isActive) break
                        }
                        AlarmTone.URGENT -> {
                            // High-pitch urgent staccato beeps (1200Hz)
                            playSine(audioTrack, sampleRate, 1200.0, 120)
                            if (!isActive) break
                            delay(60)
                            playSine(audioTrack, sampleRate, 1200.0, 120)
                            if (!isActive) break
                            delay(250)
                        }
                        AlarmTone.SOFT -> {
                            // Melodic chime sequence (523Hz C5 -> 659Hz E5 -> 784Hz G5)
                            playSine(audioTrack, sampleRate, 523.25, 200)
                            if (!isActive) break
                            playSine(audioTrack, sampleRate, 659.25, 200)
                            if (!isActive) break
                            playSine(audioTrack, sampleRate, 783.99, 350)
                            if (!isActive) break
                            delay(400)
                        }
                    }
                }
            } catch (e: Exception) {
                // Ignore audio stream cancellation
            } finally {
                try {
                    audioTrack?.stop()
                    audioTrack?.release()
                } catch (e: Exception) {
                    // Ignore
                }
            }
        }
    }

    private suspend fun playSine(
        audioTrack: AudioTrack?,
        sampleRate: Int,
        freqHz: Double,
        durationMs: Int
    ) {
        if (audioTrack == null) {
            delay(durationMs.toLong())
            return
        }
        val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
        val buffer = ShortArray(numSamples)
        for (i in 0 until numSamples) {
            val angle = 2.0 * PI * i / (sampleRate / freqHz)
            // Apply slight envelope at edges to prevent clicking
            val factor = when {
                i < 100 -> i / 100.0
                i > numSamples - 100 -> (numSamples - i) / 100.0
                else -> 1.0
            }
            buffer[i] = (sin(angle) * Short.MAX_VALUE * 0.8 * factor).toInt().toShort()
        }
        audioTrack.write(buffer, 0, numSamples)
        delay(durationMs.toLong())
    }

    fun stopAlarmSound() {
        soundJob?.cancel()
        soundJob = null
    }

    fun startVibration() {
        if (isVibrating || vibrator == null || !vibrator.hasVibrator()) return
        isVibrating = true
        val timings = longArrayOf(0, 400, 200, 400, 200, 600, 400)
        val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255, 0)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(timings, 0)
        }
    }

    fun stopVibration() {
        isVibrating = false
        try {
            vibrator?.cancel()
        } catch (e: Exception) {
            // Ignore
        }
    }

    fun testTone(tone: AlarmTone, onFinished: () -> Unit) {
        scope.launch {
            startAlarmSound(tone)
            delay(2800)
            stopAlarmSound()
            onFinished()
        }
    }
}
