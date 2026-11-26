package com.toix.core

data class Transform(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
)

interface Component {
    fun onStart(actor: Actor, world: World) {}
    fun onTick(actor: Actor, world: World, dt: Float) {}
    fun snapshot(): Component
}

class TransformComponent(var transform: Transform = Transform()) : Component {
    override fun snapshot(): Component = TransformComponent(transform.copy())
}

inline fun <reified T : Component> Actor.get(): T? = components.filterIsInstance<T>().firstOrNull()