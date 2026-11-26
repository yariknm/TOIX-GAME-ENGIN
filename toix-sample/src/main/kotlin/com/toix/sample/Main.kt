package com.toix.sample

import com.toix.core.Actor
import com.toix.core.BlueprintComponent
import com.toix.core.LogPositionNode
import com.toix.core.MoveByNode
import com.toix.core.World
import com.toix.editor.ToixEditorController

fun main() {
    val world = World("RTS_Test_Level")
    val n1 = MoveByNode("move_right", vx = 40f, vy = 0f, nextNodeId = "log_pos")
    val n2 = LogPositionNode("log_pos", tag = "PIE")
    val bp = BlueprintComponent(entryNodeId = "move_right", nodesById = mapOf(n1.id to n1, n2.id to n2))
    val peon = Actor("peon_1", "Peon_01").add(bp)
    world.spawn(peon)
    val editor = ToixEditorController(world)
    editor.selectActor("peon_1")
    println("До старта PIE: ${editor.inspectSelected()}")
    editor.play()
    editor.stepFrame(0.5f)
    editor.stepFrame(0.5f)
    println("Во время PIE: ${editor.inspectSelected()}")
    editor.stop()
    println("После Stop (сцена восстановлена): ${editor.inspectSelected()}")
}