package com.toix.core

data class Transform(
    var x: Float = 0f,
    var y: Float = 0f,
    var z: Float = 0f,
    var scaleX: Float = 1f,
    var scaleY: Float = 1f,
) {
    fun copyWith(x: Float = this.x, y: Float = this.y, z: Float = this.z,
                 scaleX: Float = this.scaleX, scaleY: Float = this.scaleY): Transform =
        Transform(x, y, z, scaleX, scaleY)
}

interface Component {
    fun onStart(actor: Actor, world: World) {}
    fun onTick(actor: Actor, world: World, dt: Float) {}
    fun snapshot(): Component
}

class TransformComponent(var transform: Transform = Transform()) : Component {
    override fun snapshot(): Component = TransformComponent(transform.copyWith())
}

inline fun <reified T : Component> Actor.get(): T? = components.filterIsInstance<T>().firstOrNull()