package com.toix.editor

import com.toix.core.Actor
import com.toix.core.BlueprintComponent
import com.toix.core.Transform
import com.toix.core.TransformComponent
import com.toix.core.World
import com.toix.core.get
import kotlin.math.min

enum class EditorMode { EDIT, PLAYING }

data class DetailsEntry(
    val actorId: String,
    val actorName: String,
    val x: Float,
    val y: Float,
    val hasBlueprint: Boolean,
)

class ToixEditorController(
    private var editWorld: World,
) {
    private var pieWorld: World? = null

    var mode: EditorMode = EditorMode.EDIT
        private set

    private var selectedActorId: String? = null
        private set

    val activeWorld: World
        get() = if (mode == EditorMode.PLAYING) pieWorld ?: editWorld else editWorld

    fun selectActor(actorId: String?) {
        selectedActorId = actorId
    }

    fun setActorPosition(actorId: String, x: Float, y: Float) {
        val tr = activeWorld.findById(actorId)?.get<TransformComponent>()?.transform ?: return
        tr.x = x
        tr.y = y
    }

    fun inspectSelected(): DetailsEntry? {
        val id = selectedActorId ?: return null
        val actor = activeWorld.findById(id) ?: return null
        val tr = actor.get<TransformComponent>()?.transform ?: Transform()
        return DetailsEntry(
            actorId = actor.id,
            actorName = actor.name,
            x = tr.x,
            y = tr.y,
            hasBlueprint = actor.get<BlueprintComponent>() != null,
        )
    }

    fun play() {
        if (mode == EditorMode.PLAYING) return
        pieWorld = editWorld.snapshot().also { it.startPlay() }
        mode = EditorMode.PLAYING
    }

    fun stop() {
        if (mode == EditorMode.EDIT) return
        pieWorld?.stopPlay()
        pieWorld = null
        mode = EditorMode.EDIT
    }

    fun stepFrame(rawDtSeconds: Float, maxDtSeconds: Float = 0.05f) {
        val dt = min(rawDtSeconds.coerceAtLeast(0f), maxDtSeconds)
        if (mode == EditorMode.PLAYING) {
            pieWorld?.tick(dt)
        }
    }
}