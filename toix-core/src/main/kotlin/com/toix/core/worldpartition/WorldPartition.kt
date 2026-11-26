package com.toix.core.worldpartition

import com.toix.core.Actor
import com.toix.core.Component
import com.toix.core.World
import kotlin.math.abs

data class GridCell(
    val cellX: Int,
    val cellY: Int,
    val cellZ: Int,
    val size: Float,
    val actors: MutableList<String> = mutableListOf(),
)

class WorldPartitionManager(
    private val cellSize: Float = 1000f,
    private val loadRadius: Int = 2,
    private val unloadRadius: Int = 4,
) {
    private val loadedCells = mutableMapOf<String, GridCell>()
    private val allCells = mutableMapOf<String, GridCell>()

    fun registerCell(cell: GridCell) {
        allCells["${cell.cellX}_${cell.cellY}_${cell.cellZ}"] = cell
    }

    fun registerActorInCell(actorId: String, cellX: Int, cellY: Int, cellZ: Int) {
        val key = "${cellX}_${cellY}_${cellZ}"
        var cell = allCells[key]
        if (cell == null) {
            cell = GridCell(cellX, cellY, cellZ, cellSize)
            allCells[key] = cell
        }
        if (!cell.actors.contains(actorId)) {
            cell.actors.add(actorId)
        }
    }

    fun updateStreaming(cameraX: Float, cameraY: Float, cameraZ: Float) {
        val camCellX = (cameraX / cellSize).toInt()
        val camCellY = (cameraY / cellSize).toInt()
        val camCellZ = (cameraZ / cellSize).toInt()

        val shouldLoad = mutableSetOf<String>()
        for (x in camCellX - loadRadius .. camCellX + loadRadius) {
            for (y in camCellY - loadRadius .. camCellY + loadRadius) {
                for (z in camCellZ - loadRadius .. camCellZ + loadRadius) {
                    shouldLoad.add("${x}_${y}_${z}")
                }
            }
        }

        for (key in shouldLoad) {
            if (!loadedCells.containsKey(key)) {
                allCells[key]?.let { loadedCells[key] = it }
            }
        }

        val toUnload = loadedCells.keys.filter { key ->
            val parts = key.split("_").map { it.toInt() }
            val dx = abs(parts[0] - camCellX)
            val dy = abs(parts[1] - camCellY)
            val dz = abs(parts[2] - camCellZ)
            dx > unloadRadius || dy > unloadRadius || dz > unloadRadius
        }
        for (key in toUnload) {
            loadedCells.remove(key)
        }
    }

    fun getLoadedCells(): List<GridCell> = loadedCells.values.toList()

    fun getActorsInLoadedCells(): List<String> =
        loadedCells.values.flatMap { it.actors }.distinct()
}

data class HLODCluster(
    val id: String,
    val boundsMinX: Float,
    val boundsMinY: Float,
    val boundsMinZ: Float,
    val boundsMaxX: Float,
    val boundsMaxY: Float,
    val boundsMaxZ: Float,
    val meshId: String,
    val materialId: String,
    val lodLevel: Int,
)

class WorldPartitionComponent(
    val cellX: Int,
    val cellY: Int,
    val cellZ: Int,
    var isStreamingEnabled: Boolean = true,
) : Component {
    override fun snapshot(): Component = WorldPartitionComponent(cellX, cellY, cellZ, isStreamingEnabled)

    override fun onStart(actor: Actor, world: World) {}

    override fun onTick(actor: Actor, world: World, dt: Float) {}
}