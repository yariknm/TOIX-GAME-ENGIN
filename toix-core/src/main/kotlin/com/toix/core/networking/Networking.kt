package com.toix.core.networking

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.World

/**
 * Network identity for replicated actors.
 */
data class NetworkIdentity(
    val netId: Int,
    val ownerConnectionId: String?,
    var isRelevant: Boolean = true,
    var priority: Float = 1.0f,
)

/**
 * RPC (Remote Procedure Call) descriptor.
 */
data class RpcDescriptor(
    val functionName: String,
    val rpcType: RpcType,
    val parameters: List<Any>,
    val targetConnectionId: String?,
    val reliable: Boolean,
)

enum class RpcType {
    Server,
    Client,
    Multicast,
}

/**
 * Replication condition for an actor property.
 */
enum class ReplicationCondition {
    Always,
    OwnerOnly,
    SkipOwner,
    InitialOnly,
}

/**
 * Replicated property definition.
 */
data class ReplicatedProperty(
    val propertyName: String,
    val condition: ReplicationCondition = ReplicationCondition.Always,
)

/**
 * Network replication manager: tracks which actors are replicated to which connections.
 */
class ReplicationManager {
    private val replicatedActors = mutableMapOf<Int, NetworkIdentity>()
    private val replicatedProperties = mutableMapOf<Int, List<ReplicatedProperty>>()
    private val connections = mutableSetOf<String>()

    fun registerActor(actorId: Int, identity: NetworkIdentity) {
        replicatedActors[actorId] = identity
    }

    fun unregisterActor(actorId: Int) {
        replicatedActors.remove(actorId)
        replicatedProperties.remove(actorId)
    }

    fun registerProperties(actorId: Int, properties: List<ReplicatedProperty>) {
        replicatedProperties[actorId] = properties
    }

    fun addConnection(connectionId: String) {
        connections.add(connectionId)
    }

    fun removeConnection(connectionId: String) {
        connections.remove(connectionId)
    }

    fun getReplicationList(connectionId: String): List<Int> {
        return replicatedActors.entries.filter { it.value.isRelevant }.map { it.key }
    }

    fun getPropertiesForActor(actorId: Int): List<ReplicatedProperty> =
        replicatedProperties[actorId] ?: emptyList()

    fun getAllConnections(): List<String> = connections.toList()
}

/**
 * Networked actor component.
 */
class NetworkedComponent(
    var netId: Int = -1,
    var isRelevant: Boolean = true,
    var priority: Float = 1.0f,
    var ownerConnectionId: String? = null,
) : Component {
    override fun snapshot(): Component = NetworkedComponent(netId, isRelevant, priority, ownerConnectionId)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}

/**
 * RPC invocation record for sending/receiving.
 */
data class RpcCall(
    val actorNetId: Int,
    val descriptor: RpcDescriptor,
    val timestamp: Long = System.currentTimeMillis(),
)

/**
 * Network connection stub.
 */
data class NetworkConnection(
    val connectionId: String,
    val ip: String,
    val port: Int,
    var isConnected: Boolean = true,
    var pingMs: Int = 0,
)
