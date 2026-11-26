package com.toix.core.audio.dsp

import com.toix.core.audio.AudioSource

/**
 * DSP effect base.
 */
open class DspEffect {
    open fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        return samples // pass-through
    }

    open fun reset() {}
}

/**
 * Gain effect.
 */
class GainEffect(var gain: Float = 1.0f) : DspEffect() {
    override fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        for (i in samples.indices) {
            samples[i] *= gain
        }
        return samples
    }
}

/**
 * Low-pass filter (one-pole).
 */
class LowPassFilter(var cutoff: Float = 1000.0f) : DspEffect() {
    private var previous: Float = 0.0f

    override fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        val rc = 1.0f / (2.0f * Math.PI.toFloat() * cutoff * sampleRate.toFloat())
        val alpha = rc / (rc + 1.0f / sampleRate.toFloat())
        for (i in samples.indices) {
            previous = previous + alpha * (samples[i] - previous)
            samples[i] = previous
        }
        return samples
    }

    override fun reset() {
        previous = 0.0f
    }
}

/**
 * Reverb effect (stub).
 */
class ReverbEffect(var roomSize: Float = 0.5f, var damp: Float = 0.5f) : DspEffect() {
    override fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        // Stub - real implementation uses comb/allpass filters
        return samples
    }
}

/**
 * Audio bus with effects chain.
 */
class AudioBus(
    val name: String,
    val effects: MutableList<DspEffect> = mutableListOf(),
    var volume: Float = 1.0f,
) {
    fun process(samples: FloatArray, sampleRate: Int): FloatArray {
        var result = samples
        for (effect in effects) {
            result = effect.process(result, sampleRate)
        }
        for (i in result.indices) {
            result[i] *= volume
        }
        return result
    }
}

/**
 * Spatial audio parameters for 3D positioning.
 */
data class SpatialAudioParams(
    var listenerX: Float = 0f,
    var listenerY: Float = 0f,
    var listenerZ: Float = 0f,
    var sourceX: Float = 0f,
    var sourceY: Float = 0f,
    var sourceZ: Float = 0f,
    var minDistance: Float = 1f,
    var maxDistance: Float = 100f,
    var rolloff: Float = 1.0f, // logarithmic rolloff
) {
    fun computeVolume(): Float {
        val dx = sourceX - listenerX
        val dy = sourceY - listenerY
        val dz = sourceZ - listenerZ
        val distance = Math.sqrt(dx.toDouble() * dx + dy.toDouble() * dy + dz.toDouble() * dz).toFloat()
        if (distance <= minDistance) return 1.0f
        if (distance >= maxDistance) return 0.0f
        // Logarithmic rolloff
        return 1.0f - rolloff * Math.log(distance.toDouble() / minDistance).toFloat() /
            Math.log(maxDistance.toDouble() / minDistance).toFloat()
    }

    fun computePan(): Float {
        val dx = sourceX - listenerX
        return dx.coerceIn(-1.0f, 1.0f)
    }
}

/**
 * Audio mixer with buses and spatial support.
 */
class SpatialAudioMixer {
    val buses = mutableMapOf<String, AudioBus>()
    val spatialParams = SpatialAudioParams()

    fun getOrCreateBus(name: String): AudioBus {
        return buses.getOrPut(name) { AudioBus(name) }
    }

    fun mix(source: AudioSource, samples: FloatArray, sampleRate: Int): FloatArray {
        val volume = spatialParams.computeVolume()
        val pan = spatialParams.computePan()

        // Apply volume and pan (simple stereo pan)
        val result = FloatArray(samples.size)
        for (i in samples.indices) {
            val left = samples[i] * volume * (1.0f - pan) * 0.5f
            val right = samples[i] * volume * (1.0f + pan) * 0.5f
            result[i] = (left + right) / 2.0f // downmix to mono for simplicity
        }
        return result
    }
}
