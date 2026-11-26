package com.toix.core.render

import com.toix.core.Component

/**
 * Компонент отрисовки спрайта. Добавляется к актеру, чтобы отображать его на экране.
 */
class SpriteRendererComponent(
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

import com.toix.core.Actor
import com.toix.core.World
