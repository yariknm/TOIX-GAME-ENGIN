package com.toix.core.scene

import com.toix.core.Actor
import com.toix.core.World

data class Scene(
    val name: String,
    val actors: MutableList<Actor> = mutableListOf(),
)

class SceneManager(
    private var currentScene: Scene = Scene("Untitled"),
) {
    private val additiveScenes = mutableListOf<Scene>()

    fun loadScene(name: String): Scene {
        val scene = Scene(name)
        currentScene = scene
        return scene
    }

    fun loadAdditive(name: String): Scene {
        val scene = Scene(name)
        additiveScenes += scene
        return scene
    }

    fun unloadAdditive(name: String): Boolean {
        return additiveScenes.removeIf { it.name == name }
    }

    fun getCurrentScene(): Scene = currentScene

    fun getAdditiveScenes(): List<Scene> = additiveScenes.toList()

    fun addActor(sceneName: String, actor: Actor): Boolean {
        val scene = if (sceneName == currentScene.name) currentScene else additiveScenes.find { it.name == sceneName }
        if (scene == null) return false
        scene.actors += actor
        return true
    }

    fun getAllActors(): List<Actor> = currentScene.actors + additiveScenes.flatMap { it.actors }
}