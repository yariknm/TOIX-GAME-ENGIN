package com.toix.core.render

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.TransformComponent
import com.toix.core.World
import com.toix.core.get
import com.toix.core.render.Color
import com.toix.core.render.SpriteRendererComponent
import kotlin.math.min

data class Camera(
    var position: TransformComponent = TransformComponent(),
    var width: Float = 800f,
    var height: Float = 600f,
    var orthoSize: Float = 10f,
    var near: Float = 0.1f,
    var far: Float = 1000f,
)

data class Color(
    var r: Float = 1f,
    var g: Float = 1f,
    var b: Float = 1f,
    var a: Float = 1f,
) {
    companion object {
        val white = Color(1f, 1f, 1f, 1f)
        val red = Color(1f, 0f, 0f, 1f)
        val green = Color(0f, 1f, 0f, 1f)
        val blue = Color(0f, 0f, 1f, 1f)
        val black = Color(0f, 0f, 0f, 1f)
        val gray = Color(0.5f, 0.5f, 0.5f, 1f)
    }
}

data class SpriteRendererComponent(
    var spriteName: String = "",
    var width: Float = 1f,
    var height: Float = 1f,
    var color: Color = Color.white,
    var visible: Boolean = true,
    var layer: Int = 0,
) : Component {
    override fun snapshot(): Component = SpriteRendererComponent(
        spriteName, width, height, color, visible, layer
    )

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}

data class DrawCommand(
    val spriteName: String,
    val x: Float,
    val y: Float,
    val z: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    val layer: Int,
    val rotationDegrees: Float = 0f,
)

interface RenderBackend {
    fun clear(color: Color)
    fun draw(command: DrawCommand)
    fun present()
    fun setViewport(width: Float, height: Float)
}

class Renderer(
    val backend: RenderBackend,
    val camera: Camera = Camera(),
) {
    private val commands = mutableListOf<DrawCommand>()

    fun render(world: World) {
        commands.clear()
        for (actor in world.allActors()) {
            val tr = actor.get<TransformComponent>()?.transform ?: continue
            val sprite = actor.get<SpriteRendererComponent>() ?: continue
            if (!sprite.visible) continue
            commands.add(DrawCommand(
                spriteName = sprite.spriteName,
                x = tr.x,
                y = tr.y,
                z = tr.z,
                width = sprite.width,
                height = sprite.height,
                color = sprite.color,
                layer = sprite.layer,
            ))
        }
        commands.sortBy { it.layer }
        backend.setViewport(camera.width, camera.height)
        backend.clear(Color.black)
        for (cmd in commands) {
            backend.draw(cmd)
        }
        backend.present()
    }

    fun addCommand(command: DrawCommand) {
        commands.add(command)
    }
}