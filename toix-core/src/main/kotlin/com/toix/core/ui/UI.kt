package com.toix.core.ui

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.World

data class Rect(
    val x: Float = 0f,
    val y: Float = 0f,
    val width: Float = 100f,
    val height: Float = 50f,
) {
    fun contains(px: Float, py: Float): Boolean =
        px >= x && px <= x + width && py >= y && py <= y + height
}

data class Color(
    val r: Float = 1f,
    val g: Float = 1f,
    val b: Float = 1f,
    val a: Float = 1f,
) {
    companion object {
        val white = Color(1f, 1f, 1f, 1f)
    }
}

open class UIElement : Component {
    var rect: Rect = Rect()
    var parent: UIElement? = null
    var active: Boolean = true
    var visible: Boolean = true

    override fun snapshot(): Component = this

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}

    open fun render() {
        if (!active || !visible) return
    }
}

class Canvas : UIElement() {
    val elements = mutableListOf<UIElement>()

    fun addElement(element: UIElement) {
        elements += element
    }

    fun renderAll() {
        for (element in elements) element.render()
    }
}

class TextElement(
    var text: String = "",
    var fontSize: Float = 14f,
    var color: Color = Color(),
) : UIElement() {
    override fun render() {}
}

class ButtonElement(
    var label: String = "",
    var onClick: (() -> Unit)? = null,
) : UIElement() {
    override fun render() {
        onClick?.invoke()
    }
}