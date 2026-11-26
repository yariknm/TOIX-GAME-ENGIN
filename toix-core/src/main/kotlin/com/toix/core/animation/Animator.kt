package com.toix.core.animation

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.TransformComponent
import com.toix.core.World
import com.toix.core.get

data class AnimationKeyframe(
    val time: Float,
    val x: Float,
    val y: Float,
    val z: Float,
    val scaleX: Float,
    val scaleY: Float,
)

data class AnimationClip(
    val name: String,
    val frames: List<AnimationKeyframe>,
    val loop: Boolean = true,
)

class Animator(
    var clips: Map<String, AnimationClip> = emptyMap(),
    var currentClipName: String? = null,
    var currentTime: Float = 0f,
    var isPlaying: Boolean = false,
) : Component {
    override fun snapshot(): Component = Animator(clips, currentClipName, currentTime, isPlaying)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {
        if (!isPlaying || currentClipName == null) return
        val clip = clips[currentClipName] ?: return
        currentTime += dt
        if (clip.loop) {
            val maxTime = clip.frames.maxByOrNull { it.time }?.time ?: 0f
            if (currentTime > maxTime) currentTime -= maxTime
        }
        val targetFrame = clip.frames.find { it.time >= currentTime } ?: clip.frames.firstOrNull() ?: return
        val tr = actor.get<TransformComponent>()?.transform ?: return
        tr.x = targetFrame.x
        tr.y = targetFrame.y
        tr.z = targetFrame.z
        tr.scaleX = targetFrame.scaleX
        tr.scaleY = targetFrame.scaleY
    }

    fun play(clipName: String) {
        currentClipName = clipName
        currentTime = 0f
        isPlaying = true
    }

    fun stop() {
        isPlaying = false
    }

    fun addClip(clip: AnimationClip) {
        clips = clips.toMutableMap().apply { put(clip.name, clip) }
    }
}