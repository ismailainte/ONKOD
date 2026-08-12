package com.somalikeyboard.app.keyboard.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

private const val SAMPLE_RATE = 44100
private const val BEEP_DURATION_MS = 40
private const val BEEP_FREQUENCY_HZ = 1500.0
private const val BEEP_PEAK_GAIN = 0.05
private const val BEEP_TAIL_GAIN = 0.0005
private const val BEEP_DECAY_WINDOW_S = 0.035
private const val VIBRATE_DURATION_MS = 8L

/**
 * Per-keypress feedback: an 8ms vibration and/or a short synthesized click, matching
 * `feedback()` in the original design (`navigator.vibrate(8)` + a 1500Hz square-wave
 * WebAudio oscillator that decays from gain 0.05 to 0.0005 over 35ms).
 */
class FeedbackPlayer(context: Context) {

    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= 31) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        manager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    private val beepSamples: ShortArray by lazy { generateBeep() }
    private var audioTrack: AudioTrack? = null

    fun trigger(haptics: Boolean, sound: Boolean) {
        if (haptics) vibrateShort()
        if (sound) beep()
    }

    private fun vibrateShort() {
        val v = vibrator ?: return
        try {
            v.vibrate(VibrationEffect.createOneShot(VIBRATE_DURATION_MS, VibrationEffect.DEFAULT_AMPLITUDE))
        } catch (_: Exception) {
            // Vibrator hardware missing/denied — silently ignore, as the source design does.
        }
    }

    private fun beep() {
        try {
            val track = ensureAudioTrack()
            track.stop()
            track.reloadStaticData()
            track.play()
        } catch (_: Exception) {
            // No audio output available — ignore.
        }
    }

    private fun ensureAudioTrack(): AudioTrack {
        audioTrack?.let { return it }
        val bytes = beepSamples.size * 2
        val track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        track.write(beepSamples, 0, beepSamples.size)
        audioTrack = track
        return track
    }

    private fun generateBeep(): ShortArray {
        val sampleCount = SAMPLE_RATE * BEEP_DURATION_MS / 1000
        val tau = BEEP_DECAY_WINDOW_S / ln(BEEP_PEAK_GAIN / BEEP_TAIL_GAIN)
        val buffer = ShortArray(sampleCount)
        for (i in 0 until sampleCount) {
            val t = i.toDouble() / SAMPLE_RATE
            val square = if (sin(2 * PI * BEEP_FREQUENCY_HZ * t) >= 0) 1.0 else -1.0
            val envelope = BEEP_PEAK_GAIN * exp(-t / tau)
            buffer[i] = (square * envelope * Short.MAX_VALUE).toInt().toShort()
        }
        return buffer
    }

    fun release() {
        audioTrack?.release()
        audioTrack = null
    }
}
