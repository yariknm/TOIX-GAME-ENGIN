package com.toix.core

sealed interface BlueprintNode {
    val id: String
    val nextNodeId: String?
    fun execute(actor: Actor, world: World, dt: Float)
}

data class MoveByNode(
    override val id: String,
    val vx: Float,
    val vy: Float,
    override val nextNodeId: String? = null,
) : BlueprintNode {
    override fun execute(actor: Actor, world: World, dt: Float) {
        val tr = actor.get<TransformComponent>()?.transform ?: return
        tr.x += vx * dt
        tr.y += vy * dt
    }
}

data class LogPositionNode(
    override val id: String,
    val tag: String,
    override val nextNodeId: String? = null,
) : BlueprintNode {
    override fun execute(actor: Actor, world: World, dt: Float) {
        val tr = actor.get<TransformComponent>()?.transform ?: return
        println("[$tag] ${tr.x}, ${tr.y}")
    }
}

class BlueprintComponent(
    val entryNodeId: String?,
    val nodesById: Map<String, BlueprintNode>,
) : Component {
    override fun onTick(actor: Actor, world: World, dt: Float) {
        var currentId = entryNodeId
        var guard = 0
        while (currentId != null && guard++ < 256) {
            val node = nodesById[currentId] ?: break
            node.execute(actor, world, dt)
            currentId = node.nextNodeId
        }
    }

    override fun snapshot(): Component = BlueprintComponent(entryNodeId, nodesById)
}