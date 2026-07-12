package com.example.service

import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

class SoundSynthesizer {
    private var audioTrack: AudioTrack? = null
    private var synthJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    @Volatile
    private var currentVolume = 0.5f

    @Volatile
    private var currentSoundType = "White Noise"

    fun setVolume(volume: Float) {
        currentVolume = volume.coerceIn(0.0f, 1.0f)
        try {
            audioTrack?.let { track ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    track.setVolume(currentVolume)
                } else {
                    @Suppress("DEPRECATION")
                    track.setStereoVolume(currentVolume, currentVolume)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundSynthesizer", "Error setting volume", e)
        }
    }

    fun start(soundType: String, volume: Float = 0.5f) {
        stop()
        currentSoundType = soundType
        currentVolume = volume

        synthJob = scope.launch {
            runSynthLoop()
        }
    }

    fun stop() {
        synthJob?.cancel()
        synthJob = null
        try {
            audioTrack?.let { track ->
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.stop()
                }
                track.release()
            }
            audioTrack = null
        } catch (e: Exception) {
            Log.e("SoundSynthesizer", "Error stopping track", e)
        }
    }

    private fun runSynthLoop() {
        val sampleRate = 44100
        val channelConfig = AudioFormat.CHANNEL_OUT_STEREO
        val audioFormat = AudioFormat.ENCODING_PCM_16BIT
        val minBufferSize = AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat)
        val bufferSize = (minBufferSize * 2).coerceAtLeast(8192)

        try {
            audioTrack = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                AudioTrack.Builder()
                    .setAudioAttributes(
                        android.media.AudioAttributes.Builder()
                            .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                            .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(audioFormat)
                            .setSampleRate(sampleRate)
                            .setChannelMask(channelConfig)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize)
                    .setTransferMode(AudioTrack.MODE_STREAM)
                    .build()
            } else {
                @Suppress("DEPRECATION")
                AudioTrack(
                    AudioManager.STREAM_MUSIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize,
                    AudioTrack.MODE_STREAM
                )
            }

            audioTrack?.play()
            audioTrack?.let { track ->
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    track.setVolume(currentVolume)
                } else {
                    @Suppress("DEPRECATION")
                    track.setStereoVolume(currentVolume, currentVolume)
                }
            }
        } catch (e: Exception) {
            Log.e("SoundSynthesizer", "Initialization failed", e)
            return
        }

        val buffer = ShortArray(4096) // 2048 stereo frames
        var phase = 0.0
        var phase2 = 0.0
        var filterVal = 0.0
        
        // Pink noise generator states (Voss-McCartney algorithm)
        val pinkRows = DoubleArray(12)
        var pinkRunningSum = 0.0
        val random = Random(System.currentTimeMillis())

        // Ocean waves state (modulates amplitude over 6-8 seconds)
        var oceanTime = 0.0

        // Fireplace crackle state
        var fireplacePopTimer = 0

        while (synthJob?.isActive == true) {
            val type = currentSoundType
            
            for (i in 0 until buffer.size step 2) {
                var leftVal = 0.0
                var rightVal = 0.0

                when (type) {
                    "White Noise" -> {
                        leftVal = random.nextDouble(-1.0, 1.0)
                        rightVal = random.nextDouble(-1.0, 1.0)
                    }

                    "Brown Noise" -> {
                        // Brownian noise: integrate white noise with leaky integration
                        val whiteLeft = random.nextDouble(-1.0, 1.0)
                        val whiteRight = random.nextDouble(-1.0, 1.0)
                        
                        // We use a simple 1st-order low-pass filter (leaky integration)
                        filterVal = 0.992 * filterVal + 0.008 * ((whiteLeft + whiteRight) * 0.5)
                        leftVal = filterVal * 5.0 // scale to raise volume
                        rightVal = filterVal * 5.0
                    }

                    "Pink Noise" -> {
                        // Voss-McCartney algorithm for pink noise approximation
                        val r = random.nextInt()
                        var index = 0
                        while (index < 12 && (r and (1 shl index)) != 0) {
                            index++
                        }
                        if (index < 12) {
                            pinkRunningSum -= pinkRows[index]
                            pinkRows[index] = random.nextDouble(-0.1, 0.1)
                            pinkRunningSum += pinkRows[index]
                        }
                        val white = random.nextDouble(-0.1, 0.1)
                        val pink = pinkRunningSum + white
                        leftVal = pink * 4.0
                        rightVal = pink * 4.0
                    }

                    "Binaural Beats" -> {
                        // Left channel: 200 Hz, Right channel: 210 Hz -> 10 Hz alpha beat
                        val freqLeft = 200.0
                        val freqRight = 210.0
                        leftVal = sin(phase * freqLeft) * 0.5
                        rightVal = sin(phase2 * freqRight) * 0.5

                        val stepLeft = 2.0 * PI / sampleRate
                        val stepRight = 2.0 * PI / sampleRate
                        phase += stepLeft
                        phase2 += stepRight
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                        if (phase2 > 2.0 * PI) phase2 -= 2.0 * PI
                    }

                    "Isochronic Tones" -> {
                        // 150 Hz carrier tone, pulsed at 10 Hz (alpha wave)
                        val carrierFreq = 150.0
                        val pulseFreq = 10.0
                        
                        val carrier = sin(phase * carrierFreq)
                        // Square pulse amplitude modulation
                        val pulse = sin(phase * pulseFreq)
                        val amp = if (pulse >= 0) 0.6 else 0.05
                        
                        val value = carrier * amp
                        leftVal = value
                        rightVal = value

                        val step = 2.0 * PI / sampleRate
                        phase += step
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    "Rain" -> {
                        // Pink noise base + soft filtered rain droplet pops
                        val r = random.nextInt()
                        var index = 0
                        while (index < 12 && (r and (1 shl index)) != 0) {
                            index++
                        }
                        if (index < 12) {
                            pinkRunningSum -= pinkRows[index]
                            pinkRows[index] = random.nextDouble(-0.1, 0.1)
                            pinkRunningSum += pinkRows[index]
                        }
                        val basePink = pinkRunningSum * 2.0
                        
                        var dropNoise = 0.0
                        if (random.nextDouble() < 0.0003) {
                            // simulate a drop splash
                            dropNoise = random.nextDouble(-0.8, 0.8)
                        }
                        
                        leftVal = basePink * 0.4 + dropNoise * 0.2
                        rightVal = basePink * 0.4 + dropNoise * 0.2
                    }

                    "Ocean" -> {
                        // Wave amplitude modulation
                        val r = random.nextInt()
                        var index = 0
                        while (index < 12 && (r and (1 shl index)) != 0) {
                            index++
                        }
                        if (index < 12) {
                            pinkRunningSum -= pinkRows[index]
                            pinkRows[index] = random.nextDouble(-0.1, 0.1)
                            pinkRunningSum += pinkRows[index]
                        }
                        val basePink = pinkRunningSum * 2.5
                        
                        // Modulate wave volume slowly over 8 seconds
                        oceanTime += 1.0 / sampleRate
                        val waveMod = (sin(2.0 * PI * oceanTime / 8.0) + 1.0) * 0.5 // 0.0 to 1.0
                        
                        leftVal = basePink * (0.1 + waveMod * 0.6)
                        rightVal = basePink * (0.1 + waveMod * 0.6)
                    }

                    "Fireplace" -> {
                        // Brown noise base + random crackling pops
                        val white = random.nextDouble(-1.0, 1.0)
                        filterVal = 0.992 * filterVal + 0.008 * white
                        val baseBrown = filterVal * 3.0

                        var crackle = 0.0
                        fireplacePopTimer--
                        if (fireplacePopTimer <= 0) {
                            if (random.nextDouble() < 0.0005) {
                                crackle = random.nextDouble(-0.9, 0.9)
                                fireplacePopTimer = random.nextInt(500, 3000) // dead time
                            }
                        }

                        leftVal = baseBrown * 0.5 + crackle * 0.4
                        rightVal = baseBrown * 0.5 + crackle * 0.4
                    }

                    "Piano" -> {
                        // Procedural ambient pad mimicking soft synthesizer chords
                        // Combination of low frequency sine waves: 110Hz (A2), 130.8Hz (C3), 164.8Hz (E3), 196Hz (G3)
                        val freq1 = 110.0
                        val freq2 = 130.81
                        val freq3 = 164.81
                        val freq4 = 196.00

                        oceanTime += 1.0 / sampleRate
                        // Slow LFO for chords modulation
                        val lfo1 = (sin(2.0 * PI * oceanTime / 12.0) + 1.0) * 0.5
                        val lfo2 = (sin(2.0 * PI * (oceanTime + 3.0) / 16.0) + 1.0) * 0.5

                        val v1 = sin(phase * freq1) * lfo1 * 0.2
                        val v2 = sin(phase * freq2) * (1.0 - lfo1) * 0.2
                        val v3 = sin(phase2 * freq3) * lfo2 * 0.15
                        val v4 = sin(phase2 * freq4) * (1.0 - lfo2) * 0.15

                        leftVal = v1 + v3
                        rightVal = v2 + v4

                        val step = 2.0 * PI / sampleRate
                        phase += step
                        phase2 += step * 0.999
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                        if (phase2 > 2.0 * PI) phase2 -= 2.0 * PI
                    }

                    "Forest" -> {
                        // Soft wind noise + gentle periodic cricket-like hum
                        // Wind: modulated bandpass-like noise
                        val white = random.nextDouble(-1.0, 1.0)
                        filterVal = 0.98 * filterVal + 0.02 * white
                        
                        oceanTime += 1.0 / sampleRate
                        val windMod = (sin(2.0 * PI * oceanTime / 10.0) + 1.0) * 0.5
                        val windNoise = filterVal * 1.5 * (0.2 + windMod * 0.5)

                        // Periodic high frequency pulse (cricket chirp: 3500 Hz pulsed at 4Hz)
                        val cricketFreq = 3500.0
                        val cricketPulse = sin(2.0 * PI * oceanTime * 4.0)
                        val cricketAmp = if (cricketPulse > 0.8 && sin(2.0 * PI * oceanTime / 5.0) > 0.2) 0.015 else 0.0
                        val cricketSqueak = sin(phase * cricketFreq) * cricketAmp

                        leftVal = windNoise + cricketSqueak
                        rightVal = windNoise + cricketSqueak

                        val step = 2.0 * PI / sampleRate
                        phase += step
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }

                    "Wind" -> {
                        // Blowing wind: slow, sweep-filtered white noise
                        val white = random.nextDouble(-1.0, 1.0)
                        oceanTime += 1.0 / sampleRate
                        // Frequency sweep of a lowpass filter
                        val sweep = 0.90 + 0.08 * sin(2.0 * PI * oceanTime / 14.0)
                        filterVal = sweep * filterVal + (1.0 - sweep) * white
                        
                        leftVal = filterVal * 2.0
                        rightVal = filterVal * 2.0
                    }

                    else -> {
                        // Default to quiet ambient hum
                        leftVal = sin(phase * 110.0) * 0.05
                        rightVal = sin(phase * 110.0) * 0.05
                        val step = 2.0 * PI / sampleRate
                        phase += step
                        if (phase > 2.0 * PI) phase -= 2.0 * PI
                    }
                }

                // Convert to Short PCM and write to buffer
                // Note: Multiply by a master gain factor to fit beautifully in short range
                val masterGain = 22000.0
                buffer[i] = (leftVal * masterGain).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
                buffer[i+1] = (rightVal * masterGain).coerceIn(Short.MIN_VALUE.toDouble(), Short.MAX_VALUE.toDouble()).toInt().toShort()
            }

            audioTrack?.let { track ->
                try {
                    track.write(buffer, 0, buffer.size)
                } catch (e: Exception) {
                    Log.e("SoundSynthesizer", "Error writing audio data", e)
                }
            }
        }
    }
}
