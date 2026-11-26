package com.toix.core.physics

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.TransformComponent
import com.toix.core.World
import com.toix.core.get
import kotlin.math.min
import kotlin.math.max

data class CollisionInfo(
    val normalX: Float = 1f,
    val normalY: Float = 0f,
    val penetration: Float = 0f,
    val contactX: Float = 0f,
    val contactY: Float = 0f,
    val isPenetrating: Boolean = false,
)

class BoxCollider(
    var sizeX: Float = 1f,
    var sizeY: Float = 1f,
    var isTrigger: Boolean = false,
) : Component {
    override fun snapshot(): Component = BoxCollider(sizeX, sizeY, isTrigger)

    override fun onStart(actor: Actor, world: World) {}
    override fun onTick(actor: Actor, world: World, dt: Float) {}
}

class RigidBody(
    var mass: Float = 1f,
    var velocityX: Float = 0f,
    var velocityY: Float = 0f,
    var gravityX: Float = 0f,
    var gravityY: Float = -9.8f,
) : Component {
    var isKinematic: Boolean = false

    override fun snapshot(): Component = RigidBody(mass, velocityX, velocityY, gravityX, gravityY).apply {
        this.isKinematic = isKinematic
    }

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {
        if (isKinematic) return
        val tr = actor.get<TransformComponent>()?.transform ?: return
        velocityY += gravityY * dt
        tr.x += velocityX * dt
        tr.y += velocityY * dt
    }

    fun applyCollision(collision: CollisionInfo) {
        velocityX = 0f
        velocityY = 0f
    }
}

class CollisionManager : Component {
    override fun snapshot(): Component = CollisionManager()

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}

    fun checkCollisions(world: World) {
        val bodies = world.allActors().filter { it.get<RigidBody>() != null && it.get<BoxCollider>() != null }
        for (i in bodies.indices) {
            val a1 = bodies[i]
            val t1 = a1.get<TransformComponent>()?.transform ?: continue
            val c1 = a1.get<BoxCollider>()!!
            val rb1 = a1.get<RigidBody>()!!

            for (j in i + 1 until bodies.size) {
                val a2 = bodies[j]
                val t2 = a2.get<TransformComponent>()?.transform ?: continue
                val c2 = a2.get<BoxCollider>()!!
                val rb2 = a2.get<RigidBody>()!!

                // AABB overlap detection
                val minX1 = t1.x - c1.sizeX / 2f
                val maxX1 = t1.x + c1.sizeX / 2f
                val minY1 = t1.y - c1.sizeY / 2f
                val maxY1 = t1.y + c1.sizeY / 2f

                val minX2 = t2.x - c2.sizeX / 2f
                val maxX2 = t2.x + c2.sizeX / 2f
                val minY2 = t2.y - c2.sizeY / 2f
                val maxY2 = t2.y + c2.sizeY / 2f

                if (maxX1 > minX2 && minX1 < maxX2 && maxY1 > minY2 && minY1 < maxY2) {
                    // Calculate penetration depths
                    val overlapX = min(maxX1, maxX2) - max(minX1, minX2)
                    val overlapY = min(maxY1, maxY2) - max(minY1, minY2)
                    val penetration = min(overlapX, overlapY)
                    val col = CollisionInfo(
                        normalX = if (overlapX > overlapY) 1f else 0f,
                        normalY = if (overlapX > overlapY) 0f else 1f,
                        penetration = penetration,
                        contactX = (t1.x + t2.x) / 2f,
                        contactY = (t1.y + t2.y) / 2f,
                        isPenetrating = true,
                    )
                    rb1.applyCollision(col)
                    rb2.applyCollision(col)
                }
            }
        }
    }
}