package com.toix.core.vfx

import com.toix.core.Component
import com.toix.core.Actor
import com.toix.core.World

/**
 * Particle system module base.
 */
sealed class ParticleModule {
    data class Spawn(
        val ratePerSecond: Float,
        val burstCount: Int = 0,
        val burstInterval: Float = 0f,
    ) : ParticleModule()

    data class Velocity(
        val minX: Float, val maxX: Float,
        val minY: Float, val maxY: Float,
        val minZ: Float, val maxZ: Float,
    ) : ParticleModule()

    data class Size(
        val min: Float, val max: Float,
        val overLifetime: Boolean = false,
    ) : ParticleModule()

    data class Color(
        val start: com.toix.core.render.Color,
        val end: com.toix.core.render.Color,
        val overLifetime: Boolean = false,
    ) : ParticleModule()

    data class Lifetime(
        val min: Float, val max: Float,
    ) : ParticleModule()
}

/**
 * Individual particle state.
 */
data class Particle(
    val id: String,
    var x: Float, var y: Float, var z: Float,
    var vx: Float, var vy: Float, var vz: Float,
    var size: Float,
    var r: Float, var g: Float, var b: Float, var a: Float,
    var age: Float = 0f,
    var lifetime: Float = 1f,
    var active: Boolean = true,
)

/**
 * Niagara-like particle system description.
 */
data class ParticleSystem(
    val id: String,
    val modules: List<ParticleModule> = emptyList(),
    val maxParticles: Int = 1000,
    val simulationSpace: SimulationSpace = SimulationSpace.Local,
    var isPlaying: Boolean = true,
)

enum class SimulationSpace {
    Local,
    World,
}

/**
 * Particle system component.
 */
class ParticleSystemComponent(
    var system: ParticleSystem,
    var particles: MutableList<Particle> = mutableListOf(),
    var emitterX: Float = 0f,
    var emitterY: Float = 0f,
    var emitterZ: Float = 0f,
) : Component {
    override fun snapshot(): Component = ParticleSystemComponent(system, particles, emitterX, emitterY, emitterZ)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {
        if (!system.isPlaying) return

        // Spawn new particles
        val spawnModule = system.modules.firstOrNull { it is ParticleModule.Spawn } as? ParticleModule.Spawn
        if (spawnModule != null && particles.size < system.maxParticles) {
            val count = (spawnModule.ratePerSecond * dt).toInt()
            for (i in 0 until count) {
                particles.add(Particle(
                    id = "${system.id}_${System.currentTimeMillis()}_$i",
                    x = emitterX, y = emitterY, z = emitterZ,
                    vx = 0f, vy = 0f, vz = 0f,
                    size = 1f,
                    r = 1f, g = 1f, b = 1f, a = 1f,
                    lifetime = 1f,
                ))
            }
        }

        // Simulate particles
        val velocityModule = system.modules.firstOrNull { it is ParticleModule.Velocity } as? ParticleModule.Velocity
        for (p in particles) {
            if (!p.active) continue
            p.age += dt
            if (p.age >= p.lifetime) {
                p.active = false
                continue
            }
            p.x += p.vx * dt
            p.y += p.vy * dt
            p.z += p.vz * dt

            // Size over lifetime
            val sizeModule = system.modules.firstOrNull { it is ParticleModule.Size } as? ParticleModule.Size
            if (sizeModule != null && sizeModule.overLifetime) {
                val t = p.age / p.lifetime
                p.size = sizeModule.min + (sizeModule.max - sizeModule.min) * t
            }

            // Color over lifetime
            val colorModule = system.modules.firstOrNull { it is ParticleModule.Color } as? ParticleModule.Color
            if (colorModule != null && colorModule.overLifetime) {
                val t = p.age / p.lifetime
                p.r = colorModule.start.r + (colorModule.end.r - colorModule.start.r) * t
                p.g = colorModule.start.g + (colorModule.end.g - colorModule.start.g) * t
                p.b = colorModule.start.b + (colorModule.end.b - colorModule.start.b) * t
                p.a = colorModule.start.a + (colorModule.end.a - colorModule.start.a) * t
            }
        }

        // Remove dead particles
        particles.removeIf { !it.active }
    }
}
