package com.toix.core.editor.gui

import com.toix.core.Actor
import com.toix.core.World
import com.toix.editor.ToixEditorController

/**
 * Editor GUI stub for Compose Desktop / Swing / Web.
 * Real implementation uses Compose Multiplatform for the viewport, outliner, inspector.
 */
class ToixEditorGui(
    private val controller: ToixEditorController,
    private val world: World,
) {
    var viewportWidth: Int = 1280
    var viewportHeight: Int = 720
    var showOutliner: Boolean = true
    var showInspector: Boolean = true
    var showContentBrowser: Boolean = true
    var selectedActorId: String? = null

    fun renderFrame() {
        // Called each frame by the host window
        renderViewport()
        renderOutliner()
        renderInspector()
    }

    private fun renderViewport() {
        // Draw 3D/2D scene view via Renderer
        // Real implementation integrates with Vulkan/OpenGL surface
    }

    private fun renderOutliner() {
        if (!showOutliner) return
        for (actor in world.allActors()) {
            // List actor in tree view
        }
    }

    private fun renderInspector() {
        if (!showInspector) return
        selectedActorId?.let { id ->
            world.findById(id)?.let { actor ->
                // Show actor properties
            }
        }
    }

    fun selectActor(actorId: String?) {
        selectedActorId = actorId
        controller.selectActor(actorId)
    }

    fun setActorPosition(actorId: String, x: Float, y: Float) {
        controller.setActorPosition(actorId, x, y)
    }

    fun play() {
        controller.play()
    }

    fun stop() {
        controller.stop()
    }

    fun stepFrame(dt: Float) {
        controller.stepFrame(dt)
    }
}

/**
 * Compose Desktop main window stub (placeholder).
 * In a real project this would be:
 *   Window(onClose = { requestClose() }) {
 *       Surface { ... }
 *   }
 */
class EditorWindow {
    var title: String = "TOIX Engine Editor"
    var width: Int = 1280
    var height: Int = 720
    var isOpen: Boolean = false

    fun open() {
        isOpen = true
    }

    fun close() {
        isOpen = false
    }

    fun run(render: () -> Unit) {
        // Main loop stub
        while (isOpen) {
            render()
        }
    }
}
