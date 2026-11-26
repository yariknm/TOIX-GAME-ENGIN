package com.toix.core

class Actor(
    val id: String,
    var name: String,
    val components: MutableList<Component> = mutableListOf(TransformComponent()),
) {
    fun add(component: Component): Actor = apply { components += component }

    val transform: Transform
        get() = components.firstOrNull { it is TransformComponent }?.let { (it as TransformComponent).transform }
            ?: TransformComponent().transform

    fun allComponents(): List<Component> = components

    fun start(world: World) = components.forEach { it.onStart(this, world) }

    fun tick(world: World, dt: Float) = components.forEach { it.onTick(this, world, dt) }

    fun snapshot(): Actor = Actor(
        id = id,
        name = name,
        components = components.map { it.snapshot() }.toMutableList(),
    )
}

class World(
    var name: String = "Level_01",
    private val actors: MutableList<Actor> = mutableListOf(),
) {
    private var started = false

    fun spawn(actor: Actor): Actor {
        actors += actor
        if (started) actor.start(this)
        return actor
    }

    fun findById(id: String): Actor? = actors.find { it.id == id }

    fun allActors(): List<Actor> = actors

    fun startPlay() {
        started = true
        actors.forEach { it.start(this) }
    }

    fun tick(dt: Float) {
        if (!started) return
        actors.forEach { it.tick(this, dt) }
    }

    fun stopPlay() {
        started = false
    }

    fun snapshot(): World = World(
        name = name,
        actors = actors.map { it.snapshot() }.toMutableList(),
    )
}