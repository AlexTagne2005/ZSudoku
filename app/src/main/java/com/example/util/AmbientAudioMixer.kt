package com.example.util

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sin

class AmbientAudioMixer {

    private val sampleRate = 22050
    private var audioTrack: AudioTrack? = null
    private var playbackJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    private val _isAmbientEnabled = MutableStateFlow(false)
    val isAmbientEnabled: StateFlow<Boolean> = _isAmbientEnabled.asStateFlow()

    private val _rainVolume = MutableStateFlow(0.5f)
    val rainVolume: StateFlow<Float> = _rainVolume.asStateFlow()

    private val _forestVolume = MutableStateFlow(0.3f)
    val forestVolume: StateFlow<Float> = _forestVolume.asStateFlow()

    private val _wavesVolume = MutableStateFlow(0.4f)
    val wavesVolume: StateFlow<Float> = _wavesVolume.asStateFlow()

    fun updateSettings(
        isEnabled: Boolean,
        rainVol: Float,
        forestVol: Float,
        wavesVol: Float
    ) {
        _rainVolume.value = rainVol.coerceIn(0f, 1f)
        _forestVolume.value = forestVol.coerceIn(0f, 1f)
        _wavesVolume.value = wavesVol.coerceIn(0f, 1f)
        _isAmbientEnabled.value = isEnabled

        if (isEnabled) {
            startPlayback()
        } else {
            stopPlayback()
        }
    }

    @Synchronized
    private fun startPlayback() {
        if (playbackJob?.isActive == true) return

        try {
            val minBufSize = AudioTrack.getMinBufferSize(
                sampleRate,
                AudioFormat.CHANNEL_OUT_MONO,
                AudioFormat.ENCODING_PCM_16BIT
            )

            audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufSize * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            audioTrack?.play()
        } catch (_: Exception) {
            return
        }

        playbackJob = scope.launch {
            val bufferSize = 1024
            val buffer = ShortArray(bufferSize)
            var sampleIndex = 0L

            var lastPink0 = 0.0
            var lastPink1 = 0.0

            while (_isAmbientEnabled.value) {
                val rain = _rainVolume.value
                val forest = _forestVolume.value
                val waves = _wavesVolume.value

                for (i in 0 until bufferSize) {
                    val t = (sampleIndex + i) / sampleRate.toDouble()

                    // Rain synthesis: filtered pink noise
                    val white = (Math.random() * 2.0 - 1.0)
                    lastPink0 = (lastPink0 + 0.02 * white) / 1.02
                    val rainSample = lastPink0 * rain

                    // Waves synthesis: LFO modulated low swell
                    val waveLfo = (sin(2.0 * Math.PI * 0.08 * t) + 1.0) / 2.0 // 0.08 Hz tide swell
                    val waveNoise = (Math.random() * 2.0 - 1.0) * waveLfo * waveLfo
                    val waveSample = waveNoise * waves

                    // Forest synthesis: gentle breeze + soft bird harmonics
                    val breezeLfo = (sin(2.0 * Math.PI * 0.15 * t) + 1.0) / 2.0
                    lastPink1 = (lastPink1 + 0.05 * white) / 1.05
                    val breeze = lastPink1 * breezeLfo

                    val birdChimeLfo = sin(2.0 * Math.PI * 0.25 * t)
                    val birdSample = if (birdChimeLfo > 0.985) {
                        sin(2.0 * Math.PI * 1800.0 * t) * 0.15
                    } else 0.0

                    val forestSample = (breeze + birdSample) * forest

                    val mixed = (rainSample + waveSample + forestSample).coerceIn(-1.0, 1.0)
                    buffer[i] = (mixed * 12000).toInt().toShort()
                }

                sampleIndex += bufferSize
                audioTrack?.write(buffer, 0, bufferSize)
            }
        }
    }

    @Synchronized
    fun stopPlayback() {
        playbackJob?.cancel()
        playbackJob = null
        try {
            audioTrack?.stop()
            audioTrack?.release()
        } catch (_: Exception) {}
        audioTrack = null
    }
}
