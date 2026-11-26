package com.toix.core.prefab

import com.toix.core.Actor
import com.toix.core.World

data class Prefab(
    val name: String,
    val template: Actor,
)

class PrefabManager {
    private val prefabs = mutableMapOf<String, Prefab>()

    fun register(prefab: Prefab) {
        prefabs[prefab.name] = prefab
    }

    fun get(name: String): Prefab? = prefabs[name]

    fun instantiate(name: String, actorName: String): Actor? {
        val prefab = prefabs[name] ?: return null
        val newActor = prefab.template.snapshot()
        newActor.name = actorName
        return newActor
    }

    fun instantiateIn(name: String, actorName: String, world: World): Actor? {
        val actor = instantiate(name, actorName) ?: return null
        world.spawn(actor)
        return actor
    }

    fun getAll(): List<Prefab> = prefabs.values.toList()
}