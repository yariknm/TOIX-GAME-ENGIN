package com.toix.core.audio

import com.toix.core.Component
import com.toix.core.Actor
import com.toix.core.World

open class AudioComponent(
    var audioClip: String? = null,
    var volume: Float = 1f,
    var pitch: Float = 1f,
    var isPlaying: Boolean = false,
) : Component {
    override fun snapshot(): Component = AudioComponent(audioClip, volume, pitch, isPlaying)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}

class AudioSource(
    var clipName: String? = null,
) : AudioComponent(clipName) {
    override fun snapshot(): Component = AudioSource(clipName)
}

class AudioListener(
    var position: com.toix.core.Transform = com.toix.core.Transform(),
) : AudioComponent(null) {
    override fun snapshot(): Component = AudioListener(position)
}

class AudioMixer {
    val sources = mutableListOf<AudioSource>()

    fun play(source: AudioSource) {
        source.isPlaying = true
        sources += source
    }

    fun stop(source: AudioSource) {
        source.isPlaying = false
        sources.remove(source)
    }

    fun stopAll() {
        sources.clear()
    }
}